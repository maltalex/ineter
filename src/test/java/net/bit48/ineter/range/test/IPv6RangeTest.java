/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.range.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import net.bit48.ineter.base.IPv6Address;
import net.bit48.ineter.range.IPv6Range;
import net.bit48.ineter.range.IPv6Subnet;

@RunWith(JUnitPlatform.class)
public class IPv6RangeTest {

	@Test
	void ofAddress() {
		IPv6Range range = IPv6Range.of(IPv6Address.of("::1"), IPv6Address.of("1::"));
		assertTrue(range.getFirst().equals(IPv6Address.of("::1")));
		assertTrue(range.getLast().equals(IPv6Address.of("1::")));
	}

	@Test
	void ofString() {
		IPv6Range range = IPv6Range.of("::1", "1::");
		assertTrue(range.getFirst().equals(IPv6Address.of("::1")));
		assertTrue(range.getLast().equals(IPv6Address.of("1::")));
	}

	@Test
	void ofInetAddress() throws UnknownHostException {
		IPv6Range range = IPv6Range.of((Inet6Address) InetAddress.getByName("::1"),
				(Inet6Address) InetAddress.getByName("1::"));
		assertTrue(range.getFirst().equals(IPv6Address.of("::1")));
		assertTrue(range.getLast().equals(IPv6Address.of("1::")));
	}

	@Test
	void ofArray() {
		IPv6Range range = IPv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		assertTrue(range.getFirst().equals(IPv6Address.of("::1")));
		assertTrue(range.getLast().equals(IPv6Address.of("1::")));
	}

	@Test
	void invalidRange() {
		assertThrows(IllegalArgumentException.class, () -> IPv6Range.of("1::", "::1"));
	}

	@Test
	void nullAddress() {
		assertThrows(NullPointerException.class, () -> new IPv6Range(null, IPv6Address.of("::1")));
		assertThrows(NullPointerException.class, () -> new IPv6Range(IPv6Address.of("::1"), null));
	}

