package exercism;

import java.util.Arrays;

/**
 * The Class Pangram.
 * 
 * Determine if a sentence is a pangram.
 * 
 * Determine if a sentence is a pangram. A pangram is a sentence using every
 * letter of the alphabet at least once. The best known English pangram is "The
 * quick brown fox jumps over the lazy dog."
 * 
 * The alphabet used is ASCII, and case insensitive, from 'a' to 'z'
 * inclusively.
 */
public final class Pangram {

	private Pangram() {
	}

	/**
	 * Checks if is pangram.
	 *
	 * @param s
	 *            the s
	 * @return true, if is pangram
	 */
	public static boolean isPangram(String s) {
		return Arrays.asList(s.toLowerCase().split("")).containsAll(Arrays.asList("abcdefghijklmnoqrstuvwxyz".split("")));
	}

}