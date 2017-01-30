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

public class JavaExercise15 {
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

	static class BigNo {
		private static final int BASE = 10000;
		private static final int DIGS = 4;
		private static final String FORMAT = "%0" + DIGS + "d";
		private int[] value = new int[200];
		private BigNo() {
			this(0);
		}

		// This converts an ordinary int into a BigNo
		public BigNo(int m) {
			int i;
			int n = m;
			for (i = 0; n > 0; n /= BASE) {
				value[++i] = n % BASE;
			}
			value[0] = i;
		}

		// This converts a BigNo into a String
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= value[0]; i++) {
				sb.insert(0, String.format(FORMAT, value[i]));
			}

			String replaceAll = sb.toString().replaceAll("^0+", "");
			return replaceAll.isEmpty() ? "0" : replaceAll;
		}

		// This multiplies one BigNo
		public BigNo multiply(BigNo that) {
			BigNo result = new BigNo();

			for (int i = 1; i < that.value.length - 1; i++) {
				for (int j = 1; j < value.length; j++) {
					int a = that.value[i] * value[j];
					result.value[(i + j - 1) % result.value.length] += a % BASE;
					result.value[(i + j) % result.value.length] += a / BASE;
				}
			}
			for (int i = 1; i < result.value.length; i++) {
				if (i < result.value.length - 1) {
					result.value[i + 1] += result.value[i] / BASE;
				}
				result.value[i] = result.value[i] % BASE;
				if (result.value[i] > 0) {
					result.value[0]=i;
				}
			}

			return result;
		}
	}
}
