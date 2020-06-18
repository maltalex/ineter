/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.base;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.range.IPv6Range;
import com.github.maltalex.ineter.range.IPv6Subnet;

@RunWith(JUnitPlatform.class)
public class IPv6AddressTest {

	@ParameterizedTest
	@ValueSource(strings = { "::1", "::", "1234:4321:abcd:dcba::" })
	void equality(String ipStr) {
		IPv6Address ip1 = IPv6Address.of(ipStr);
		IPv6Address zonedIp1 = IPv6Address.of(ipStr + "%foo");
		IPv6Address ip2 = IPv6Address.of(ipStr);
		IPv6Address zonedIp2 = IPv6Address.of(ipStr + "%foo");

		assertEquals(ip1, ip2);
		assertEquals(zonedIp1, zonedIp2);

		assertEquals(ip2, ip1);
		assertEquals(zonedIp2, zonedIp1);

		assertEquals(ip1, ip1);
		assertEquals(zonedIp1, zonedIp1);

		assertEquals(ip1.hashCode(), ip2.hashCode());
		assertEquals(zonedIp1.hashCode(), zonedIp2.hashCode());

		assertNotSame(ip1, ip2);
		assertNotSame(zonedIp1, zonedIp2);
	}

	@ParameterizedTest
	@ValueSource(strings = { "::1%blah", "::%eth0", "1234:4321:abcd:dcba::%foo" })
	void zonedType(String ipStr) {
		IPv6Address ip1 = IPv6Address.of(ipStr);
		IPv6Address ip2 = ZonedIPv6Address.of(ipStr);
		assertTrue(ip1 instanceof ZonedIPv6Address);
		assertTrue(ip2 instanceof ZonedIPv6Address);
	}

	@Test
	void notZonedThrowing() {
		assertThrows(IllegalArgumentException.class, () -> ZonedIPv6Address.of("::"));
	}

	@Test
	void ip6AddressWithZoneConstructor() {
		IPv6Address unzoned = IPv6Address.of("::");
		assertEquals(ZonedIPv6Address.of(unzoned, "foo"), ZonedIPv6Address.of("::%foo"));
	}

