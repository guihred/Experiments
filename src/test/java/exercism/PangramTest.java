package exercism;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PangramTest {

	@Test
	public void emptySentence() {
        assertFalse("", Pangram.isPangram(""));
	}


	@Test
	public void testLowercasePangram() {
        assertTrue("", Pangram.isPangram("the quick brown fox jumps over the lazy dog"));
	}


	@Test
	public void missingCharacterX() {
        assertFalse("", Pangram.isPangram("a quick movement of the enemy will jeopardize five gunboats"));
	}


	@Test
	public void mixedCaseAndPunctuation() {
        assertTrue("", Pangram.isPangram("\"Five quacking Zephyrs jolt my wax bed.\""));
	}


	@Test
	public void nonAsciiCharacters() {
        assertTrue("", Pangram.isPangram("Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich."));
	}

}