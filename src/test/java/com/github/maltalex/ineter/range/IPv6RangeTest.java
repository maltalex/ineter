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

import java.math.BigInteger;
import java.net.Inet6Address;
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

import com.github.maltalex.ineter.base.IPv6Address;
import com.google.common.collect.ImmutableList;

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
		assertNotEquals(range1, null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), IPv6Range.parse("1234::1234-1234::ffff"));
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
		assertThrows(NoSuchElementException.class, i::next);
	}

	@Test
	void iterationRemove() {
		Iterator<IPv6Address> i = IPv6Range.of("1234::", "1234::").iterator();
		assertThrows(UnsupportedOperationException.class, i::remove);
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
		// noinspection OptionalGetWithoutIsPresent
		assertEquals(manual.stream().map(IPv6Subnet::length).reduce(BigInteger::add).get(),
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

		final IPv6Range fourth = IPv6Range.of("::8", "::10");
		final IPv6Range fifth = IPv6Range.of("::8", "::11");

		final IPv6Range sixth = IPv6Range.of("2001::", "2002::");

		final List<IPv6Range> merge = IPv6Range.merge(sixth, fifth, fourth, third, second, first);
		assertEquals(Arrays.asList(IPv6Range.of("::1", "::6"), IPv6Range.of("::8", "::11"),
				IPv6Range.of("2001::", "2002::")), merge);
	}

	@Test
	void shouldNotMergeSeparated() {
		final IPv6Range first = IPv6Range.of("::1", "::3");
		final IPv6Range second = IPv6Range.of("::5", "::6");
		final List<IPv6Range> merge = IPv6Range.merge(first, second);
		assertEquals(ImmutableList.of(first, second), merge);
	}

	@Test
	void mergeShouldThrowOnNull() {
		assertThrows(NullPointerException.class, () -> IPv6Range.merge((IPv6Range) null));
	}

	@Test
	void shouldReturnEmptyOnEmpty() {
		assertTrue(IPv6Range.merge(Collections.emptyList()).isEmpty());
	}

	@Test
	void testIntLength() {
		assertEquals(256, IPv6Subnet.of("::/120").intLength());
		assertEquals(8, IPv6Subnet.of("::/125").intLength());
		assertEquals(Integer.MAX_VALUE, IPv6Subnet.of("::/64").intLength());
	}

	@Test
	void testWithLast() {
		assertEquals(IPv6Range.of("::", "1234::"), IPv6Subnet.of("::/120").withLast(IPv6Address.of("1234::")));
		assertEquals(IPv6Range.of("1234::", "1235::"), IPv6Subnet.of("1234::/16").withLast(IPv6Address.of("1235::")));
		assertThrows(IllegalArgumentException.class, () -> IPv6Subnet.of("1234::/16").withLast(IPv6Address.of("::")));
	}

	@Test
	void testWithFirst() {
		assertEquals(IPv6Range.of("::5", "::00ff"), IPv6Subnet.of("::/120").withFirst(IPv6Address.of("::5")));
		assertEquals(IPv6Range.of("::", "::123f"), IPv6Subnet.of("::1230/124").withFirst(IPv6Address.of("::")));
		assertThrows(IllegalArgumentException.class,
				() -> IPv6Subnet.of("1234::/16").withFirst(IPv6Address.of("2222::")));
	}

	@Test
	void withRemovedAll() {
		IPv6Range subnet = IPv6Range.parse("2001::/16");
		assertEquals(Arrays.asList(subnet), subnet.withRemoved(emptyList()));
		assertEquals(emptyList(), subnet.withRemoved(subnet));
		assertEquals(emptyList(), subnet.withRemoved(IPv6Range.parse("2000::/8")));
		assertEquals(emptyList(), subnet.withRemoved(IPv6Range.parse("::/0")));
		assertEquals(emptyList(), IPv6Range.parse("::/0").withRemoved(IPv6Range.parse("::/0")));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
		"::1234:1-::1234:ffff, ::1234:0/112, ::1234:0",
		"::1234:0-::1234:fffe, ::1234:0/112, ::1234:ffff",
			"::1234:0/113, ::1234:0/112, ::1234:8000/113",
			"::1234:8000/113, ::1234:0/112, ::1234:0/113",
			"::1234:0-::1234:fff ::1234:2001-::1234:ffff, ::1234:0/112, ::1234:1000-::1234:2000",
			"::-0fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff 1001::-ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff, ::/0, 1000::/16"
	//@formatter:on
	})
	void withRemovedSingle(String ans, String original, String toExclude) {
		List<IPv6Range> ansList = Arrays.stream(ans.trim().split(" ")).map(IPv6Range::parse)
				.collect(Collectors.toList());
		assertEquals(ansList, IPv6Range.parse(original).withRemoved(IPv6Range.parse(toExclude)));
	}

	@Test
	void withRemovedCollectionEmpty() {
		assertEquals(singletonList(IPv6Range.parse("1234::/16")),
				IPv6Range.parse("1234::/16").withRemoved(emptyList()));
		assertEquals(emptyList(),
				IPv6Range.parse("1234::/16").withRemoved(Arrays.asList(IPv6Range.parse("1234::/16"))));
		assertEquals(emptyList(), IPv6Range.parse("1234::/16").withRemoved(Arrays.asList(IPv6Range.parse("::/0"))));
		assertEquals(emptyList(), IPv6Range.parse("1234::/16")
				.withRemoved(Arrays.asList(IPv6Range.parse("1230::-1240::"), IPv6Range.parse("2222::0/16"))));
	}

	@ParameterizedTest
	@CsvSource({
	//@formatter:off
		"::1234:0/112 , ::1234:0/112 , ::1235:0/112 ::1236:0/112",
		"::1234:0/112 , ::1234:0/112 , ::1232:0/112 ::1233:0/112",
		"::1234:0/112 , ::1234:0/112 , 4321::",
		"::1234:1001-::1234:1fff ::1234:3001-::1234:efff, ::1234:0/112, ::1000:0/112 ::1233:f000-::1234:1000 ::1234:2000-::1234:3000 ::1234:f000-::1235:1000 ::1236:0/112",
		"::1234:1-::1234:0fff ::1234:1001-::1234:fffe,::1234:0/112, ::-::1234:0 ::1234:1000 ::1234:ffff-::1235:0",
		"::1234:0/112,::1234:0/112, ::1232:0/113 ::1233:0/113",
		"::1234:0/112,::1234:0/112, ::1232:0/113 ::1235:0/113",
		"::1234:0-::1234:ff ::1234:101-::1234:fff ::1234:1001-::1234:ffff,::1234:0/112, ::1234:100 ::1234:1000",
		"::1234:0-::1234:0fff,::1234:0/112, ::1232:0/113 ::1234:1000-::1235:1000"
	//@formatter:on
	})
	void withRemovedCollection(String ans, String original, String toExclude) {
		List<IPv6Range> ansList = Arrays.stream(ans.trim().split(" ")).map(IPv6Range::parse)
				.collect(Collectors.toList());
		List<IPv6Range> toExcludeList = toExclude == null ? emptyList()
				: Arrays.stream(toExclude.trim().split(" ")).map(IPv6Range::parse).collect(Collectors.toList());

		assertEquals(ansList, IPv6Range.parse(original).withRemoved(toExcludeList));
	}
}
