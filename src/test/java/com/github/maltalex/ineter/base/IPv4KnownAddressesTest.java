/**
 * Copyright (c) 2020, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.base;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.base.IPv4Address;

@RunWith(JUnitPlatform.class)
public class IPv4KnownAddressesTest {

	@ParameterizedTest()
	@CsvSource({ "0.0.0.0", "0.1.2.3", "0.255.255.255" })
	void isUnspecifiedTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isUnspecified());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "192.168.1.1", "255.255.255.255", "1.0.0.0", "10.0.0.1", "172.16.255.255",
			"250.1.2.3", "203.0.113.0" })
	void isUnspecifiedFalse(String ip) {

		assertFalse(IPv4Address.of(ip).isUnspecified());
	}

	@ParameterizedTest()
	@CsvSource({ "255.255.255.255" })
	void isBroadcastTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isBroadcast());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "192.168.1.1", "1.0.0.0", "10.0.0.1", "172.16.255.255", "250.1.2.3",
			"203.0.113.0" })
	void isBroadcastFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isBroadcast());
	}

	@ParameterizedTest()
	@CsvSource({ "192.88.99.0", "192.88.99.100", "192.88.99.255" })
	void is6To4True(String ip) {
		assertTrue(IPv4Address.of(ip).is6To4());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "192.168.1.1", "1.0.0.0", "10.0.0.1", "172.16.255.255", "250.1.2.3",
			"203.0.113.0" })
	void is6To4False(String ip) {
		assertFalse(IPv4Address.of(ip).is6To4());
	}

	@ParameterizedTest()
	@CsvSource({ "169.254.0.0", "169.254.100.100", "169.254.255.255" })
	void isDHCPFallbackTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isLinkLocal());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "192.168.1.1", "1.0.0.0", "10.0.0.1", "172.16.255.255", "250.1.2.3",
			"203.0.113.0" })
	void isDHCPFallbackFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isLinkLocal());
	}

	@ParameterizedTest()
	@CsvSource({ "192.168.0.0", "192.168.255.255", "10.0.0.0", "10.255.255.255", "172.16.0.0.", "172.31.255.255",
			"100.64.0.0", "100.127.255.255" })
	void isPrivateTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isPrivate());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "1.0.0.0", "250.1.2.3", "203.0.113.0", "1.2.3.4" })
	void isPrivateFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isPrivate());
	}

	@ParameterizedTest()
	@CsvSource({ "240.1.2.3", "192.0.0.1", "250.1.2.3", "192.0.2.1", "198.51.100.255", "203.0.113.128",
			"198.19.255.255", "255.255.255.255" })
	void isReservedTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isReserved());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3", "192.168.1.1", "1.0.0.0", "10.0.0.1", "172.16.255.255" })
	void isReservedFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isReserved());
	}

	@ParameterizedTest()
	@CsvSource({ "127.0.0.1", "127.1.2.3" })
	void isLoopbackTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isLoopback());
	}

	@ParameterizedTest()
	@CsvSource({ "128.0.0.0", "126.255.255.255", "1.2.3.4", "10.0.0.1", "198.19.255.255" })
	void isLoopbackFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isLoopback());
	}

	@ParameterizedTest()
	@CsvSource({ "224.0.0.0", "234.1.2.3", "239.255.255.254" })
	void isMulticastTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isMulticast());
	}

	@ParameterizedTest()
	@CsvSource({ "240.0.0.0", "250.0.0.0", "255.255.255.255", "127.0.0.1", "10.0.0.1" })
	void isMulticastFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isMulticast());
	}

	@ParameterizedTest()
	@CsvSource({ "240.0.0.0", "192.88.99.1", "169.254.1.2", "250.0.0.0", "255.255.255.255", "127.0.0.1",
			"192.168.100.100", "172.16.100.1", "100.64.1.1", "10.0.0.1", "198.19.255.255", "0.1.2.3", "224.1.2.3" })
	void isMartianTrue(String ip) {
		assertTrue(IPv4Address.of(ip).isMartian());
	}

	@ParameterizedTest()
	@CsvSource({ "152.235.122.12", "11.12.13.14", "8.8.4.4", "128.1.2.3" })
	void isMartianFalse(String ip) {
		assertFalse(IPv4Address.of(ip).isMartian());
	}
}
