package fxtests;

import com.google.common.collect.ImmutableMap;
import exercism.DNA;
import org.junit.Assert;
import org.junit.Test;

public class DNATest {

	@Test
	public void testEmptyDnaStringHasNoAdenosine() {
		DNA dna = new DNA("");
        Assert.assertEquals("", 0, dna.count('A'));
	}


	@Test
	public void testEmptyDnaStringHasNoNucleotides() {
		DNA dna = new DNA("");

		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 0L).put('C', 0L).put('G', 0L).put('T', 0L).build();
        Assert.assertEquals("", dna.getNucleotideCounts(), build);
	}


	@Test
	public void testRepetitiveCytidineGetsCounted() {
		DNA dna = new DNA("CCCCC");
        Assert.assertEquals("", 5, dna.count('C'));
	}


	@Test
	public void testRepetitiveSequenceWithOnlyGuanosine() {
		DNA dna = new DNA("GGGGGGGG");
		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 0L).put('C', 0L).put('G', 8L).put('T', 0L).build();

        Assert.assertEquals("", dna.getNucleotideCounts(), build);
	}


	@Test
	public void testCountsOnlyThymidine() {
		DNA dna = new DNA("GGGGGTAACCCGG");
        Assert.assertEquals("", 1, dna.count('T'));
	}


	@Test
	public void testCountsANucleotideOnlyOnce() {
		DNA dna = new DNA("CGATTGGG");
		dna.count('T');
        Assert.assertEquals("", 2, dna.count('T'));
	}


	@Test
	public void testDnaCountsDoNotChangeAfterCountingAdenosine() {
		DNA dna = new DNA("GATTACA");
		dna.count('A');

		ImmutableMap<Character, Long> build = ImmutableMap.<Character, Long> builder().put('A', 3L).put('C', 1L).put('G', 1L).put('T', 2L).build();
        Assert.assertEquals("", dna.getNucleotideCounts(), build);
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
        Assert.assertEquals("", dna.getNucleotideCounts(), build);
	}
}