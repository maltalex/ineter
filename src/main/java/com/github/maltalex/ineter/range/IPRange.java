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

import com.github.maltalex.ineter.base.IPAddress;

public abstract class IPRange<T extends IPAddress & Comparable<T>> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

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
		if (getClass() != obj.getClass()) {
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
	 *
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
}
