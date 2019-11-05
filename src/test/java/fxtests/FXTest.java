package fxtests;

import ex.j8.Chapter4;
import fxsamples.WorkingListsViews;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
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
        showNewStage(randomItem(FXTesting.getClasses(Application.class)));
        String[] list = new File("src/main/resources/").list((d, f) -> f.endsWith(".css"));
        interactNoWait(() -> StageHelper.displayCSSStyler(lookup(".root").query().getScene(), list[0]));
        tryClickButtons();
    }


    @Test
    public void verifyWorkingListsViews() {
        showNewStage(WorkingListsViews.class);
        WaitForAsyncUtils.waitForFxEvents();
        List<Node> lookup = lookup(ListView.class).stream().collect(Collectors.toList());
        List<Button> buttons = lookup(Button.class).stream().collect(Collectors.toList());
        for (int i = 0; i < lookup.size(); i++) {
            Node queryAs = from(lookup.get(i)).lookup(ListCell.class::isInstance).query();
            clickOn(queryAs);
            clickOn(buttons.get(i));
        }

    }

}
