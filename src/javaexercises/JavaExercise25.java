package javaexercises;

import java.util.Locale;

/**
 * 25. The Quadratic Equation Problem Write a program which solves quadratic
 * equations of the form: ax 2 + bx + c = 0; Data should be organised into lines
 * with a set of three test coefficients on each line. If a set of coefficients
 * is such as to lead to complex roots, then the program should put out a
 * message to this effect and pass on to the next set of coefficients without
 * further calculation. The program should aim to produce results to the
 * greatest accuracy warranted by the input, and deal sensibly with extreme
 * coefficient values and other awkward cases. The following test cases should
 * be used:
 */
public final class JavaExercise25 {

	private JavaExercise25() {
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);

		double[] A = { 1, 0.1, 0, 0, 0, 0, 0, 0, 1, 0.1, 10e-34, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10e34, 10e-34, 1, 1, 10e-10 };
		double[] B = { 5, 0.5, 0, 1, 1, 10000, 10e-34, 10e34, -20000, -20000, -2, 0, 1, 10e-34, 0, 1, 10, 100, 1000, 10000, 10e34, 0, 0, 10e-34,
				10e34, -10e30 };
		double[] C = { 6, 0.6, 2, 0, 1, 1, -10e34, -10e-34, 10e8, 10e7, 10e34, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, -10e34, -10e-34, -10e34, 10e-34, 10e-10 };
		for (int i = 0; i < A.length; i++) {

			double a = A[i];
			double b = B[i];
			double c = C[i];
			if (a == 0.0) {
				System.out.printf("%.1fx + %.1f = 0 %n", b, c);
				System.out.printf("x=%f%n", -c / b);
				continue;
			}
			System.out.printf("%fx^2 + %fx + %f = 0 %n", a, b, c);

			double delta = b * b - 4 * a * c;

			if (delta < 0) {
				double x1 = -b / 2 / a;
				double x1i = Math.sqrt(Math.abs(delta)) / 2 / a;
				double x2 = -b / 2 / a;
				double x2i = -Math.sqrt(Math.abs(delta)) / 2 / a;
				System.out.printf("x1=%.1f%+.1fi, x2=%.1f%+.1fi%n", x1, x1i, x2, x2i);
			} else {
				double x1 = (-b + Math.sqrt(delta)) / 2 / a;
				double x2 = (-b - Math.sqrt(delta)) / 2 / a;
				System.out.printf("x1=%.1f, x2=%.1f%n", x1, x2);
			}
		}

	}

}