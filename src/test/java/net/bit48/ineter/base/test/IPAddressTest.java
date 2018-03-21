/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.base.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import net.bit48.ineter.base.IPAddress;
import net.bit48.ineter.base.IPv4Address;
import net.bit48.ineter.base.IPv6Address;

@RunWith(JUnitPlatform.class)
public class IPAddressTest {

	@Test
	public void ofArrayIPv4() {
		byte[] arr = new byte[] { 1, 2, 3, 4 };
		assertTrue(IPAddress.of(arr).equals(IPv4Address.of(arr)));
		assertThrows(IllegalArgumentException.class, () -> IPAddress.of(new byte[] { 1, 2, 3 }));
		assertThrows(IllegalArgumentException.class, () -> IPAddress.of(new byte[] { 1, 2, 3, 4, 5 }));
	}

	@Test
	public void ofStringIPv4() {
		String str = "1.2.3.4";
		assertEquals(IPAddress.of(str), IPv4Address.of(str));
	}

	@Test
	public void ofInetAddressV4() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName("1.2.3.4");
		assertEquals(IPAddress.of(addr), IPv4Address.of(addr.getAddress()));
	}

	@Test
	public void ofArrayIPv6() {
		byte[] arr = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
		assertEquals(IPAddress.of(arr), IPv6Address.of(arr));
		assertThrows(IllegalArgumentException.class,
				() -> IPAddress.of(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 }));
		assertThrows(IllegalArgumentException.class,
				() -> IPAddress.of(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 }));
	}

	@Test
	public void ofStringIPv6() {
		String str = "[1234:1234:1234:1234:1234:1234:1234:1234]";
		assertEquals(IPAddress.of(str), IPv6Address.of(str));
	}

	@Test
	public void ofInetAddressV6() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName("::");
		assertEquals(IPAddress.of(addr), IPv6Address.of(addr.getAddress()));
	}

	@Test
	public void ofStringBadString() {
		assertThrows(IllegalArgumentException.class, () -> IPAddress.of(":"));
		assertThrows(IllegalArgumentException.class, () -> IPAddress.of(""));
		assertThrows(IllegalArgumentException.class,
				() -> IPAddress.of("this is some random long string that's not an ip address"));
	}
}
