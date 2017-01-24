package exercism;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
public class AnagramTest {

	@Test
	public void testNoMatches() {

		Anagram detector = new Anagram("diaper");
		Assert.assertTrue(detector.match(Arrays.asList("hello", "world", "zombies", "pants")).isEmpty());
	}


	@Test
	public void testSimpleAnagram() {
		Anagram detector = new Anagram("ant");
		List<String> anagram = detector.match(Arrays.asList("tan", "stand", "at"));
		Assert.assertTrue(anagram.equals(Arrays.asList("tan")));
	}


	@Test
	public void testDetectMultipleAnagrams() {
		Anagram detector = new Anagram("master");
		List<String> anagrams = detector.match(Arrays.asList("stream", "pigeon", "maters"));
		Assert.assertTrue(anagrams.contains("maters") && anagrams.contains("stream"));
	}


	@Test
	public void testDoesNotConfuseDifferentDuplicates() {
		Anagram detector = new Anagram("galea");
		List<String> anagrams = detector.match(Arrays.asList("eagle"));
		assertTrue(anagrams.isEmpty());
	}


	@Test
	public void testIdenticalWordIsNotAnagram() {
		Anagram detector = new Anagram("corn");
		List<String> anagrams = detector.match(Arrays.asList("corn", "dark", "Corn", "rank", "CORN", "cron", "park"));
		assertTrue(anagrams.equals(Arrays.asList("cron")));
	}


	@Test
	public void testEliminateAnagramsWithSameChecksum() {
		Anagram detector = new Anagram("mass");
		assertTrue(detector.match(Arrays.asList("last")).isEmpty());
	}


	@Test
	public void testEliminateAnagramSubsets() {
		Anagram detector = new Anagram("good");
		assertTrue(detector.match(Arrays.asList("dog", "goody")).isEmpty());
	}


	@Test
	public void testDetectAnagrams() {
		Anagram detector = new Anagram("listen");
		List<String> anagrams = detector.match(Arrays.asList("enlists", "google", "inlets", "banana"));
		assertTrue(anagrams.contains("inlets"));
	}


	@Test
	public void testMultipleAnagrams() {
		Anagram detector = new Anagram("allergy");
		List<String> anagrams = detector.match(Arrays.asList("gallery", "ballerina", "regally", "clergy", "largely", "leading"));
		assertTrue(anagrams.contains("gallery"));
		assertTrue(anagrams.contains("largely"));
		assertTrue(anagrams.contains("regally"));
	}


	@Test
	public void testAnagramsAreCaseInsensitive() {
		Anagram detector = new Anagram("Orchestra");
		List<String> anagrams = detector.match(Arrays.asList("cashregister", "Carthorse", "radishes"));
		assertTrue(anagrams.contains("Carthorse"));
	}

}

class Anagram {
	private Map<String, Long> histogram;
	private String original;

	public Anagram(String st) {
		original = st;
		histogram = createHistogram(st);
	}

	private Map<String, Long> createHistogram(String st) {
		return Stream.of(st.toLowerCase().split("")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public List<String> match(List<String> listOfAnagram) {
		return listOfAnagram.stream().filter(this::isAnagram).collect(Collectors.toList());
	}

	private boolean isAnagram(String s) {
		return histogram.equals(createHistogram(s)) && !s.equalsIgnoreCase(original);
	}
}