[![Build Status](https://travis-ci.org/maltalex/Ineter.svg?branch=master)](https://travis-ci.org/maltalex/Ineter)
[![codecov](https://codecov.io/gh/maltalex/Ineter/branch/master/graph/badge.svg)](https://codecov.io/gh/maltalex/Ineter)

Ineter
======

What?
-----

Ineter (pronounced "Eye-netter") is a tiny Java library for working with:
- Individual IP addresses - `IPv4Address`, `IPv6Address`/`ZonedIPv6Address`
- IP address ranges - `IPv4Range`, `IPv6Range`
- IP subnets - `IPv4Subnet`, `IPv6Subnet`

Why?
----

- Low memory (and GC) footprint: Ineter uses primitive types to represent addresses - an `int` for IPv4, two `long` fields for IPv6. For comparison, Java's `InetAddress` uses an `InetAddressHolder` with two `String` fields and two `int` fields just for IPv4
- Immutability: Ineter is immutable and thread-safe by default
- Speed: Ineter is written with performance in mind
- Rich set of supported operations
- MPL-2.0 license, allowing commercial use as well as re-licensing under GNU

How?
----
//todo

