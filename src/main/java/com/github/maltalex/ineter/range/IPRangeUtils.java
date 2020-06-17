/*
 * Copyright (c) 2020, ineter Contributors
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.github.maltalex.ineter.range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.maltalex.ineter.base.IPAddress;

abstract class IPRangeUtils {

	static <T> T parseRange(String from, BiFunction<String, String, ? extends T> rangeProducer,
			Function<String, ? extends T> subnetProducer) {
		String[] parts = from.split("-");
		if (parts.length == 2) {
			return rangeProducer.apply(parts[0].trim(), parts[1].trim());
		} else if (parts.length == 1) {
			if (from.contains("/")) {
				return subnetProducer.apply(from);
			}
			return rangeProducer.apply(parts[0].trim(), parts[0].trim());
		} else {
			throw new IllegalArgumentException(
					String.format("Inappropriate format for address range string %s.", from));
		}
	}

	static <T> T parseSubnet(String from, BiFunction<String, Integer, ? extends T> subnetProducer,
			int singleAddressMask) {
		final String[] parts = from.split("/");
		if (parts.length == 2) {
			return subnetProducer.apply(parts[0].trim(), Integer.parseInt(parts[1].trim()));
		} else if (parts.length == 1) {
			return subnetProducer.apply(parts[0].trim(), singleAddressMask);
		} else {
			throw new IllegalArgumentException(
					String.format("Inappropriate format for address subnet string %s.", from));
		}
	}

	static <L extends Number & Comparable<L>, I extends IPAddress & Comparable<I>, R extends IPRange<I, L>> List<R> merge(
			Collection<R> rangesToMerge, BiFunction<I, I, R> rangeCreator) {
		if (rangesToMerge.isEmpty()) {
			return Collections.emptyList();
		}

		ArrayList<R> sortedRanges = new ArrayList<>(rangesToMerge);
		Collections.sort(sortedRanges, Comparator.comparing(R::getFirst));

		int mergedRangeIndex = 0, candidateIndex = 0;
		while (candidateIndex < sortedRanges.size()) {
			// Grab first un-merged range
			R mergedRange = sortedRanges.get(candidateIndex++);
			I pendingRangeStart = mergedRange.getFirst();
			// extend "mergedRange" as much as possible
			while (candidateIndex < sortedRanges.size()) {
				R candidateRange = sortedRanges.get(candidateIndex);
				if (!overlapsOrAdjacent(mergedRange, candidateRange)) {
					break;
				}
				mergedRange = rangeCreator.apply(pendingRangeStart,
						IPAddress.max(mergedRange.getLast(), candidateRange.getLast()));
				candidateIndex++;
			}
			sortedRanges.set(mergedRangeIndex++, mergedRange);
		}

		return new ArrayList<>(sortedRanges.subList(0, mergedRangeIndex));
	}

	static <L extends Number & Comparable<L>, I extends IPAddress & Comparable<I>, R extends IPRange<I, L>> boolean overlapsOrAdjacent(
			R mergedRange, R candidateRange) {
		return mergedRange.overlaps(candidateRange) || mergedRange.getLast().next().equals(candidateRange.getFirst());
	}
}
