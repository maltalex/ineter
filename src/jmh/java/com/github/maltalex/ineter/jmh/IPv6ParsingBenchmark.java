/*
 * Copyright (c) 2020, ineter Contributors
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.jmh;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.github.maltalex.ineter.base.IPv6Address;
import com.github.maltalex.ineter.base.IPv6AddressParseTest;
import com.google.common.net.InetAddresses;

@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class IPv6ParsingBenchmark {
	private static final int ADDR_CNT = 1000;
	private List<String> addresses;

	@Setup(Level.Trial)
	public void setUp() {
		// no brackets, guava doesn't like it
		this.addresses = IPv6AddressParseTest.generateIP6AddressStrings(0, ADDR_CNT, false);
	}

	@Benchmark
	public void ineterParsing(Blackhole hole) {
		for (String addr : this.addresses) {
			hole.consume(IPv6Address.of(addr));
		}
	}

	@Benchmark
	public void inetAddressParsing(Blackhole hole) {
		for (String addr : this.addresses) {
			try {
				hole.consume(InetAddress.getAllByName(addr));
			} catch (UnknownHostException e) {
				//
			}
		}
	}

	@Benchmark
	public void guavaAddressParsing(Blackhole hole) {
		for (String addr : this.addresses) {
			hole.consume(InetAddresses.forString(addr));
		}
	}

}