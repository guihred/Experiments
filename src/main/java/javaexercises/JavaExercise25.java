package javaexercises;

import java.util.Locale;
import simplebuilder.HasLogging;

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
public final class JavaExercise25 implements HasLogging {


	public static void main(String[] args) {
        new JavaExercise25().solveQuadraticEquation();
	}

    public void solveQuadraticEquation() {
		double[] coefA = { 1, 0.1, 0, 0, 0, 0, 0, 0, 1, 0.1, 10e-34, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 10e34, 10e-34, 1, 1,
				10e-10 };
		double[] coefB = { 5, 0.5, 0, 1, 1, 10000, 10e-34, 10e34, -20000, -20000, -2, 0, 1, 10e-34, 0, 1, 10, 100, 1000,
				10000, 10e34, 0, 0, 10e-34,
				10e34, -10e30 };
		double[] coefC = { 6, 0.6, 2, 0, 1, 1, -10e34, -10e-34, 10e8, 10e7, 10e34, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, -10e34,
				-10e-34, -10e34, 10e-34, 10e-10 };
        solveQuadradicEquation(coefA, coefB, coefC);
    }

    private void solveQuadradicEquation(double[] coefA, double[] coefB, double[] coefC) {
        Locale.setDefault(Locale.ENGLISH);
        for (int i = 0; i < coefA.length; i++) {

			double a = coefA[i];
			double b = coefB[i];
			double c = coefC[i];
			if (a == 0.0) {
                printf("%.1fx + %.1f = 0 ", b, c);
                printf("x=%.1f", -c / b);
				continue;
			}
            printf("%.1fx^2 + %.1fx + %.1f = 0 ", a, b, c);

			double delta = b * b - 4 * a * c;

			if (delta < 0) {
				double x1 = -b / 2 / a;
				double x1i = Math.sqrt(Math.abs(delta)) / 2 / a;
				double x2 = -b / 2 / a;
				double x2i = -Math.sqrt(Math.abs(delta)) / 2 / a;
                printf("x1=%.1f%+.1fi, x2=%.1f%+.1fi", x1, x1i, x2, x2i);
			} else {
				double x1 = (-b + Math.sqrt(delta)) / 2 / a;
				double x2 = (-b - Math.sqrt(delta)) / 2 / a;
                printf("x1=%.1f, x2=%.1f", x1, x2);
			}
		}
    }

}