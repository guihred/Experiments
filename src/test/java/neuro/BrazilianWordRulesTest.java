package neuro;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import simplebuilder.ResourceFXUtils;

public class BrazilianWordRulesTest {


	@Test
	public void testEverySyllableMustHaveAtMost3Vowels() throws IOException {
		Stream<String> words = getWords(ResourceFXUtils.toURI("words.dic"),
				s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü]{3,}.*"));
		Pattern compile = Pattern.compile("(?i)([aeiouáéíóúâêîôûàèìòùãõüyÁÉÍÓÚÂÊÎÔÛÀÈÌÒÙÃÕ])");
		words.forEach(w -> {
			String syllables = BrazilianWordSyllableSplitter.splitSyllables(w);
			String[] split = syllables.split("-");
			// Ignore word with hyphen and words without a vowel
			if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
				Assert.assertTrue("Every syllable must have at most 3 vowels word=" + w + " sylables=" + syllables,
						Stream.of(split).allMatch(s -> countMatches(compile, s) <= 3));
			}
		});
	}

	private static int countMatches(Pattern compile, String w) {
		Matcher matcher = compile.matcher(w);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	@Test
	public void testEverySyllableMustHaveAVowel() throws IOException {
		Stream<String> words = getWords(ResourceFXUtils.toURI("words.dic"));
		words.forEach(w -> {
			String syllables = BrazilianWordSyllableSplitter.splitSyllables(w);
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


	private static Stream<String> getWords(URI txtFile) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
				.filter(s -> !s.isEmpty()).distinct();
	}

	private static Stream<String> getWords(URI txtFile, Predicate<? super String> predicate) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential().map(String::trim)
				.filter(s -> !s.isEmpty()).filter(predicate).distinct();
	}

}