/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.math.BigInteger;

import com.github.maltalex.ineter.base.IPv6Address;

public class IPv6Subnet extends IPv6Range implements IPSubnet<IPv6Address, BigInteger> {

	static enum IPv6SubnetMask {

		//@formatter:off
		MASK_000, MASK_001, MASK_002, MASK_003, MASK_004, MASK_005, MASK_006, MASK_007,
		MASK_008, MASK_009, MASK_010, MASK_011, MASK_012, MASK_013, MASK_014, MASK_015,
		MASK_016, MASK_017, MASK_018, MASK_019, MASK_020, MASK_021, MASK_022, MASK_023,
		MASK_024, MASK_025, MASK_026, MASK_027, MASK_028, MASK_029, MASK_030, MASK_031,
		MASK_032, MASK_033, MASK_034, MASK_035, MASK_036, MASK_037, MASK_038, MASK_039,
		MASK_040, MASK_041, MASK_042, MASK_043, MASK_044, MASK_045, MASK_046, MASK_047,
		MASK_048, MASK_049, MASK_050, MASK_051, MASK_052, MASK_053, MASK_054, MASK_055,
		MASK_056, MASK_057, MASK_058, MASK_059, MASK_060, MASK_061, MASK_062, MASK_063,
		MASK_064, MASK_065, MASK_066, MASK_067, MASK_068, MASK_069, MASK_070, MASK_071,
		MASK_072, MASK_073, MASK_074, MASK_075, MASK_076, MASK_077, MASK_078, MASK_079,
		MASK_080, MASK_081, MASK_082, MASK_083, MASK_084, MASK_085, MASK_086, MASK_087,
		MASK_088, MASK_089, MASK_090, MASK_091, MASK_092, MASK_093, MASK_094, MASK_095,
		MASK_096, MASK_097, MASK_098, MASK_099, MASK_100, MASK_101, MASK_102, MASK_103,
		MASK_104, MASK_105, MASK_106, MASK_107, MASK_108, MASK_109, MASK_110, MASK_111,
		MASK_112, MASK_113, MASK_114, MASK_115, MASK_116, MASK_117, MASK_118, MASK_119,
		MASK_120, MASK_121, MASK_122, MASK_123, MASK_124, MASK_125, MASK_126, MASK_127, MASK_128;
		//@formatter:on

		public static IPv6SubnetMask fromMaskLen(int maskLen) {
			if (maskLen >= 0 && maskLen <= IPv6Address.ADDRESS_BITS) {
				return IPv6SubnetMask.values()[maskLen];
			}
			throw new IllegalArgumentException("The mask length must be between 0 and 128");
		}

		private final long maskUpper, maskLower;
		private final int bitCount;

		private IPv6SubnetMask() {
			this.bitCount = ordinal();
			int upperCount = Math.min(64, this.bitCount);
			int lowerCount = this.bitCount - upperCount;
			this.maskUpper = upperCount != 0 ? 0x8000000000000000L >> upperCount - 1 : 0;
			this.maskLower = lowerCount != 0 ? 0x8000000000000000L >> lowerCount - 1 : 0;
		}

		public int maskBitCount() {
			return this.bitCount;
		}

		public IPv6Address and(IPv6Address ip) {
			return IPv6Address.of(ip.getUpper() & this.maskUpper, ip.getLower() & this.maskLower);
		}

		public IPv6Address orInverted(IPv6Address ip) {
			return IPv6Address.of(ip.getUpper() | ~this.maskUpper, ip.getLower() | ~this.maskLower);
		}

		public IPv6Address toAddress() {
			return IPv6Address.of(this.maskUpper, this.maskLower);
		}
	}

	private static final long serialVersionUID = 1L;

	public static IPv6Subnet of(String cidr) {
		String[] cidrSplit = cidr.split("/");
		return of(cidrSplit[0], cidrSplit[1]);
	}

	public static IPv6Subnet parse(String from) {
		return IPRange.parseSubnet(from, IPv6Subnet::of, 128);
	}

	public static IPv6Subnet of(String address, int maskLen) {
		return new IPv6Subnet(IPv6Address.of(address), IPv6SubnetMask.fromMaskLen(maskLen));
	}

	public static IPv6Subnet of(IPv6Address address, int maskLen) {
		return new IPv6Subnet(address, IPv6SubnetMask.fromMaskLen(maskLen));
	}

	public static IPv6Subnet of(String address, String maskLen) {
		return new IPv6Subnet(IPv6Address.of(address), IPv6SubnetMask.fromMaskLen(Integer.parseInt(maskLen)));
	}

	protected final int networkBitCount;

	public IPv6Subnet(IPv6Address address, IPv6SubnetMask mask) {
		super(mask.and(address), mask.orInverted(address));
		this.networkBitCount = mask.maskBitCount();
	}

	@Override
	public String toString() {
		return String.format("%s/%s", super.firstAddress, this.networkBitCount);
	}

	@Override
	public int getNetworkBitCount() {
		return this.networkBitCount;
	}

	@Override
	public IPv6Address getNetworkMask() {
		return IPv6SubnetMask.fromMaskLen(this.networkBitCount).toAddress();
	}

	@Override
	public int getHostBitCount() {
		return IPv6Address.ADDRESS_BITS - this.networkBitCount;
	}

	@Override
	public IPv6Address getNetworkAddress() {
		return getFirst();
	}
}
