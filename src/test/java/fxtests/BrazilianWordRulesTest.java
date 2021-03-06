package fxtests;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import neuro.BrazilianWordSyllableSplitter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

@SuppressWarnings("static-method")
public class BrazilianWordRulesTest {

    private static final Logger LOG = HasLogging.log();

    @Test
    public void testEverySyllableMustHaveAtMost3Vowels() throws IOException {
        Stream<String> words =
                getWords(ResourceFXUtils.toURI("words.dic"), s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü]{3,}.*"));
        Pattern compile = Pattern.compile("(?i)([aeiouáéíóúâêîôûàèìòùãõüyÁÉÍÓÚÂÊÎÔÛÀÈÌÒÙÃÕ])");
        words.forEach(w -> {
            String syllables = BrazilianWordSyllableSplitter.splitSyllables(w);
            String[] syllableParts = syllables.split("-");
            // Ignore word with hyphen and words without a vowel
            if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
                Assert.assertTrue("Every syllable must have at most 3 vowels word=" + w + " sylables=" + syllables,
                        Stream.of(syllableParts).allMatch(s -> countMatches(compile, s) <= 3));
            }
        });
    }

    @Test
    public void testEverySyllableMustHaveAVowel() throws IOException {
        Stream<String> words = getWords(ResourceFXUtils.toURI("words.dic"));
        words.forEach(w -> {
            String syllables = BrazilianWordSyllableSplitter.splitSyllables(w);
            String[] syllableSplit = syllables.split("-");
            // Ignore word with hyphen and words without a vowel
            if (!w.contains("-") && w.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõyü].*")) {
                Assert.assertTrue("Every syllable must have at least a vowel word=" + w + " sylables=" + syllables,
                        Stream.of(syllableSplit)
                                .allMatch(s -> s.matches("(?i).*[aeiouáéíóúâêîôûàèìòùãõüyÁÉÍÓÚÂÊÎÔÛÀÈÌÒÙÃÕ].*")));
            }
        });
    }

    @Test
    public void testWords() {
        try {
            Stream<String> words = getWords(ResourceFXUtils.toURI("pt_PT.dic"));
            Map<String, Set<String>> wordAttributes = words.filter(e -> e.contains("\t"))
                    .map(e -> e.replaceAll(".+\t\\[(\\$\\.+\\$)*(.+)\\]", "$2")).flatMap(e -> Stream.of(e.split(",")))
                    .collect(Collectors.groupingBy(e -> e.split("=")[0].replaceAll("\\$.+\\$", ""),
                            Collectors.mapping(e -> e.split("=")[1].replaceAll("\\$[A-Z]+", ""), Collectors.toSet())));
            // T
            // [inf, ppa, pp, c, f, ip, i, pic, p, pmp, pc, pi, fc]
            List<String> a =
                    Arrays.asList("inf", "ppa", "pp", "c", "f", "ip", "i", "pic", "p", "pmp", "pc", "pi", "fc");
            Assert.assertTrue("Deve conter todos os tempos", wordAttributes.get("T").containsAll(a));
        } catch (IOException e) {
            LOG.error("", e);
        }

    }

    private static int countMatches(Pattern compile, String w) {
        Matcher matcher = compile.matcher(w);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
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