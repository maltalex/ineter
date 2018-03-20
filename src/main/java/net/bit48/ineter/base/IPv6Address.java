/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.base;

import java.net.Inet6Address;

import net.bit48.ineter.range.IPv6Range;
import net.bit48.ineter.range.IPv6Subnet;

public class IPv6Address extends IPAddress implements Comparable<IPv6Address> {

	public static enum IPv6KnownRange {

		//@formatter:off
		/**
		 * ::/128 - RFC 4291
		 */
		UNSPECIFIED(IPv6Subnet.of("::/128")),
		/**
		 * ::1/128 - RFC 4291
		 */
		LOOPBACK(IPv6Subnet.of("::1/128")),

		/**
		 * 100::/64 - RFC 6666
		 */
		DISCARD(IPv6Subnet.of("100::/64")),
		/**
		 * 2001:10::/28 - RFC 4843
		 */
		ORCHID(IPv6Subnet.of("2001:10::/28")),
		/**
		 * 2001:20::/28
		 */
		ORCHID_2(IPv6Subnet.of("2001:20::/28")),
		/**
		 * 2001:db8::/32 - RFC 3849
		 */
		DOCUMENTATION(IPv6Subnet.of("2001:db8::/32")),

		/**
		 * ::/96 - RFC 4291
		 */
		IPV4_COMPATIBLE_IPV6_DEPRECATED(IPv6Subnet.of("::/96")),
		/**
		 * ::ffff:0:0/96 - RFC 4291
		 */
		IPV4_MAPPED_IPV6(IPv6Subnet.of("::ffff:0:0/96")),
		/**
		 * 64:ff9b::/96 - RFC 6052
		 */
		IPV4_IPV6_TRANSLATION_WELL_KNOWN(IPv6Subnet.of("64:ff9b::/96")),
		/**
		 * 2002::/16 - RFC 3056
		 */
		TRANSLATION_6_TO_4(IPv6Subnet.of("2002::/16")),
		/**
		 * 2001:0000:/32 - RFC 4380
		 */
		TEREDO(IPv6Subnet.of("2001::/32")),

		/**
		 * fc00::/7 - RFC 4193
		 */
		ULA(IPv6Subnet.of("fc00::/7")),

		/**
		 * ff00::/8 - RFC 4291
		 */
		MULTICAST(IPv6Subnet.of("ff00::/8")),
		/**
		 * ff0e::/16 - RFC 4291
		 */
		GLOBAL_MULTICAST(IPv6Subnet.of("ff0e::/16")),
		/**
		 * ff05::/16 - RFC 4291
		 */
		SITE_LOCAL_MULTICAST(IPv6Subnet.of("ff05::/16")),
		/**
		 * ff02::/16 - RFC 4291
		 */
		LINK_LOCAL_MULTICAST(IPv6Subnet.of("ff02::/16")),
		/**
		 * ff01::/16 - RFC 4291
		 */
		INTERFACE_LOCAL_MULTICAST(IPv6Subnet.of("ff01::/16")),

		/**
		 * 2000::/3 - RFC 3587
		 */
		GLOBAL_UNICAST(IPv6Subnet.of("2000::/3")),
		/**
		 * fe80::/10 - RFC 4291
		 */
		LINK_LOCAL_UNICAST(IPv6Subnet.of("fe80::/10")),
		/**
		 * fec::/10 - RFC 4291
		 */
		SITE_LOCAL_UNICAST_DEPRECATED(IPv6Subnet.of("fec::/10"));
		//@formatter:on

		private IPv6Range range;

		private IPv6KnownRange(IPv6Range subnet) {
			this.range = subnet;
		}

		public boolean contains(IPv6Address address) {
			return this.range.contains(address);
		}

		public IPv6Range range() {
			return this.range;
		}
	}

	/**
	 * Enum for extracting 16-bit shorts from 64-bit longs
	 */
	static enum LongShort {
		SHORT_A(0), SHORT_B(1), SHORT_C(2), SHORT_D(3);

		private final long mask;
		private final int shift;

		private LongShort(int shortShift) {
			this.shift = 48 - (shortShift << 4);
			this.mask = 0xffff000000000000L >>> (shortShift << 4);
		}

