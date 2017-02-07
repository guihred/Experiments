package exercism;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class Scrabble.
 * 
 * A class to calculate the score of a word in the game Scrabble.
 */
public class Scrabble {

	/** The per value. */
	static Map<Integer, List<String>> PER_VALUE;

	/** The per letter. */
	private static Map<String, Integer> PER_LETTER;
	static {
		PER_VALUE = ImmutableMap.<Integer, List<String>> builder()
		.put(1, Arrays.asList("A", "E", "I", "O", "U", "L", "N", "R", "S", "T"))
		.put(2, Arrays.asList("D", "G"))
		.put(3, Arrays.asList("B", "C", "M", "P"))
		.put(4, Arrays.asList("F", "H", "V", "W", "Y"))
		.put(5, Arrays.asList("K"))
		.put(8, Arrays.asList("J", "X"))
		.put(10, Arrays.asList("Q", "Z"))
		.build();

		PER_LETTER = Scrabble.PER_VALUE.entrySet().stream()
				.flatMap(e -> e.getValue().stream().collect(Collectors.toMap(a -> a, a -> e.getKey())).entrySet().stream())
				.collect(Collectors.toMap(Entry<String, Integer>::getKey, Entry<String, Integer>::getValue));
	}

	/** The input. */
	private String input;

	/**
	 * Instantiates a new scrabble.
	 *
	 * @param input
	 *            the input
	 */
	public Scrabble(String input) {
		this.input = input;
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	int getScore() {
		if (input == null) {
			return 0;
		}
		return Stream.of(input.toUpperCase().split("")).filter(Pattern.compile("[A-Z]").asPredicate()).mapToInt(PER_LETTER::get).sum();
	}
}