/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.range;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import net.bit48.ineter.base.IPv6Address;

public class IPv6Range extends IPRange<IPv6Address> {

	private static final long serialVersionUID = 1L;

	public static IPv6Range of(IPv6Address firstAddress, IPv6Address lastAddress) {
		return new IPv6Range(firstAddress, lastAddress);
	}

	public static IPv6Range of(String firstAddress, String lastAddress) {
		return new IPv6Range(IPv6Address.of(firstAddress), IPv6Address.of(lastAddress));
	}

	public static IPv6Range of(byte[] firstAddress, byte[] lastAddress) {
		return new IPv6Range(IPv6Address.of(firstAddress), IPv6Address.of(lastAddress));
	}

	public static IPv6Range of(Inet6Address firstAddress, Inet6Address lastAddress) {
		return new IPv6Range(IPv6Address.of(firstAddress), IPv6Address.of(lastAddress));
	}

	public static IPv6Range between(String between) {
		String[] parts = between.split("-");
		return IPv6Range.of(IPv6Address.of(parts[0].trim()), IPv6Address.of(parts[1].trim()));
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
		long notLower = ~a.getLower();
		return (notLower == 0) ? IPv6Address.HOLDER_BITS + Long.numberOfTrailingZeros(~a.getUpper())
				: Long.numberOfTrailingZeros(notLower);
	}

	int numberOfTrailingZeros(IPv6Address a) {
		return (a.getLower() == 0) ? IPv6Address.HOLDER_BITS + Long.numberOfTrailingZeros(a.getUpper())
				: Long.numberOfTrailingZeros(a.getLower());
	}

	int numberOfLeadingEq(IPv6Address a, IPv6Address b) {
		long upperXOR = a.getUpper() ^ b.getUpper();
		if (upperXOR == 0) {
			return IPv6Address.HOLDER_BITS + Long.numberOfLeadingZeros(a.getLower() ^ b.getLower());
		}
		return Long.numberOfLeadingZeros(upperXOR);
	}

	IPv6Subnet maxSubnetInRange(IPv6Address addr) {
		int addrHostBits = numberOfTrailingZeros(addr);
		int networkBitsEq = numberOfLeadingEq(this.lastAddress, addr);
		int hostBitsMax = IPv6Address.ADDRESS_BITS - networkBitsEq;
		if (numberOfTrailingOnes(this.lastAddress) < hostBitsMax) {
			hostBitsMax--;
		}

		int hostBits = Math.min(addrHostBits, hostBitsMax);
		return IPv6Subnet.of(addr, IPv6Address.ADDRESS_BITS - hostBits);
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
