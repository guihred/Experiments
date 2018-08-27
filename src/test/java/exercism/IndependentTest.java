package exercism;

import com.google.api.client.repackaged.com.google.common.base.Supplier;
import java.io.File;
import java8.exercise.*;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public class IndependentTest implements HasLogging {
    public static final Logger LOGGER = HasLogging.log(IndependentTest.class);

    @Test
    public void matrixTest() {
        double[][] matr = { { 4, 5, 3 }, { 2, -5, -2 }, { 4, 5, 6 } };
        double[] coef2 = new double[] { 3.1, -4.3, 4.9 };
        double[] solve = MatrixSolver.solve(matr, coef2);
        Assert.assertArrayEquals(solve, new double[] { -0.3, 0.5, 0.6 }, 0.01);
    }

    @Test

    public void testChapter8() throws Exception {

        measureTime("Chapter8.ex1", () -> Chapter8.ex1());
        measureTimeExpectException("Chapter8.ex2", () -> Chapter8.ex2());
        measureTime("Chapter8.ex3", () -> Chapter8.ex3());
        measureTime("Chapter8.ex4", () -> Chapter8.ex4());
        measureTime("Chapter8.ex5", () -> Chapter8.ex5());
        measureTime("Chapter8.ex6", () -> Chapter8.ex6());
        measureTime("Chapter8.ex7", () -> Chapter8.ex7());
        measureTime("Chapter8.ex9", () -> Chapter8.ex9());
        measureTime("Chapter8.ex10", () -> Chapter8.ex10());
        measureTime("Chapter8.ex11", () -> Chapter8.ex11());
        measureTime("Chapter8.ex15", () -> Chapter8.ex15());
        measureTime("Chapter8.ex16", () -> Chapter8.ex16());
        try {
            Chapter8.ex14();
        } catch (Exception e) {
            Assert.assertNotNull("Expected NullPointer Exception", e);
        }
    }

    @Test
    public void testChapter6() throws Exception {
        measureTime("Chapter6.ex1", () -> Chapter6.ex1());
        measureTime("Chapter6.ex3", () -> Chapter6.ex3());
        measureTime("Chapter6.ex5", () -> Chapter6.ex5());
        measureTime("Chapter6.ex6", () -> Chapter6.ex6());
        measureTime("Chapter6.ex7", () -> Chapter6.ex7());
        measureTime("Chapter6.ex8", () -> Chapter6.ex8());
        measureTime("Chapter6.ex9", () -> Chapter6.ex9());
        measureTime("Chapter6.ex10", () -> Chapter6.ex10());
    }

    public static void measureTime(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Throwable e) {
            LOGGER.error("Exception in " + name, e);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
        LOGGER.info("{} took {}", name, formatDuration);
    }
    
    public static void measureTimeExpectException(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Throwable e) {
            long currentTimeMillis2 = System.currentTimeMillis();
            long arg2 = currentTimeMillis2 - currentTimeMillis;
            String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
            LOGGER.info("{} took {}", name, formatDuration);
            LOGGER.trace("Exception in " + name, e);
        }
    }

    public static <T> T measureTime(String name, Supplier<T> runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        T t = null;
        try {
            t = runnable.get();
        } catch (Exception e) {
            LOGGER.error("Exception thrown", e);
            Assert.fail("Exception in " + name);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
        LOGGER.info("{} took {}", name, formatDuration);
        return t;
    }

    @Test
    public void testChapter5() {
        measureTime("Chapter5.ex1", () -> Chapter5.ex1());
        measureTime("Chapter5.ex2", () -> Chapter5.ex2());
        measureTime("Chapter5.ex3", () -> Chapter5.ex3());
        measureTime("Chapter5.ex4", () -> Chapter5.ex4());
        measureTime("Chapter5.ex5", () -> Chapter5.ex5());
        measureTime("Chapter5.ex6", () -> Chapter5.ex6());
        measureTime("Chapter5.ex7", () -> Chapter5.ex7());
        measureTime("Chapter5.ex8", () -> Chapter5.ex8());
        measureTime("Chapter5.ex9", () -> Chapter5.ex9());
        measureTime("Chapter5.ex10", () -> Chapter5.ex10());
        measureTime("Chapter5.ex11", () -> Chapter5.ex11());
    }

    @Test
    public void testChapter2() {
        measureTime("Chapter2.ex1", () -> Chapter2.ex1());
        measureTime("Chapter2.ex2", () -> Chapter2.ex2());
        measureTime("Chapter2.ex3", () -> Chapter2.ex3());
        measureTime("Chapter2.ex4", () -> Chapter2.ex4());
        measureTime("Chapter2.ex5", () -> Chapter2.ex5());
        measureTime("Chapter2.ex6", () -> Chapter2.ex6());
        measureTime("Chapter2.ex8", () -> Chapter2.ex8());
        measureTime("Chapter2.ex9", () -> Chapter2.ex9());
        measureTime("Chapter2.ex10", () -> Chapter2.ex10());
        measureTime("Chapter2.ex11", () -> Chapter2.ex11());
        measureTime("Chapter2.ex12", () -> Chapter2.ex12());
        measureTime("Chapter2.ex13", () -> Chapter2.ex13());
    }

    @Test
    public void testChapter1() {
        String threadName = measureTime("Chapter1.ex5", () -> Chapter1.ex1(new Integer[] { 1, 2, 3, 4, 5, 6, 7 }));
        Assert.assertEquals("Thread name must be equal", Thread.currentThread().getName(), threadName);
        measureTime("Chapter1.ex5", () -> Chapter1.ex2(new File(".")));
        measureTime("Chapter1.ex5", () -> Chapter1.ex3(new File(Chapter1.DOCUMENTS_FOLDER), "log"));
        measureTime("Chapter1.ex5", () -> Chapter1.ex4(new File(Chapter1.DOCUMENTS_FOLDER).listFiles()));
        measureTime("Chapter1.ex5", () -> Chapter1.ex5());
        measureTime("Chapter1.ex6", () -> Chapter1.ex6());
        measureTime("Chapter1.ex7", () -> Chapter1.ex7());
        measureTime("Chapter1.ex8", () -> Chapter1.ex8());
        measureTime("Chapter1.ex9", () -> Chapter1.ex9());
    }
}
