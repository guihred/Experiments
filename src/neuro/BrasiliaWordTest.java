package neuro;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class BrasiliaWordTest {
	@Test
	public void testEverySyllableMustHaveAVowel() throws IOException {
		Stream<String> words = getWords(new File("words.dic").toURI());
		words.forEach(w -> {
			String syllables = BrasilianWordSyllableSplitter.splitSyllables(w);
			String[] split = syllables.split("-");
			// Ignore word with hyphen and words without a vowel
			if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
				Assert.assertTrue("Every syllable must have at least a vowel word=" + w + " sylables=" + syllables,
						Stream.of(split)
								.allMatch(s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõüyÁÉÍÓÚÂÊÎÔÛÀÈÌÒÙÃÕ].*")));
			}
		}
		);
	}

	@Test
	public void testEverySyllableMustAtMost4Vowels() throws IOException {
		Stream<String> words = getWords(new File("words.dic").toURI(),
				s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü]{3,4}.*"));
		words.forEach(w -> {
			String syllables = BrasilianWordSyllableSplitter.splitSyllables(w);
			String[] split = syllables.split("-");
			// Ignore word with hyphen and words without a vowel
			if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
				Assert.assertTrue("Every syllable must have at least a vowel word=" + w + " sylables=" + syllables,
						Stream.of(split)
.allMatch(
								s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõüyÁÉÍÓÚÂÊÎÔÛÀÈÌÒÙÃÕ]{1,3}.*")));
			}
		});
	}

	private static Stream<String> getWords(URI txtFile) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
				.filter(s -> !s.isEmpty()).distinct();
	}

	private static Stream<String> getWords(URI txtFile, Predicate<? super String> predicate) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
				.filter(s -> !s.isEmpty()).filter(predicate).distinct();
	}
}
