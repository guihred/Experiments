package ml.data;

import static java.lang.Math.*;

import java.util.List;
import java.util.stream.DoubleStream;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class FastFourierTransform {
	private static final Logger LOGGER = HasLogging.log();

    public static Complex[] fft(double[] input) {
        Complex[] cinput = new Complex[input.length];
        for (int i = 0; i < input.length; i++) {
            cinput[i] = new Complex(input[i], 0.0);
        }
        fft(cinput);
        return cinput;
    }

    public static Complex[] fft(List<Double> input) {
        return fft(input.stream().limit(highestExponentOf2(input.size())).mapToDouble(e -> e).toArray());
    }

    public static long log2(double x) {
        return (long) floor(log(x) / log(2));
    }

    public static void main(String[] args) {
        double[] input = DoubleStream.iterate(0, i -> i + 1).limit(16).toArray();
        Complex[] cinput = fft(input);
        LOGGER.info("Results:");
        for (Complex c : cinput) {
            LOGGER.info("{}", c);
        }
    }

    private static int bitReverse(int num, int bits) {
		int n = num;
        int reversedN = n;
        int count = bits - 1;
        n >>= 1;
        while (n > 0) {
            reversedN = reversedN << 1 | n & 1;
            count--;
            n >>= 1;
        }
        return reversedN << count & (1 << bits) - 1;
    }

    private static void fft(Complex[] buffer) {

        int bits = (int) (log(buffer.length) / log(2));
        for (int j = 1; j < buffer.length / 2; j++) {

            int swapPos = bitReverse(j, bits);
            Complex temp = buffer[j];
            buffer[j] = buffer[swapPos];
            buffer[swapPos] = temp;
        }

        for (int N = 2; N <= buffer.length; N <<= 1) {
            for (int i = 0; i < buffer.length; i += N) {
                for (int k = 0; k < N / 2; k++) {

                    int evenIndex = i + k;
                    int oddIndex = i + k + N / 2;
                    Complex even = buffer[evenIndex];
                    Complex odd = buffer[oddIndex];
                    double term = -2 * PI * k / N;
                    Complex exp = new Complex(cos(term), sin(term)).multiply(odd);
                    buffer[evenIndex] = even.add(exp);
                    buffer[oddIndex] = even.subtract(exp);
                }
            }
        }
    }

    private static int highestExponentOf2(double x) {
        return 1 << (int) (log(x) / log(2));
    }
}
