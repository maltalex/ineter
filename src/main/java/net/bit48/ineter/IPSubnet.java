/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

public interface IPSubnet<T extends IPAddress> {

	public T getNetworkMask();

	public int getNetworkBitCount();

	public int getHostBitCount();

	public T getNetworkAddress();

}
