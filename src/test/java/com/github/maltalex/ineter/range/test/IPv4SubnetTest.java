/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.base.IPv4Address;
import com.github.maltalex.ineter.range.IPv4Subnet;

@RunWith(JUnitPlatform.class)
public class IPv4SubnetTest {

	@Test
	void constructors() {
		IPv4Subnet a = IPv4Subnet.of("10.0.0.0/8");
		IPv4Subnet b = IPv4Subnet.of(IPv4Address.of("10.0.0.0"), (byte) 8);
		IPv4Subnet c = IPv4Subnet.of("10.0.0.0", (byte) 8);

		assertEquals(a.getFirst(), IPv4Address.of("10.0.0.0"));
		assertEquals(a.getLast(), IPv4Address.of("10.255.255.255"));
		assertEquals(a, b);
		assertEquals(b, c);
	}

	@Test
	void getters() {
		IPv4Subnet subnet = IPv4Subnet.of("192.168.1.0/24");

		assertEquals(subnet.getFirst(), IPv4Address.of("192.168.1.0"));
		assertEquals(subnet.getLast(), IPv4Address.of("192.168.1.255"));
		assertEquals(subnet.getNetworkBitCount(), 24);
		assertEquals(subnet.getHostBitCount(), 8);
		assertEquals(subnet.getNetworkMask(), IPv4Address.of("255.255.255.0"));
		assertEquals(subnet.getNetworkAddress(), IPv4Address.of("192.168.1.0"));
		assertEquals(subnet.toString(), "192.168.1.0/24");
	}

	@Test
	void equaly() {
		IPv4Subnet subnet1 = IPv4Subnet.of("192.168.1.0/24");
		IPv4Subnet subnet2 = IPv4Subnet.of("192.168.1.0/24");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
		assertNotEquals(subnet1, null);
	}

	@Test
	void unequal() {
		List<IPv4Subnet> l = Arrays.asList(IPv4Subnet.of("192.168.1.0/24"), IPv4Subnet.of("192.168.1.0/25"),
				IPv4Subnet.of("192.168.0.0/24"), IPv4Subnet.of("192.168.0.0/16"), IPv4Subnet.of("192.168.0.0/32"));

		for (IPv4Subnet s1 : l) {
			for (IPv4Subnet s2 : l) {
				if (!(s1 == s2)) {
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
		assertNotEquals(IPv4Subnet.of("1.2.3.0/24"), new Object());
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
		final IPv4Subnet subnet = IPv4Subnet.of(address, (byte) 32);
		assertEquals(subnet, parsedSubnet);
	}
}
