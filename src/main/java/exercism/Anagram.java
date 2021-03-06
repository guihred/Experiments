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
    private final Map<String, Long> histogram;
    private final String original;

	public Anagram(String st) {
		original = st;
		histogram = createHistogram(st);
	}

	public List<String> match(Collection<String> listOfAnagram) {
		return listOfAnagram.stream().filter(this::isAnagram).collect(Collectors.toList());
	}

    private boolean isAnagram(String s) {
		return histogram.equals(createHistogram(s)) && !s.equalsIgnoreCase(original);
	}


	private static Map<String, Long> createHistogram(String st) {
        return Stream.of(st.toLowerCase().split(""))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}
}