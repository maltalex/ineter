/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import static java.lang.String.format;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import com.github.maltalex.ineter.base.IPv4Address;

public class IPv4Range extends IPRange<IPv4Address> {

	private static final long serialVersionUID = 1L;

	public static IPv4Range of(IPv4Address firstAddress, IPv4Address lastAddress) {
		return new IPv4Range(firstAddress, lastAddress);
	}

	public static IPv4Range of(IPv4Address address) {
		return IPv4Range.of(address, address);
	}

	public static IPv4Range of(String firstAddress, String lastAddress) {
		return new IPv4Range(IPv4Address.of(firstAddress), IPv4Address.of(lastAddress));
	}

	public static IPv4Range of(String address) {
		return IPv4Range.of(address, address);
	}

	public static IPv4Range of(byte[] firstAddress, byte[] lastAddress) {
		return new IPv4Range(IPv4Address.of(firstAddress), IPv4Address.of(lastAddress));
	}

	public static IPv4Range of(byte[] address) {
		return IPv4Range.of(address, address);
	}

	public static IPv4Range of(Inet4Address firstAddress, Inet4Address lastAddress) {
		return new IPv4Range(IPv4Address.of(firstAddress), IPv4Address.of(lastAddress));
	}

	public static IPv4Range of(Inet4Address address) {
		return IPv4Range.of(address, address);
	}

	/**
	 * Use {@link IPv4Range#parse(String)} instead
	 */
	@Deprecated
	public static IPv4Range between(String between) {
		String[] parts = between.split("-");
		return IPv4Range.of(IPv4Address.of(parts[0].trim()), IPv4Address.of(parts[1].trim()));
	}

	/**
	 * Parses the given String into an {@link IPv4Range} The String can be either a
	 * single address, a range such as "192.168.0.0-192.168.1.2" or a subnet such as
	 * "192.168.0.0/16"
	 *
	 * @param from - a String representation of a single IPv4 address, a range or a
	 *             subnet
	 * @return An {@link IPv4Range}
	 */
	public static IPv4Range parse(String from) {
		return parseRange(from, IPv4Range::of, IPv4Subnet::of);
	}

	protected final IPv4Address firstAddress;
	protected final IPv4Address lastAddress;

	public IPv4Range(IPv4Address firstAddress, IPv4Address lastAddress) {
		this.firstAddress = firstAddress;
		this.lastAddress = lastAddress;
		if (this.firstAddress == null || this.lastAddress == null) {
			throw new NullPointerException("Neither the first nor the last address can be null");
		}

		if (this.firstAddress.compareTo(lastAddress) > 0) {
			throw new IllegalArgumentException(
					format("The first address in the range (%s) has to be lower than the last address (%s)",
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
					return IPv4Address.of((int) tempNext);
				}
				throw new NoSuchElementException();
			}
		};
	}

	protected IPv4Subnet maxSubnetInRange(IPv4Address addr) {
		int addrHostBits = Integer.numberOfTrailingZeros(addr.toInt());
		int networkBitsEq = Integer.numberOfLeadingZeros(this.lastAddress.toInt() ^ addr.toInt());
		int hostBitsMax = IPv4Address.ADDRESS_BITS - networkBitsEq;
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

	/**
	 * Merges adjacent ranges to the bigger ones
	 *
	 * @param addressesToMerge ranges to merge
	 * @return list of merged ranges
	 */
	public static List<IPv4Range> merge(IPv4Range... addressesToMerge) {
		return merge(Arrays.asList(addressesToMerge));
	}

	/**
	 * Merges adjacent ranges to the bigger ones
	 *
	 * @param addressesToMerge ranges to merge
	 * @return list of merged ranges
	 */
	public static List<IPv4Range> merge(Collection<IPv4Range> addressesToMerge) {
		return merge(addressesToMerge, IPv4Range::of);
	}

	/**
	 * Extends this range with adjacent one
	 *
	 * @param extension adjacent range to be merged with current one
	 * @return merged range
	 * @throws IllegalArgumentException extension range is not adjacent with this one
	 */
	public IPv4Range extend(IPv4Range extension) throws IllegalArgumentException {
		return IPRange.extend(this, extension, IPv4Range::of);
	}

	/**
	 * Extends this range with adjacent IPv4 address
	 *
	 * @param extension adjacent address to be merged with current range
	 * @return merged range
	 * @throws IllegalArgumentException extension address is not adjacent with this range
	 */
	public IPv4Range extend(IPv4Address extension) throws IllegalArgumentException {
		return extend(IPv4Range.of(extension));
	}
}
