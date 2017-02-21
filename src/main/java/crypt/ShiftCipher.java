package crypt;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public final class ShiftCipher {

	public ShiftCipher() {
	}

	public static String encrypt(int k,String s) {
		return s.chars().map(i -> (i - 'a' + k) % 26 + 'a').mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString).collect(Collectors.joining());
	}

	public static String decrypt(int k, String s) {
		return s.chars().map(i -> (i - 'a' - k + 26) % 26 + 'a').mapToObj(i -> Character.valueOf((char) i))
				.map(Object::toString)
				.collect(Collectors.joining());
	}


	@Test
	public void encryptDecryptEquals() {
		int k = 25;
		String s = "cryptoisfun";
		String encrypt = encrypt(k, s);
		Assert.assertEquals(s, decrypt(k, encrypt));
	}

}
