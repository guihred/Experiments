package javaexercises;

import java.math.BigInteger;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

/**
 * The following program makes use of a class BigNo which is used to represent
 * large integers and contains methods for operating on these integers.The
 * method power takes two parameters m and n and raises m to the power of n by
 * repeatedly using the method multiply in BigNo objects. As a test case 2 is
 * raised to the power of 2241 and the result is printed out. Complete the
 * program. The method power should work for all m, n > 0 provided m^n is no
 * more than 800 digits long.
 */

public final class JavaExercise15 {
    private static final Logger LOGGER = HasLogging.log();

	private JavaExercise15() {
	}
	public static void main(String[] args) {
        BigNo ten = new BigNo(12_345_678);
        LOGGER.info("{}", ten);
		BigNo multiply = new BigNo(55).multiply(new BigNo(55));
        LOGGER.info("{}", multiply);
        BigNo b = BigNo.power(2, 2241);
        LOGGER.info("{}", b);
        BigInteger pow = BigInteger.valueOf(2).pow(2241);
        LOGGER.info("{}", pow);
		
	}


}
