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
public class Crypto {

	private String plainText;

	public Crypto(String plainText) {
		this.plainText = plainText;

	}

	public String getCipherText() {
		List<String> plaintextSegments = getPlaintextSegments();
		StringBuilder a = new StringBuilder();
		int squareSize = getSquareSize();
		for (int i = 0; i < squareSize; i++) {
			for (String string : plaintextSegments) {
				if (i < string.length()) {
					a.append(Character.toString(string.charAt(i)));
				}
			}
		}

		return a.toString();
	}

	public String getNormalizedCipherText() {
		String cipherText = getCipherText();

		int sqrtCipher = (int) Math.floor(Math.sqrt(cipherText.length()));
        String[] parts = cipherText.split("(?<=\\G.{" + sqrtCipher + "})");
        if (parts.length > 1) {
            int length = parts[parts.length - 1].length();
			while (length < sqrtCipher - 1) {
				for (int i = sqrtCipher - length-1,j=0; i > 0; i--,j++) {
                    String a = parts[parts.length - 2 - j];
                    String b = parts[parts.length - 1 - j];
                    parts[parts.length - 2 - j] = a.substring(0, a.length() - 1);
                    parts[parts.length - 1 - j] = a.substring(a.length() - 1, a.length()) + b;
				}
                length = parts[parts.length - 1].length();
			}
		}

        return Stream.of(parts).collect(Collectors.joining(" "));
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

	public int getSquareSize() {
		return (int) Math.ceil(Math.sqrt(getNormalizedPlaintext().length()));
	}

}