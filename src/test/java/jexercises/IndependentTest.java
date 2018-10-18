package jexercises;

import static java.util.stream.Collectors.toList;
import static language.FXTesting.measureTime;

import cubesystem.ElementWiseOp;
import ex.j9.Ch1;
import ex.j9.Ch3;
import ex.j9.Ch3.Employee;
import ex.j9.ch4.Ch4;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.Line;
import ex.j9.ch4.Point;
import image.ImageCreating;
import image.ImageLoading;
import japstudy.HiraganaMaker;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;
import ml.data.FastFourierTransform;
import ml.data.QuickSortML;
import org.apache.commons.math3.complex.Complex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import others.OthersTests;
import others.RandomHelloWorld;
import others.TermFrequency;
import others.TermFrequencyIndex;
import others.UnRar;
import others.UnZip;
import utils.HasLogging;
import utils.MatrixSolver;
import utils.ResourceFXUtils;

public class IndependentTest implements HasLogging {
	private static final Logger LOGGER = HasLogging.log();

	@Test
    public void matrixTest() {
        double[][] matr = { { 4, 5, 3 }, { 2, -5, -2 }, { 4, 5, 6 } };
        double[] coef2 = new double[] { 3.1, -4.3, 4.9 };
        double[] solve = MatrixSolver.solve(matr, coef2);
        Assert.assertArrayEquals("The solved Matrix should match the solution", solve, new double[] { -0.3, 0.5, 0.6 },
                0.01);
    }

	@Test
    public void testCh1() {
        measureTime("Ch1.extremeDoubles", Ch1::extremeDoubles);
        measureTime("Ch1.factorial", () -> Ch1.factorial(1000));
        measureTime("Ch1.lotteryCombination", Ch1::lotteryCombination);
        measureTime("Ch1.pascalTriangle", () -> Ch1.pascalTriangle(10));
        measureTime("Ch1.average", () -> Ch1.average(1, 2, 3, 4, 5, 6, 7, 8));
    }

	@Test
    public void testCh3() {
        Random random = new Random();
		List<Employee> randomEmployees = random.ints(1, 11).map(e -> (e + 1) * 500).limit(5).mapToObj(Employee::new)
                .collect(toList());
        measureTime("Ch3.average", () -> Ch3.average(randomEmployees));
        measureTime("Ch3.largest", () -> Ch3.largest(randomEmployees));
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
    public void testTermFrequencyIndex() {
        measureTime("TermFrequencyIndex.identifyKeyWordsInSourceFiles",
                TermFrequencyIndex::identifyKeyWordsInSourceFiles);
    }

	@Test
    public void testQuickSort() {
        List<Integer> input = Arrays.asList(24, 2, 45, 20, 56, 75, 2, 56, 99, 53, 12);
        Comparator<Integer> c = Integer::compareTo;
        measureTime("QuickSortML.sort", () -> QuickSortML.sort(input, c.reversed()));
        Assert.assertTrue("List should be sorted", Ch3.isSorted(input, c.reversed()));

    }

	@Test
    public void testFastFourierTransform() {
        double[] input = DoubleStream.iterate(0, i -> i + 1).limit(16).toArray();
        Complex[] cinput = measureTime("FastFourierTransform.fft", () -> FastFourierTransform.fft(input));
        for (Complex c : cinput) {
            LOGGER.trace("{}", c);
        }
    }

	@Test
    public void testRandomHelloWorld() {
        measureTime("RandomHelloWorld.displayHelloWorld", RandomHelloWorld::displayHelloWorld);
    }

	@Test
    public void testTermFrequency() {
        measureTime("TermFrequency.displayTermFrequency", TermFrequency::displayTermFrequency);
    }

	@Test
    public void testElementWiseOperations() {
        measureTime("ElementWiseOp.scalarOp",
                () -> ElementWiseOp.printMatrix(ElementWiseOp.scalarOp(ElementWiseOp.Operation.MUL,
                        new Double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 }, { 7.0, 8.0, 9.0 } }, 3.0)));
        measureTime("ElementWiseOp.matrOp",
                () -> ElementWiseOp.printMatrix(ElementWiseOp.matrOp(ElementWiseOp.Operation.DIV,
                        new Double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 }, { 7.0, 8.0, 9.0 } },
                        new Double[][] { { 1.0, 2.0 }, { 3.0, 4.0 } })));
    }

	@Test
    public void testCh4() {
        measureTime("Ch4.cyclicToString",
                () -> Ch4.cyclicToString(new Line(new Point(2, 3), new LabeledPoint("a", 3, 3))));
    }

	@Test
    public void testRarAndZIP() {
        measureTime("UnRar.extractRarFiles", () -> UnRar.extractRarFiles(UnRar.SRC_DIRECTORY));
        measureTime("UnZip.extractZippedFiles", () -> UnZip.extractZippedFiles(UnZip.ZIPPED_FILE_FOLDER));
    }

	@Test
    public void testOthersTest() {
        int[] arr = { 3, 5, 6, 8, 7, 2 };
        measureTime("OthersTests.minMax", () -> OthersTests.minMax(arr));
        measureTime("OthersTests.nth", () -> OthersTests.nth(5));
        measureTime("OthersTests.squareDigits", () -> OthersTests.squareDigits(5));
        measureTime("OthersTests.unique", () -> OthersTests.unique(arr));
        measureTime("OthersTests.reverse", () -> OthersTests.reverse("HIHI"));
        measureTime("OthersTests.p", () -> OthersTests.p(new Complex(1.0 / 2.0), new Complex(-3, -3),
                new Complex(-1, 1), new Complex(-9, -5)));
        measureTime("OthersTests.getStateMachine", () -> OthersTests.getStateMachine(OthersTests.Estado.CLOSED,
                Arrays.asList("APP_PASSIVE_OPEN", "RCV_SYN", "RCV_ACK", "APP_CLOSE", "APP_SEND")));
    }

    @Test
    public void testImagesTest() {
		File userFolder = ResourceFXUtils.toFile("out").getParentFile();
        String dataDir = userFolder + "\\";
        String nameFile = ResourceFXUtils.getFirstPathByExtension(userFolder, ".jpg").toString();
        String svgFile = ResourceFXUtils.getFirstPathByExtension(userFolder, ".svg").toString();
        String pngFile = ResourceFXUtils.getFirstPathByExtension(userFolder, ".png").toString();
        measureTime("ImageCreating.creating", () -> ImageCreating.creating(nameFile));
        measureTime("ImageLoading.convertSVG", () -> ImageLoading.convertSVG(dataDir, svgFile));
        measureTime("ImageLoading.binarize", () -> ImageLoading.binarize(dataDir, nameFile));
        measureTime("ImageLoading.bradleyThreshold", () -> ImageLoading.bradleyThreshold(dataDir, pngFile));
        measureTime("ImageLoading.convertSVG", () -> ImageLoading.convertSVG(dataDir, nameFile));
        measureTime("ImageLoading.cropImage", () -> ImageLoading.cropImage(dataDir, nameFile));
        measureTime("ImageLoading.exporting", () -> ImageLoading.exporting(dataDir, nameFile));
        measureTime("ImageLoading.grayScale", () -> ImageLoading.grayScale(dataDir, nameFile));
        measureTime("ImageLoading.grayScaling", () -> ImageLoading.grayScaling(dataDir, nameFile));
    }
}
