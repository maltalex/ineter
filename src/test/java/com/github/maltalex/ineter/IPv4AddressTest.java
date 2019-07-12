/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.IPv4Address;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(JUnitPlatform.class)
public class IPv4AddressTest {

	@ParameterizedTest
	@ValueSource(strings = { "255.255.255.255", "0.0.0.0", "1.2.3.4" })
	void equality(String ipStr) {
		IPv4Address ip1 = IPv4Address.of(ipStr);
		IPv4Address ip2 = IPv4Address.of(ipStr);
		assertEquals(ip1, ip2);
		assertEquals(ip1.hashCode(), ip2.hashCode());
		assertNotSame(ip1, ip2);
	}

	@Test
	void version() {
		IPv4Address ip1 = IPv4Address.of("1.2.3.4");
		assertEquals(4, ip1.version());
	}

	@Test
	void unequalToObject() {
		IPv4Address ip1 = IPv4Address.of("1.2.3.4");
		assertFalse(ip1.equals(new Object()));
	}

	@Test
	void unequalToNull() {
		IPv4Address ip1 = IPv4Address.of("1.2.3.4");
		assertFalse(ip1.equals(null));
	}

	@ParameterizedTest
	@CsvSource({ "255.255.255.255,127.255.255.255", "0.0.0.0,255.255.255.255", "1.2.3.4,0.0.0.0",
			"127.255.255.255,1.2.3.4" })
	void inequality(String ipStr1, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		assertNotEquals(ip1, ip2);
		assertNotSame(ip1, ip2);
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0,255.255.255.255", "127.255.255.0, 128.0.0.0" })
	void ordering(String ipStr1, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		assertEquals(-1, ip1.compareTo(ip2));
		assertEquals(1, ip2.compareTo(ip1));
		assertEquals(0, ip1.compareTo(ip1));
		assertEquals(0, ip2.compareTo(ip2));

		assertEquals(1, ip1.compareTo(null));
		assertEquals(1, ip2.compareTo(null));
	}

	@ParameterizedTest
	@ValueSource(strings = { "asdfasf", "260.52.123.260", "1.2.3.4.5", "1.2.3" })
	void badStringConstructor(String ipStr) {
		assertThrows(IllegalArgumentException.class, () -> IPv4Address.of(ipStr));
	}

	@Test
	void nullStringConstructor() {
		String a = null;
		assertThrows(NullPointerException.class, () -> IPv4Address.of(a));
	}

	@Test
	void inetAddressConstructor() {
		try {
			assertEquals("8.8.8.8", IPv4Address.of((Inet4Address) InetAddress.getByName("8.8.8.8")).toString());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void inet4AddressConstructor() {
		try {
			Inet4Address a = (Inet4Address) InetAddress.getByName("8.8.8.8");
			IPv4Address.of(a);
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void byteArrayConstructor() {
		IPv4Address ip = IPv4Address.of(new byte[] { 1, 2, 3, 4 });
		assertEquals("1.2.3.4", ip.toString());
		assertThrows(IllegalArgumentException.class, () -> IPv4Address.of(new byte[] { 1, 2, 3 }));
		assertThrows(IllegalArgumentException.class, () -> IPv4Address.of(new byte[] { 1, 2, 3, 4, 5 }));
		assertThrows(NullPointerException.class, () -> IPv4Address.of((byte[]) null));
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0,1,10.0.0.1", "0.0.0.0,7fffffff,127.255.255.255", "127.255.255.255,1,128.0.0.0",
			"255.255.255.255,1,0.0.0.0" })
	void plusMinus(String ipStr1, String i, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		int j = Integer.parseInt(i, 16);
		assertEquals(ip1.plus(j), ip2);
		assertEquals(ip2.minus(j), ip1);
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0,10.0.0.1", "0.0.0.0,0.0.0.1", "127.255.255.255,128.0.0.0", "255.255.255.255,0.0.0.0" })
	void nextPrev(String ipStr1, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		assertEquals(ip1.next(), ip2);
		assertEquals(ip2.previous(), ip1);
	}

	@Test
	void toStr() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertEquals("130.123.1.2", ip.toString());
	}

	@Test
	void toInetAddress() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		try {
			assertEquals(InetAddress.getByName("130.123.1.2"), ip.toInet4Address());
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void toInt() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertEquals(0x827b0102, ip.toInt());
		assertEquals(BigInteger.valueOf(0x827b0102L), ip.toBigInteger());
		assertEquals(BigInteger.valueOf(0xffffffff827b0102L), ip.toSignedBigInteger());
	}

	@Test
	void toArray() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertArrayEquals(ip.toArray(), new byte[]{(byte) 130, 123, 1, 2});
		assertArrayEquals(ip.toBigEndianArray(), new byte[]{(byte) 130, 123, 1, 2});
		assertArrayEquals(ip.toLittleEndianArray(), new byte[]{2, 1, 123, (byte) 130});
	}
}
