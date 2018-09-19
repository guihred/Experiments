package exercism;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/*
 * We use Joda Time here to encourage the use of a saner date manipulation library.
 */

@RunWith(Parameterized.class)
public class ScrabbleScoreTest {

	private int expectedOutput;
	private String input;

	public ScrabbleScoreTest(String input, int expectedOutput) {
		this.input = input;
		this.expectedOutput = expectedOutput;
	}

	@Test
	public void test() {
		Scrabble scrabble = new Scrabble(input);
        assertEquals("The pontuation should match", expectedOutput, scrabble.getScore());
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList( 
                new Object[] { "", 0 },
    		    new Object[]{ " \t\n", 0 },
    		    new Object[]{ null, 0 }, 
    		    new Object[]{ "a", 1 }, 
    		    new Object[]{ "f", 4 }, 
    		    new Object[]{ "street", 6 },
    		    new Object[]{ "quirky", 22 },
    		    new Object[]{ "OXYPHENBUTAZONE", 41 }, 
    			new Object[]{ "alacrity", 13 });
	}
}