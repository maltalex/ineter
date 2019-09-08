/**
 * Copyright (c) 2018, Ineter Contributors
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.maltalex.ineter.base.ExtendedIPAddress;

public abstract class IPRange<T extends ExtendedIPAddress<T>> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	protected static <T> T parseRange(String from, BiFunction<String, String, ? extends T> rangeProducer,
			Function<String, ? extends T> subnetProducer) {
		String[] parts = from.split("-");
		if (parts.length == 2) {
			return rangeProducer.apply(parts[0].trim(), parts[1].trim());
		} else if (parts.length == 1) {
			if (from.contains("/")) {
				return subnetProducer.apply(from);
			}
			return rangeProducer.apply(parts[0].trim(), parts[0].trim());
		} else {
			throw new IllegalArgumentException(
					String.format("Inappropriate format for address range string %s.", from));
		}
	}

	protected static <T> T parseSubnet(String from, BiFunction<String, Integer, ? extends T> subnetProducer,
			int singleAddressMask) {
		final String[] parts = from.split("/");
		if (parts.length == 2) {
			return subnetProducer.apply(parts[0].trim(), Integer.parseInt(parts[1].trim()));
		} else if (parts.length == 1) {
			return subnetProducer.apply(parts[0].trim(), singleAddressMask);
		} else {
			throw new IllegalArgumentException(
					String.format("Inappropriate format for address subnet string %s.", from));
		}
	}

	public abstract T getFirst();

	public abstract T getLast();

	/**
	 * Checks whether this range has any overlapping addresses with a given
	 * range. To check whether all addresses are contained, use
	 * {@link IPRange#contains(IPRange)}
	 *
	 * @param range
	 *            the range to check for overlap
	 * @return true if the given range overlaps with this one
	 */
	public boolean overlaps(IPRange<T> range) {
		// Either one of the ends of the other range is within this one
		// Or this range is completely inside the other range. In that case,
		// it's enough to check just one of the edges of this range
		return this.contains(range.getFirst()) || this.contains(range.getLast()) || range.contains(this.getFirst());
	}

	/**
	 * Checks whether this range is adjacent to another one without overlap
	 * between the two
	 *
	 * @param other
	 *            range to check adjacency with the current one
	 * @return true - is adjacent, false - is not adjacent
	 */
	public <R extends IPRange<T>> boolean isAdjacent(R other) {
		return (this.getFirst().isAdjacentTo(other.getLast()) || this.getLast().isAdjacentTo(other.getFirst()))
				&& !this.overlaps(other);
	}

	/**
	 * Checks whether a given address is inside this range
	 *
	 * @param ip
	 * @return true if the given address is inside this range
	 */
	public boolean contains(T ip) {
		return this.getFirst().compareTo(ip) <= 0 && this.getLast().compareTo(ip) >= 0;
	}

	/**
	 * Checks whether this range contains all addresses of a given range. To
	 * check for partial overlap, use {@link IPRange#overlaps(IPRange)}
	 *
	 * @param range
	 *            range to check
	 * @return true if the entire given range is contained within this range
	 */
	public boolean contains(IPRange<T> range) {
		return this.contains(range.getFirst()) && this.contains(range.getLast());
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + this.getFirst().hashCode();
		result = prime * result + this.getLast().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IPRange)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		IPRange<T> other = (IPRange<T>) obj;
		return this.getFirst().equals(other.getFirst()) && this.getLast().equals(other.getLast());
	}

	@Override
	public String toString() {
		return String.format("%s - %s", this.getFirst().toString(), this.getLast().toString());
	}

	/**
	 * Returns the number of addresses in the range
	 *
	 * @return number of addresses in the range
	 */
	public abstract Number length();

	@Override
	public Iterator<T> iterator() {
		return iterator(false);
	}

	/**
	 * Returns an iterator that optionally skips both the first and last
	 * addresses in the range
	 *
	 * @param trim
	 *            set to true to skip first and last addresses
	 * @return a new iterator instance
	 */
	public Iterator<T> iterator(boolean trim) {
		return iterator(trim, trim);
	}

	/**
	 * Returns an iterator that optionally skips the first, last or both
	 * addresses in the range
	 *
	 * @param skipFirst
	 *            set to true to skip the first address
	 * @param skipLast
	 *            set to true to skip the last addresses
	 * @return a new iterator instance
	 */
	public abstract Iterator<T> iterator(boolean skipFirst, boolean skipLast);

	/**
	 * Calculates and returns the minimal list of Subnets that compose this
	 * address range.
	 *
	 * @return a list of Subnets that compose this address range
	 */
	public abstract List<? extends IPSubnet<? extends T>> toSubnets();

	protected static <T extends ExtendedIPAddress<T>, R extends IPRange<T>> List<R> merge(Collection<R> rangesToMerge,
			BiFunction<T, T, R> rangeProducer) {
		ArrayList<R> sortedRanges = new ArrayList<>(rangesToMerge);
		Collections.sort(sortedRanges, Comparator.comparing(R::getFirst));

		int mergedRangeIndex = 0, candidateIndex = 0;
		while (candidateIndex < sortedRanges.size()) {
			R mergedRange = sortedRanges.get(candidateIndex++); // Grab first
																// un-merged
																// range
			T mergedRangeStart = mergedRange.getFirst();
			// While subsequent ranges overlap (or are adjacent), keep expanding
			// the merged range
			while (candidateIndex < sortedRanges.size()
					&& overlapsOrAdjacent(mergedRange, sortedRanges.get(candidateIndex))) {
				T pendingRangeEnd = max(mergedRange.getLast(), sortedRanges.get(candidateIndex).getLast());
				mergedRange = rangeProducer.apply(mergedRangeStart, pendingRangeEnd);
				candidateIndex++;
			}
			sortedRanges.set(mergedRangeIndex++, mergedRange);
		}

		return new ArrayList<>(sortedRanges.subList(0, mergedRangeIndex));
	}

	protected static <T extends ExtendedIPAddress<T>, R extends IPRange<T>> R extend(R self, R extension,
			BiFunction<T, T, R> rangeProducer) {
		if (self.equals(extension)) {
			return self;
		}
		if (!self.isAdjacent(extension)) {
			throw new IllegalArgumentException(String.format("Extension %s is not adjacent to this range %s",
					extension.toString(), self.toString()));
		}
		return rangeProducer.apply(min(self.getFirst(), extension.getFirst()),
				max(self.getLast(), extension.getLast()));
	}

	private static <T extends ExtendedIPAddress<T>, R extends IPRange<T>> boolean overlapsOrAdjacent(R first,
			R second) {
		return first.overlaps(second) || first.isAdjacent(second);
	}

	private static <T extends Comparable<T>> T max(T a, T b) {
		return a.compareTo(b) >= 0 ? a : b;
	}

	private static <T extends Comparable<T>> T min(T a, T b) {
		return a.compareTo(b) < 0 ? a : b;
	}
}
