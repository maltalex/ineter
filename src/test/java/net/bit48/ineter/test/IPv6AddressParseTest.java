/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.base.IPv6Address;
import net.bit48.ineter.base.ZonedIPv6Address;

@RunWith(JUnitPlatform.class)
public class IPv6AddressParseTest {

	// Do not rename used by @MethodSource below
	public static List<String> generateIP6AddressStrings() {
		return generateIP6AddressStrings(0, 10_000, true);
	}

	public static List<String> generateIP6AddressStrings(int seed, int count, boolean brackets) {
		List<String> addresses = new ArrayList<>(count);
		Random r = new Random(seed);

		for (int i = 0; i < count; i++) {
			List<String> currentAddress = new ArrayList<>(8);

			for (int j = 0; j < 8; j++) {
				currentAddress.add(Integer.toHexString(r.nextInt(Short.MAX_VALUE + 1)));
			}

			// 50% Chance of double colon
			if (r.nextBoolean()) {
				int first = r.nextInt(8); // First part to erase
				int last = first + r.nextInt(8 - first); // Last part to erase

				// Set one part to either "", ":" or "::"
				currentAddress.set(first, (first == 0 ? ":" : "") + (last == 7 ? ":" : ""));
				for (int j = ++first; j <= last; j++) { // Delete parts
					currentAddress.remove(first);
				}
			}
			if (brackets && r.nextBoolean()) {
				addresses.add("[" + String.join(":", currentAddress) + "]");
			} else {
				addresses.add(String.join(":", currentAddress));
			}
		}
		return addresses;
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc", "A:B:C:D:E:F:a:B", "::1", "1::", "1::1",
			"1:2:3:4:5:6:7:8", "12:34:56::abcd", "::", "1:02:003:0004::", "1000:200:30:4::" })
	void passeAndToString(String addressStr) throws UnknownHostException {
		String java = InetAddress.getByName(addressStr).getHostAddress();
		IPv6Address ineter = IPv6Address.of(addressStr);
		assertFalse(ineter.isZoned());
		assertEquals(java, ineter.toString());
	}

	@ParameterizedTest
	@MethodSource("generateIP6AddressStrings")
	void randomAddressesCompareParsing(String addressStr) throws UnknownHostException {
		String java = InetAddress.getByName(addressStr).getHostAddress();
		String ineter = IPv6Address.of(addressStr).toString();
		assertEquals(java, ineter);
	}

	@ParameterizedTest
	@ValueSource(strings = { "[::1]", "[1::]", "[1::1]", "[1:2:3:4:5:6:7:8]", "[12:34:56::abcd]", "[::]",
			"[abcd:dbca:1234:4321:aabb:bbaa:ccdd:ddcc]", "[A:B:C:D:E:F:a:B]" })
	void validBrackets(String addressStr) throws UnknownHostException {
		String java = InetAddress.getByName(addressStr).getHostAddress();
		String ineter = IPv6Address.of(addressStr).toString();
		assertEquals(java, ineter);
	}

	@ParameterizedTest
	@ValueSource(strings = { "::1]", "[1::" })
	void invalidBrackets(String addressStr) {
		assertThrows(IllegalArgumentException.class, () -> IPv6Address.of(addressStr));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "1", "[0000:0000:0000:0000:0000:0000:0000:00001]", })
	void badLength(String addressStr) {
		try {
			IPv6Address.of(addressStr);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("length"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "00001:0000:0000:0000:0000:0000:0000:000", "0000:00001:0000:0000:0000:0000:0000:000",
			"0000:0000:00001:0000:0000:0000:0000:000", "0000:0000:0000:00001:0000:0000:0000:000",
			"0000:0000:0000:0000:00001:0000:0000:000", "0000:0000:0000:0000:0000:00001:0000:000",
			"0000:0000:0000:0000:0000:0000:00001:000", "000:0000:0000:0000:0000:0000:0000:00001", "00001:",
			"0000:00001:", "0000:0000:00001::", "0000:0000:0000:00001::", "::00001:0000:0000:0000", "::00001:0000:0000",
			"::00001:0000", "::00001" })
	void tooManyDigitsInPart(String addressStr) {
		try {
			IPv6Address.of(addressStr);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("digits"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1:1", "[1:1]", "1:1:1", "::1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1:1",
			"1:1:1:1:1:1:1:1:1:1:1", "1:1:1:1:1:1:1:1:1:1:1:1", "::1:1:1:1:1:1:1:1:1", "1::1:1:1:1:1:1:1:1",
			"1:1::1:1:1:1:1:1:1", "1:1:1::1:1:1:1:1:1", "1:1:1:1::1:1:1:1:1", "1:1:1:1:1::1:1:1:1",
			"1:1:1:1:1:1::1:1:1", "1:1:1:1:1:1:1::1:1", "1:1:1:1:1:1:1:1::1", "1:1:1:1:1:1:1:1:1::", })
	void numberOfParts(String addressStr) {
		try {
			IPv6Address.of(addressStr);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("parts"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1:::1:1:1:1:1:1:1", "1:1:::1:1:1:1:1:1", "1:1:1:::1:1:1:1:1", "1:1:1:1:::1:1:1:1",
			"1:1:1:1:1:::1:1:1", "1:1:1:1:1:1:::1:1", "1:1:1:1:1:1:::1:1", "1:1:1:1:1:1:1:::1", "1:1:1:1:1:1:1:1:::",
			"1::1:1:1:1:1:1::1", "1:1::1:1:1:1:1::1", "1:1:1::1:1:1:1::1", "1:1:1:1::1:1:1::1", "1:1:1:1:1::1:1::1",
			"1:1:1:1:1:1::1::1", "1:1:1:1:1:1::1::1", "::1:1:1:1:1:1:1:1::", })
	void badColons(String addressStr) {
		try {
			IPv6Address.of(addressStr);
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("colon"));
			return;
		}
		fail("Exception expected!");
	}

	@ParameterizedTest
	@CsvSource({ "::1%eth0,eth0", "::%eth0,eth0", "1234:1234:1234:1234:1234:1234:1234:1234%blah,blah" })
	void zoned(String addressStr, String zone) {
		IPv6Address of = IPv6Address.of(addressStr);
		assertTrue(of instanceof ZonedIPv6Address);
		assertTrue(of.isZoned());
		ZonedIPv6Address zoned = (ZonedIPv6Address) of;
		assertEquals(zoned.getZone(), zone);
	}

	@SuppressWarnings("boxing")
	@Test
	void illegalChar() {
		List<Character> charsNoDigits = IntStream.range(0, 128).mapToObj(c -> new Character((char) c))
				.filter(c -> Character.digit(c, 16) == -1)
				.filter(c -> !(c.equals(':') || c.equals('%') || c.equals(']') || c.equals('[')))
				.collect(Collectors.toList());
		for (Character c : charsNoDigits) {
			try {
				IPv6Address.of("1::" + c); // After colons
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("character"));
				continue;
			}
			fail("Exception expected!");
		}

		for (Character c : charsNoDigits) {
			try {
				IPv6Address.of(c + "::1"); // Before colons
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("character"));
				continue;
			}
			fail("Exception expected!");
		}
	}
}
