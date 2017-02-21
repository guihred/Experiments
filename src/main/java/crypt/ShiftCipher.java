package crypt;

import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public final class ShiftCipher {

	private static final int NUMBER_OF_LETTERS = 26;


	public ShiftCipher() {
	}

	public static String encrypt(int k,String s) {
		return s.chars().map(i -> (i - 'a' + k) % NUMBER_OF_LETTERS + 'a').mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString).collect(Collectors.joining());
	}

	public static String decrypt(int k, String s) {
		return s.chars().map(i -> (i - 'a' - k + NUMBER_OF_LETTERS) % NUMBER_OF_LETTERS + 'a')
				.mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString)
				.collect(Collectors.joining());
	}


	@Test
	public void encryptDecryptEquals() {
		int k = 20;
		String s = "cryptoisfun";
		String encrypt = encrypt(k, s);
		Assert.assertEquals(s, decrypt(k, encrypt));
	}

}
