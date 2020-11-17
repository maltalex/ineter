/*
 * Copyright (c) 2020, ineter contributors
 *
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
		// The shortest valid string is :: (length 2)
		for (int i = from.length() - 1; i > 1; i--) {
			char c = from.charAt(i);
			if (c == '-') {
				return rangeProducer.apply(from.substring(0, i).trim(), from.substring(i + 1, from.length()).trim());
			}
			if (c == '/') {
				return subnetProducer.apply(from.trim());
			}
		}
		String trimmed = from.trim();
		return rangeProducer.apply(trimmed, trimmed);
	}

	static <T> T parseSubnet(String from, BiFunction<String, Integer, ? extends T> subnetProducer,
			int singleAddressMask) {
		int position = from.length() - 1;
		int charsToCheck = 4; // The slash (/) has to be in the last 4 positions
		while (position > 0 && charsToCheck > 0) {
			if (from.charAt(position) == '/') {
				return subnetProducer.apply(from.substring(0, position).trim(),
						Integer.parseUnsignedInt(from.substring(position + 1, from.length()).trim()));
			}
			position--;
			charsToCheck--;
		}
		return subnetProducer.apply(from.trim(), singleAddressMask);
	}

	static <L extends Number & Comparable<L>, I extends IPAddress & Comparable<I>, R extends IPRange<R, ?, I, L>> List<R> merge(
			Collection<R> rangesToMerge, BiFunction<I, I, R> rangeCreator) {
		if (rangesToMerge.isEmpty()) {
			return Collections.emptyList();
		}

		ArrayList<R> sortedRanges = new ArrayList<>(rangesToMerge);
		sortedRanges.sort(Comparator.comparing(R::getFirst));

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

	static <L extends Number & Comparable<L>, I extends IPAddress & Comparable<I>, R extends IPRange<R, ?, I, L>> boolean overlapsOrAdjacent(
			R mergedRange, R candidateRange) {
		return mergedRange.overlaps(candidateRange) || mergedRange.getLast().next().equals(candidateRange.getFirst());
	}
}
