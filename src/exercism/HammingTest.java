package exercism;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HammingTest {

	@Test
	public void testNoDifferenceBetweenIdenticalStrands() {
		assertThat(Hamming.compute("A", "A"), is(0));
	}


	@Test
	public void testCompleteHammingDistanceOfForSingleNucleotideStrand() {
		assertThat(Hamming.compute("A", "G"), is(1));
	}


	@Test
	public void testCompleteHammingDistanceForSmallStrand() {
		assertThat(Hamming.compute("AG", "CT"), is(2));
	}


	@Test
	public void testSmallHammingDistance() {
		assertThat(Hamming.compute("AT", "CT"), is(1));
	}


	@Test
	public void testSmallHammingDistanceInLongerStrand() {
		assertThat(Hamming.compute("GGACG", "GGTCG"), is(1));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testValidatesFirstStrandNotLonger() {
		Hamming.compute("AAAG", "AAA");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testValidatesOtherStrandNotLonger() {
		Hamming.compute("AAA", "AAAG");
	}


	@Test
	public void testLargeHammingDistance() {
		assertThat(Hamming.compute("GATACA", "GCATAA"), is(4));
	}


	@Test
	public void testHammingDistanceInVeryLongStrand() {
		assertThat(Hamming.compute("GGACGGATTCTG", "AGGACGGATTCT"), is(9));
	}

}

class Hamming {

	public static int compute(String s1, String s2) {
		if (s1.length() != s2.length()) {
			throw new IllegalArgumentException();
		}
		int count = 0;
		for (int i = 0; i < s1.length(); i++) {
			char charAt = s1.charAt(i);
			char charAt2 = s2.charAt(i);
			if (charAt != charAt2) {
				count++;
			}
		}
		return count;
	}
}