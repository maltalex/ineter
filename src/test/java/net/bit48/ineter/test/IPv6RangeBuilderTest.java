/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.IPv6Address;
import net.bit48.ineter.IPv6Range;

@RunWith(JUnitPlatform.class)
public class IPv6RangeBuilderTest {

	@Test
	void defaultBuilder() {
		IPv6Range range = IPv6Range.newBuilder().build();
		assertEquals(range.getFirst(), IPv6Address.of("::"));
		assertEquals(range.getLast(), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
	}

	@Test
	void firstLastIp6() {
		IPv6Range range = IPv6Range.newBuilder().first(IPv6Address.of("0:0:0:0:0:0:0:1"))
				.last(IPv6Address.of("aa:aa:aa:aa:aa:aa:aa:aa")).build();
		assertEquals(range.getFirst().toString(), "0:0:0:0:0:0:0:1");
		assertEquals(range.getLast().toString(), "aa:aa:aa:aa:aa:aa:aa:aa");
	}

	@Test
	void firstLastArr() {
		IPv6Range range = IPv6Range.newBuilder().first(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 })
				.last(new byte[] { 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa,
						00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa })
				.build();
		assertEquals(range.getFirst().toString(), "0:0:0:0:0:0:0:1");
		assertEquals(range.getLast().toString(), "aa:aa:aa:aa:aa:aa:aa:aa");
	}

	@Test
	void firstLastInet() {
		IPv6Range range;
		try {
			range = IPv6Range.newBuilder()
					.first((Inet6Address) InetAddress
							.getByAddress(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }))
					.last((Inet6Address) InetAddress
							.getByAddress(new byte[] { 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa, 00,
									(byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa, 00, (byte) 0xaa }))
					.build();
			assertEquals(range.getFirst().toString(), "0:0:0:0:0:0:0:1");
			assertEquals(range.getLast().toString(), "aa:aa:aa:aa:aa:aa:aa:aa");
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void firstLastStr() {
		IPv6Range range = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:1").last("aa:aa:aa:aa:aa:aa:aa:aa").build();
		assertEquals(range.getFirst().toString(), "0:0:0:0:0:0:0:1");
		assertEquals(range.getLast().toString(), "aa:aa:aa:aa:aa:aa:aa:aa");
	}

	@Test
	void staticFactory() {
		IPv6Range range = IPv6Range.of(IPv6Address.of("0:0:0:0:0:0:0:1"), IPv6Address.of("aa:aa:aa:aa:aa:aa:aa:aa"));
		assertEquals(range.getFirst().toString(), "0:0:0:0:0:0:0:1");
		assertEquals(range.getLast().toString(), "aa:aa:aa:aa:aa:aa:aa:aa");
	}

	@Test
	void unequalToNull() {
		IPv6Range.Builder range = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:1").last("aa:aa:aa:aa:aa:aa:aa:aa");
		assertNotEquals(range, null);
	}

	@Test
	void equalLastNull() {
		IPv6Address nullIp = null;
		IPv6Range.Builder range1 = IPv6Range.newBuilder().first("::").last(nullIp);
		IPv6Range.Builder range2 = IPv6Range.newBuilder().first(nullIp).last("::");
		IPv6Range.Builder range3 = IPv6Range.newBuilder().first("::").last("::");

		assertNotEquals(range1, range2);
		assertNotEquals(range2, range1);

		assertNotEquals(range2, range3);
		assertNotEquals(range3, range2);

		assertNotEquals(range1, range3);
		assertNotEquals(range3, range1);

		assertEquals(range1, range1);
		assertEquals(range2, range2);
		assertEquals(range3, range3);
	}

	@Test
	void equal() {
		IPv6Range.Builder range1 = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:1").last("aa:aa:aa:aa:aa:aa:aa:aa");
		IPv6Range.Builder range2 = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:1").last("aa:aa:aa:aa:aa:aa:aa:aa");
		assertEquals(range1, range2);
		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range1);
		IPv6Range.Builder range3 = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:2").last("aa:aa:aa:aa:aa:aa:aa:ab");
		IPv6Range.Builder range4 = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:3").last("aa:aa:aa:aa:aa:aa:aa:ac");
		assertNotEquals(range1, range3);
		assertNotEquals(range1, range4);
		assertNotEquals(range3, range4);
		assertNotEquals(range3, new Object());
	}

	@Test
	void toStr() {
		String str = IPv6Range.newBuilder().first("0:0:0:0:0:0:0:1").last("aa:aa:aa:aa:aa:aa:aa:aa").toString();
		assertTrue(str.contains("0:0:0:0:0:0:0:1"));
		assertTrue(str.contains("aa:aa:aa:aa:aa:aa:aa:aa"));
	}

	@Test
	void illegalArg() {
		assertThrows(IllegalArgumentException.class,
				() -> IPv6Range.newBuilder().first("aa:aa:aa:aa:aa:aa:aa:aa").last("0:0:0:0:0:0:0:1").build());
	}

}