	@Test
	void between() {
		IPv6Range range = IPv6Range.between("::-1::");
		assertEquals(range.getFirst(), IPv6Address.of("::"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
		assertTrue(range.toString().contains("1:0:0:0:0:0:0:0"));
		assertTrue(range.toString().contains("0:0:0:0:0:0:0:0"));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::", "::,::1234,::1000", "1234::,1234::1234,1234::1000" })
	void contains(String start, String end, String between) {
		assertTrue(IPv6Range.between(start + "-" + end).contains(IPv6Address.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1", "::,::1234,::1235", "1234::,1234::1234,1234::1235" })
	void notContains(String start, String end, String between) {
		assertFalse(IPv6Range.between(start + "-" + end).contains(IPv6Address.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::/128", "::,::1234,::1000/120", "1234::,1234::1234,1234::1230/126" })
	void containsRange(String start, String end, String between) {
		assertTrue(IPv6Range.between(start + "-" + end).contains(IPv6Subnet.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1/128", "::,::1234,::/112", "1234::,1234::1234,1235::/16" })
	void notContainsRange(String start, String end, String between) {
		assertFalse(IPv6Range.between(start + "-" + end).contains(IPv6Subnet.of(between)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::-::", "::,::1234,::1-::2", "1234::,1234::1234,::-1234::", "1::,f::,::-ffff::" })
	void overlaps(String start, String end, String between) {
		assertTrue(IPv6Range.between(start + "-" + end).overlaps(IPv6Range.between(between)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1-::2", "::,::1234,::1235-1::", "1234::,1234::1234,1234::1235-1234::1235" })
	void notOverlaps(String start, String end, String between) {
		assertFalse(IPv6Range.between(start + "-" + end).overlaps(IPv6Range.between(between)));
	}

	@Test
	void equal() {
		IPv6Range range1 = IPv6Range.between("1234::1234-1234::ffff");
		IPv6Range range2 = IPv6Range.of(IPv6Address.of("1234::1234"), IPv6Address.of("1234::ffff"));

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		IPv6Range range1 = IPv6Range.between("1234::1234-1234::ffff");
		IPv6Range range2 = IPv6Range.of(IPv6Address.of("1234::"), IPv6Address.of("1234::ffff"));

		assertNotEquals(range1, range2);
	}

	@ParameterizedTest
	@CsvSource({ "::-::,1", "::-::fffe,ffff",
			"::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe,ffffffffffffffffffffffffffffffff",
			"::-0000:0000:0000:0000:ffff:ffff:ffff:fffe,0000000000000000ffffffffffffffff",
			"::-0000:0000:0000:0001:ffff:ffff:ffff:fffe,0000000000000001ffffffffffffffff" })
	void length(String between, String length) {
		assertEquals(IPv6Range.between(between).length(), new BigInteger(length, 16));
	}

	@Test
	void iterationOrder() {
		ArrayList<IPv6Address> itemList = new ArrayList<>();
		IPv6Range.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").iterator()
				.forEachRemaining(itemList::add);

		assertEquals(itemList.size(), 256);
		assertEquals(itemList.get(0), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00"));
		assertEquals(itemList.get(itemList.size() - 1), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));

		ListIterator<IPv6Address> listIterator = itemList.listIterator();
		IPv6Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			IPv6Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationOrderSkipEdges() {
		ArrayList<IPv6Address> itemList = new ArrayList<>();
		IPv6Range.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
				.iterator(true).forEachRemaining(itemList::add);

		assertEquals(itemList.size(), 254);
		assertEquals(itemList.get(0), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff01"));
		assertEquals(itemList.get(itemList.size() - 1), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe"));

		ListIterator<IPv6Address> listIterator = itemList.listIterator();
		IPv6Address previous = listIterator.next();
		while (listIterator.hasNext()) {
			IPv6Address current = listIterator.next();
			assertTrue(current.compareTo(previous) > 0);
			previous = current;
		}
	}

	@Test
	void iterationLastElement() {
		Iterator<IPv6Address> i = IPv6Range.of("1234::", "1234::").iterator();
		assertTrue(i.hasNext());
		assertEquals(i.next(), IPv6Address.of("1234::"));
		assertThrows(NoSuchElementException.class, () -> i.next());
	}

	@Test
	void iterationRemove() {
		Iterator<IPv6Address> i = IPv6Range.of("1234::", "1234::").iterator();
		assertThrows(UnsupportedOperationException.class, () -> i.remove());
	}

	@ParameterizedTest
	@CsvSource({ "::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,0:0:0:0:0:0:0:0/0",
			"::-7fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,0:0:0:0:0:0:0:0/1",
			"::ffff:ffff:ffff:ffff-::1:0:0:0:1fff,::ffff:ffff:ffff:ffff/128 ::1:0:0:0:0/115",
			"::-1::0:0:0:1234, 0:0:0:0:0:0:0:0/16 1:0:0:0:0:0:0:0/116 1:0:0:0:0:0:0:1000/119 1:0:0:0:0:0:0:1200/123 1:0:0:0:0:0:0:1220/124 1:0:0:0:0:0:0:1230/126 1:0:0:0:0:0:0:1234/128",
			"::ffff:ffff:ffff:ffff-::ffff:ffff:ffff:ffff,0:0:0:0:ffff:ffff:ffff:ffff/128" })
	void toSubnets(String range, String subnets) {
		List<IPv6Subnet> generated = IPv6Range.between(range).toSubnets();
		List<IPv6Subnet> manual = Arrays.stream(subnets.split(" ")).map(IPv6Subnet::of).collect(Collectors.toList());
		assertEquals(generated, manual);
		assertEquals(manual.stream().map(IPv6Subnet::length).collect(Collectors.reducing((a, b) -> a.add(b))).get(),
				IPv6Range.between(range).length());
	}
}
