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
			// While subsequent ranges overlap (or are adjacent), keep expanding
			// the merged range
			while (candidateIndex < sortedRanges.size() && (mergedRange.overlaps(sortedRanges.get(candidateIndex))
					|| mergedRange.getLast().next().equals(sortedRanges.get(candidateIndex).getFirst()))) {
				I pendingRangeEnd = max(mergedRange.getLast(), sortedRanges.get(candidateIndex).getLast());
				mergedRange = rangeCreator.apply(pendingRangeStart, pendingRangeEnd);
				candidateIndex++;
			}
			sortedRanges.set(mergedRangeIndex++, mergedRange);
		}

		return new ArrayList<>(sortedRanges.subList(0, mergedRangeIndex));
	}
	
	static <C extends Comparable<C>> C max(C a, C b) {
		return a.compareTo(b) > 0 ? a : b;
	}

}
