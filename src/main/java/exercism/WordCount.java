package exercism;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class WordCount.
 * 
 * Given a phrase, count the occurrences of each word in that phrase.
 */
public class WordCount {

	/**
	 * Phrase.
	 *
	 * @param string
	 *            the string
	 * @return the map
	 */
	public Map<String, Integer> phrase(String string) {

		Map<String, Long> collect = Stream.of(string.split("[^a-zA-Z0-9]+"))
				.collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));
		HashMap<String, Integer> hashMap = new HashMap<>();
		collect.forEach((w, n) -> hashMap.put(w, n.intValue()));
		return hashMap;
	}

}