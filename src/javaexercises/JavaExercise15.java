package javaexercises;

import java.math.BigInteger;

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

	private JavaExercise15() {
	}
	public static void main(String[] args) {
		BigNo ten = new BigNo(12345678);
		System.out.println(ten);
		BigNo multiply = new BigNo(55).multiply(new BigNo(55));
		System.out.println(multiply);
		BigNo b = power(2, 2241);
		System.out.printf("%s%n", b);
		BigInteger pow = new BigInteger("2").pow(2241);
		System.out.println(pow);
		
	}

	private static BigNo power(int m, int pow) {
		BigNo p, s;
		int n = pow;

		p = n % 2 != 0 ? new BigNo(m) : new BigNo(1);
		s = new BigNo(m);
		while (n > 1) {
			s = s.multiply(s);
			n /= 2;
			if (n % 2 != 0) {

				p = p.multiply(s);
			}
		}
		return p;
	}
}
