/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.range;

import net.bit48.ineter.base.IPv4Address;

public class IPv4Subnet extends IPv4Range implements IPSubnet<IPv4Address> {

	protected static enum IPv4SubnetMask {

		// @formatter:off
		MASK_00, MASK_01, MASK_02, MASK_03,
		MASK_04, MASK_05, MASK_06, MASK_07,
		MASK_08, MASK_09, MASK_10, MASK_11,
		MASK_12, MASK_13, MASK_14, MASK_15,
		MASK_16, MASK_17, MASK_18, MASK_19,
		MASK_20, MASK_21, MASK_22, MASK_23,
		MASK_24, MASK_25, MASK_26, MASK_27,
		MASK_28, MASK_29, MASK_30, MASK_31, MASK_32;
		// @formatter:on

		public static IPv4SubnetMask fromMaskLen(byte maskLen) {
			if (maskLen >= 0 && maskLen <= IPv4Address.ADDRESS_BITS) {
				return IPv4SubnetMask.values()[maskLen];
			}
			throw new IllegalArgumentException("The mask length must be between 0 and 32");
		}

		private final int mask;
		private final byte bitCount;

		private IPv4SubnetMask() {
			this.bitCount = (byte) ordinal();
			this.mask = this.bitCount != 0 ? 0xffffffff << (32 - this.bitCount) : 0;
		}

		public int mask() {
			return this.mask;
		}

		public byte maskBitCount() {
			return this.bitCount;
		}

		public int and(int ip) {
			return this.mask & ip;
		}

		public IPv4Address and(IPv4Address ip) {
			return IPv4Address.of(and(ip.toInt()));
		}

		public int orInverted(int ip) {
			return (~this.mask) | ip;
		}

		public IPv4Address orInverted(IPv4Address ip) {
			return IPv4Address.of(orInverted(ip.toInt()));
		}

		public IPv4Address toAddress() {
			return IPv4Address.of(mask());
		}
	}

	private static final long serialVersionUID = 1L;

	public static IPv4Subnet of(String cidr) {
		String[] cidrSplit = cidr.split("/");
		return new IPv4Subnet(IPv4Address.of(cidrSplit[0]), IPv4SubnetMask.fromMaskLen(Byte.parseByte(cidrSplit[1])));
	}

	public static IPv4Subnet of(String address, byte maskLen) {
		return new IPv4Subnet(IPv4Address.of(address), IPv4SubnetMask.fromMaskLen(maskLen));
	}

	public static IPv4Subnet of(IPv4Address address, byte maskLen) {
		return new IPv4Subnet(address, IPv4SubnetMask.fromMaskLen(maskLen));
	}

	protected final byte networkBitCount;

	public IPv4Subnet(IPv4Address address, IPv4SubnetMask mask) {
		super(mask.and(address), mask.orInverted(address));
		this.networkBitCount = mask.maskBitCount();
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.networkBitCount;
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
		IPv4Subnet other = (IPv4Subnet) obj;
		if (this.networkBitCount != other.networkBitCount) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s/%d", super.firstAddress, this.networkBitCount);
	}

	@Override
	public int getNetworkBitCount() {
		return this.networkBitCount;
	}

	@Override
	public IPv4Address getNetworkMask() {
		return IPv4SubnetMask.fromMaskLen(this.networkBitCount).toAddress();
	}

	@Override
	public int getHostBitCount() {
		return IPv4Address.ADDRESS_BITS - this.networkBitCount;
	}

	@Override
	public IPv4Address getNetworkAddress() {
		return getFirst();
	}
}
