package crypt;

import java.util.stream.Collectors;

public final class ShiftCipherExercise {

	private static final int NUMBER_OF_LETTERS = 26;

	private ShiftCipherExercise() {
	}

	public static String decrypt(int k, String s) {
	    return s.chars().map(i -> (i - 'a' - k + NUMBER_OF_LETTERS) % NUMBER_OF_LETTERS + 'a')
				.mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString)
				.collect(Collectors.joining());
	}

	public static String encrypt(int k,String s) {
		return s.chars().map(i -> (i - 'a' + k) % NUMBER_OF_LETTERS + 'a').mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString).collect(Collectors.joining());
	}

}
