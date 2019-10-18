package fxtests;

import ex.j8.Chapter4;
import javafx.application.Application;
import org.junit.Test;

@SuppressWarnings("static-method")
public final class FXTest {
    public void test() throws Throwable {
        FXTesting.testApps(FXTesting.getClasses(Application.class));
    }

    @Test
    public void testChapter4() throws Throwable {
        FXTesting.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class, Chapter4.Ex10.class, Chapter4.Ex5.class,
            Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class);
    }


}
