/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.IPv4Address;
import net.bit48.ineter.IPv4Range;
import net.bit48.ineter.IPv4Subnet;

@RunWith(JUnitPlatform.class)
public class IPv4RangeTest {

	@Test
	void between() {
		IPv4Range range = IPv4Range.between("1.2.3.4-5.4.3.2");
		assertTrue(range.getFirst().equals(IPv4Address.of("1.2.3.4")));
		assertTrue(range.getLast().equals(IPv4Address.of("5.4.3.2")));
		assertTrue(range.toString().contains("1.2.3.4"));
		assertTrue(range.toString().contains("5.4.3.2"));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.4", "0.0.0.0,255.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,127.1.2.3" })
	void contains(String start, String end, String between) {
		assertTrue(IPv4Range.between(start + "-" + end).contains(IPv4Address.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.2", "0.0.0.0,127.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,128.0.0.0" })
	void notContains(String start, String end, String between) {
		assertFalse(IPv4Range.between(start + "-" + end).contains(IPv4Address.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.4.0/24", "0.0.0.0,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/16" })
	void containsRange(String start, String end, String between) {
		assertTrue(IPv4Range.between(start + "-" + end).contains(IPv4Subnet.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.0/24", "0.0.0.1,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/7" })
	void notContainsRange(String start, String end, String between) {
		assertFalse(IPv4Range.between(start + "-" + end).contains(IPv4Subnet.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.0.0.0-1.2.3.5", "0.0.0.0,255.255.255.255,1.2.3.4-1.2.3.4",
			"127.0.0.0,127.255.255.255,0.0.0.0-128.0.0.0" })
	void overlaps(String start, String end, String between) {
		assertTrue(IPv4Range.between(start + "-" + end).overlaps(IPv4Range.between(between)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,128.0.0.0-128.2.3.5", "0.0.0.1,255.255.255.255,0.0.0.0-0.0.0.0",
			"127.0.0.0,127.255.255.255,128.0.0.0-255.255.255.255" })
	void notOverlaps(String start, String end, String between) {
		assertFalse(IPv4Range.between(start + "-" + end).overlaps(IPv4Range.between(between)));
	}

	@Test
	void equal() {
		IPv4Range range1 = IPv4Range.between("192.168.0.0-192.168.255.255");
		IPv4Range range2 = IPv4Range.of(IPv4Address.of("192.168.0.0"), IPv4Address.of("192.168.255.255"));

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		IPv4Range range1 = IPv4Range.between("192.168.0.0-192.168.255.255");
		IPv4Range range2 = IPv4Range.of(IPv4Address.of("10.0.0.0"), IPv4Address.of("10.255.255.255"));

		assertNotEquals(range1, range2);
	}

	@Test
	void unequalToNull() {
		IPv4Range range1 = IPv4Range.between("192.168.0.0-192.168.255.255");
		assertFalse(range1.equals(null));
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0-255.255.255.255,100000000", "10.0.0.0-10.0.0.255,100", "10.0.0.1-10.0.0.1,1" })
	void length(String between, String length) {
		assertEquals(IPv4Range.between(between).length().longValue(), Long.parseLong(length, 16));
	}

	@Test
	void iterationOrder() {
		ArrayList<IPv4Address> itemList = new ArrayList<>();
		IPv4Range.newBuilder().first("127.255.255.0").last("128.0.0.1").build().iterator()
				.forEachRemaining(itemList::add);

		assertEquals(itemList.size(), 258);
		assertEquals(itemList.get(0), IPv4Address.of("127.255.255.0"));
		assertEquals(itemList.get(itemList.size() - 1), IPv4Address.of("128.0.0.1"));

		ListIterator<IPv4Address> listIterator = itemList.listIterator();
		IPv4Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			IPv4Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationLastElement() {
		Iterator<IPv4Address> i = IPv4Range.newBuilder().first("127.255.255.0").last("127.255.255.0").build()
				.iterator();
		assertTrue(i.hasNext());
		assertEquals(i.next(), IPv4Address.of("127.255.255.0"));
		assertThrows(NoSuchElementException.class, () -> i.next());
	}

	@Test
	void iterationRemove() {
		Iterator<IPv4Address> i = IPv4Range.newBuilder().first("127.255.255.0").last("127.255.255.0").build()
				.iterator();
		assertThrows(UnsupportedOperationException.class, () -> i.remove());
	}

	@ParameterizedTest
	@CsvSource({
			"0.0.0.0-255.255.255.1,0.0.0.0/1 128.0.0.0/2 192.0.0.0/3 224.0.0.0/4 240.0.0.0/5 248.0.0.0/6 252.0.0.0/7"
					+ " 254.0.0.0/8 255.0.0.0/9 255.128.0.0/10 255.192.0.0/11 255.224.0.0/12 255.240.0.0/13 255.248.0.0/14"
					+ " 255.252.0.0/15 255.254.0.0/16 255.255.0.0/17 255.255.128.0/18 255.255.192.0/19 255.255.224.0/20"
					+ " 255.255.240.0/21 255.255.248.0/22 255.255.252.0/23 255.255.254.0/24 255.255.255.0/31",
			"0.0.0.0-255.255.255.255,0.0.0.0/0", "127.255.255.255-128.0.0.1,127.255.255.255/32 128.0.0.0/31",
			"10.100.0.0-10.255.255.255,10.100.0.0/14 10.104.0.0/13 10.112.0.0/12 10.128.0.0/9",
			"123.45.67.89-123.45.68.4, 123.45.67.89/32 123.45.67.90/31 123.45.67.92/30 123.45.67.96/27 123.45.67.128/25 123.45.68.0/30 123.45.68.4/32" })
	void toSubnets(String range, String subnets) {
		List<IPv4Subnet> generated = IPv4Range.between(range).toSubnets();
		List<IPv4Subnet> manual = Arrays.stream(subnets.split(" ")).map(IPv4Subnet::of).collect(Collectors.toList());
		assertEquals(generated, manual);
		assertEquals(manual.stream().mapToLong(IPv4Subnet::length).sum(),
				IPv4Range.between(range).length().longValue());
	}
}
