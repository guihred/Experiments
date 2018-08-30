package crypt;

import static crypt.FXTesting.measureTime;
import static crypt.FXTesting.measureTimeExpectException;

import exercise.java8.*;
import java.io.File;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import simplebuilder.HasLogging;

public final class Java8ExercisesTest implements HasLogging {
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
    public void testChapter3() {
        measureTime("Chapter3.getRich",
                () -> Chapter3.getRich(Arrays.asList(20, 66, 12, 48, 38, 38, 20, 65, 54), 0, 8));
    }

    @Test
    public void testChapter4() {
        measureTime("Chapter4.testApps", () -> FXTesting.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class,
                Chapter4.Ex5.class, Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class, Chapter4.Ex10.class));
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

}
