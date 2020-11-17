/*
 * Copyright (c) 2020, ineter contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.base.IPv4Address;
import com.github.maltalex.ineter.range.IPv4Subnet.IPv4SubnetMask;

@RunWith(JUnitPlatform.class)
public class IPv4SubnetTest {

	@Test
	void constructors() {
		IPv4Subnet a = IPv4Subnet.of("10.0.0.0/8");
		IPv4Subnet b = IPv4Subnet.of(IPv4Address.of("10.0.0.0"), 8);
		IPv4Subnet c = IPv4Subnet.of("10.0.0.0", 8);

		assertEquals(a.getFirst(), IPv4Address.of("10.0.0.0"));
		assertEquals(a.getLast(), IPv4Address.of("10.255.255.255"));
		assertEquals(a, b);
		assertEquals(b, c);
		assertThrows(IllegalArgumentException.class, () -> IPv4Subnet.of("0.0.0.0/-0"));
		assertThrows(IllegalArgumentException.class, () -> IPv4Subnet.of("0.0.0.0"));
		assertThrows(IllegalArgumentException.class, () -> IPv4Subnet.of("0.0.0.0/0/2"));
		assertThrows(IllegalArgumentException.class, () -> IPv4Subnet.of("0.0.0.0/0/"));
	}

	@Test
	void getters() {
		IPv4Subnet subnet = IPv4Subnet.of("192.168.1.0/24");

		assertEquals(subnet.getFirst(), IPv4Address.of("192.168.1.0"));
		assertEquals(subnet.getLast(), IPv4Address.of("192.168.1.255"));
		assertEquals(24, subnet.getNetworkBitCount());
		assertEquals(8, subnet.getHostBitCount());
		assertEquals(subnet.getNetworkMask(), IPv4Address.of("255.255.255.0"));
		assertEquals(subnet.getNetworkAddress(), IPv4Address.of("192.168.1.0"));
		assertEquals("192.168.1.0/24", subnet.toString());
	}

	@Test
	void equality() {
		IPv4Subnet subnet1 = IPv4Subnet.of("192.168.1.0/24");
		IPv4Subnet subnet2 = IPv4Subnet.of("192.168.1.0/24");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void unequal() {
		List<IPv4Subnet> l = Arrays.asList(IPv4Subnet.of("192.168.1.0/24"), IPv4Subnet.of("192.168.1.0/25"),
				IPv4Subnet.of("192.168.0.0/24"), IPv4Subnet.of("192.168.0.0/16"), IPv4Subnet.of("192.168.0.0/32"));

		for (IPv4Subnet s1 : l) {
			for (IPv4Subnet s2 : l) {
				if (s1 != s2) {
					assertNotEquals(s1, s2);
				}
			}
		}
	}

	@Test
	void unequalToNull() {
		assertNotEquals(IPv4Subnet.of("1.2.3.0/24"), null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), IPv4Subnet.of("1.2.3.0/24"));
	}

	@Test
	void equalToRangeWithSameAddresses() {
		IPv4Subnet subnet1 = IPv4Subnet.of("192.168.1.0/24");
		IPv4Range subnet2 = IPv4Range.parse("192.168.1.0-192.168.1.255");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet2, subnet1);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void parseCidr() {
		final String cidr = "192.168.0.0/24";
		final IPv4Subnet parsedSubnet = IPv4Subnet.parse(cidr);
		final IPv4Subnet cidrSubnet = IPv4Subnet.of(cidr);

		assertEquals(cidrSubnet, parsedSubnet);
	}

	@Test
	void parseSingleAddress() {
		final String address = "172.20.0.1";
		final IPv4Subnet parsedSubnet = IPv4Subnet.parse(address);
		final IPv4Subnet subnet = IPv4Subnet.of(address, 32);
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void validAndInvalidMaskTest() {
		for (int i = 0; i <= 32; i++) {
			assertNotNull(IPv4SubnetMask.fromMaskLen(i));
		}
		for (int i = -100; i < 0; i++) {
			int j = i;
			assertThrows(IllegalArgumentException.class, () -> IPv4SubnetMask.fromMaskLen(j));
		}
		for (int i = 33; i < 200; i++) {
			int j = i;
			assertThrows(IllegalArgumentException.class, () -> IPv4SubnetMask.fromMaskLen(j));
		}
	}
}
