[![Build Status](https://travis-ci.org/maltalex/ineter.svg?branch=master)](https://travis-ci.org/maltalex/ineter)
[![codecov](https://codecov.io/gh/maltalex/ineter/branch/master/graph/badge.svg)](https://codecov.io/gh/maltalex/ineter)

# ineter

## What?

ineter (pronounced "Eye-netter") is a tiny Java library for working with:
- Individual IP addresses - `IPv4Address`, `IPv6Address`/`ZonedIPv6Address`
- IP address ranges - `IPv4Range`, `IPv6Range`
- IP subnets - `IPv4Subnet`, `IPv6Subnet`

## Why?

- Low memory (and GC) footprint: *ineter* uses primitive types to represent addresses - an `int` for IPv4, two `long` fields for IPv6. For comparison, Java's `InetAddress` uses an `InetAddressHolder` with two `String` fields and two `int` fields just for IPv4
- Immutability: *ineter* is immutable and thread-safe by default
- Speed: *ineter* is written with performance in mind
- Rich set of supported operations
- MPL-2.0 license, allowing commercial use as well as re-licensing under GNU

## Where?
	
#### Maven:

	<dependency>
    	<groupId>com.github.maltalex</groupId>
    	<artifactId>ineter</artifactId>
    	<version>0.2.0</version>
	</dependency>

#### Gradle:

	compile 'com.github.maltalex:ineter:0.2.0'

## How?

### Individual IPv4/IPv6 addresses

	IPv4Address ipv4 = IPv4Address.of("10.0.0.1");
	IPv6Address ipv6 = IPv6Address.of("2001::1234:4321");

	ipv4.isPrivate(); // true
	ipv4.isMulticast(); // false
	ipv6.isGlobalUnicast(); // true
	ipv4.compareTo(IPv4Address.of(0x0a000001)); // addresses are comparable
	ipv6.toInetAddress(); // addresses can be converted to other forms
	ipv6.toSubnet(); // IPv6Address as a single /128 subnet
	ipv4.plus(5); // 10.0.0.6
	ipv4.distanceTo(IPv4Address.of("10.0.0.100")); // 99
	ipv4.previous(); // 10.0.0.0
	
### Arbitrary address ranges

	IPv4Range ipv4Range = IPv4Range.parse("192.168.100.0-192.168.101.127");
	IPv6Range ipv6Range = IPv6Range.of("2001::","2001::1000"); //Build using first and last address
	IPv6Range singletonRange = IPv6Range.parse("2002::"); // A single address in range form

	ipv6Range.contains(IPv6Address.of("2001::1000")); //true
	ipv4Range.overlaps(IPv4Range.of("10.0.0.1", "10.0.0.10")); //false
	ipv4Range.toSubnets(); // Returns the list of subnets that make up the range
	ipv4Range.withLast(IPv4Address.of("192.168.102.0")); //range with different last address
	ipv6Range.length(); //4097
	IPv6Range.merge(ipv6Range, singletonRange); //ranges can be merged

### Subnets

	IPv4Subnet ipv4Subnet = IPv4Subnet.of("192.168.0.0/16");
	IPv6Range ipv6Subnet = IPv6Range.parse("2001::/64"); // subnets are ranges too!

	ipv4Subnet.getNetworkMask(); //255.255.0.0
	ipv4Subnet.getNetworkBitCount(); //16