		public long isolateAsLong(long l) {
			return (l & this.mask) >>> this.shift;
		}

		public int isolateAsInt(long l) {
			return (int) isolateAsLong(l);
		}
	}

	/**
	 * Enum for extracting bytes from 64-bit longs
	 */
	static enum LongByte {
		BYTE_A(0), BYTE_B(1), BYTE_C(2), BYTE_D(3), BYTE_E(4), BYTE_F(5), BYTE_G(6), BYTE_H(7);

		private final long mask;
		private final int shift;

		private LongByte(int shortShift) {
			this.shift = 56 - (shortShift << 3);
			this.mask = 0xff00000000000000L >>> (shortShift << 3);
		}

		public long isolateAsLong(long l) {
			return (l & this.mask) >>> this.shift;
		}

		public byte isolateAsByte(long l) {
			return (byte) isolateAsLong(l);
		}

		public long expand(byte b) {
			return (b & 0xffL) << this.shift;
		}

		static long extractLong(byte[] bigEndianByteArr, int offset) {
			return LongByte.BYTE_A.expand(bigEndianByteArr[offset])
					| LongByte.BYTE_B.expand(bigEndianByteArr[offset + 1])
					| LongByte.BYTE_C.expand(bigEndianByteArr[offset + 2])
					| LongByte.BYTE_D.expand(bigEndianByteArr[offset + 3])
					| LongByte.BYTE_E.expand(bigEndianByteArr[offset + 4])
					| LongByte.BYTE_F.expand(bigEndianByteArr[offset + 5])
					| LongByte.BYTE_G.expand(bigEndianByteArr[offset + 6])
					| LongByte.BYTE_H.expand(bigEndianByteArr[offset + 7]);
		}
	}

	public static final int ADDRESS_BITS = 128;
	public static final int ADDRESS_BYTES = 16;
	public static final int ADDRESS_SHORTS = 8;
	public static final int HOLDER_BITS = 64;

	private static final long serialVersionUID = 1L;

	public static IPv6Address of(long upper, long lower) {
		return new IPv6Address(upper, lower);
	}

	static void verifyArray(byte[] bigEndianByteArr) {
		if (bigEndianByteArr == null) {
			throw new NullPointerException();
		}
		if (bigEndianByteArr.length != ADDRESS_BYTES) {
			throw new IllegalArgumentException("The given array must be 16 bytes long");
		}
	}

	public static IPv6Address of(byte[] bigEndianByteArr) {
		verifyArray(bigEndianByteArr);
		return new IPv6Address(LongByte.extractLong(bigEndianByteArr, 0), LongByte.extractLong(bigEndianByteArr, 8));
	}

	public static IPv6Address of(Inet6Address address) {
		if (address.getScopedInterface() == null) {
			return of(address.getAddress());
		}
		return ZonedIPv6Address.of(address);
	}

