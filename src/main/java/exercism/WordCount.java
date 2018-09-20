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
     * @param phrase
     *            the string
     * @return the map
     */
    public Map<String, Integer> phrase(String phrase) {

        Map<String, Long> splitWords = Stream.of(phrase.split("[^a-zA-Z0-9]+"))
				.collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));
        Map<String, Integer> wordCount = new HashMap<>();
        splitWords.forEach((w, n) -> wordCount.put(w, n.intValue()));
        return wordCount;
	}

}