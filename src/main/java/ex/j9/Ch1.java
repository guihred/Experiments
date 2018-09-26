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
import utils.HasLogging;

public class Ch1 {

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
        Logger log = HasLogging.log(Ch1.class);

        log.info("{}", Double.MAX_VALUE);
        log.info("{}", Math.nextUp(0));

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


	/**
	 * 13. Write a program that prints a lottery combination, picking six distinct
	 * numbers between 1 and 49. To pick six distinct numbers, start with an array
	 * list filled with1 ... 49. Pick a random index and remove the element. Repeat
	 * six times. Print the result in sorted order.
	 */
	public static List<Integer> lotteryCombination() {
		Random random = new Random();
		return IntStream.generate(() -> random.nextInt(49) + 1).distinct().limit(6).sorted().boxed().collect(toList());
	}

	public static void main(String[] args) {
		extremeDoubles();
        // System.out.println(factorial(1000))
        // System.out.println(lotteryCombination())
        // System.out.println(pascalTriangle(10))
        // System.out.println(average(1,2,3,4,5,6,7,8))
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

		Random random = new Random();
		long nextLong = random.nextLong();
        return Long.toString(abs(nextLong), 36);
	}

	private static List<Integer> mapPascal(List<Integer> previousPascal) {
		List<Integer> pascal = Stream.concat(Stream.of(1), previousPascal.stream()).collect(Collectors.toList());
		for (int i = 1; i < pascal.size() - 1; i++) {
			pascal.set(i, previousPascal.get(i) + previousPascal.get(i - 1));
		}
		return pascal;
	}

}
