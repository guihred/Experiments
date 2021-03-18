package utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import utils.ex.HasLogging;

/**
 * The Class MatrixSolver.
 * 
 * Used to solve a linear system with its method solve. The method receives a
 * series of linear equations represented as a matrix.
 * 
 * a*x +b*y = c
 * 
 * d*x +e*y = f
 * 
 * so solve({{a,b},{d,e}},{c,f}) will return an array with the calculated values
 * for x and y
 *
 * It can also be used to calculate the determinant of a matrix with its method
 * determinant(matrix)
 * 
 */
public final class MatrixSolver {

    private static final Logger LOGGER = HasLogging.log();
    private final static boolean debug = true;

    private MatrixSolver() {
    }

    public static double determinant(double[][] matrix) {
        if (matrix.length == 1) {
            return matrix[0][0];
        }
        double sum = 0;
        for (int i = 0; i < matrix.length; i++) {
            double[][] smaller = new double[matrix.length - 1][matrix.length - 1];
            for (int a = 1; a < matrix.length; a++) {
                for (int b = 0; b < matrix.length; b++) {
                    if (b < i) {
                        smaller[a - 1][b] = matrix[a][b];
                    } else if (b > i) {
                        smaller[a - 1][b - 1] = matrix[a][b];
                    }
                }
            }
            int s = i % 2 == 0 ? 1 : -1;
            sum += s * matrix[0][i] * determinant(smaller);
        }
        return sum;
    }

    public static double[] div(double[] v, double norm) {
        return DoubleStream.of(v).map(e -> e / norm).toArray();
    }

    public static double[] matmul(double[][] matrix, double[] v) {
        double[] a = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            a[i] = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                a[i] += matrix[i][j] * v[j];
            }
        }
        return a;
    }

    public static double norm(double[] v) {
        return vectorNorm(v, 2);
    }

    public static double[] solve(double[][] matr, double[] coef2) {

        double[] coef = Arrays.copyOf(coef2, coef2.length);

        double[][] temp = new double[coef.length][coef.length];
        for (int i = 0; i < coef.length; i++) {
            temp[i] = Arrays.copyOf(matr[i], coef.length);
        }
        correctOrder(coef, temp);
        printMatrix(temp, coef);
        for (int i = 0; i < coef.length; i++) {
            for (int j = (i + 1) % coef.length; j != i; j = (j + 1) % coef.length) {
                if (DoubleStream.of(temp[j]).filter(d -> d != 0).count() > 1) {
                    multiplyAndAdd(temp[j], temp[i], coef, i, j);
                    printMatrix(temp, coef);
                }
            }
        }
        printMatrix(temp, coef);
        for (int i = 0; i < coef.length; i++) {
            coef[i] /= nonZero(temp[i]);
        }

        return coef;
    }

    public static double[] sub(double[] v, double[] lastV) {
        return IntStream.range(0, v.length).mapToDouble(i -> v[i] - lastV[i]).toArray();
    }

    private static void correctOrder(double[] coef, double[][] temp) {
        for (int i = 0; i < coef.length; i++) {
            if (temp[i][0] == 0 && i < coef.length - 1) {
                swap(temp, i);
                double d = coef[i];
                coef[i] = coef[i + 1];
                coef[i + 1] = d;
            }
        }
    }

    private static void multiplyAndAdd(double[] ds, double[] ds2, double[] coef, int i, int j) {
        double nonZero = nonZero(ds);
        double nonZero2 = nonZero(ds2);
        if (nonZeroIndex(ds) != nonZeroIndex(ds2)) {
            nonZero = ds[nonZeroIndex(ds2)];

        }
        coef[j] = coef[j] * -nonZero2 + coef[i] * nonZero;

        for (int k = 0; k < ds.length; k++) {
            ds[k] = ds[k] * -nonZero2 + ds2[k] * nonZero;
        }

    }

    private static double nonZero(double[] temp) {
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] != 0) {
                return temp[i];
            }
        }
        return 1;
    }

    private static int nonZeroIndex(double[] temp) {
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    private static void printMatrix(double[][] matr, double[] coef) {
        if (debug) {
            StringBuilder desc = new StringBuilder();
            for (int i = 0; i < matr.length; i++) {
                desc.append("\n\t");
                desc.append(DoubleStream.of(matr[i]).mapToObj(e -> String.format(Locale.US, "[%.3f]", e))
                    .collect(Collectors.joining(", ")));
                if (coef != null && coef.length > 0) {
                    desc.append(String.format(Locale.US, "[%.1f]", coef[i % coef.length]));
                }
            }
            LOGGER.info("{}", desc);
        }
    }

    private static void swap(double[][] temp, int i) {
        double[] a = temp[i];
        temp[i] = temp[i + 1];
        temp[i + 1] = a;
    }

    private static double vectorNorm(double[] a, final double p) {
        double n = 0;
        for (int i = 0; i < a.length; i++) {
            n += Math.abs(Math.pow(a[i], p));
        }
        return Math.pow(n, 1. / p);
    }

}
