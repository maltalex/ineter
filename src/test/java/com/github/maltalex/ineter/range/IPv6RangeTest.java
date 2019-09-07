/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.base.IPv6Address;

@RunWith(JUnitPlatform.class)
public class IPv6RangeTest {

	@Test
	void ofAddress() {
		IPv6Range range = IPv6Range.of(IPv6Address.of("::1"), IPv6Address.of("1::"));
		assertEquals(range.getFirst(), IPv6Address.of("::1"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
	}

	@Test
	void ofString() {
		IPv6Range range = IPv6Range.of("::1", "1::");
		assertEquals(range.getFirst(), IPv6Address.of("::1"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
	}

	@Test
	void ofInetAddress() throws UnknownHostException {
		IPv6Range range = IPv6Range.of((Inet6Address) InetAddress.getByName("::1"),
				(Inet6Address) InetAddress.getByName("1::"));
		assertEquals(range.getFirst(), IPv6Address.of("::1"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
	}

	@Test
	void ofArray() {
		IPv6Range range = IPv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		assertEquals(range.getFirst(), IPv6Address.of("::1"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
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

	@Test
	void parse() {
		IPv6Range range = IPv6Range.parse("::-1::");
		assertEquals(range.getFirst(), IPv6Address.of("::"));
		assertEquals(range.getLast(), IPv6Address.of("1::"));
		assertTrue(range.toString().contains("1:0:0:0:0:0:0:0"));
		assertTrue(range.toString().contains("0:0:0:0:0:0:0:0"));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::", "::,::1234,::1000", "1234::,1234::1234,1234::1000" })
	void contains(String start, String end, String parse) {
		assertTrue(IPv6Range.parse(start + "-" + end).contains(IPv6Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1", "::,::1234,::1235", "1234::,1234::1234,1234::1235" })
	void notContains(String start, String end, String parse) {
		assertFalse(IPv6Range.parse(start + "-" + end).contains(IPv6Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::/128", "::,::1234,::1000/120", "1234::,1234::1234,1234::1230/126" })
	void containsRange(String start, String end, String parse) {
		assertTrue(IPv6Range.parse(start + "-" + end).contains(IPv6Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1/128", "::,::1234,::/112", "1234::,1234::1234,1235::/16" })
	void notContainsRange(String start, String end, String parse) {
		assertFalse(IPv6Range.parse(start + "-" + end).contains(IPv6Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::-::", "::,::1234,::1-::2", "1234::,1234::1234,::-1234::", "1::,f::,::-ffff::" })
	void overlaps(String start, String end, String parse) {
		assertTrue(IPv6Range.parse(start + "-" + end).overlaps(IPv6Range.parse(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "::,::,::1-::2", "::,::1234,::1235-1::", "1234::,1234::1234,1234::1235-1234::1235" })
	void notOverlaps(String start, String end, String parse) {
		assertFalse(IPv6Range.parse(start + "-" + end).overlaps(IPv6Range.parse(parse)));
	}

	@Test
	void equal() {
		IPv6Range range1 = IPv6Range.parse("1234::1234-1234::ffff");
		IPv6Range range2 = IPv6Range.of(IPv6Address.of("1234::1234"), IPv6Address.of("1234::ffff"));

		assertEquals(range1, range1);
		assertEquals(range2, range2);

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		IPv6Range range1 = IPv6Range.parse("1234::1234-1234::ffff");
		IPv6Range range2 = IPv6Range.of(IPv6Address.of("1234::"), IPv6Address.of("1234::ffff"));

		assertNotEquals(range1, range2);
	}

	@Test
	void unequalToNull() {
		IPv6Range range1 = IPv6Range.parse("1234::1234-1234::ffff");
		assertFalse(range1.equals(null));
	}

	@Test
	void unequalToObject() {
		assertFalse(IPv6Range.parse("1234::1234-1234::ffff").equals(new Object()));
	}

	@ParameterizedTest
	@CsvSource({ "::-::,1", "::-::fffe,ffff",
			"::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe,ffffffffffffffffffffffffffffffff",
			"::-0000:0000:0000:0000:ffff:ffff:ffff:fffe,0000000000000000ffffffffffffffff",
			"::-0000:0000:0000:0001:ffff:ffff:ffff:fffe,0000000000000001ffffffffffffffff" })
	void length(String parse, String length) {
		assertEquals(IPv6Range.parse(parse).length(), new BigInteger(length, 16));
	}

	@Test
	void iterationOrder() {
		ArrayList<IPv6Address> itemList = new ArrayList<>();
		IPv6Range.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").iterator()
				.forEachRemaining(itemList::add);

		assertEquals(256, itemList.size());
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

		assertEquals(254, itemList.size());
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
		List<IPv6Subnet> generated = IPv6Range.parse(range).toSubnets();
		List<IPv6Subnet> manual = Arrays.stream(subnets.split(" ")).map(IPv6Subnet::of).collect(Collectors.toList());
		assertEquals(generated, manual);
		assertEquals(manual.stream().map(IPv6Subnet::length).collect(Collectors.reducing((a, b) -> a.add(b))).get(),
				IPv6Range.parse(range).length());
	}

	@Test
	void singleIPRangeParse() {
		final IPv6Range explicitRange = IPv6Range.parse("1234::1234-1234::1234");
		final IPv6Range range = IPv6Range.parse("1234::1234");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfBytes() {
		final IPv6Range explicitRange = IPv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		final IPv6Range range = IPv6Range.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfIPv6Address() {
		final IPv6Range explicitRange = IPv6Range.of(IPv6Address.of("::1"), IPv6Address.of("::1"));
		final IPv6Range range = IPv6Range.of(IPv6Address.of("::1"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfString() {
		final IPv6Range explicitRange = IPv6Range.of("1234::1234", "1234::1234");
		final IPv6Range range = IPv6Range.of("1234::1234");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfInet6Address() throws UnknownHostException {
		final IPv6Range explicitRange = IPv6Range.of((Inet6Address) InetAddress.getByName("::1"),
				(Inet6Address) InetAddress.getByName("::1"));
		final IPv6Range range = IPv6Range.of((Inet6Address) InetAddress.getByName("::1"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void parseSubnet() {
		final IPv6Range range = IPv6Range.parse("1234::/16");
		assertEquals(IPv6Address.of("1234::"), range.getFirst());
		assertEquals(IPv6Address.of("1235::").previous(), range.getLast());
	}

	@Test
	void shouldMergeAdjacent() {
		final IPv6Range first = IPv6Range.of("::1", "::2");
		final IPv6Range second = IPv6Range.of("::3", "::4");
		final List<IPv6Range> merge = IPv6Range.merge(first, second);
		assertEquals(ImmutableList.of(IPv6Range.of("::1", "::4")), merge);
	}

	@Test
	void shouldMergeOverlapping() {
		final IPv6Range first = IPv6Range.of("::1", "::3");
		final IPv6Range second = IPv6Range.of("::2", "::4");
		final List<IPv6Range> merge = IPv6Range.merge(first, second);
		assertEquals(ImmutableList.of(IPv6Range.of("::1", "::4")), merge);
	}

	@Test
	void shouldMergeMixed() {
		final IPv6Range first = IPv6Range.of("::1", "::3");
		final IPv6Range second = IPv6Range.of("::2", "::4");
		final IPv6Range third = IPv6Range.of("::5", "::6");
		final List<IPv6Range> merge = IPv6Range.merge(first, second, third);
		assertEquals(ImmutableList.of(IPv6Range.of("::1", "::6")), merge);
	}

	@Test
	void shouldNotMergeSeparated() {
		final IPv6Range first = IPv6Range.of("::1", "::3");
		final IPv6Range second = IPv6Range.of("::5", "::6");
		final List<IPv6Range> merge = IPv6Range.merge(first, second);
		assertEquals(ImmutableList.of(second, first), merge);
	}

	@Test
	void shouldReturnEmptyOnNull() {
		assertTrue(IPv6Range.merge((IPv6Range) null).isEmpty());
	}

	@Test
	void shouldReturnEmptyOnEmpty() {
		assertTrue(IPv6Range.merge(emptyList()).isEmpty());
	}

	@Test
	void shouldThrowOnNotAdjacent() {
		assertThrows(IllegalArgumentException.class, () -> IPv6Range.of("::1").extend(IPv6Range.of("::3")));
	}

	@Test
	void shouldExtendAdjacent() {
		final IPv6Range first = IPv6Range.of("::1");
		final IPv6Range second = IPv6Range.of("::2", "::5");
		assertEquals(IPv6Range.of("::1", "::5"), first.extend(second));
		assertEquals(IPv6Range.of("::1", "::5"), second.extend(first));
	}

	@Test
	void shouldStaySameOnExtensionByItself() {
		final IPv6Range range = IPv6Range.of("::1");
		assertEquals(range, range.extend(range));
	}

	@Test
	void shouldExtendByAddress() {
		assertEquals(IPv6Range.of("::1", "::2"), IPv6Range.of("::1").extend(IPv6Address.of("::2")));
		assertEquals(IPv6Range.of("::1", "::2"), IPv6Range.of("::2").extend(IPv6Address.of("::1")));
	}
	
	@Test
	void shouldNotBeAdjacentIfOverlaps() {
		assertFalse(IPv6Range.parse("::-::2").isAdjacent(IPv6Range.parse("::1-::3")));
		assertFalse(IPv6Range.parse("::1-::3").isAdjacent(IPv6Range.parse("127.0.0.1-127.0.0.3")));
	}
}
