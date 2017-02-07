package exercism;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class Crypto.
 * 
 * Implement the classic method for composing secret messages called a square
 * code.
 * 
 * Given an English text, output the encoded version of that text.
 * 
 * First, the input is normalized: the spaces and punctuation are removed from
 * the English text and the message is downcased.
 * 
 * Then, the normalized characters are broken into rows. These rows can be
 * regarded as forming a rectangle when printed with intervening newlines.
 */
class Crypto {

	private String plainText;

	public Crypto(String plainText) {
		this.plainText = plainText;

	}

	public String getNormalizedPlaintext() {
		return plainText.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	public List<String> getPlaintextSegments() {
		String normalizedPlaintext = getNormalizedPlaintext();
		String a = normalizedPlaintext;
		int squareSize = getSquareSize();

		return Arrays.asList(a.split("(?<=\\G.{" + squareSize + "})"));
	}

	public String getCipherText() {
		List<String> plaintextSegments = getPlaintextSegments();
		String a = "";
		int squareSize = getSquareSize();
		for (int i = 0; i < squareSize; i++) {
			for (String string : plaintextSegments) {
				if (i < string.length()) {
					a += string.charAt(i);
				}
			}
		}

		return a;
	}

	public int getSquareSize() {
		return Double.valueOf(Math.ceil(Math.sqrt(getNormalizedPlaintext().length()))).intValue();
	}

	public String getNormalizedCipherText() {
		String cipherText = getCipherText();

		int intValue = Double.valueOf(Math.floor(Math.sqrt(cipherText.length()))).intValue();

		return Stream.of(cipherText.split("(?<=\\G.{" + intValue + "})")).collect(Collectors.joining(" "));
	}

}