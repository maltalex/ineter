/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class IPv4Range extends IPRange<IPv4Address> {

	private static final long serialVersionUID = 1L;

	public static IPv4Range of(IPv4Address firstAddress, IPv4Address lastAddress) {
		return new IPv4Range(firstAddress, lastAddress);
	}

	public static IPv4Range between(String between) {
		String[] parts = between.split("-");
		return IPv4Range.of(IPv4Address.of(parts[0].trim()), IPv4Address.of(parts[1].trim()));
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder implements Serializable {

		private static final long serialVersionUID = 1L;

		public static final IPv4Address DEFAULT_LAST = IPv4Address.of("255.255.255.255");
		public static final IPv4Address DEFAULT_FIRST = IPv4Address.of("0.0.0.0");

		private final IPv4Address firstAddress;
		private final IPv4Address lastAddress;

		Builder() {
			this.firstAddress = DEFAULT_FIRST;
			this.lastAddress = DEFAULT_LAST;
		}

		Builder(IPv4Address firstAddress, IPv4Address lastAddress) {
			this.firstAddress = firstAddress;
			this.lastAddress = lastAddress;
		}

		public Builder first(IPv4Address firstAddress) {
			return new Builder(firstAddress, this.lastAddress);
		}

		public Builder first(byte[] bigEndianByteArr) {
			return this.first(IPv4Address.of(bigEndianByteArr));
		}

		public Builder first(int ip) {
			return this.first(IPv4Address.of(ip));
		}

		public Builder first(String ip) {
			return this.first(IPv4Address.of(ip));
		}

		public Builder first(Inet4Address address) {
			return this.first(IPv4Address.of(address));
		}

		public Builder last(IPv4Address lastAddress) {
			return new Builder(this.firstAddress, lastAddress);
		}

		public Builder last(byte[] bigEndianByteArr) {
			return this.last(IPv4Address.of(bigEndianByteArr));
		}

		public Builder last(int ip) {
			return this.last(IPv4Address.of(ip));
		}

		public Builder last(String ip) {
			return this.last(IPv4Address.of(ip));
		}

		public Builder last(Inet4Address address) {
			return this.last(IPv4Address.of(address));
		}

		public IPv4Range build() {
			return new IPv4Range(this.firstAddress, this.lastAddress);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((this.firstAddress == null) ? 0 : this.firstAddress.hashCode());
			result = prime * result + ((this.lastAddress == null) ? 0 : this.lastAddress.hashCode());
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
			Builder other = (Builder) obj;
			if (this.firstAddress == null) {
				if (other.firstAddress != null) {
					return false;
				}
			} else if (!this.firstAddress.equals(other.firstAddress)) {
				return false;
			}
			if (this.lastAddress == null) {
				if (other.lastAddress != null) {
					return false;
				}
			} else if (!this.lastAddress.equals(other.lastAddress)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return String.format("Ip4Range.Builder [firstAddress=%s, lastAddress=%s]", this.firstAddress,
					this.lastAddress);
		}

	}

	final IPv4Address firstAddress;
	final IPv4Address lastAddress;

	IPv4Range(IPv4Address firstAddress, IPv4Address lastAddress) {
		this.firstAddress = firstAddress;
		this.lastAddress = lastAddress;
		if (this.firstAddress == null || this.lastAddress == null) {
			throw new NullPointerException("Neither the first nor the last address can be null");
		}

		if (this.firstAddress.compareTo(lastAddress) > 0) {
			throw new IllegalArgumentException(
					String.format("The first address in the range (%s) has to be lower than the last address (%s)",
							firstAddress.toString(), lastAddress.toString()));
		}
	}

	@Override
	public IPv4Address getFirst() {
		return this.firstAddress;
	}

	@Override
	public IPv4Address getLast() {
		return this.lastAddress;
	}

	@Override
	public Long length() {
		return Long.valueOf(this.lastAddress.toLong() - this.firstAddress.toLong() + 1);
	}

	@Override
	public Iterator<IPv4Address> iterator(boolean skipFirst, boolean skipLast) {
		return new Iterator<IPv4Address>() {

			AtomicLong next = new AtomicLong(
					skipFirst ? IPv4Range.this.firstAddress.next().toLong() : IPv4Range.this.firstAddress.toLong());
			long last = skipLast ? IPv4Range.this.lastAddress.previous().toLong() : IPv4Range.this.lastAddress.toLong();

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return this.next.get() <= this.last;
			}

			@Override
			public IPv4Address next() {
				long tempNext;
				if ((tempNext = this.next.getAndIncrement()) <= this.last) {
					return new IPv4Address((int) tempNext);
				}
				throw new NoSuchElementException();
			}
		};
	}

	IPv4Subnet maxSubnetInRange(IPv4Address addr) {
		int addrHostBits = Integer.numberOfTrailingZeros(addr.toInt());
		int networkBitsEq = Integer.numberOfLeadingZeros(this.lastAddress.toInt() ^ addr.toInt());
		int hostBitsMax = 32 - networkBitsEq;
		if (Integer.numberOfTrailingZeros(~this.lastAddress.toInt()) < hostBitsMax) {
			hostBitsMax--;
		}

		int hostBits = Math.min(addrHostBits, hostBitsMax);
		return IPv4Subnet.of(addr, (byte) (32 - hostBits));
	}

	@Override
	public List<IPv4Subnet> toSubnets() {
		ArrayList<IPv4Subnet> result = new ArrayList<>();
		IPv4Address lastAddress = this.firstAddress.previous();
		do {
			IPv4Subnet nextSubnet = maxSubnetInRange(lastAddress.next());
			result.add(nextSubnet);
			lastAddress = nextSubnet.lastAddress;
		} while (lastAddress.compareTo(this.lastAddress) < 0);

		return result;
	}
}
