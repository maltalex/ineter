/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.maltalex.ineter.base.IPAddress;

public interface IPRange<I extends IPAddress & Comparable<I>, L extends Number & Comparable<L>> extends Iterable<I>, Serializable {

	public I getFirst();

	public I getLast();

	/**
	 * Checks whether this range has any overlapping addresses with a given
	 * range. To check whether all addresses are contained, use
	 * {@link IPRange#contains(IPRange)}
	 *
	 * @param range
	 *            the range to check for overlap
	 * @return true if the given range overlaps with this one
	 */
	default boolean overlaps(IPRange<I,L> range) {
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
	default boolean contains(I ip) {
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
	default boolean contains(IPRange<I,L> range) {
		return this.contains(range.getFirst()) && this.contains(range.getLast());
	}

	/**
	 * Returns the number of addresses in the range
	 *
	 * @return number of addresses in the range
	 */
	public L length();

	/**
	 * Returns the number of addresses in the range
	 * 
	 * If the number is larger than Integer.MAX_VALUE, returns Integer.MAX_VALUE
	 * 
	 * @return number of addresses in the range, up to Integer.MAX_VALUE
	 */
	public int intLength();

	@Override
	default Iterator<I> iterator() {
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
	default Iterator<I> iterator(boolean trim) {
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
	public Iterator<I> iterator(boolean skipFirst, boolean skipLast);

	/**
	 * Calculates and returns the minimal list of Subnets that compose this
	 * address range.
	 *
	 * @return a list of Subnets that compose this address range
	 */
	public List<? extends IPSubnet<I, L>> toSubnets();

	/**
	 * Returns the list of addresses contained in the range. The list is
	 * {@link IPRange#intLength()} elements long (up to Integer.MAX_VALUE)
	 * 
	 * @return The list of addresses contained in the range
	 */
	default List<I> toList() {
		ArrayList<I> list = new ArrayList<>(this.intLength());
		Iterator<I> iter = this.iterator();
		for (int i = 0; i < this.intLength(); i++) {
			list.add(iter.next());
		}
		return list;
	}

	public IPRange<I,L> withLast(I address);
	 
	public IPRange<I,L> withFirst(I address);
}