	public static IPv6Address of(String address) {
		// This (over-engineered) method parses and validates an IPv6 address in
		// String form in a single pass using only primitive types (except the
		// zone String).

		// The idea is to iterate over the address start to finish, accumulating
		// the current "part" (i.e. 16 bit piece between colons).
		// if we stumble upon a double colon (::), then we need to figure out
		// how many zeroes it represents.

		// To do that, we stop the forward iteration and start
		// iterating from the end, accumulating "parts" as we go along until we
		// reach the same double colon.

		// According to a JMH benchmark, this method parses randomly generated
		// addresses, half of which contain a double colon ("::"), about
		// 40% faster than Java's default InetAddress parsing

		// 0. Validate Not null
		if (address == null) {
			throw new IllegalArgumentException("Attempted to parse null address");
		}

		// 1. Validate Length
		int first = 0, last = address.length();
		if (address.length() < 2) {
			throw new IllegalArgumentException(
					String.format("Invalid length - the string %s is too short to be an IPv6 address", address));
		}
		if (address.charAt(0) == '[') {
			first++;
			if (!(address.charAt(--last) == ']')) {
				throw new IllegalArgumentException("The address begins with \"[\" but doesn't end with \"]\"");
			}
		}
		String zone = null;
		for (int i = last - 1; i > first; i--) {
			char ch = address.charAt(i);
			if (ch == ':') { // Looks like a normal address, carry on parsing
				break;
			}
			if (ch == '%') { // This is a zoned address - take out the zone and
								// move the "last" index
				zone = address.substring(i + 1, last); // skip the "%" itself
				last = i;
				break;
			}
		}
		int length = last - first;
		if (length > 39) {
			throw new IllegalArgumentException(
					String.format("Invalid length - the string %s is too long to be an IPv6 address. Length: %d",
							address, address.length()));
		}

		//@formatter:off
		//Holders
		long partAccumulator = 0; // Accumulator for the current address part, before it's added to the upper/lower accumulators
		long upperAccumulator = 0, lowerAccumulator = 0; //Accumulators for the upper and lower 64 bit parts of the address
		//Indexes
		int partIndex = 0; //Index of the current 16 bit part - should be 0 to 7
		int afterDoubleSemicolonIndex = last + 2; //Index after :: characters. Originally set past the string length
		//Counters
		int partCount = 1; //Total number of 16 bit address parts, for address verification
		int partHexDigitCount = 0; //Number of hex digits in current 16 bit part (should be up to 4)
		//@formatter:on

		// 2. Iterate start to finish or until a :: is encountered
		for (int i = first; i < last; i++) {
			char c = address.charAt(i);
			if (isHexDigit(c)) {
				if (++partHexDigitCount > 4) {
					throw new IllegalArgumentException(
							"Address parts must contain no more than 16 bits (4 hex digits)");
				}
				// Add to part accumulator
				partAccumulator = (partAccumulator << 4) | (Character.digit(c, 16) & 0xffff);
			} else {
				if (c == ':') {
					// Reached end of current part. Add to accumulator
					if (partIndex < 4) {
						upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
					} else {
						lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
					}
					partIndex++;
					partCount++;
					partAccumulator = 0;
					partHexDigitCount = 0;
					// Is next char ":"?
					if (i < last - 1 && address.charAt(i + 1) == ':') {
						// Found :: - continue to (3) - iterate from the end
						afterDoubleSemicolonIndex = i + 2;
						break;
					}
					continue;
				}
				throw new IllegalArgumentException(String.format("Illegal character: %c at index %d", c, i));
			}
		}

		// 3. Iterate from the end until the ::
		int lastFilledPartIndex = partIndex - 1;
		partIndex = 7;
		for (int i = last - 1; i >= afterDoubleSemicolonIndex; i--) {
			char c = address.charAt(i);
			if (isHexDigit(c)) {
				if (partIndex <= lastFilledPartIndex) {
					throw new IllegalArgumentException("Too many parts. Expected 8 parts");
				}
				partAccumulator |= ((Character.digit(c, 16) & 0xffff) << (partHexDigitCount << 2));
				if (++partHexDigitCount > 4) {
					throw new IllegalArgumentException(
							"Address parts must contain no more than 16 bits (4 hex digits)");
				}
			} else {
				if (c == ':') {
					if (partIndex < 4) {
						upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
					} else {
						lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
					}
					if (address.charAt(i - 1) == ':') {
						throw new IllegalArgumentException(String.format("Error at index %d - unexpected colon", i));
					}
					partCount++;
					partIndex--;
					partAccumulator = 0;
					partHexDigitCount = 0;
					continue;
				}
				throw new IllegalArgumentException(String.format("Illegal character: %c at index %d", c, i));
			}
		}

		// 4. Append last part
		if (partIndex < 4) {
			upperAccumulator |= partAccumulator << (48 - (partIndex << 4));
		} else {
			lowerAccumulator |= partAccumulator << (48 - ((partIndex - 4) << 4));
		}

		// 5. Check total number of parts
		if (partCount > ADDRESS_SHORTS || (partCount < ADDRESS_SHORTS && afterDoubleSemicolonIndex == last + 2)) {
			throw new IllegalArgumentException(String.format("Invalid number of parts. Expected 8, got %d", partCount));
		}
		return zone == null ? new IPv6Address(upperAccumulator, lowerAccumulator)
				: new ZonedIPv6Address(upperAccumulator, lowerAccumulator, zone);
	}

