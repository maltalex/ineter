/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.github.maltalex.ineter.base.IPv4Address;
import com.google.common.collect.ImmutableList;

@RunWith(JUnitPlatform.class)
public class IPv4RangeTest {

	@Test
	void ofAddress() {
		IPv4Range range = IPv4Range.of(IPv4Address.of("1.2.3.4"), IPv4Address.of("5.4.3.2"));
		assertEquals(range.getFirst(), IPv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), IPv4Address.of("5.4.3.2"));
	}

	@Test
	void ofString() {
		IPv4Range range = IPv4Range.of("1.2.3.4", "5.4.3.2");
		assertEquals(range.getFirst(), IPv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), IPv4Address.of("5.4.3.2"));
	}

	@Test
	void ofInetAddress() throws UnknownHostException {
		IPv4Range range = IPv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"),
				(Inet4Address) InetAddress.getByName("5.4.3.2"));
		assertEquals(range.getFirst(), IPv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), IPv4Address.of("5.4.3.2"));
	}

	@Test
	void ofArray() {
		IPv4Range range = IPv4Range.of(new byte[] { 1, 2, 3, 4 }, new byte[] { 5, 4, 3, 2 });
		assertEquals(range.getFirst(), IPv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), IPv4Address.of("5.4.3.2"));
	}

	@Test
	void invalidRange() {
		assertThrows(IllegalArgumentException.class, () -> IPv4Range.of("5.4.3.2", "1.2.3.4"));
	}

	@Test
	void nullAddress() {
		assertThrows(NullPointerException.class, () -> new IPv4Range(null, IPv4Address.of("1.2.3.4")));
		assertThrows(NullPointerException.class, () -> new IPv4Range(IPv4Address.of("1.2.3.4"), null));
	}

	@Test
	void parse() {
		IPv4Range range = IPv4Range.parse("1.2.3.4-5.4.3.2");
		assertEquals(range.getFirst(), IPv4Address.of("1.2.3.4"));
		assertEquals(range.getLast(), IPv4Address.of("5.4.3.2"));
		assertTrue(range.toString().contains("1.2.3.4"));
		assertTrue(range.toString().contains("5.4.3.2"));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.4", "0.0.0.0,255.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,127.1.2.3" })
	void contains(String start, String end, String parse) {
		assertTrue(IPv4Range.parse(start + "-" + end).contains(IPv4Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.2", "0.0.0.0,127.255.255.255,255.255.255.255",
			"127.0.0.0,127.255.255.255,128.0.0.0" })
	void notContains(String start, String end, String parse) {
		assertFalse(IPv4Range.parse(start + "-" + end).contains(IPv4Address.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.4.0/24", "0.0.0.0,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/16" })
	void containsRange(String start, String end, String parse) {
		assertTrue(IPv4Range.parse(start + "-" + end).contains(IPv4Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.2.3.0/24", "0.0.0.1,255.255.255.255,0.0.0.0/0",
			"127.0.0.0,127.255.255.255,127.0.0.0/7" })
	void notContainsRange(String start, String end, String parse) {
		assertFalse(IPv4Range.parse(start + "-" + end).contains(IPv4Subnet.of(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,1.0.0.0-1.2.3.5", "0.0.0.0,255.255.255.255,1.2.3.4-1.2.3.4",
			"127.0.0.0,127.255.255.255,0.0.0.0-128.0.0.0" })
	void overlaps(String start, String end, String parse) {
		assertTrue(IPv4Range.parse(start + "-" + end).overlaps(IPv4Range.parse(parse)));
	}

	@ParameterizedTest
	@CsvSource({ "1.2.3.4,2.3.4.5,128.0.0.0-128.2.3.5", "0.0.0.1,255.255.255.255,0.0.0.0-0.0.0.0",
			"127.0.0.0,127.255.255.255,128.0.0.0-255.255.255.255" })
	void notOverlaps(String start, String end, String parse) {
		assertFalse(IPv4Range.parse(start + "-" + end).overlaps(IPv4Range.parse(parse)));
	}

	@Test
	void equal() {
		IPv4Range range1 = IPv4Range.parse("192.168.0.0-192.168.255.255");
		IPv4Range range2 = IPv4Range.of(IPv4Address.of("192.168.0.0"), IPv4Address.of("192.168.255.255"));

		assertEquals(range1, range1);
		assertEquals(range2, range2);

		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range2);
	}

	@Test
	void notEqual() {
		IPv4Range range1 = IPv4Range.parse("192.168.0.0-192.168.255.255");
		IPv4Range range2 = IPv4Range.of(IPv4Address.of("10.0.0.0"), IPv4Address.of("10.255.255.255"));

		assertNotEquals(range1, range2);
	}

	@Test
	void unequalToNull() {
		IPv4Range range1 = IPv4Range.parse("192.168.0.0-192.168.255.255");
		assertFalse(range1.equals(null));
	}

	@Test
	void unequalToObject() {
		assertFalse(IPv4Range.parse("192.168.0.0-192.168.255.255").equals(new Object()));
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0-255.255.255.255,100000000", "10.0.0.0-10.0.0.255,100", "10.0.0.1-10.0.0.1,1" })
	void length(String parse, String length) {
		assertEquals(IPv4Range.parse(parse).length().longValue(), Long.parseLong(length, 16));
	}

	@Test
	void iterationOrder() {
		ArrayList<IPv4Address> itemList = new ArrayList<>();
		IPv4Range.of("127.255.255.0", "128.0.0.1").iterator().forEachRemaining(itemList::add);

		assertEquals(258, itemList.size());
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
	void iterationOrderSkipEdges() {
		ArrayList<IPv4Address> itemList = new ArrayList<>();
		IPv4Range.of("127.255.255.0", "128.0.0.1").iterator(true).forEachRemaining(itemList::add);

		assertEquals(256, itemList.size());
		assertEquals(itemList.get(0), IPv4Address.of("127.255.255.1"));
		assertEquals(itemList.get(itemList.size() - 1), IPv4Address.of("128.0.0.0"));

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
		Iterator<IPv4Address> i = IPv4Range.of("127.255.255.0", "127.255.255.0").iterator();
		assertTrue(i.hasNext());
		assertEquals(i.next(), IPv4Address.of("127.255.255.0"));
		assertThrows(NoSuchElementException.class, () -> i.next());
	}

	@Test
	void iterationRemove() {
		Iterator<IPv4Address> i = IPv4Range.of("127.255.255.0", "127.255.255.0").iterator();
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
		List<IPv4Subnet> generated = IPv4Range.parse(range).toSubnets();
		List<IPv4Subnet> manual = Arrays.stream(subnets.split(" ")).map(IPv4Subnet::of).collect(Collectors.toList());
		assertEquals(generated, manual);
		assertEquals(manual.stream().mapToLong(IPv4Subnet::length).sum(), IPv4Range.parse(range).length().longValue());
	}

	@Test
	void singleIPRangeParse() {
		final IPv4Range explicitRange = IPv4Range.parse("127.0.0.1-127.0.0.1");
		final IPv4Range range = IPv4Range.parse("127.0.0.1");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfBytes() {
		final IPv4Range explicitRange = IPv4Range.of(new byte[] { 1, 2, 3, 4 }, new byte[] { 1, 2, 3, 4 });
		final IPv4Range range = IPv4Range.of(new byte[] { 1, 2, 3, 4 });
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfString() {
		final IPv4Range explicitRange = IPv4Range.of("1.2.3.4", "1.2.3.4");
		final IPv4Range range = IPv4Range.of("1.2.3.4");
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfIPv4Address() {
		final IPv4Range explicitRange = IPv4Range.of(IPv4Address.of("1.2.3.4"), IPv4Address.of("1.2.3.4"));
		final IPv4Range range = IPv4Range.of(IPv4Address.of("1.2.3.4"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void singleIPRangeOfInetAddress() throws UnknownHostException {
		final IPv4Range explicitRange = IPv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"),
				(Inet4Address) InetAddress.getByName("1.2.3.4"));
		final IPv4Range range = IPv4Range.of((Inet4Address) InetAddress.getByName("1.2.3.4"));
		assertEquals(explicitRange, range,
				"Single address range doesn't match explicit range with same addresses on both ends.");
	}

	@Test
	void parsedSubnet() {
		final IPv4Range range = IPv4Range.parse("127.0.0.0/24");
		assertEquals(IPv4Address.of("127.0.0.0"), range.getFirst());
		assertEquals(IPv4Address.of("127.0.0.255"), range.getLast());
	}

	@Test
	void shouldMergeAdjacent() {
		final IPv4Range first = IPv4Range.of("127.0.0.1", "127.0.0.2");
		final IPv4Range second = IPv4Range.of("127.0.0.3", "127.0.0.4");
		final List<IPv4Range> merge = IPv4Range.merge(first, second);
		assertEquals(Collections.singletonList(IPv4Range.of("127.0.0.1", "127.0.0.4")), merge);
	}

	@Test
	void shouldMergeOverlapping() {
		final IPv4Range first = IPv4Range.of("127.0.0.1", "127.0.0.3");
		final IPv4Range second = IPv4Range.of("127.0.0.2", "127.0.0.4");
		final List<IPv4Range> merge = IPv4Range.merge(first, second);
		assertEquals(Collections.singletonList(IPv4Range.of("127.0.0.1", "127.0.0.4")), merge);
	}

	@Test
	void shouldMergeMixed() {
		final IPv4Range first = IPv4Range.of("127.0.0.1", "127.0.0.3");
		final IPv4Range second = IPv4Range.of("127.0.0.2", "127.0.0.4");
		final IPv4Range third = IPv4Range.of("127.0.0.5", "127.0.0.6");

		final IPv4Range fourth = IPv4Range.of("127.0.0.8", "127.0.0.10");
		final IPv4Range fifth = IPv4Range.of("127.0.0.8", "127.0.0.11");

		final IPv4Range sixth = IPv4Range.of("128.0.0.0", "255.255.255.255");
		final List<IPv4Range> merge = IPv4Range.merge(sixth, fifth, fourth, third, second, first);

		assertEquals(Arrays.asList(IPv4Range.of("127.0.0.1", "127.0.0.6"), IPv4Range.of("127.0.0.8", "127.0.0.11"),
				IPv4Range.of("128.0.0.0", "255.255.255.255")), merge);
	}

	@Test
	void shouldNotMergeSeparated() {
		final IPv4Range first = IPv4Range.of("127.0.0.1", "127.0.0.3");
		final IPv4Range second = IPv4Range.of("127.0.0.5", "127.0.0.6");
		final List<IPv4Range> merge = IPv4Range.merge(first, second);
		assertEquals(ImmutableList.of(first, second), merge);
	}

	@Test
	void mergeShouldThrowOnNull() {
		assertThrows(NullPointerException.class, () -> IPv4Range.merge((IPv4Range) null));
	}

	@Test
	void shouldReturnEmptyOnEmpty() {
		assertTrue(IPv4Range.merge(Collections.emptyList()).isEmpty());
	}

	@Test
	void testIntLength() {
		assertEquals(256, IPv4Subnet.of("192.168.0.0/24").intLength());
		assertEquals(8, IPv4Subnet.of("10.0.0.0/29").intLength());
		assertEquals(Integer.MAX_VALUE, IPv4Subnet.of("0.0.0.0/0").intLength());
	}

	@Test
	void testWithLast() {
		assertEquals(IPv4Range.of("0.0.0.0", "1.2.3.4"),
				IPv4Subnet.of("0.0.0.0/0").withLast(IPv4Address.of("1.2.3.4")));
		assertEquals(IPv4Range.of("1.2.3.0", "1.2.3.4"),
				IPv4Subnet.of("1.2.3.0/24").withLast(IPv4Address.of("1.2.3.4")));
		assertThrows(IllegalArgumentException.class,
				() -> IPv4Subnet.of("1.2.3.0/24").withLast(IPv4Address.of("0.0.0.0")));
	}

	@Test
	void testWithFirst() {
		assertEquals(IPv4Range.of("0.0.0.0", "255.255.255.255"),
				IPv4Subnet.of("128.0.0.0/1").withFirst(IPv4Address.of("0.0.0.0")));
		assertEquals(IPv4Range.of("10.0.0.255", "10.0.0.255"),
				IPv4Subnet.of("10.0.0.0/24").withFirst(IPv4Address.of("10.0.0.255")));
		assertThrows(IllegalArgumentException.class,
				() -> IPv4Subnet.of("10.0.0.0/8").withFirst(IPv4Address.of("12.12.12.12")));
	}

	@Test
	void withRemovedAll() {
		IPv4Range subnet = IPv4Range.parse("10.0.0.0/24");
		assertEquals(Arrays.asList(subnet), subnet.withRemoved(emptyList()));
		assertEquals(emptyList(), subnet.withRemoved(subnet));
		assertEquals(emptyList(), subnet.withRemoved(IPv4Range.parse("10.0.0.0/8")));
		assertEquals(emptyList(), subnet.withRemoved(IPv4Range.parse("0.0.0.0/0")));
		assertEquals(emptyList(), IPv4Range.parse("0.0.0.0/0").withRemoved(IPv4Range.parse("0.0.0.0/0")));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
		"10.0.0.1-10.0.0.255, 10.0.0.0/24,  10.0.0.0",
		"10.0.0.0-10.0.0.254, 10.0.0.0/24 ,10.0.0.255",
		"10.0.0.128-10.0.0.255, 10.0.0.0/24,10.0.0.0/25",
		"10.0.0.0-10.0.0.127, 10.0.0.0/24,10.0.0.128/25",
		"10.0.0.0-10.0.0.99 10.0.0.201-10.0.0.255, 10.0.0.0/24, 10.0.0.100-10.0.0.200",
		"0.0.0.0-127.255.255.255 129.0.0.0-255.255.255.255, 0.0.0.0/0, 128.0.0.0/8"
	//@formatter:on
	})
	void withRemovedSingle(String ans, String original, String toExclude) {
		List<IPv4Range> ansList = Arrays.stream(ans.trim().split(" ")).map(IPv4Range::parse)
				.collect(Collectors.toList());
		assertEquals(ansList, IPv4Range.parse(original).withRemoved(IPv4Range.parse(toExclude)));
	}

	@Test
	void withRemovedCollectionEmpty() {
		assertEquals(singletonList(IPv4Range.parse("10.0.0.0/24")),
				IPv4Range.parse("10.0.0.0/24").withRemoved(emptyList()));
		assertEquals(emptyList(),
				IPv4Range.parse("10.0.0.0/24").withRemoved(Arrays.asList(IPv4Range.parse("10.0.0.0/24"))));
		assertEquals(emptyList(),
				IPv4Range.parse("10.0.0.0/24").withRemoved(Arrays.asList(IPv4Range.parse("0.0.0.0/0"))));
		assertEquals(emptyList(), IPv4Range.parse("10.0.0.0/24")
				.withRemoved(Arrays.asList(IPv4Range.parse("9.0.0.0-11.0.0.0"), IPv4Range.parse("13.0.0.0/24"))));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
		"10.0.0.128-10.0.0.255, 10.0.0.0/24, 10.0.0.0/25",
		"10.0.0.0-10.0.0.127, 10.0.0.0/24, 10.0.0.128/25",
		"10.0.0.0/24, 10.0.0.0/24, 11.0.0.0/24 12.0.0.0/24",
		"10.0.0.0/24, 10.0.0.0/24, 8.0.0.0/24 7.0.0.0/24",
		"10.0.0.0/24, 10.0.0.0/24, ",
		"10.0.0.0/24, 10.0.0.0/24, 1.2.3.4",
		"10.0.0.1-10.0.0.9 10.0.0.20-10.0.0.249 , 10.0.0.0/24 , 8.0.0.0/24 9.0.0.0-10.0.0.0 10.0.0.10-10.0.0.19 10.0.0.250-255.255.255.255",
		"10.0.0.20-10.0.0.249, 10.0.0.0/24 , 9.0.0.0-10.0.0.19 10.0.0.250-10.0.0.255",
		"10.0.0.0-10.0.0.99 10.0.0.101-10.0.0.199 10.0.0.201-10.0.0.255 , 10.0.0.0/24 , 10.0.0.100 10.0.0.200",
		"10.0.0.0-10.0.0.9 10.0.0.20-10.0.0.249, 10.0.0.0/24, 10.0.0.10-10.0.0.19 10.0.0.250-10.0.0.255 12.0.0.0/24",
		"10.0.0.0 10.0.0.2 10.0.0.4-10.0.0.199, 10.0.0.0/24, 10.0.0.1 10.0.0.3 10.0.0.200-10.0.1.100" 
	//@formatter:on
	})
	void withRemovedCollection(String ans, String original, String toExclude) {
		List<IPv4Range> ansList = Arrays.stream(ans.trim().split(" ")).map(IPv4Range::parse)
				.collect(Collectors.toList());
		List<IPv4Range> toExcludeList = toExclude == null ? emptyList()
				: Arrays.stream(toExclude.trim().split(" ")).map(IPv4Range::parse).collect(Collectors.toList());

		assertEquals(ansList, IPv4Range.parse(original).withRemoved(toExcludeList));
	}
}
