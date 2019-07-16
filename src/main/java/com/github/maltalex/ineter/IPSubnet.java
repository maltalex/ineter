/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter;

public interface IPSubnet<T extends IPAddress & Comparable<T>, S extends Number & Comparable<S>> extends IPRange<T, S> {

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
}
