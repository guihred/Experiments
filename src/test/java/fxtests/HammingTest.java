package fxtests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import exercism.Hamming;
import org.junit.Test;

@SuppressWarnings("static-method")
public class HammingTest {

    @Test
	public void testCompleteHammingDistanceForSmallStrand() {
        assertThat("", Hamming.compute("AG", "CT"), is(2));
	}


	@Test
	public void testCompleteHammingDistanceOfForSingleNucleotideStrand() {
        assertThat("", Hamming.compute("A", "G"), is(1));
	}


	@Test
	public void testHammingDistanceInVeryLongStrand() {
        assertThat("", Hamming.compute("GGACGGATTCTG", "AGGACGGATTCT"), is(9));
	}


	@Test
	public void testLargeHammingDistance() {
        assertThat("", Hamming.compute("GATACA", "GCATAA"), is(4));
	}


	@Test
	public void testNoDifferenceBetweenIdenticalStrands() {
        assertThat("", Hamming.compute("A", "A"), is(0));
	}


	@Test
	public void testSmallHammingDistance() {
        assertThat("", Hamming.compute("AT", "CT"), is(1));
	}


	@Test
	public void testSmallHammingDistanceInLongerStrand() {
        assertThat("", Hamming.compute("GGACG", "GGTCG"), is(1));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testValidatesFirstStrandNotLonger() {
		Hamming.compute("AAAG", "AAA");
	}


	@Test(expected = IllegalArgumentException.class)
	public void testValidatesOtherStrandNotLonger() {
		Hamming.compute("AAA", "AAAG");
	}

}