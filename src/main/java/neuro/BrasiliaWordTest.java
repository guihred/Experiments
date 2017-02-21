package neuro;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import simplebuilder.ResourceFXUtils;

public class BrasiliaWordTest {
	@Test
	public void testSomeOccurrences() throws IOException {
		List<String> words = Arrays.asList("ex-ce-ção", "des-cer", "ter-ra", "pás-sa-ro", "guer-ra", "ni-nho",
				"chu-va", "quei-jo", "ca-de-a-do", "ju-í-za", "La-ís", "Pa-ra-guai", "a-ve-ri-guei", "cai-xa",
				"fei-xe", "flau-ta", "pra-to", "ap-to", "ab-dô-men", "cír-cu-lo", "pneu-mo-ni-a", "pneu-má-ti-co",
				"psi-có-lo-go");
		for (String word : words) {
			String splitSyllables = BrasilianWordSyllableSplitter.splitSyllables(word.replaceAll("-", ""));
			Assert.assertEquals("The syllables don't match", word, splitSyllables);
		}
	}

	@Test
	public void testOtherOccurrences() throws IOException {
		List<String> words = Arrays.asList("au-tô-no-mo", "tui-ui-ú", "ou-to-no", "di-nhei-ro", "sal-dar",
				"dês-mai-a-do", "U-ru-guai", "i-guais", "quais-quer", "u-ru-guai-a-na", "prai-a", "tei-a", "joi-a",
				"sa-bo-rei-e", "es-tei-o", "ar-roi-o", "con-lui-o", "a-mên-do", "ca-a-tin-ga", "sa-ú-de", "flu-ir",
				"chu-va", "mo-lha", "es-ta-nho", "guel-ra", "a-que-la", "to-cha", "fi-lha", "ni-nho", "que-rer",
				"guei-xa", "bar-ro", "as-sun-to", "des-cer", "nas-ço", "es-xu-dar", "ex-ce-to", "car-ro", "nas-cer",
				"dês-ço", "ex-ces-so", "ab-do-me", "sub-ma-ri-no", "ap-ti-dão", "dig-no", "con-vic-ção", "as-tu-to",
				"ap-to", "cír-cu-lo", "ad-mi-tir", "ob-tu-rar", "a-pli-ca-ção", "a-pre-sen-tar", "a-brir", "re-tra-to",
				"de-ca-tlo", "gnós-ti-co", "pneu-má-ti-co", "mne-mô-ni-co", "Sa-a-ra", "com-pre-en-do", "xi-i-ta",
				"vo-o", "pa-ra-cu-u-ba", "oc-ci-pi-tal", "in-te-lec-ção", "de-sa-ten-to", "di-sen-te-ri-a",
				"tran-sa-tlân-ti-co", "su-ben-ten-di-do", "su-ben-ten-der", "dis-fun-ção",
				"di-sen-te-ri-a", "su-per-mer-ca-do", "su-pe-ra-mi-go");
		for (String word : words) {
			String splitSyllables = BrasilianWordSyllableSplitter.splitSyllables(word.replaceAll("-", ""));
			Assert.assertEquals("The syllables don't match", word, splitSyllables);
		}
	}

	@Test
	public void testEverySyllableMustHaveAVowel() throws IOException {
		Stream<String> words = getWords(ResourceFXUtils.toURI("words.dic"));
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
	public void testEverySyllableMustAtMost3Vowels() throws IOException {
		Stream<String> words = getWords(ResourceFXUtils.toURI("words.dic"),
				s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü]{3,4}.*"));
		words.forEach(w -> {
			String syllables = BrasilianWordSyllableSplitter.splitSyllables(w);
			String[] split = syllables.split("-");
			// Ignore word with hyphen and words without a vowel
			if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
				Assert.assertTrue("Every syllable must have at least a vowel word=" + w + " sylables=" + syllables,
						Stream.of(split).allMatch(
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
