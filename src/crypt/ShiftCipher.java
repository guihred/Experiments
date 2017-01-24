package crypt;

import java.util.stream.Collectors;

public class ShiftCipher {

	public static String encrypt(int k,String s) {
		return s.chars().map(i->
		
		
		(i - 'a' + k) % 26 + 'a').mapToObj(i -> Character.valueOf((char) i)).map(c -> Character.toString(c)).collect(Collectors.joining());
	}

	public static String decrypt(int k, String s) {
		return s.chars().map(i -> (i - 'a' - k + 26) % 26 + 'a').mapToObj(i -> Character.valueOf((char) i)).map(c -> Character.toString(c))
				.collect(Collectors.joining());
	}

	public static void main(String[] args) {

		int k = 25;
		System.out.println(encrypt(k, "cryptoisfun"));
		System.out.println(decrypt(k, encrypt(k, "cryptoisfun")));

	}

}
