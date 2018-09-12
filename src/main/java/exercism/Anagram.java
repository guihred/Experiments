package exercism;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class to check if a word is an anagram of other word.
 */
public class Anagram {
	private Map<String, Long> histogram;
	private String original;

	public Anagram(String st) {
		original = st;
		histogram = createHistogram(st);
	}

	private static Map<String, Long> createHistogram(String st) {
		return Stream.of(st.toLowerCase().split("")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

    public List<String> match(Collection<String> listOfAnagram) {
		return listOfAnagram.stream().filter(this::isAnagram).collect(Collectors.toList());
	}

	private boolean isAnagram(String s) {
		return histogram.equals(createHistogram(s)) && !s.equalsIgnoreCase(original);
	}

	public static boolean isAnagram(String original, String test) {
		return createHistogram(original).equals(createHistogram(test)) && !test.equalsIgnoreCase(original);
	}
}