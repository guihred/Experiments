package crypt;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShiftCipher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShiftCipher.class);

	private ShiftCipher() {
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

	public static void main(String[] args) {

		int k = 25;
		LOGGER.info(encrypt(k, "cryptoisfun"));
		LOGGER.info(decrypt(k, encrypt(k, "cryptoisfun")));

	}

}
