package exercism;

import java.util.Arrays;

class Pangram {
	public static boolean isPangram(String s) {
		return Arrays.asList(s.toLowerCase().split("")).containsAll(Arrays.asList("abcdefghijklmnoqrstuvwxyz".split("")));
	}

}