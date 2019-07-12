/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.maltalex.ineter.base.IPAddress;

public interface IPRange<T extends IPAddress & Comparable<T>> extends Iterable<T>, Serializable {

	static <T> T parseRange(String from, BiFunction<String, String, ? extends T> rangeProducer,
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

	static <T> T parseSubnet(String from, BiFunction<String, Integer, ? extends T> subnetProducer,
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

	public T getFirst();

	public T getLast();

	/**
	 * Checks whether this range has any overlapping addresses with a given
	 * range. To check whether all addresses are contained, use
	 * {@link IPRange#contains(IPRange)}
	 *
	 * @param range
	 *            the range to check for overlap
	 * @return true if the given range overlaps with this one
	 */
	default boolean overlaps(IPRange<T> range) {
		// Either one of the ends of the other range is within this one
		// Or this range is completely inside the other range. In that case,
		// it's enough to check just one of the edges of this range
		return this.contains(range.getFirst()) || this.contains(range.getLast()) || range.contains(this.getFirst());
	}

	/**
	 * Checks whether a given address is inside this range
	 *
	 * @param ip
	 * @return true if the given address is inside this range
	 */
	default boolean contains(T ip) {
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
	default boolean contains(IPRange<T> range) {
		return this.contains(range.getFirst()) && this.contains(range.getLast());
	}

	/**
	 * Returns the number of addresses in the range
	 *
	 * @return number of addresses in the range
	 */
	public Number length();

	@Override
	default Iterator<T> iterator() {
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
	default Iterator<T> iterator(boolean trim) {
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
	public Iterator<T> iterator(boolean skipFirst, boolean skipLast);

	/**
	 * Calculates and returns the minimal list of Subnets that compose this
	 * address range.
	 *
	 * @return a list of Subnets that compose this address range
	 */
	public List<? extends IPSubnet<? extends T>> toSubnets();
}
