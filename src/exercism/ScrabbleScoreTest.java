package exercism;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/*
 * We use Joda Time here to encourage the use of a saner date manipulation library.
 */

@RunWith(Parameterized.class)
public class ScrabbleScoreTest {

	private String input;
	private int expectedOutput;

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "", 0 }, { " \t\n", 0 }, { null, 0 }, { "a", 1 }, { "f", 4 }, { "street", 6 }, { "quirky", 22 },
				{ "OXYPHENBUTAZONE", 41 }, { "alacrity", 13 }, });
	}

	public ScrabbleScoreTest(String input, int expectedOutput) {
		this.input = input;
		this.expectedOutput = expectedOutput;
	}

	@Test
	public void test() {
		Scrabble scrabble = new Scrabble(input);

		assertEquals(expectedOutput, scrabble.getScore());
	}
}

class Scrabble {

	static Map<Integer, List<String>> PER_VALUE;
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
	private String input;

	public Scrabble(String input) {
		this.input = input;
	}

	int getScore() {
		if (input == null) {
			return 0;
		}

		int sum = Stream.of(input.toUpperCase().split("")).filter(Pattern.compile("[A-Z]").asPredicate()).mapToInt(PER_LETTER::get).sum();
		return sum;
	}
}