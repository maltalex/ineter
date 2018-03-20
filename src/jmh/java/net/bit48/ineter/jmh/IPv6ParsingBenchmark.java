/**
 * Copyright (c) 2018-present, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter.jmh;

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

import com.google.common.net.InetAddresses;

import net.bit48.ineter.base.IPv6Address;
import net.bit48.ineter.test.IPv6AddressParseTest;

@Measurement(iterations = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class IPv6ParsingBenchmark {
	private static final int ADDR_CNT = 1000;
	List<String> addresses;

	@Setup(Level.Trial)
	public void setUp() {
		// no brackets, guava doesn't like it
		this.addresses = IPv6AddressParseTest.generateIP6AddressStrings(0, ADDR_CNT, false);
	}

	@Benchmark
	public void ineterParsing() {
		for (String addr : this.addresses) {
			IPv6Address.of(addr);
		}
	}

	@Benchmark
	public void inetAddressParsing() {
		for (String addr : this.addresses) {
			try {
				InetAddress.getAllByName(addr);
			} catch (UnknownHostException e) {
				//
			}
		}
	}

	@Benchmark
	public void guavaAddressParsing() {
		for (String addr : this.addresses) {
			InetAddresses.forString(addr);
		}
	}

}