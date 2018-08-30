package exercism;

import static crypt.FXTesting.measureTime;
import static java.util.stream.Collectors.toList;

import exercise.java9.Ch1;
import exercise.java9.Ch3;
import exercise.java9.Ch3.Employee;
import exercise.java9.ch4.Ch4;
import exercise.java9.ch4.LabeledPoint;
import exercise.java9.ch4.Line;
import exercise.java9.ch4.Point;
import japstudy.HiraganaMaker;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public class IndependentTest implements HasLogging {
    private static final Logger LOGGER = HasLogging.log(IndependentTest.class);

    @Test
    public void matrixTest() {
        double[][] matr = { { 4, 5, 3 }, { 2, -5, -2 }, { 4, 5, 6 } };
        double[] coef2 = new double[] { 3.1, -4.3, 4.9 };
        double[] solve = MatrixSolver.solve(matr, coef2);
        Assert.assertArrayEquals(solve, new double[] { -0.3, 0.5, 0.6 }, 0.01);
    }

    @Test
    public void testCh1() {
        measureTime("Ch1.extremeDoubles", () -> Ch1.extremeDoubles());
        measureTime("Ch1.factorial", () -> Ch1.factorial(1000));
        measureTime("Ch1.lotteryCombination", () -> Ch1.lotteryCombination());
        measureTime("Ch1.pascalTriangle", () -> Ch1.pascalTriangle(10));
        measureTime("Ch1.average", () -> Ch1.average(1, 2, 3, 4, 5, 6, 7, 8));
    }

    @Test
    public void testCh3() {
        Random random = new Random();
        List<Employee> collect = random.ints(1, 11).map(e -> ++e * 500).limit(5).mapToObj(Employee::new)
                .collect(toList());
        measureTime("Ch3.average", () -> Ch3.average(collect));
        measureTime("Ch3.largest", () -> Ch3.largest(collect));
        measureTime("Ch3.IntSequence.of", () -> Ch3.IntSequence.of(1, 2, 3).foreach(e -> LOGGER.trace("{}", e)));
        measureTime("new Ch3.SquareSequence",
                () -> new Ch3.SquareSequence().limit(10).foreach(e -> LOGGER.trace("{}", e)));
        measureTime("Ch3.isSorted", () -> Ch3.isSorted(Arrays.asList(1, 2, 2, 3), Integer::compareTo));
        measureTime("Ch3.luckySort",
                () -> Ch3.luckySort(Arrays.asList("f", "f", "f", "f", "f", "g", "d", "e", "e"), String::compareTo));
        measureTime("Ch3.subdirectories", () -> Ch3.subdirectories(new File(".")));
        measureTime("Ch3.sortFiles", () -> Ch3.sortFiles(new File(".").listFiles()));

    }

    @Test
    public void testHiragana() {
        measureTime("HiraganaMaker.displayInHiragana", HiraganaMaker::displayInHiragana);
    }

    @Test
    public void testCh4() {
        measureTime("Ch4.cyclicToString",
                () -> Ch4.cyclicToString(new Line(new Point(2, 3), new LabeledPoint("a", 3, 3))));
    }
}