	private static boolean isHexDigit(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	static int unsignedCompare(long a, long b) {
		if (a == b) {
			return 0;
		}
		return (a + Long.MIN_VALUE) < (b + Long.MIN_VALUE) ? -1 : 1;
	}

	// Instance variables and methods

	protected final long upper;
	protected final long lower;

	IPv6Address(long upper, long lower) {
		this.upper = upper;
		this.lower = lower;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.lower ^ (this.lower >>> 32));
		result = prime * result + (int) (this.upper ^ (this.upper >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IPv6Address other = (IPv6Address) obj;
		if (this.lower != other.lower) {
			return false;
		}
		if (this.upper != other.upper) {
			return false;
		}
		return true;
	}

	public long getUpper() {
		return this.upper;
	}

	public long getLower() {
		return this.lower;
	}

	@Override
	public boolean is6To4() {
		return IPv6KnownRange.TRANSLATION_6_TO_4.contains(this);
	}

	@Override
	public boolean isMartian() {
		return isUnspecified() || isLoopback() || IPv6KnownRange.IPV4_MAPPED_IPV6.contains(this)
				|| IPv6KnownRange.IPV4_COMPATIBLE_IPV6_DEPRECATED.contains(this)
				|| IPv6KnownRange.IPV4_IPV6_TRANSLATION_WELL_KNOWN.contains(this) || isReserved() || isPrivate()
				|| isLinkLocal() || (isMulticast() && !IPv6KnownRange.GLOBAL_MULTICAST.contains(this));
	}

	public boolean isIPv4Translation() {
		return IPv6KnownRange.TEREDO.contains(this) || IPv6KnownRange.IPV4_MAPPED_IPV6.contains(this)
				|| IPv6KnownRange.TRANSLATION_6_TO_4.contains(this)
				|| IPv6KnownRange.IPV4_IPV6_TRANSLATION_WELL_KNOWN.contains(this);
	}

	@Override
	public boolean isLoopback() {
		return IPv6KnownRange.LOOPBACK.contains(this);
	}

	public boolean isGlobalUnicast() {
		return IPv6KnownRange.GLOBAL_UNICAST.contains(this);
	}

	@Override
	public boolean isLinkLocal() {
		return IPv6KnownRange.LINK_LOCAL_UNICAST.contains(this);
	}

	@Override
	public boolean isMulticast() {
		return IPv6KnownRange.MULTICAST.contains(this);
	}

	@Override
	public boolean isPrivate() {
		return IPv6KnownRange.ULA.contains(this);
	}

	@Override
	public boolean isReserved() {
		return IPv6KnownRange.ORCHID.contains(this) || IPv6KnownRange.ORCHID_2.contains(this)
				|| IPv6KnownRange.DISCARD.contains(this) || IPv6KnownRange.DOCUMENTATION.contains(this);
	}

	@Override
	public boolean isUnspecified() {
		return IPv6KnownRange.UNSPECIFIED.contains(this);
	}

	@Override
	public IPv6Address next() {
		return plus(1);
	}

	static boolean hasCarry(long a, long b, long result) {
		long aMSB = a >>> 63;
		long bMSB = b >>> 63;
		long resutlMSB = result >>> 63;

		/* @formatter:off
		 * a b r Carry
		 * 0 0 0 0
		 * 0 0 1 0
		 * 0 1 0 1
		 * 0 1 1 0
		 * 1 0 0 1
		 * 1 0 1 0
		 * 1 1 0 1
		 * 1 1 1 1
		 * @formatter:on
		 */
		return ((aMSB & bMSB) == 1) || ((aMSB ^ bMSB) == 1 && resutlMSB == 0);
	}

	static boolean hasBorrow(long a, long b, long result) {
		long aMSB = a >>> 63;
		long bMSB = b >>> 63;
		long resutlMSB = result >>> 63;

		/* @formatter:off
		 * a b r Borrow
		 * 0 0 0 0
		 * 0 0 1 1
		 * 0 1 0 1
		 * 0 1 1 1
		 * 1 0 0 0
		 * 1 0 1 0
		 * 1 1 0 0
		 * 1 1 1 1
		 * @formatter:on
		 */
		return ((aMSB & bMSB & resutlMSB) == 1) || (aMSB == 0 && (bMSB | resutlMSB) == 1);
	}

	public IPv6Address plus(long n) {
		if (n < 0) {
			return minus(-n);
		}
		long newLower = this.lower + n;
		long newUpper = this.upper;

		if (hasCarry(this.lower, n, newLower)) {
			newUpper++;
		}

		return new IPv6Address(newUpper, newLower);
	}

	@Override
	public IPv6Address plus(int n) {
		return plus((long) n);
	}

	@Override
	public IPv6Address previous() {
		return minus(1);
	}

	@Override
	public IPv6Address minus(int n) {
		return minus((long) n);
	}

	public IPv6Address minus(long n) {
		if (n < 0) {
			return plus(-n);
		}
		long newLower = this.lower - n;
		long newUpper = this.upper;

		// If there's a borrow from the lower addition
		if (hasBorrow(this.lower, n, newLower)) {
			newUpper--;
		}
		return new IPv6Address(newUpper, newLower);
	}

	@Override
	public byte[] toBigEndianArray() {
		return new byte[] { LongByte.BYTE_A.isolateAsByte(this.upper), LongByte.BYTE_B.isolateAsByte(this.upper),
				LongByte.BYTE_C.isolateAsByte(this.upper), LongByte.BYTE_D.isolateAsByte(this.upper),
				LongByte.BYTE_E.isolateAsByte(this.upper), LongByte.BYTE_F.isolateAsByte(this.upper),
				LongByte.BYTE_G.isolateAsByte(this.upper), LongByte.BYTE_H.isolateAsByte(this.upper),
				LongByte.BYTE_A.isolateAsByte(this.lower), LongByte.BYTE_B.isolateAsByte(this.lower),
				LongByte.BYTE_C.isolateAsByte(this.lower), LongByte.BYTE_D.isolateAsByte(this.lower),
				LongByte.BYTE_E.isolateAsByte(this.lower), LongByte.BYTE_F.isolateAsByte(this.lower),
				LongByte.BYTE_G.isolateAsByte(this.lower), LongByte.BYTE_H.isolateAsByte(this.lower) };
	}

	@Override
	public byte[] toLittleEndianArray() {
		return new byte[] { LongByte.BYTE_H.isolateAsByte(this.lower), LongByte.BYTE_G.isolateAsByte(this.lower),
				LongByte.BYTE_F.isolateAsByte(this.lower), LongByte.BYTE_E.isolateAsByte(this.lower),
				LongByte.BYTE_D.isolateAsByte(this.lower), LongByte.BYTE_C.isolateAsByte(this.lower),
				LongByte.BYTE_B.isolateAsByte(this.lower), LongByte.BYTE_A.isolateAsByte(this.lower),
				LongByte.BYTE_H.isolateAsByte(this.upper), LongByte.BYTE_G.isolateAsByte(this.upper),
				LongByte.BYTE_F.isolateAsByte(this.upper), LongByte.BYTE_E.isolateAsByte(this.upper),
				LongByte.BYTE_D.isolateAsByte(this.upper), LongByte.BYTE_C.isolateAsByte(this.upper),
				LongByte.BYTE_B.isolateAsByte(this.upper), LongByte.BYTE_A.isolateAsByte(this.upper) };
	}

	@Override
	public int compareTo(IPv6Address o) {
		if (o == null) {
			return 1; // Bigger than null
		}
		if (o.isZoned()) {
			return -1;// Zoned addresses are "bigger"
		}

		return longCompare(o);
	}

	int longCompare(IPv6Address o) {
		int upperCompare = unsignedCompare(this.upper, o.upper);
		return (upperCompare == 0) ? unsignedCompare(this.lower, o.lower) : upperCompare;
	}

	public Inet6Address toInet6Address() {
		return (Inet6Address) super.toInetAddress();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(39);
		builder.append(Integer.toHexString(LongShort.SHORT_A.isolateAsInt(this.upper)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_B.isolateAsInt(this.upper)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_C.isolateAsInt(this.upper)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_D.isolateAsInt(this.upper)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_A.isolateAsInt(this.lower)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_B.isolateAsInt(this.lower)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_C.isolateAsInt(this.lower)));
		builder.append(":");
		builder.append(Integer.toHexString(LongShort.SHORT_D.isolateAsInt(this.lower)));
		return builder.toString();
	}

	@Override
	public int version() {
		return 6;
	}

	public boolean isZoned() {
		return false;
	}
}
