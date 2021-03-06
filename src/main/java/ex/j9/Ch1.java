package ex.j9;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class Ch1 {
    private static final Logger LOG = HasLogging.log();
	/**
	 * 13. Write a program that prints a lottery combination, picking six distinct
	 * numbers between 1 and 50. To pick six distinct numbers, start with an array
	 * list filled with1 ... 50. Pick a random index and remove the element. Repeat
	 * six times. Print the result in sorted order.
	 */
    private static final Random RANDOM = new Random();

	private Ch1() {
    }

	/**
	 * 16. Improve the average method so that it is called with at least one
	 * parameter.
	 */

	public static double average(double... i) {
		return DoubleStream.of(i).average().orElse(0);
	}

	/**
	 * 4. Write a program that prints the smallest and largest positive double
	 * values. Hint: Look up Math.nextUp in the Java API.
	 */
	public static void extremeDoubles() {
		LOG.info("{}", Double.MAX_VALUE);
		LOG.info("{}", Math.nextUp(0));

	}


	/**
	 * 6. Write a program that computes the factorial n! = 1 × 2 × ... × n, using
	 * BigInteger. Compute the factorial of 1000.
	 * 
	 * @return the factorial of n
	 * 
	 */
	public static BigInteger factorial(int n) {
		return Stream.iterate(BigInteger.ONE, BigInteger.ONE::add).limit(n).parallel().reduce(BigInteger.ONE,
				BigInteger::multiply);
	}
	public static List<Integer> lotteryCombination() {
		return IntStream.generate(() -> RANDOM.nextInt(50)).distinct().limit(6).sorted().boxed().collect(toList());
	}

	/**
	 * 15. Write a program that stores Pascal's triangle up to a given n in an
	 * List<List<Integer>> .
	 */
	public static List<List<Integer>> pascalTriangle(int n) {
		return Stream.iterate(Arrays.asList(1), Ch1::mapPascal).limit(n).collect(Collectors.toList());
	}

	/**
	 * 10. Write a program that produces a random string of letters and digits by
	 * generating a random long value and printing it in base 36.
	 */
	public static String randomLetters() {

		long nextLong = RANDOM.nextLong();
		return Long.toString(abs(nextLong), Character.MAX_RADIX);
	}

	private static List<Integer> mapPascal(List<Integer> previousPascal) {
		List<Integer> pascal = Stream.concat(Stream.of(1), previousPascal.stream()).collect(Collectors.toList());
		for (int i = 1; i < pascal.size() - 1; i++) {
			pascal.set(i, previousPascal.get(i) + previousPascal.get(i - 1));
		}
		return pascal;
	}

}
