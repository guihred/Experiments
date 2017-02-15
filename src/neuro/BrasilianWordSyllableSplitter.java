package neuro;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrasilianWordSyllableSplitter {
	private static final Logger LOGGER = LoggerFactory.getLogger(BrasilianWordSyllableSplitter.class);
	private static final String VOWELS = "[aeiouáéíóúâêîôûàèìòùãõ]";
	private static final String CONSONANT_CLUSTER = "[bcdfgkptv][rl]|[cnlst][h]|mn|bs|tch";
	private static final String REGEX_VOWEL_CLUSTER_VOWEL = "(?i)" + VOWELS + "(" + CONSONANT_CLUSTER + ")" + VOWELS;
	private static final String REGEX_VOWEL_CONSONANT_VOWEL = "(?i)" + VOWELS + "[bçcdfghjklmnpqrstvwxyz]" + VOWELS;
	private static final String REGEX_CONSONANT_CLUSTER = "(?i)" + CONSONANT_CLUSTER;
	private static final String REGEX_CONSONANTS = "(?i)[bçcdfghjklmnpqrstvwxyz]";
	// notice the consonants do not include 'q'
	private static final String REGEX_ASCENDING_DIPHTHONG = "(?i)[bçcdfghjklmnprstvwxyz]*([iu][aeo]|iu|[eo]a|eo|ee|oo)[ms]*";
	private static final String REGEX_HAS_ACCENT = "(?i).*[áéíóúâêîôû].*";
	private static final String REGEX_VOWEL = "(?i)" + VOWELS;
	private static final String REGEX_DIPHTHONG = "(?i)(?<=[aeiou])(?=[aeiou])";
	// cases when the vowel cluster should be split
	private static final String REGEX_HIATUS = "(?i)(?<=[^qg][aeiou])(?=[aeiou][lnrz])|(?<=[aeou])(?=[íúîû])|(?<=[íúéóe])(?=[a])|(?<=[ieao])(?=[ú])|(?<=[^q][aeoui][ui])(?=[aeiou])|(?<=[íúe])(?=ei)";

	private BrasilianWordSyllableSplitter() {
	}

	public static void main(String[] args) {

		try {
			Stream<String> words;
			words = getWords(new File("words.dic").toURI());
			words.forEach(BrasilianWordSyllableSplitter::splitSyllables);
		} catch (IOException e) {
			LOGGER.error("", e);
		}

	}

	private static Stream<String> getWords(URI txtFile) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).sequential()
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.distinct();
	}

	public static String splitSyllables(String word) {

		int length = word.length();
		List<String> syllable = new ArrayList<>();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < length; i++) {
			String a = word.substring(i, i + 1);
			if ("-".equals(a)) {
				syllable.add(s.toString());
				s.delete(0, s.length());
				continue;
			}
			s.append(a);
			String b = i + 2 <= length ? word.substring(i + 1, i + 2) : "";
			String c = i + 3 <= length ? word.substring(i + 2, i + 3) : "";
			String d = i + 4 <= length ? word.substring(i + 3, i + 4) : "";
			if (splitSyllableCondition(i, a, b, c, d)) {
				syllable.add(s.toString());
				s.delete(0, s.length());
			}
		}
		syllable.add(s.toString());
		if (!hasAccent(word)) {
			String sy = syllable.get(syllable.size() - 1);
			if (isAscending(sy)) {
				syllable.remove(syllable.size() - 1);
				String[] split = sy.split(REGEX_DIPHTHONG);
				syllable.add(split[0]);
				syllable.add(split[1]);
			}
		}

		String collect = syllable.stream().flatMap(sy -> Stream.of(sy.split(REGEX_HIATUS)))
				.collect(Collectors.joining("-"));

		System.out.println(word + " " + collect);
		return collect;

	}

	private static boolean splitSyllableCondition(int i, String a, String b, String c, String d) {
		return (a + b + c).matches(REGEX_VOWEL_CONSONANT_VOWEL)
				|| isConsonant(a) && isConsonant(b) && !isConsonantCluster(a + b) && isVowel(c) && i != 0
				|| isConsonant(a) && isConsonant(b)&& isConsonant(c) && !isConsonantCluster(a + b) && isConsonantCluster(b + c) && isVowel(d)
				&& i != 0
				|| (a + b + c + d).matches(REGEX_VOWEL_CLUSTER_VOWEL);
	}

	private static boolean isConsonantCluster(String a) {
		return a.matches(REGEX_CONSONANT_CLUSTER);
	}
	private static boolean isConsonant(String b) {
		return b.matches(REGEX_CONSONANTS);
	}

	private static boolean isAscending(String b) {
		return b.matches(REGEX_ASCENDING_DIPHTHONG);
	}

	private static boolean isVowel(String a) {
		return a.matches(REGEX_VOWEL);
	}

	private static boolean hasAccent(String a) {
		return a.matches(REGEX_HAS_ACCENT);
	}


}