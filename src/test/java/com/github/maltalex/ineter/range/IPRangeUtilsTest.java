package com.github.maltalex.ineter.range;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.github.maltalex.ineter.range.IPv4Range;
import com.github.maltalex.ineter.range.IPv4Subnet;

@RunWith(JUnitPlatform.class)
class IPRangeUtilsTest {

	private static final BiFunction<String, String, IPv4Range> IPv4_RANGE_PRODUCER = IPv4Range::of;
	private static final Function<String, IPv4Subnet> IPv4_SUBNET_PRODUCER = IPv4Subnet::of;

	@Test
	void parseRange() {
		final String from = "127.0.0.1-127.0.0.2";
		final IPv4Range iPv4Addresses = IPRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		final IPv4Range range = IPv4Range.of("127.0.0.1", "127.0.0.2");

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void parseSubnetAsRange() {
		final String from = "172.20.88.0/24";
		final IPv4Range iPv4Addresses = IPRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER);

		final IPv4Subnet range = IPv4Subnet.of("172.20.88.0", 24);

		assertEquals(range, iPv4Addresses);
	}

	@Test
	void throwOnNonsenseOnRange() {
		final String from = "127-127-127";
		assertThrows(IllegalArgumentException.class, () -> IPRangeUtils.parseRange(from, IPv4_RANGE_PRODUCER, IPv4_SUBNET_PRODUCER));
	}

	@Test
	void parseSubnet() {
		final String from = "172.20.88.0/24";
		final IPv4Subnet parsedSubnet = IPRangeUtils.parseSubnet(from, IPv4Subnet::of, (byte) 32);

		final IPv4Subnet subnet = IPv4Subnet.of("172.20.88.0/24");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void parseSingleAddressSubnet() {
		final String from = "172.20.88.1";
		final IPv4Subnet parsedSubnet = IPRangeUtils.parseSubnet(from, IPv4Subnet::of, (byte) 32);

		final IPv4Subnet subnet = IPv4Subnet.of("172.20.88.1/32");
		assertEquals(subnet, parsedSubnet);
	}

	@Test
	void throwOnNonsenseOnSubnet() {
		final String from = "127/127/127";
		assertThrows(IllegalArgumentException.class, () -> IPRangeUtils.parseSubnet(from, IPv4Subnet::of, (byte) 32));
	}
}