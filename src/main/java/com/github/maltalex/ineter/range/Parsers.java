/*
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.util.function.BiFunction;
import java.util.function.Function;

class Parsers {
	static <T> T parseRange(String from, BiFunction<String, String, ? extends T> rangeProducer, Function<String, ? extends T> subnetProducer) {
		String[] parts = from.split("-");
		if (parts.length == 2) {
			return rangeProducer.apply(parts[0].trim(), parts[1].trim());
		} else if (parts.length == 1) {
			try {
				//TODO: maybe precondition  with validation would be cheaper than exception handling
				return subnetProducer.apply(from);
			} catch (Exception e) {
				return rangeProducer.apply(parts[0].trim(), parts[0].trim());
			}
		} else {
			throw new IllegalArgumentException(String.format("Inappropriate format for address range string %s.", from));
		}
	}

	static <T> T parseSubnet(String from, BiFunction<String, Integer, ? extends T> subnetProducer, int singleAddressMask) {
		final String[] parts = from.split("/");
		if (parts.length == 2) {
			return subnetProducer.apply(parts[0].trim(), Integer.parseInt(parts[1].trim()));
		} else if (parts.length == 1) {
			return subnetProducer.apply(parts[0].trim(), singleAddressMask);
		} else {
			throw new IllegalArgumentException(String.format("Inappropriate format for address subnet string %s.", from));
		}
	}
}
