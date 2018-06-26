package neuro;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    @SuppressWarnings("boxing")
    public static void main(String[] args) throws IOException {
        Stream<String> words = getWords(ResourceFXUtils.toURI("pt_PT.dic"));
        Map<String, Set<String>> collect = words.filter(e -> e.contains("\t"))
                .map(e -> e.replaceAll(".+\t\\[(\\$\\.+\\$)*(.+)\\]", "$2"))
                .flatMap(e -> Stream.of(e.split(",")))
                .collect(Collectors.groupingBy(e -> e.split("=")[0].replaceAll("\\$.+\\$", ""),
                        Collectors.mapping(e -> e.split("=")[1].replaceAll("\\$[A-Z]+", ""), Collectors.toSet())));
        collect.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue().size())).forEach(e -> {
            //            System.out.println(e.getKey());
            //            System.out.println("\t" + e.getValue());
            //            e.getValue().forEach(v -> System.out.println("\t" + v));
        });
        //        T
        //        [inf, ppa, pp, c, f, ip, i, pic, p, pmp, pc, pi, fc]
        List<String> a = Arrays.asList("inf", "ppa", "pp", "c", "f", "ip", "i", "pic", "p", "pmp", "pc", "pi", "fc");
        for (String s : a) {

            System.out.println(s);
            getWords(ResourceFXUtils.toURI("pt_PT.dic"))
                    .filter(e -> e.contains("T=" + s))
                    .map(e -> e.split("\t")[0].replaceAll("/\\w+", ""))
                    .forEach(e -> {
                        System.out.print(e + " ");
                    });

            System.out.println();

        }

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