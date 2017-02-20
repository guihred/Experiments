package crypt;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VigenereCipher {
	private static final Logger LOGGER = LoggerFactory.getLogger(VigenereCipher.class);

	private int current;

	public String encrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i - 'a' + k.charAt(current++ % length) - 'a' + 52) % 26 + 'a')
				.mapToObj(i -> Character.valueOf((char) i)).map(Object::toString)
				.collect(Collectors.joining());
	}

	public String decrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i - k.charAt(current++ % length) + 52) % 26 + 'a')
				.mapToObj(i -> Character.valueOf((char) i)).map(Object::toString)
				.collect(Collectors.joining());
	}

	public static void main(String[] args) {
		VigenereCipher vigenereCypher = new VigenereCipher();
		String k = "spy";
		LOGGER.info(vigenereCypher.encrypt(k, "seeyouatnoon"));
		LOGGER.info(vigenereCypher.decrypt(k, vigenereCypher.encrypt(k, "seeyouatnoon")));

	}

}