	@Test
	void inet6AddressConstructor() {
		try {
			Inet6Address a = (Inet6Address) InetAddress.getByName("::1");
			IPv6Address.of(a);
		} catch (UnknownHostException e) {
			fail(e);
		}
		try {
			Inet6Address a = (Inet6Address) InetAddress.getByName("fe80::1%1");
			IPv6Address.of(a);
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void version() {
		assertEquals(6, IPv6Address.of("::").version());
		assertEquals(6, ZonedIPv6Address.of("::%eth0").version());
	}

	@Test
	void unequalToObject() {
		assertFalse(IPv6Address.of("::").equals(new Object()));
		assertFalse(ZonedIPv6Address.of("::%foo").equals(new Object()));
	}

	@Test
	void unequalToNull() {
		assertFalse(IPv6Address.of("::").equals(null));
		assertFalse(ZonedIPv6Address.of("::%foo").equals(null));
	}

	@ParameterizedTest
	@CsvSource({ "::,::1", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,1234::", "1:2:3:4:5:6:7:8, 8:7:6:5:4:3:2:1",
			"::1234,1234::", "aa::bb,aa::cc", "aa::bb,cc::bb" })
	void inequality(String ipStr1, String ipStr2) {
		IPv6Address ip1 = IPv6Address.of(ipStr1);
		IPv6Address zonedIp1 = IPv6Address.of(ipStr1 + "%foo");
		IPv6Address ip2 = IPv6Address.of(ipStr2);
		IPv6Address zonedIp2 = IPv6Address.of(ipStr2 + "%foo");

		assertNotEquals(ip1, ip2);
		assertNotEquals(zonedIp1, zonedIp2);

		assertNotEquals(ip1, zonedIp1);
		assertNotEquals(zonedIp1, ip1);
		assertNotEquals(ip2, zonedIp2);
		assertNotEquals(zonedIp2, ip2);

		assertNotSame(ip1, ip2);
		assertNotSame(zonedIp1, zonedIp2);
	}

	@ParameterizedTest
	@CsvSource({ "::,ffff::", "::7fff:ffff:ffff:ffff,0:0:0:8000::" })
	void unzonedOrdering(String ipStr1, String ipStr2) {
		IPv6Address ip1 = IPv6Address.of(ipStr1);
		IPv6Address ip2 = IPv6Address.of(ipStr2);
		assertEquals(-1, ip1.compareTo(ip2));
		assertEquals(1, ip2.compareTo(ip1));
		assertEquals(0, ip1.compareTo(ip1));
		assertEquals(0, ip2.compareTo(ip2));

		assertEquals(1, ip1.compareTo(null));
		assertEquals(1, ip2.compareTo(null));
	}

	@ParameterizedTest
	@CsvSource({ "::%foo,ffff::%foo", "ffff::%bar,::%foo" })
	void zonedOrdering(String ipStr1, String ipStr2) {
		IPv6Address ip1 = IPv6Address.of(ipStr1);
		IPv6Address ip2 = IPv6Address.of(ipStr2);
		assertTrue(ip1.compareTo(ip2) < 0);
		assertTrue(ip2.compareTo(ip1) > 0);
		assertEquals(0, ip1.compareTo(ip1));
		assertEquals(0, ip2.compareTo(ip2));

		assertTrue(ip1.compareTo(null) > 0);
		assertTrue(ip2.compareTo(null) > 0);
	}

	@Test
	void mixedOrdering() {
		IPv6Address zoned = IPv6Address.of("::%eth0");
		IPv6Address unzoned = IPv6Address.of("::");

		assertTrue(zoned.compareTo(unzoned) > 0);
		assertTrue(unzoned.compareTo(zoned) < 0);
	}

	@Test
	void nullStringConstructor() {
		String a = null;
		assertThrows(IllegalArgumentException.class, () -> IPv6Address.of(a));
		assertThrows(IllegalArgumentException.class, () -> ZonedIPv6Address.of(a));
	}

	@Test
	void inetAddressConstructor() {
		try {
			String zonedIp = "1234:0:0:0:0:0:0:4321%1";
			assertEquals(zonedIp, IPAddress.of(InetAddress.getByName(zonedIp)).toString());
			assertEquals("1234:0:0:0:0:0:0:4321", IPAddress.of(InetAddress.getByName("1234::4321")).toString());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void byteArrayConstructor() {
		byte[] goodArr = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
		byte[] badArr1 = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee };
		byte[] badArr2 = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff, 0x00 };

		assertEquals("11:2233:4455:6677:8899:aabb:ccdd:eeff", IPv6Address.of(goodArr).toString());
		assertEquals("11:2233:4455:6677:8899:aabb:ccdd:eeff%foo", ZonedIPv6Address.of(goodArr, "foo").toString());

		assertThrows(IllegalArgumentException.class, () -> IPv6Address.of(badArr1));
		assertThrows(IllegalArgumentException.class, () -> ZonedIPv6Address.of(badArr1, "foo"));

		assertThrows(IllegalArgumentException.class, () -> IPv6Address.of(badArr2));
		assertThrows(IllegalArgumentException.class, () -> ZonedIPv6Address.of(badArr2, "foo"));

		assertThrows(NullPointerException.class, () -> IPv6Address.of((byte[]) null));
		assertThrows(NullPointerException.class, () -> ZonedIPv6Address.of((byte[]) null, "foo"));
	}

	@ParameterizedTest
	@CsvSource({ "::,1,::1", "::,7fffffffffffffff,::7fff:ffff:ffff:ffff",
			"::7fff:ffff:ffff:ffff,7fffffffffffffff,::ffff:ffff:ffff:fffe",
			"::ffff:ffff:ffff:ffff,7fffffffffffffff,::1:7fff:ffff:ffff:fffe", "::,0,::", "1::1,0,1::1",
			"ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff,1,::", "::ffff:ffff:ffff:ffff,1,0:0:0:1::" })
	void plusMinus(String ipStr1, String i, String ipStr2) {
		IPv6Address ip1 = IPv6Address.of(ipStr1);
		IPv6Address zonedIp1 = IPv6Address.of(ipStr1 + "%foo");
		IPv6Address ip2 = IPv6Address.of(ipStr2);
		IPv6Address zonedIp2 = IPv6Address.of(ipStr2 + "%foo");
		BigInteger j = new BigInteger(i, 16);

		assertEquals(ip1.plus(j.longValue()), ip2);
		assertEquals(ip2.plus(-j.longValue()), ip1);
		assertEquals(ip2.minus(j.longValue()), ip1);
		assertEquals(ip1.minus(-j.longValue()), ip2);

		assertEquals(zonedIp1.plus(j.longValue()), zonedIp2);
		assertEquals(zonedIp2.plus(-j.longValue()), zonedIp1);
		assertEquals(zonedIp2.minus(j.longValue()), zonedIp1);
		assertEquals(zonedIp1.minus(-j.longValue()), zonedIp2);

	}

	@ParameterizedTest
	@CsvSource({ "::,::1", "::1234,::1235", "8000::,8000::1" })
	void nextPrev(String ipStr1, String ipStr2) {
		IPv6Address ip1 = IPv6Address.of(ipStr1);
		IPv6Address zonedIp1 = IPv6Address.of(ipStr1 + "%foo");
		IPv6Address ip2 = IPv6Address.of(ipStr2);
		IPv6Address zonedIp2 = IPv6Address.of(ipStr2 + "%foo");

		assertEquals(ip1.next(), ip2);
		assertEquals(ip2.previous(), ip1);

		assertEquals(zonedIp1.next(), zonedIp2);
		assertEquals(zonedIp2.previous(), zonedIp1);

	}

	@Test
	void toStr() {
		IPv6Address ip = IPv6Address.of("::1");
		assertEquals("0:0:0:0:0:0:0:1", ip.toString());
	}

	@Test
	void toInetAddress() {
		IPv6Address ip = IPv6Address.of("::1");
		try {
			assertEquals(InetAddress.getByName("::1"), ip.toInet6Address());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void toArray() {
		IPv6Address ip = IPv6Address.of("0010:2030:4050:6070:8090:a0b0:c0d0:e0f0");
		assertArrayEquals(ip.toArray(), new byte[] { 0, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, (byte) 0x80,
				(byte) 0x90, (byte) 0xa0, (byte) 0xb0, (byte) 0xc0, (byte) 0xd0, (byte) 0xe0, (byte) 0xf0 });
		assertArrayEquals(ip.toBigEndianArray(), new byte[] { 0, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, (byte) 0x80,
				(byte) 0x90, (byte) 0xa0, (byte) 0xb0, (byte) 0xc0, (byte) 0xd0, (byte) 0xe0, (byte) 0xf0 });
		assertArrayEquals(ip.toLittleEndianArray(), new byte[] { (byte) 0xf0, (byte) 0xe0, (byte) 0xd0, (byte) 0xc0,
				(byte) 0xb0, (byte) 0xa0, (byte) 0x90, (byte) 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10, 0 });
	}

	@Test
	void distanceTo() {
		assertEquals(BigInteger.valueOf(2), IPv6Address.of("::1").distanceTo(IPv6Address.of("::3")));
		assertEquals(BigInteger.valueOf(-2), IPv6Address.of("::3").distanceTo(IPv6Address.of("::1")));
		assertEquals(BigInteger.ZERO, IPv6Address.of("::1").distanceTo(IPv6Address.of("::1")));
		assertEquals(BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE),
				IPv6Address.of("::").distanceTo(IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")));
		assertEquals(BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE).negate(),
				IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").distanceTo(IPv6Address.of("::")));
	}

	@Test
	void isAdjacentTo() {
		assertFalse(IPv6Address.of("::1").isAdjacentTo(IPv6Address.of("::3")));
		assertTrue(IPv6Address.of("::1").isAdjacentTo(IPv6Address.of("::2")));
		assertFalse(IPv6Address.of("::1").isAdjacentTo(IPv6Address.of("::1")));
		assertTrue(IPv6Address.of("::0").isAdjacentTo(IPv6Address.of("::1")));
		assertTrue(IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
				.isAdjacentTo(IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fffe")));
	}

	@Test
	void toRange() {
		assertEquals(IPv6Range.of("::", "::1234"), IPv6Address.of("::").toRange(IPv6Address.of("::1234")));
		assertEquals(IPv6Range.of("::", "::1234"), IPv6Address.of("::1234").toRange(IPv6Address.of("::")));
	}

	@Test
	void toSubnet() {
		assertEquals(IPv6Subnet.of("::1234/128"), IPv6Address.of("::1234").toSubnet());
	}

	@Test
	void and() {
		assertEquals(IPv6Address.of("::1230"),
				IPv6Address.of("::1234").and(IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:fff0")));
	}

	@Test
	void or() {
		assertEquals(IPv6Address.of("::123f"), IPv6Address.of("::1230").or(IPv6Address.of("::f")));
	}

	@Test
	void xor() {
		assertEquals(IPv6Address.of("::aaaa"), IPv6Address.of("::ffff").xor(IPv6Address.of("::5555")));
	}

	@Test
	void not() {
		assertEquals(IPv6Address.of("::"), IPv6Address.of("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").not());
	}
}
