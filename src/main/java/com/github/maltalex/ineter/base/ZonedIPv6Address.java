/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.base;

import java.net.Inet6Address;
import java.util.Objects;

public class ZonedIPv6Address extends IPv6Address {

	private static final long serialVersionUID = 1L;

	/**
	 * Build an ZonedIPv6Address from a literal IPv6 with a zone designation
	 * such as "[fe80::1%2]","fe80::1%eth0", and similar valid forms
	 *
	 * @param address
	 *            with zone
	 * @return new ZonedIPv6Address instance
	 * @throws IllegalArgumentException
	 *             if the provided address is invalid
	 */
	public static ZonedIPv6Address of(String address) {
		IPAddress ip = IPv6Address.of(address);
		if (!(ip instanceof ZonedIPv6Address)) {
			throw new IllegalArgumentException(String.format("The provided address (%s) is not zoned", address));
		}
		return (ZonedIPv6Address) ip;
	}

	/**
	 * Build a ZonedIPv6Address from two longs and zone - upper and lower 64
	 * bits in form of longs, and a String zone
	 *
	 * @param upper
	 *            upper 64 bits of the IPv6Address
	 * @param lower
	 *            lower 64 bits of the IPv6Address
	 * @param zone
	 *            zone String
	 * @return new IPv6Address instance
	 */
	public static ZonedIPv6Address of(long upper, long lower, String zone) {
		return new ZonedIPv6Address(upper, lower, zone);
	}

	/**
	 * Build a ZonedIPv6Address from a 16 byte long big-endian (highest byte
	 * first) byte array and a zone String
	 *
	 * @param bigEndianByteArr
	 *            16 byte big-endian byte array
	 * @param zone
	 *            zone String
	 * @return new ZoneIPv6Address instance
	 */
	public static ZonedIPv6Address of(byte[] bigEndianByteArr, String zone) {
		verifyArray(bigEndianByteArr);
		long upper = LongByte.extractLong(bigEndianByteArr, 0);
		long lower = LongByte.extractLong(bigEndianByteArr, 8);
		return new ZonedIPv6Address(upper, lower, zone);
	}

	/**
	 * Build a ZonedIPv6Address from an IPv6Address and a separate zone String
	 *
	 * @param address
	 * @param zone
	 * @return new ZonedIPv6Address instance
	 */
	public static ZonedIPv6Address of(IPv6Address address, String zone) {
		return of(address.upper, address.lower, zone);
	}

	/**
	 * Build a ZonedIPv6Address from an Inet6Address with a scoped interface or
	 * scope id
	 *
	 * @param address
	 * @return new IPv6Address instance
	 */
	public static ZonedIPv6Address of(Inet6Address address) {
		if (address.getScopedInterface() != null) {
			return of(address.getAddress(), address.getScopedInterface().getName());
		}
		return of(address.getAddress(), Integer.toString(address.getScopeId()));
	}

	protected final String zone;

	/**
	 * Build a ZonedIPv6Address from two longs an a String - upper and lower 64
	 * bits in form of longs and a zone
	 *
	 * @param upper
	 *            upper 64 bits of the IPv6Address
	 * @param lower
	 *            lower 64 bits of the IPv6Address
	 * @param zone
	 *            the IPv6Address
	 */
	public ZonedIPv6Address(long upper, long lower, String zone) {
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

	/**
	 * Does this address have a specific zone?
	 *
	 * @return true
	 */
	@Override
	public boolean isZoned() {
		return true;
	}

	/**
	 * Returns the zone this address belongs to
	 *
	 * @return the zone
	 */
	public String getZone() {
		return this.zone;
	}
}
