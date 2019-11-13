package others;

import java.util.Arrays;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.complex.Complex;

public class OthersTests {
    public static int[] minMax(int[] arr) {
        IntSummaryStatistics s = Arrays.stream(arr).summaryStatistics();
        return new int[] { s.getMin(), s.getMax() };
    }

    public static String nth(int n) {
        return String.format("%.2f", IntStream.range(0, n).mapToDouble(j -> 1.0 / (3 * j + 1)).sum());
    }

    public static Complex p(Complex t, Complex a, Complex b, Complex c) {

        Complex c1 = a.subtract(b.multiply(2)).add(c).multiply(t.multiply(t));
        Complex c2 = b.subtract(a).multiply(t.multiply(2));
        return c1.add(c2).add(a);
    }

    public static String reverse(String a) {
        List<String> asList = Arrays.asList(a.split(""));
        Collections.reverse(asList);
        return asList.stream().collect(Collectors.joining());
    }

    public static String shorterReverseLonger(String a, String b) {
        return a.length() < b.length() ? a + reverse(b) + a : b + reverse(a) + b;
    }

    public static int squareDigits(int n) {
        return Integer.valueOf(String.valueOf(n).chars().mapToObj(Character::getNumericValue)
            .map(i -> String.valueOf(i * i)).collect(Collectors.joining()));
    }

    public static int[] unique(int[] integers) {
        return Arrays.stream(integers).distinct().toArray();
    }

}
