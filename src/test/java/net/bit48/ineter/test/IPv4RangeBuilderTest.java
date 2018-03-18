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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.IPv4Address;
import net.bit48.ineter.IPv4Range;

@RunWith(JUnitPlatform.class)
public class IPv4RangeBuilderTest {

	@Test
	void defaultBuilder() {
		IPv4Range range = IPv4Range.newBuilder().build();
		assertEquals(range.getFirst(), IPv4Address.of("0.0.0.0"));
		assertEquals(range.getLast(), IPv4Address.of("255.255.255.255"));
	}

	@Test
	void firstLastIp4() {
		IPv4Range range = IPv4Range.newBuilder().first(IPv4Address.of("1.2.3.4")).last(IPv4Address.of("4.3.2.1"))
				.build();
		assertEquals(range.getFirst().toString(), "1.2.3.4");
		assertEquals(range.getLast().toString(), "4.3.2.1");
	}

	@Test
	void firstLastArr() {
		IPv4Range range = IPv4Range.newBuilder().first(new byte[] { 1, 2, 3, 4 }).last(new byte[] { 4, 3, 2, 1 })
				.build();
		assertEquals(range.getFirst().toString(), "1.2.3.4");
		assertEquals(range.getLast().toString(), "4.3.2.1");
	}

	@Test
	void firstLastInet() {
		IPv4Range range;
		try {
			range = IPv4Range.newBuilder().first((Inet4Address) InetAddress.getByAddress(new byte[] { 1, 2, 3, 4 }))
					.last((Inet4Address) InetAddress.getByAddress(new byte[] { 4, 3, 2, 1 })).build();
			assertEquals(range.getFirst().toString(), "1.2.3.4");
			assertEquals(range.getLast().toString(), "4.3.2.1");
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void firstLastStr() {
		IPv4Range range = IPv4Range.newBuilder().first("1.2.3.4").last("4.3.2.1").build();
		assertEquals(range.getFirst().toString(), "1.2.3.4");
		assertEquals(range.getLast().toString(), "4.3.2.1");
	}

	@Test
	void firstLastInt() {
		IPv4Range range = IPv4Range.newBuilder().first(0x01020304).last(0x04030201).build();
		assertEquals(range.getFirst().toString(), "1.2.3.4");
		assertEquals(range.getLast().toString(), "4.3.2.1");
	}

	@Test
	void staticFactory() {
		IPv4Range range = IPv4Range.of(IPv4Address.of("1.2.3.4"), IPv4Address.of("4.3.2.1"));
		assertEquals(range.getFirst().toString(), "1.2.3.4");
		assertEquals(range.getLast().toString(), "4.3.2.1");
	}

	@Test
	void unequalToNull() {
		IPv4Range.Builder range = IPv4Range.newBuilder().first("1.2.3.4").last("4.3.2.1");
		assertNotEquals(range, null);
	}

	@Test
	void equalLastNull() {
		IPv4Address nullIp = null;
		IPv4Range.Builder range1 = IPv4Range.newBuilder().first("1.2.3.4").last(nullIp);
		IPv4Range.Builder range2 = IPv4Range.newBuilder().first(nullIp).last("1.2.3.4");
		IPv4Range.Builder range3 = IPv4Range.newBuilder().first("1.2.3.4").last("1.2.3.4");

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
		IPv4Range.Builder range1 = IPv4Range.newBuilder().first("1.2.3.4").last("4.3.2.1");
		IPv4Range.Builder range2 = IPv4Range.newBuilder().first("1.2.3.4").last("4.3.2.1");
		assertEquals(range1, range2);
		assertEquals(range1.hashCode(), range2.hashCode());
		assertEquals(range1, range1);
		IPv4Range.Builder range3 = IPv4Range.newBuilder().first("1.2.3.5").last("4.3.2.1");
		IPv4Range.Builder range4 = IPv4Range.newBuilder().first("1.2.3.4").last("5.3.2.1");
		assertNotEquals(range1, range3);
		assertNotEquals(range1, range4);
		assertNotEquals(range3, range4);
		assertNotEquals(range3, new Object());
	}

	@Test
	void toStr() {
		String str = IPv4Range.newBuilder().first("1.2.3.4").last("4.3.2.1").toString();
		assertTrue(str.contains("1.2.3.4"));
		assertTrue(str.contains("4.3.2.1"));
	}

	@Test
	void illegalArg() {
		assertThrows(IllegalArgumentException.class,
				() -> IPv4Range.newBuilder().first("1.1.1.1").last("0.0.0.0").build());
	}

}
