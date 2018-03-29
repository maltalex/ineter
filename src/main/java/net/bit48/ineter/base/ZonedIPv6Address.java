/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.base;

import java.net.Inet6Address;
import java.util.Objects;

public class ZonedIPv6Address extends IPv6Address {

	private static final long serialVersionUID = 1L;

	public static ZonedIPv6Address of(String address) {
		IPAddress ip = IPv6Address.of(address);
		if (!(ip instanceof ZonedIPv6Address)) {
			throw new IllegalArgumentException(String.format("The provided address (%s) is not zoned", address));
		}
		return (ZonedIPv6Address) ip;
	}

	public static ZonedIPv6Address of(long upper, long lower, String zone) {
		return new ZonedIPv6Address(upper, lower, zone);
	}

	public static ZonedIPv6Address of(byte[] bigEndianByteArr, String zone) {
		verifyArray(bigEndianByteArr);
		long upper = LongByte.extractLong(bigEndianByteArr, 0);
		long lower = LongByte.extractLong(bigEndianByteArr, 8);
		return new ZonedIPv6Address(upper, lower, zone);
	}

	public static ZonedIPv6Address of(IPv6Address address, String zone) {
		return of(address.upper, address.lower, zone);
	}

	public static ZonedIPv6Address of(Inet6Address address) {
		if (address.getScopedInterface() != null) {
			return of(address.getAddress(), address.getScopedInterface().getName());
		}
		return of(address.getAddress(), Integer.toString(address.getScopeId()));
	}

	protected final String zone;

	protected ZonedIPv6Address(long upper, long lower, String zone) {
		super(upper, lower);
		this.zone = zone;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.zone == null) ? 0 : this.zone.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return Objects.equals(this.zone, ((ZonedIPv6Address) obj).zone);
	}

	@Override
	public int compareTo(IPv6Address o) {
		if (o == null) {
			return 1; // Bigger than null
		}
		if (o.isZoned()) {
			int zoneCompare = this.zone.compareTo(((ZonedIPv6Address) o).zone);
			return zoneCompare == 0 ? super.longCompare(o) : zoneCompare;
		}
		return 1; // Zoned addresses are "bigger"
	}

	@Override
	public String toString() {
		return String.format("%s%%%s", super.toString(), this.zone);
	}

	@Override
	public ZonedIPv6Address next() {
		return plus(1);
	}

	@Override
	public ZonedIPv6Address plus(long n) {
		if (n < 0) {
			return minus(-n);
		}
		long newLower = this.lower + n;
		long newUpper = this.upper;

		if (hasCarry(this.lower, n, newLower)) {
			newUpper++;
		}

		return new ZonedIPv6Address(newUpper, newLower, this.zone);
	}

	@Override
	public ZonedIPv6Address plus(int n) {
		return plus((long) n);
	}

	@Override
	public ZonedIPv6Address previous() {
		return minus(1);
	}

	@Override
	public ZonedIPv6Address minus(int n) {
		return minus((long) n);
	}

	@Override
	public ZonedIPv6Address minus(long n) {
		if (n < 0) {
			return plus(-n);
		}
		long newLower = this.lower - n;
		long newUpper = this.upper;

		// If there's a borrow from the lower addition
		if (hasBorrow(this.lower, n, newLower)) {
			newUpper--;
		}
		return new ZonedIPv6Address(newUpper, newLower, this.zone);
	}

	@Override
	public boolean isZoned() {
		return true;
	}

	public String getZone() {
		return this.zone;
	}
}
