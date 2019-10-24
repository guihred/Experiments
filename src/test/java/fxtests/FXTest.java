package fxtests;

import ex.j8.Chapter4;
import java.io.File;
import javafx.application.Application;
import org.junit.Test;
import utils.StageHelper;

@SuppressWarnings("static-method")
public final class FXTest extends AbstractTestExecution {
    public void test() throws Throwable {
        FXTesting.testApps(FXTesting.getClasses(Application.class));
    }

    @Test
    public void testChapter4() throws Throwable {
        FXTesting.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class, Chapter4.Ex10.class, Chapter4.Ex5.class,
            Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class);
    }

    @Test
    public void testDisplayCSSStyler() throws Throwable {
        show(randomItem(FXTesting.getClasses(Application.class)));
        String[] list = new File("src/main/resources/").list((d, f) -> f.endsWith(".css"));
        interactNoWait(() -> StageHelper.displayCSSStyler(lookup(".root").query().getScene(), list[0]));
        tryClickButtons();
    }

}
