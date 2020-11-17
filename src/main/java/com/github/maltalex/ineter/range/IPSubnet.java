/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import com.github.maltalex.ineter.base.IPAddress;

public interface IPSubnet<
		S extends IPSubnet<S, R, I, L>,
		R extends IPRange<R, S, I, L>,
		I extends IPAddress & Comparable<I>,
		L extends Number & Comparable<L>> extends IPRange<R, S, I, L> {

	/**
	 * Returns the network mask in address form
	 *
	 * @return network mask of this subnet
	 */
	I getNetworkMask();

	/**
	 * Returns the number of bits used for the network address. This number is equal
	 * to the number of bits in the address (32 or 128) minus the number of bits
	 * used for the host. This is the same as the number that comes after the "/" in
	 * CIDR notation
	 *
	 * @return number of bits in network
	 */
	int getNetworkBitCount();

	/**
	 * Returns the number of bits used for the host part of the address. This number
	 * is equal to the number of bits in the address (32 or 128) minus the number of
	 * bits used for the network
	 *
	 * @return number of bits in the host part of the subnet
	 */
	int getHostBitCount();

	/**
	 * Returns the address of the network (with all host bits set to zero) same as
	 * {@link IPSubnet#getFirst()}
	 *
	 * @return network address
	 */
	I getNetworkAddress();
}
