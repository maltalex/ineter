/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.IPv4Address;

@RunWith(JUnitPlatform.class)
public class IPv4AddressTest {

	@ParameterizedTest
	@ValueSource(strings = { "255.255.255.255", "0.0.0.0", "1.2.3.4" })
	void equality(String ipStr) {
		IPv4Address ip1 = IPv4Address.of(ipStr);
		IPv4Address ip2 = IPv4Address.of(ipStr);
		assertTrue(ip1.equals(ip2));
		assertTrue(ip1.hashCode() == ip2.hashCode());
		assertFalse(ip1 == ip2);
	}

	@Test
	void version() {
		IPv4Address ip1 = IPv4Address.of("1.2.3.4");
		assertTrue(ip1.version() == 4);
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
		assertTrue(!ip1.equals(ip2));
		assertFalse(ip1 == ip2);
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0,255.255.255.255", "127.255.255.0, 128.0.0.0" })
	void ordering(String ipStr1, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		assertTrue(ip1.compareTo(ip2) == -1);
		assertTrue(ip2.compareTo(ip1) == 1);
		assertTrue(ip1.compareTo(ip1) == 0);
		assertTrue(ip2.compareTo(ip2) == 0);

		assertTrue(ip1.compareTo(null) == 1);
		assertTrue(ip2.compareTo(null) == 1);
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
			assertTrue(IPv4Address.of((Inet4Address) InetAddress.getByName("8.8.8.8")).toString().equals("8.8.8.8"));
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
	void ipCopyConstructor() {
		IPv4Address ip1 = IPv4Address.of("1.2.3.4");
		IPv4Address ip2 = IPv4Address.of(ip1);
		assertFalse(ip1 == ip2);
		assertTrue(ip1.equals(ip2));
	}

	@Test
	void byteArrayConstructor() {
		IPv4Address ip = IPv4Address.of(new byte[] { 1, 2, 3, 4 });
		assertTrue(ip.toString().equals("1.2.3.4"));
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
		assertTrue(ip1.plus(j).equals(ip2));
		assertTrue(ip2.minus(j).equals(ip1));
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0,10.0.0.1", "0.0.0.0,0.0.0.1", "127.255.255.255,128.0.0.0", "255.255.255.255,0.0.0.0" })
	void nextPrev(String ipStr1, String ipStr2) {
		IPv4Address ip1 = IPv4Address.of(ipStr1);
		IPv4Address ip2 = IPv4Address.of(ipStr2);
		assertTrue(ip1.next().equals(ip2));
		assertTrue(ip2.previous().equals(ip1));
	}

	@Test
	void toStr() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertTrue(ip.toString().equals("130.123.1.2"));
	}

	@Test
	void toInetAddress() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		try {
			assertTrue(ip.toInet4Address().equals(InetAddress.getByName("130.123.1.2")));
		} catch (UnknownHostException e) {
			fail(e);
		}
	}

	@Test
	void toInt() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertTrue(ip.toInt() == 0x827b0102);
		assertTrue(ip.toBigInteger().equals(BigInteger.valueOf(0x827b0102L)));
		assertTrue(ip.toSignedBigInteger().equals(BigInteger.valueOf(0xffffffff827b0102L)));
	}

	@Test
	void toArray() {
		IPv4Address ip = IPv4Address.of("130.123.1.2");
		assertTrue(Arrays.equals(ip.toArray(), new byte[] { (byte) 130, 123, 1, 2 }));
		assertTrue(Arrays.equals(ip.toBigEndianArray(), new byte[] { (byte) 130, 123, 1, 2 }));
		assertTrue(Arrays.equals(ip.toLittleEndianArray(), new byte[] { 2, 1, 123, (byte) 130 }));
	}
}
