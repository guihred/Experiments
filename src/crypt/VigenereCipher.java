package crypt;

import java.util.stream.Collectors;

public class VigenereCipher {

	private int current = 0;

	public String encrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i - 'a' + k.charAt(current++ % length) - 'a' + 52) % 26 + 'a')
				.mapToObj(i -> Character.valueOf((char) i)).map(c -> Character.toString(c))
				.collect(Collectors.joining());
	}

	public String decrypt(String k, String s) {
		current = 0;
		int length = k.length();
		return s.chars().map(i -> (i - k.charAt(current++ % length) + 52) % 26 + 'a')
				.mapToObj(i -> Character.valueOf((char) i)).map(c -> Character.toString(c))
				.collect(Collectors.joining());
	}

	public static void main(String[] args) {
		VigenereCipher vigenereCypher = new VigenereCipher();
		String k = "spy";
		System.out.println(vigenereCypher.encrypt(k, "seeyouatnoon"));
		System.out.println(vigenereCypher.decrypt(k, vigenereCypher.encrypt(k, "seeyouatnoon")));

	}

}
