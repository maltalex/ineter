package com.github.maltalex.ineter.range;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class IPRangeTest {

	private static final BiFunction<String, String, IPv4Range> IPv4_RANGE_PRODUCER = IPv4Range::of;
	private static final Function<String, IPv4Subnet> IPv4_SUBNET_PRODUCER = IPv4Subnet::of;

	@Test
	void parseRange() {
		String from = "127.0.0.1-127.0.0.2";
		IPv4Range iPv4Addresses = IPRange.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		IPv4Range range = IPv4Range.of("127.0.0.1", "127.0.0.2");

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void parseSubnetAsRange() {
		String from = "172.20.88.0/24";
		IPv4Range iPv4Addresses = IPRange.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		IPv4Subnet range = IPv4Subnet.of("172.20.88.0", (byte) 24);

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void throwOnNonsenseOnRange() {
		String from = "127-127-127";
		assertThrows(IllegalArgumentException.class, () -> IPRange.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER));
	}

	@Test
	void parseSubnet() {
		String from = "172.20.88.0/24";
		IPv4Subnet parsedSubnet = IPRange.parseSubnet(from, IPv4Subnet::of, (byte) 32);

		IPv4Subnet subnet = IPv4Subnet.of("172.20.88.0/24");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void parseSingleAddressSubnet() {
		String from = "172.20.88.1";
		IPv4Subnet parsedSubnet = IPRange.parseSubnet(from, IPv4Subnet::of, (byte) 32);

		IPv4Subnet subnet = IPv4Subnet.of("172.20.88.1/32");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void throwOnNonsenseOnSubnet() {
		String from = "127/127/127";
		assertThrows(IllegalArgumentException.class, () -> IPRange.parseSubnet(from, IPv4Subnet::of, (byte) 32));
	}
}