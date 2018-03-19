/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class IPAddress implements Serializable {

	private static final long serialVersionUID = 1L;

	public static IPAddress of(byte[] bigEndianByteArr) {
		if (bigEndianByteArr.length == 4) {
			return IPv4Address.of(bigEndianByteArr);
		}
		if (bigEndianByteArr.length == 16) {
			return IPv6Address.of(bigEndianByteArr);
		}

		throw new IllegalArgumentException("Array length must be 4 or 16. Given legth: " + bigEndianByteArr.length);
	}

	public static IPAddress of(String ip) {
		if (ip.length() >= 2 && ip.length() <= 41) {
			// Either a "." or ":" have to appear within the first 6 characters:
			// [1234: or 123.
			for (int i = 0; i < 6; i++) {
				char c = ip.charAt(i);
				if (c == '.') {
					return IPv4Address.of(ip);
				}
				if (c == ':') {
					return IPv6Address.of(ip);
				}
			}
		}
		throw new IllegalArgumentException(String.format("The string %s is not a valid ip address", ip));
	}

	public static IPAddress of(InetAddress address) {
		if (address instanceof Inet6Address) {
			return IPv6Address.of((Inet6Address) address);
		}

		return IPv4Address.of((Inet4Address) address);
	}

	public abstract boolean is6To4();

	public abstract boolean isMartian();

	public abstract boolean isLoopback();

	public abstract boolean isMulticast();

	public abstract boolean isPrivate();

	public abstract boolean isReserved();

	public abstract boolean isUnspecified();

	public abstract boolean isLinkLocal();

	public abstract int version();

	public abstract IPAddress next();

	public abstract IPAddress plus(int n);

	public abstract IPAddress previous();

	public abstract IPAddress minus(int n);

	public abstract byte[] toBigEndianArray();

	public abstract byte[] toLittleEndianArray();

	public byte[] toArray() {
		return toBigEndianArray();
	}

	public BigInteger toBigInteger() {
		return new BigInteger(1, toBigEndianArray());
	}

	public BigInteger toSignedBigInteger() {
		return new BigInteger(toBigEndianArray());
	}

	public InetAddress toInetAddress() {
		try {
			return InetAddress.getByAddress(toBigEndianArray());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

}