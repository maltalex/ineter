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

import com.github.maltalex.ineter.base.IPv6Address;
import com.github.maltalex.ineter.range.IPv6Subnet.IPv6SubnetMask;

@RunWith(JUnitPlatform.class)
public class IPv6SubnetTest {

	@Test
	void constructors() {
		IPv6Subnet a = IPv6Subnet.of("::/16");
		IPv6Subnet b = IPv6Subnet.of(IPv6Address.of("::"), (byte) 16);
		IPv6Subnet c = IPv6Subnet.of("::", (byte) 16);

		assertEquals(a.getFirst(), IPv6Address.of("::"));
		assertEquals(a.getLast(), IPv6Address.of("1::").previous());
		assertEquals(a, b);
		assertEquals(b, c);
		assertThrows(IllegalArgumentException.class, () -> IPv6Subnet.of("::/-0"));
	}

	@Test
	void getters() {
		IPv6Subnet subnet = IPv6Subnet.of("1234::/16");

		assertEquals(subnet.getFirst(), IPv6Address.of("1234::"));
		assertEquals(subnet.getLast(), IPv6Address.of("1235::").previous());
		assertEquals(16, subnet.getNetworkBitCount());
		assertEquals(112, subnet.getHostBitCount());
		assertEquals(subnet.getNetworkMask(), IPv6Address.of("ffff::"));
		assertEquals(subnet.getNetworkAddress(), IPv6Address.of("1234::"));
		assertEquals("1234:0:0:0:0:0:0:0/16", subnet.toString());
	}

	@Test
	void equality() {
		IPv6Subnet subnet1 = IPv6Subnet.of("1234::/16");
		IPv6Subnet subnet2 = IPv6Subnet.of("1234::/16");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void unequal() {
		List<IPv6Subnet> l = Arrays.asList(IPv6Subnet.of("1234::/16"), IPv6Subnet.of("1234::/17"),
				IPv6Subnet.of("1234::/15"), IPv6Subnet.of("::1234/128"), IPv6Subnet.of("::1234/127"));

		for (IPv6Subnet s1 : l) {
			for (IPv6Subnet s2 : l) {
				if (!(s1 == s2)) {
					assertNotEquals(s1, s2);
				}
			}
		}
	}

	@Test
	void unequalToNull() {
		assertNotEquals(IPv6Subnet.of("::/24"), null);
	}

	@Test
	void unequalToObject() {
		assertNotEquals(new Object(), IPv6Subnet.of("::/24"));
	}

	@Test
	void equalToRangeWithSameAddresses() {
		IPv6Subnet subnet1 = IPv6Subnet.of("1234::/16");
		IPv6Range subnet2 = IPv6Range.parse("1234::-1234:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		assertEquals(subnet1, subnet2);
		assertEquals(subnet2, subnet1);
		assertEquals(subnet1.hashCode(), subnet2.hashCode());
	}

	@Test
	void parseSingleAddress() {
		String address = "1234::";
		IPv6Subnet parsedSubnet = IPv6Subnet.parse(address);
		IPv6Subnet subnet = IPv6Subnet.of("1234::/128");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void parseCidr() {
		String address = "1234::/64";
		IPv6Subnet parsedSubnet = IPv6Subnet.parse(address);
		IPv6Subnet subnet = IPv6Subnet.of("1234::", 64);
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void validAndInvalidMaskTest() {
		for (int i = 0; i <= 128; i++) {
			assertNotNull(IPv6SubnetMask.fromMaskLen(i));
		}
		for (int i = -100; i < 0; i++) {
			int j = i;
			assertThrows(IllegalArgumentException.class, () -> IPv6SubnetMask.fromMaskLen(j));
		}
		for (int i = 129; i < 200; i++) {
			int j = i;
			assertThrows(IllegalArgumentException.class, () -> IPv6SubnetMask.fromMaskLen(j));
		}
	}
}
