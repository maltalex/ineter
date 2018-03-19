/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class IPv6Range extends IPRange<IPv6Address> {

	private static final long serialVersionUID = 1L;

	public static IPv6Range of(IPv6Address firstAddress, IPv6Address lastAddress) {
		return new IPv6Range(firstAddress, lastAddress);
	}

	public static IPv6Range between(String between) {
		String[] parts = between.split("-");
		return IPv6Range.of(IPv6Address.of(parts[0].trim()), IPv6Address.of(parts[1].trim()));
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder implements Serializable {

		private static final long serialVersionUID = 1L;
		public static final IPv6Address DEFAULT_LAST = IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		public static final IPv6Address DEFAULT_FIRST = IPv6Address.of("::");

		private final IPv6Address firstAddress;
		private final IPv6Address lastAddress;

		Builder() {
			this.firstAddress = DEFAULT_FIRST;
			this.lastAddress = DEFAULT_LAST;
		}

		Builder(IPv6Address firstAddress, IPv6Address lastAddress) {
			this.firstAddress = firstAddress;
			this.lastAddress = lastAddress;
		}

		public Builder first(IPv6Address firstAddress) {
			return new Builder(firstAddress, this.lastAddress);
		}

		public Builder first(byte[] bigEndianByteArr) {
			return this.first(IPv6Address.of(bigEndianByteArr));
		}

		public Builder first(String ip) {
			return this.first(IPv6Address.of(ip));
		}

		public Builder first(Inet6Address address) {
			return this.first(IPv6Address.of(address));
		}

		public Builder last(IPv6Address lastAddress) {
			return new Builder(this.firstAddress, lastAddress);
		}

		public Builder last(byte[] bigEndianByteArr) {
			return this.last(IPv6Address.of(bigEndianByteArr));
		}

		public Builder last(String ip) {
			return this.last(IPv6Address.of(ip));
		}

		public Builder last(Inet6Address address) {
			return this.last(IPv6Address.of(address));
		}

		public IPv6Range build() {
			return new IPv6Range(this.firstAddress, this.lastAddress);
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
			return String.format("Ip6Range.Builder [firstAddress=%s, lastAddress=%s]", this.firstAddress,
					this.lastAddress);
		}

	}

	final IPv6Address firstAddress;
	final IPv6Address lastAddress;

	IPv6Range(IPv6Address firstAddress, IPv6Address lastAddress) {
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
	public IPv6Address getFirst() {
		return this.firstAddress;
	}

	@Override
	public IPv6Address getLast() {
		return this.lastAddress;
	}

	@Override
	public BigInteger length() {
		return this.lastAddress.toBigInteger().subtract(this.firstAddress.toBigInteger()).add(BigInteger.ONE);
	}

	@Override
	public Iterator<IPv6Address> iterator(boolean skipFirst, boolean skipLast) {
		return new Iterator<IPv6Address>() {

			AtomicLong nextAddition = new AtomicLong(skipFirst ? 1 : 0);
			// Will throw exception if length is greater than max long
			long totalCount = skipLast ? length().longValueExact() - 1 : length().longValueExact();

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return this.nextAddition.get() < this.totalCount;
			}

			@Override
			public IPv6Address next() {
				long tempNext;
				if ((tempNext = this.nextAddition.getAndIncrement()) < this.totalCount) {
					return IPv6Range.this.firstAddress.plus(tempNext);
				}
				throw new NoSuchElementException();
			}
		};
	}

	int numberOfTrailingOnes(IPv6Address a) {
		long notLower = ~a.lower;
		return (notLower == 0) ? 64 + Long.numberOfTrailingZeros(~a.upper) : Long.numberOfTrailingZeros(notLower);
	}

	int numberOfTrailingZeros(IPv6Address a) {
		return (a.lower == 0) ? 64 + Long.numberOfTrailingZeros(a.upper) : Long.numberOfTrailingZeros(a.lower);
	}

	int numberOfLeadingEq(IPv6Address a, IPv6Address b) {
		long upperXOR = a.upper ^ b.upper;
		if (upperXOR == 0) {
			return 64 + Long.numberOfLeadingZeros(a.lower ^ b.lower);
		}
		return Long.numberOfLeadingZeros(upperXOR);
	}

	IPv6Subnet maxSubnetInRange(IPv6Address addr) {
		int addrHostBits = numberOfTrailingZeros(addr);
		int networkBitsEq = numberOfLeadingEq(this.lastAddress, addr);
		int hostBitsMax = 128 - networkBitsEq;
		if (numberOfTrailingOnes(this.lastAddress) < hostBitsMax) {
			hostBitsMax--;
		}

		int hostBits = Math.min(addrHostBits, hostBitsMax);
		return IPv6Subnet.of(addr, 128 - hostBits);
	}

	@Override
	public List<IPv6Subnet> toSubnets() {
		ArrayList<IPv6Subnet> result = new ArrayList<>();
		IPv6Address lastAddress = this.firstAddress.previous();
		do {
			IPv6Subnet nextSubnet = maxSubnetInRange(lastAddress.next());
			result.add(nextSubnet);
			lastAddress = nextSubnet.lastAddress;
		} while (lastAddress.compareTo(this.lastAddress) < 0);

		return result;
	}
}
