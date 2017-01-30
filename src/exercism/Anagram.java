package exercism;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Anagram {
	private Map<String, Long> histogram;
	private String original;

	public Anagram(String st) {
		original = st;
		histogram = createHistogram(st);
	}

	private Map<String, Long> createHistogram(String st) {
		return Stream.of(st.toLowerCase().split("")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public List<String> match(List<String> listOfAnagram) {
		return listOfAnagram.stream().filter(this::isAnagram).collect(Collectors.toList());
	}

	private boolean isAnagram(String s) {
		return histogram.equals(createHistogram(s)) && !s.equalsIgnoreCase(original);
	}
}