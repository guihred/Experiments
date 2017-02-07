package exercism;

import static org.junit.Assert.assertEquals;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/*
 * We use Joda Time here to encourage the use of a saner date manipulation library.
 */

@RunWith(Parameterized.class)
public class ScrabbleScoreTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "", 0 }, { " \t\n", 0 }, { null, 0 }, { "a", 1 }, { "f", 4 }, { "street", 6 }, { "quirky", 22 },
				{ "OXYPHENBUTAZONE", 41 }, { "alacrity", 13 }, });
	}
	private int expectedOutput;

	private String input;

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