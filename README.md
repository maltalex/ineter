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
    	<version>0.1.0</version>
	</dependency>

#### Gradle:

	compile 'com.github.maltalex:ineter:0.1.0'

## How?

### Individual IP addresses

##### IPv4

	IPv4Address ipv4 = IPv4Address.of("10.0.0.1");

	ipv4.isPrivate(); // true
	ipv4.isMulticast(); // false
	ipv4.compareTo(IPv4Address.of(0x0a000001)); // 0
	ipv4.toInetAddress().equals(InetAddress.getByName("10.0.0.1")); // true
	ipv4.plus(5); // 10.0.0.6
	ipv4.previous(); // 10.0.0.0
	
##### IPv6

	IPv6Address ipv6 = IPv6Address.of("2001::1234:4321");

	ipv6.isGlobalUnicast(); // true
	ipv6.isPrivate(); // false
	ipv6.compareTo(IPv6Address.of("::")); // 1
	ipv6.toInetAddress().equals(InetAddress.getByName("2001::1234:4321")); // true
	ipv6.plus(5); // 2001:0:0:0:0:0:1234:4326
	ipv6.previous(); // 2001:0:0:0:0:0:1234:4320
	
### Ranges and Subnets

##### IPv4Range

	IPv4Range ipv4Range = IPv4Range.parse("192.168.100.0-192.168.101.127"); //Arbitrary range of addresses
		
	ipv4Range.contains(IPv4Address.of("192.168.100.100")); //true
	ipv4Range.overlaps(IPv4Range.of("10.0.0.1", "10.0.0.10")); //false
	for(IPv4Address ip : ipv4Range) {} //Ranges are iterable
	ipv4Range.toSubnets(); //Returns list [192.168.100.0/24, 192.168.101.0/25] (IPv4Subnet instances)
	ipv4Range.length(); //384

##### IPv6Range

	IPv6Range ipv6Range = IPv6Range.of("2001::","2001::1000"); //Arbitrary range of addresses
		
	ipv6Range.contains(IPv6Address.of("2001::1000"))); //true
	ipv6Range.overlaps(IPv6Range.of("2002::", "2003::"))); //false
	for(IPv6Address ip : ipv6Range) {} //Ranges are iterable
	ipv6Range.toSubnets(); //Returns list [2001:0:0:0:0:0:0:0/116, 2001:0:0:0:0:0:0:1000/128] (IPv6Subnet instances)
	ipv6Range.length(); //4097

##### IPv4Subnet

	IPv4Subnet ipv4Subnet = IPv4Subnet.of("192.168.0.0/16"); //Extents IPv4Range, has all methods in IPv4Range
	ipv4Subnet.getNetworkMask(); //255.255.0.0
	ipv4Subnet.getNetworkBitCount(); //16
		
##### IPv6Subnet

	IPv6Subnet ipv6Subnet = IPv6Subnet.of("2001::/64"); //Extents IPv4Range, has all methods in IPv6Range
	ipv6Subnet.getNetworkMask(); //ffff:ffff:ffff:ffff:0:0:0:0
	ipv6Subnet.getNetworkBitCount(); //64

