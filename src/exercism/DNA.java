package exercism;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DNA {

	private static final List<Character> DNA_NUCLEOTIDES = Arrays.asList('A', 'G', 'C', 'T');
	private Map<Character, Long> nucleotideCounts;

	public DNA(String string) {
		Map<Character, Long> collect = string.chars().mapToObj(c -> Character.valueOf((char) c))
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Map<Character, Long> hashMap = new HashMap<>();
		collect.forEach(hashMap::put);
		DNA_NUCLEOTIDES.forEach(a -> hashMap.putIfAbsent(a, 0L));
		nucleotideCounts = hashMap;

	}

	public long count(char c) {
		if (!DNA_NUCLEOTIDES.contains(c)) {
			throw new IllegalArgumentException();
		}

		return nucleotideCounts.get(c);
	}

	public Map<Character, Long> nucleotideCounts() {
		return nucleotideCounts;
	}

}