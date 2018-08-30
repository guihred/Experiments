package exercism;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
public class AnagramTest {
	@Test
	public void testNoMatches() {
		Anagram detector = new Anagram("diaper");
        Assert.assertTrue("It should not match",
                detector.match(Arrays.asList("hello", "world", "zombies", "pants")).isEmpty());
	}


	@Test
	public void testSimpleAnagram() {
		Anagram detector = new Anagram("ant");
		List<String> anagram = detector.match(Arrays.asList("tan", "stand", "at"));
        Assert.assertTrue("There should be only one match", anagram.equals(Arrays.asList("tan")));
	}


	@Test
	public void testDetectMultipleAnagrams() {
		Anagram detector = new Anagram("master");
		List<String> anagrams = detector.match(Arrays.asList("stream", "pigeon", "maters"));
        Assert.assertTrue("It should contain both", anagrams.containsAll(Arrays.asList("maters", "stream")));
	}


	@Test
	public void testDoesNotConfuseDifferentDuplicates() {
		Anagram detector = new Anagram("galea");
		List<String> anagrams = detector.match(Arrays.asList("eagle"));
        assertTrue("It should be empty", anagrams.isEmpty());
	}


	@Test
	public void testIdenticalWordIsNotAnagram() {
		Anagram detector = new Anagram("corn");
		List<String> anagrams = detector.match(Arrays.asList("corn", "dark", "Corn", "rank", "CORN", "cron", "park"));
        assertTrue("There should be only one match", anagrams.equals(Arrays.asList("cron")));
	}


	@Test
	public void testEliminateAnagramsWithSameChecksum() {
		Anagram detector = new Anagram("mass");
        assertTrue("Same checksum does not influence", detector.match(Arrays.asList("last")).isEmpty());
	}


	@Test
	public void testEliminateAnagramSubsets() {
		Anagram detector = new Anagram("good");
        assertTrue("There should be no matches", detector.match(Arrays.asList("dog", "goody")).isEmpty());
	}


	@Test
	public void testDetectAnagrams() {
		Anagram detector = new Anagram("listen");
		List<String> anagrams = detector.match(Arrays.asList("enlists", "google", "inlets", "banana"));
        assertTrue("Should contain the word", anagrams.contains("inlets"));
	}


	@Test
	public void testMultipleAnagrams() {
		Anagram detector = new Anagram("allergy");
		List<String> anagrams = detector.match(Arrays.asList("gallery", "ballerina", "regally", "clergy", "largely", "leading"));
        assertTrue("Word should be in list", anagrams.contains("gallery"));
        assertTrue("Word should be in list", anagrams.contains("largely"));
        assertTrue("Word should be in list", anagrams.contains("regally"));
	}


	@Test
	public void testAnagramsAreCaseInsensitive() {
		Anagram detector = new Anagram("Orchestra");
		List<String> anagrams = detector.match(Arrays.asList("cashregister", "Carthorse", "radishes"));
        assertTrue("Case insensitive did not work", anagrams.contains("Carthorse"));
	}

}