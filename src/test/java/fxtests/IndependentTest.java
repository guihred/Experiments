package fxtests;

import static fxtests.FXTesting.measureTime;
import static fxtests.FXTesting.runInTime;
import static java.util.stream.Collectors.toList;

import cubesystem.ElementWiseOp;
import ex.j9.Ch1;
import ex.j9.Ch3;
import ex.j9.Employee;
import ex.j9.ch4.Ch4;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.LineCh4;
import ex.j9.ch4.PointCh4;
import extract.JsoupUtils;
import extract.QuickSortML;
import extract.WikiImagesUtils;
import fxml.utils.JsonExtractor;
import fxml.utils.TermFrequency;
import fxml.utils.TermFrequencyIndex;
import image.ImageCreating;
import image.ImageLoading;
import japstudy.HiraganaMaker;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.DoubleStream;
import ml.data.DecisionTree;
import ml.data.FastFourierTransform;
import neuro.BrazilianVerbsConjugator;
import org.apache.commons.math3.complex.Complex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import others.*;
import pdfreader.Speaker;
import utils.*;
import utils.ex.HasLogging;

@SuppressWarnings("static-method")
public class IndependentTest {
    private static final Logger LOGGER = HasLogging.log();

    @Test
    public void matrixTest() {
        double[][] matr = { { 4, 5, 3 }, { 2, -5, -2 }, { 4, 5, 6 } };
        double[] coef2 = new double[] { 3.1, -4.3, 4.9 };
        double[] solve = measureTime("MatrixSolver.solve", () -> MatrixSolver.solve(matr, coef2));
        double[] correctAnswear = new double[] { -0.3, 0.5, 0.6 };
        Assert.assertArrayEquals("The solved Matrix should match the solution", solve, correctAnswear, 0.01);
        measureTime("MatrixSolver.determinant", () -> MatrixSolver.determinant(matr));
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
        List<Employee> randomEmployees =
                random.ints(1, 11).map(e -> (e + 1) * 500).limit(5).mapToObj(Employee::new).collect(toList());
        measureTime("Ch3.average", () -> Ch3.average(randomEmployees));
        measureTime("Ch3.largest", () -> Ch3.largest(randomEmployees));
        measureTime("Ch3.IntSequence.of", () -> Ch3.IntSequence.of(1, 2, 3).foreach(e -> LOGGER.trace("{}", e)));
        measureTime("new Ch3.SquareSequence",
                () -> new Ch3.SquareSequence().limit(10).foreach(e -> LOGGER.trace("{}", e)));
        measureTime("Ch3.isSorted", () -> QuickSortML.isSorted(Arrays.asList(1, 2, 2, 3), Integer::compareTo));
        measureTime("Ch3.luckySort",
                () -> Ch3.luckySort(Arrays.asList("f", "f", "f", "f", "f", "g", "d", "e", "e"), String::compareTo));
        measureTime("Ch3.subdirectories", () -> Ch3.subdirectories(new File(".")));
        measureTime("Ch3.sortFiles", () -> Ch3.sortFiles(new File(".").listFiles()));
        measureTime("Ch3.listByExtension", () -> Ch3.listByExtension(ResourceFXUtils.getOutFile(), "png"));
        measureTime("Ch3.tasks", () -> Ch3.tasks());
        measureTime("Ch3.IntSequence.constant", () -> Ch3.IntSequence.constant(3));
        runInTime("Ch3.IntSequence.constant", () -> Ch3.IntSequence.constant(3).foreach(e -> LOGGER.trace("{}", e)),
                500);
        runInTime("Ch3.Sequence.of",
                () -> Ch3.Sequence.of("Oioi", "Lala", "Whassup").foreach(e -> LOGGER.trace("{}", e)), 500);
        runInTime("Ch3.Sequence.constant", () -> Ch3.Sequence.constant("Oioi").foreach(e -> LOGGER.trace("{}", e)),
                500);

    }

    @Test
    public void testCh4() {
        measureTime("ShapeCh4.moveBy", () -> Arrays.asList(new LineCh4(new PointCh4(2, 3), new LabeledPoint("a", 3, 3)))
                .forEach(e -> e.moveBy(10, 10)));
        measureTime("Ch4.cyclicToString",
                () -> Ch4.cyclicToString(new LineCh4(new PointCh4(2, 3), new LabeledPoint("a", 3, 3))));
        measureTime("Ch4.classRepresentations", () -> Ch4.classRepresentations());
    }

    @Test
    public void testDateUtils() {
        measureTime("DateFormatUtils.convertTimeToMillis", () -> DateFormatUtils.convertTimeToMillis("00:00:00.000"));
        measureTime("DateFormatUtils.extractDate", () -> DateFormatUtils.extractDate("21/02/2010"));
        measureTime("DateFormatUtils.format", () -> DateFormatUtils.format(LocalDateTime.now()));
        measureTime("DateFormatUtils.formatDate", () -> DateFormatUtils.formatDate(LocalDate.now()));
        measureTime("DateFormatUtils.parse", () -> DateFormatUtils.parse("00:00:01.000"));
    }

