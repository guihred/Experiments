package fxtests;

import static fxtests.FXTesting.measureTime;

import audio.mp3.PageImage;
import ex.j8.Chapter4;
import javafx.application.Application;
import org.junit.Test;

@SuppressWarnings("static-method")
public final class FXTest extends AbstractTestExecution {
    public void test() throws Throwable {
        AbstractTestExecution.testApps(FXTesting.getClasses(Application.class));
    }

    @Test
    public void testChapter4() throws Throwable {
        AbstractTestExecution.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class, Chapter4.Ex10.class, Chapter4.Ex5.class,
            Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class);
    }

    @Test
    public void testPageImage() {
        measureTime("PageImage.testApps", () -> AbstractTestExecution.testApps(PageImage.class));
    }


}
