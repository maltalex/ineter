/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.base.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.maltalex.ineter.base.IPv6Address;

public class IPv6KnownAddressesTest {

	@ParameterizedTest()
	@CsvSource({ "2002::1", "2002:ffff:ffff::" })
	void is6to4True(String ip) {
		assertTrue(IPv6Address.of(ip).is6To4());
	}

	@ParameterizedTest()
	@CsvSource({ "::", "2001::", "2003::" })
	void is6to4False(String ip) {
		assertFalse(IPv6Address.of(ip).is6To4());
	}

	@ParameterizedTest()
	@CsvSource({ "::", "::1", "::ffff:abcd:ef00", "::0:abcd:1234", "64:ff9b::1", "2001:10::1", "fc00::1", "fe80::1234",
			"ff05::1" })
	void isMartianTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isMartian());
	}

	@ParameterizedTest()
	@CsvSource({ "2001::", "2002::1", "::fffe:abcd:ef00", "::1:abcd:1234", "65:ff9b::1", "2002:10::1", "fe00::1",
			"fee0::1234", "ff0e::1" })
	void isMartianFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isMartian());
	}

	@ParameterizedTest()
	@CsvSource({ "2001:0000::1", "::ffff:0:1", "2002::1:2:3", "64:ff9b::1" })
	void isTranslationTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isIPv4Translation());
	}

	@ParameterizedTest()
	@CsvSource({ "2001:1234::ffff:0:1", "2000::1:2:3", "60:ff9b::1" })
	void isTranslationFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isIPv4Translation());
	}

	@Test()
	void isLoopbackTrue() {
		assertTrue(IPv6Address.of("::1").isLoopback());
	}

	@ParameterizedTest()
	@CsvSource({ "::", "::2", "ff::" })
	void isLoopbackFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isLoopback());
	}

	@ParameterizedTest()
	@CsvSource({ "2000::", "3000::", "2222::" })
	void isGlobalUnicastTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isGlobalUnicast());
	}

	@ParameterizedTest()
	@CsvSource({ "1000::", "4000::" })
	void isGlobalUnicastFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isGlobalUnicast());
	}

	@ParameterizedTest()
	@CsvSource({ "fe80::1", "feb0::" })
	void isLonkLocalTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isLinkLocal());
	}

	@ParameterizedTest()
	@CsvSource({ "fec0::", "ffff::" })
	void isLinkLocalFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isLinkLocal());
	}

	@ParameterizedTest()
	@CsvSource({ "ff00::1", "ff01::1", "ff0e::1" })
	void isMulticastTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isMulticast());
	}

	@ParameterizedTest()
	@CsvSource({ "fe00::1", "0f00::1", "7700::1" })
	void isMulticastFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isMulticast());
	}

	@ParameterizedTest()
	@CsvSource({ "fc00::", "fd00::" })
	void isPrivateTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isPrivate());
	}

	@ParameterizedTest()
	@CsvSource({ "fe00::", "ff00::", "::" })
	void isPrivateFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isPrivate());
	}

	@ParameterizedTest()
	@CsvSource({ "2001:10::1", "2001:20::1", "100::1", "2001:db8::1" })
	void isReservedTrue(String ip) {
		assertTrue(IPv6Address.of(ip).isReserved());
	}

	@ParameterizedTest()
	@CsvSource({ "2001:0::1", "2001:30::1", "200::1", "2001:db0::1" })
	void isReservedFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isReserved());
	}

	@Test
	void isUnspecifiedTrue() {
		assertTrue(IPv6Address.of("::").isUnspecified());
	}

	@ParameterizedTest()
	@CsvSource({ "::1", "1::" })
	void isUnspecifiedFalse(String ip) {
		assertFalse(IPv6Address.of(ip).isUnspecified());
	}
}
