package exercism;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class NucleotideTest {

	@Test
	public void testEmptyDnaStringHasNoAdenosine() {
		DNA dna = new DNA("");
		Assert.assertEquals(dna.count('A'), 0);
	}


	@Test
	public void testEmptyDnaStringHasNoNucleotides() {
		DNA dna = new DNA("");

		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 0L).put('C', 0L).put('G', 0L).put('T', 0L).build();
		Assert.assertEquals(dna.nucleotideCounts(), build);
	}


	@Test
	public void testRepetitiveCytidineGetsCounted() {
		DNA dna = new DNA("CCCCC");
		Assert.assertEquals(dna.count('C'), 5);
	}


	@Test
	public void testRepetitiveSequenceWithOnlyGuanosine() {
		DNA dna = new DNA("GGGGGGGG");
		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 0L).put('C', 0L).put('G', 8L).put('T', 0L).build();

		Assert.assertEquals(dna.nucleotideCounts(), build);
	}


	@Test
	public void testCountsOnlyThymidine() {
		DNA dna = new DNA("GGGGGTAACCCGG");
		Assert.assertEquals(dna.count('T'), 1);
	}


	@Test
	public void testCountsANucleotideOnlyOnce() {
		DNA dna = new DNA("CGATTGGG");
		dna.count('T');
		Assert.assertEquals(dna.count('T'), 2);
	}


	@Test
	public void testDnaCountsDoNotChangeAfterCountingAdenosine() {
		DNA dna = new DNA("GATTACA");
		dna.count('A');

		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 3L).put('C', 1L).put('G', 1L).put('T', 2L).build();
		Assert.assertEquals(dna.nucleotideCounts(), build);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testValidatesNucleotides() {
		DNA dna = new DNA("GACT");
		dna.count('X');
	}


	@Test
	public void testCountsAllNucleotides() {
		String s = "AGCTTTTCATTCTGACTGCAACGGGCAATATGTCTCTGTGTGGATTAAAAAAAGAGTGTCTGATAGCAGC";
		DNA dna = new DNA(s);
		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 20L).put('C', 12L).put('G', 17L).put('T', 21L)
				.build();
		Assert.assertEquals(dna.nucleotideCounts(), build);
	}
}

class DNA {

	private static final List<Character> DNA_NUCLEOTIDES = Arrays.asList('A', 'G', 'C', 'T');
	private Map<Character, Long> nucleotideCounts;

	public DNA(String string) {
		Map<Character, Long> collect = string.chars().mapToObj(c -> Character.valueOf((char) c))
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Map<Character, Long> hashMap = new HashMap<>();
		collect.forEach((c, n) -> {
			hashMap.put(c, n);
		});
		DNA_NUCLEOTIDES.forEach(a -> {
			hashMap.putIfAbsent(a, 0L);
		});
		nucleotideCounts = hashMap;

	}

	public long count(char c) {
		if (!DNA_NUCLEOTIDES.contains(c)) {
			throw new IllegalArgumentException();
		}

		return nucleotideCounts.get(c);
	}

	public Map<Character, Long> nucleotideCounts() {
		return nucleotideCounts;
	}

}