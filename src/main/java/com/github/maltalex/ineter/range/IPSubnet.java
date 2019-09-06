/**
 * Copyright (c) 2018, Ineter Contributors
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.util.Iterator;

import com.github.maltalex.ineter.base.ExtendedIPAddress;

public interface IPSubnet<T extends ExtendedIPAddress<T>> extends Iterable<T> {

	/**
	 * Returns the network mask in address form
	 *
	 * @return network mask of this subnet
	 */
	public T getNetworkMask();

	/**
	 * Returns the number of bits used for the network address. This number is
	 * equal to the number of bits in the address (32 or 128) minus the number
	 * of bits used for the host. This is the same as the number that comes
	 * after the "/" in CIDR notation
	 *
	 * @return number of bits in network
	 */
	public int getNetworkBitCount();

	/**
	 * Returns the number of bits used for the host part of the address. This
	 * number is equal to the number of bits in the address (32 or 128) minus
	 * the number of bits used for the network
	 *
	 * @return number of bits in the host part of the subnet
	 */
	public int getHostBitCount();

	/**
	 * Returns the address of the network (with all host bits set to zero) same
	 * as {@link IPSubnet#getFirst()}
	 *
	 * @return
	 */
	public T getNetworkAddress();

	/**
	 * Checks whether this subnet has any overlapping addresses with a given
	 * range. To check whether all addresses are contained, use
	 * {@link IPSubnet#contains(IPRange)}
	 *
	 * @param range
	 * 		the range to check for overlap
	 * @return true if the given range overlaps with this subnet
	 */
	public boolean overlaps(IPRange<T> range);

	/**
	 * Checks whether a given address is inside this subnet
	 *
	 * @param ip
	 * @return true if the given address is inside this subnet
	 */
	public boolean contains(T ip);

	/**
	 * Checks whether this subnet contains all addresses of a given range. To
	 * check for partial overlap, use {@link IPSubnet#overlaps(IPRange)}
	 *
	 * @param range
	 * 		range to check
	 * @return true if the entire given range is contained within this subnet
	 */
	public boolean contains(IPRange<T> range);

	/**
	 * Returns the network address, same as {@link IPSubnet#getNetworkAddress()}
	 *
	 * @return the network address
	 */
	public T getFirst();

	/**
	 * Returns the last address in the subnet
	 *
	 * @return the last address in the subnet
	 */
	public T getLast();

	/**
	 * Returns an iterator that optionally skips both the first and last
	 * addresses in the subnet
	 *
	 * @param trim
	 * 		set to true to skip first and last addresses
	 * @return a new iterator instance
	 */
	public Iterator<T> iterator(boolean trim);

	/**
	 * Returns an iterator that optionally skips the first, last or both
	 * addresses in the subnet
	 *
	 * @param skipFirst
	 * 		set to true to skip the first address
	 * @param skipLast
	 * 		set to true to skip the last addresses
	 * @return a new iterator instance
	 */
	public Iterator<T> iterator(boolean skipFirst, boolean skipLast);

	/**
	 * Returns the number of addresses in the subnet
	 *
	 * @return number of addresses in the subnet
	 */
	public Number length();
}