    @Test
    public void testDecision() {
        measureTime("DecisionTree.executeSimpleTest", DecisionTree::executeSimpleTest);
        measureTime("DecisionTree.testCatanDecisionTree", DecisionTree::testCatanDecisionTree);
    }

    @Test
    public void testDisplayErrors() {
        measureTime("StatsLogAccess.displayErrors",
                () -> StatsLogAccess.displayErrors().forEach(l -> LOGGER.info("{}", l)));
    }

    @Test
    public void testEanFactorReducer() {
        measureTime("EanFactorReducer.validate", () -> EanFactorReducer.validate("789100031550"));
        measureTime("EanFactorReducer.validate", () -> EanFactorReducer.validate("789100031557"));
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
    public void testFastFourierTransform() {
        double[] input = DoubleStream.iterate(0, i -> i + 1).limit(16).toArray();
        Complex[] cinput = measureTime("FastFourierTransform.fft", () -> FastFourierTransform.fft(input));
        for (Complex c : cinput) {
            LOGGER.trace("{}", c);
        }
    }

    @Test
    public void testGoogleImages() {
        measureTime("WikiImagesUtils.displayCountByExtension", () -> WikiImagesUtils.displayCountByExtension());
    }

    @Test
    public void testHiragana() {
        measureTime("HiraganaMaker.displayInHiragana", HiraganaMaker::displayInHiragana);
    }

    @Test
    public void testImagesTest() throws IOException {
        File userFolder = ResourceFXUtils.getOutFile();
        String dataDir = userFolder + "\\";
        File createTempFile = File.createTempFile("created", ".jpg", ResourceFXUtils.getOutFile());
        String nameFile = createTempFile.toPath().toString();
        String svgFile = FileTreeWalker.getFirstPathByExtension(userFolder.getParentFile(), ".svg").toString();
        String pngFile = FileTreeWalker.getFirstPathByExtension(userFolder.getParentFile(), ".png").toString();
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

    public void testJsonExtractor() {

        measureTime("JsonExtractor.toObject",
                () -> JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/fields.json"), "index", "fields"));
    }

    @Test
    public void testJsoupExample() {
        String ABOUT_HTML = "About.html";
        File file = ResourceFXUtils.toFile(ABOUT_HTML);
        URL url = ResourceFXUtils.convertToURL(file);
        measureTime("JsoupExample.displayAllElements", () -> JsoupUtils.displayAllElements(file));
        measureTime("JsoupExample.displayAllElementsWithOutline",
                () -> JsoupUtils.displayAllElementsWithOutline(file));
        measureTime("JsoupExample.extractDataFromHTML", () -> JsoupUtils.extractDataFromHTML(file));
        measureTime("JsoupExample.extractingJavaScriptDataWithJsoup",
                () -> JsoupUtils.extractingJavaScriptDataWithJsoup());
        measureTime("JsoupExample.extractTwitterMarkup", () -> JsoupUtils.extractTwitterMarkup());
        measureTime("JsoupExample.extractURL", () -> JsoupUtils.extractURL("http://stackoverflow.com"));
        measureTime("JsoupExample.extractURLPartialHTML", () -> JsoupUtils.extractURLPartialHTML());
        measureTime("JsoupExample.filterMailToLinks", () -> JsoupUtils.filterMailToLinks());
        final String username = "guih.red@gmail.com";
        final String pass = "";
        measureTime("JsoupExample.normalParse", () -> JsoupUtils.normalParse(file));
        measureTime("JsoupExample.renderedInTheBrowser", () -> JsoupUtils.renderedInTheBrowser(url));
        measureTime("JsoupExample.selectingElements", () -> JsoupUtils.selectingElements());
        String url1 = "yourWebsite.com/loginUrl";
        String value = "yourUsername";
        String value2 = "yourPassword";
        measureTime("JsoupExample.simpleAuthentication", () -> JsoupUtils.simpleAuthentication(url1, value, value2));
        measureTime("JsoupExample.loginGitHub", () -> JsoupUtils.loginGitHub(username, pass));

    }

    @Test
    public void testMachineState() {
        measureTime("MachineState.getStateMachine", () -> MachineState.getStateMachine(MachineState.CLOSED,
                Arrays.asList("APP_PASSIVE_OPEN", "RCV_SYN", "RCV_ACK", "APP_CLOSE", "APP_SEND")));
        measureTime("MachineState.getStateMachine", () -> {
            List<String> asList = Arrays.asList("APP_PASSIVE_OPEN", "RCV_SYN", "RCV_ACK", "APP_CLOSE", "APP_SEND");
            Collections.shuffle(asList);
            return MachineState.getStateMachine(MachineState.CLOSED, asList);
        });
    }

    @Test
    public void testOthersTest() {
        int[] arr = { 3, 5, 6, 8, 7, 2 };
        measureTime("OthersTests.minMax", () -> OthersTests.minMax(arr));
        measureTime("OthersTests.nth", () -> OthersTests.nth(5));
        measureTime("OthersTests.squareDigits", () -> OthersTests.squareDigits(5));
        measureTime("OthersTests.unique", () -> OthersTests.unique(arr));
        measureTime("OthersTests.reverse", () -> OthersTests.reverse("HIHI"));
        measureTime("OthersTests.shorterReverseLonger", () -> OthersTests.shorterReverseLonger("a", "bb"));
        measureTime("OthersTests.shorterReverseLonger", () -> OthersTests.shorterReverseLonger("aaa", "bb"));
        measureTime("OthersTests.p", () -> OthersTests.p(new Complex(1.0 / 2.0), new Complex(-3, -3),
                new Complex(-1, 1), new Complex(-9, -5)));
    }

    @Test
    public void testQuickSort() {
        List<Integer> input = Arrays.asList(24, 2, 45, 20, 56, 75, 2, 56, 99, 53, 12);
        Comparator<Integer> c = Integer::compareTo;
        measureTime("QuickSortML.sort", () -> QuickSortML.sort(input, c.reversed()));
        Assert.assertTrue("List should be sorted", QuickSortML.isSorted(input, c.reversed()));

    }

    @Test
    public void testRandomHelloWorld() {
        measureTime("RandomHelloWorld.displayHelloWorld", RandomHelloWorld::displayHelloWorld);
    }

    @Test
    public void testSpeaker() {
        measureTime("Speaker.speak", () -> {
            Speaker.SPEAKER.speak("Hi");
            Speaker.SPEAKER.speak("How Are You");
            Speaker.SPEAKER.speak("Show me the money");
            Speaker.SPEAKER.dealocate();
        });
    }

    @Test
    public void testStringSiga() {
        List<String> asList = Arrays.asList("32154", null, "私は寿司が好きです。", "");
        for (String nome : asList) {
            measureTime("StringSigaUtils.codificar", () -> StringSigaUtils.codificar(nome));
            measureTime("StringSigaUtils.decodificar", () -> StringSigaUtils.decodificar(nome));
            measureTime("StringSigaUtils.fixEncoding", () -> StringSigaUtils.fixEncoding(nome));
            measureTime("StringSigaUtils.fixEncoding", () -> StringSigaUtils.fixEncoding(nome));
            measureTime("StringSigaUtils.getApenasNumeros", () -> StringSigaUtils.getApenasNumeros(nome));
            measureTime("StringSigaUtils.getApenasNumerosInt", () -> StringSigaUtils.getApenasNumerosInt(nome));
            measureTime("StringSigaUtils.getCEPFormatado", () -> StringSigaUtils.getCEPFormatado(nome));
            measureTime("StringSigaUtils.getCEPFormatado", () -> StringSigaUtils.getCEPFormatado(nome));
            measureTime("StringSigaUtils.getCnpjFormatado", () -> StringSigaUtils.getCnpjFormatado(nome));
            measureTime("StringSigaUtils.getCnpjFormatado", () -> StringSigaUtils.getCnpjFormatado(nome));
            measureTime("StringSigaUtils.getCpfDesformatado", () -> StringSigaUtils.getCpfDesformatado(nome));
            measureTime("StringSigaUtils.getCpfFormatado", () -> StringSigaUtils.getCpfFormatado(nome));
            measureTime("StringSigaUtils.getCpfFormatado", () -> StringSigaUtils.getCpfFormatado(nome));
            measureTime("StringSigaUtils.removerDiacritico", () -> StringSigaUtils.removerDiacritico(nome));
            measureTime("StringSigaUtils.retirarMascara", () -> StringSigaUtils.retirarMascara(nome));
            measureTime("StringSigaUtils.substituirNaoNumeros", () -> StringSigaUtils.substituirNaoNumeros(nome));
            measureTime("StringSigaUtils.toInteger", () -> StringSigaUtils.toInteger(nome));
        }
    }

    @Test
    public void testTermFrequency() {
        measureTime("TermFrequency.displayTermFrequency", TermFrequency::displayTermFrequency);
        measureTime("TermFrequencyIndex.identifyKeyWordsInSourceFiles",
                TermFrequencyIndex::identifyKeyWordsInSourceFiles);
    }

    @Test
    public void testVerbs() {
        measureTime("BrazilianVerbsConjugator.conjugate", () -> BrazilianVerbsConjugator
                .getWords(ResourceFXUtils.toURI("verbs.dic")).forEach(BrazilianVerbsConjugator::conjugate));
    }
}
