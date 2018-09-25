package exercism;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Class DNA.
 * 
 * Given a DNA string, compute how many times each nucleotide occurs in the
 * string.
 * 
 * DNA is represented by an alphabet of the following symbols: 'A', 'C', 'G',
 * and 'T'.
 * 
 * Each symbol represents a nucleotide, which is a fancy name for the particular
 * molecules that happen to make up a large part of DNA.
 * 
 * DNA contains four types of them: adenine (A), cytosine (C), guanine (G), and
 * thymine (T).
 * 
 * RNA contains a slightly different set of nucleotides, but we don't care about
 * that for now.
 */
public class DNA {

	/** The Constant DNA_NUCLEOTIDES. */
	private static final List<Character> DNA_NUCLEOTIDES = Arrays.asList('A', 'G', 'C', 'T');

	/** The nucleotide counts. */
	private Map<Character, Long> nucleotideCounts;

	/**
	 * Instantiates a new dna.
	 *
	 * @param string
	 *            the string
	 */
	public DNA(String string) {
        Map<Character, Long> charHistogram = string.chars().mapToObj(c -> Character.valueOf((char) c))
				.collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Map<Character, Long> nucleoCounts = new HashMap<>();
        charHistogram.forEach(nucleoCounts::put);
        DNA_NUCLEOTIDES.forEach(a -> nucleoCounts.putIfAbsent(a, 0L));
        nucleotideCounts = nucleoCounts;

	}

	/**
	 * Count.
	 *
	 * @param c
	 *            the c
	 * @return the long
	 */
	public long count(char c) {
		if (!DNA_NUCLEOTIDES.contains(c)) {
			throw new IllegalArgumentException();
		}

		return nucleotideCounts.get(c);
	}

	/**
	 * Nucleotide counts.
	 *
	 * @return the map
	 */
	public Map<Character, Long> getNucleotideCounts() {
		return nucleotideCounts;
	}

}