package fxtests;

import fxpro.earth.CubeNode;
import fxpro.earth.EarthCubeMain;
import fxsamples.WorkingListsViews;
import fxsamples.person.FormValidation;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import ml.data.CoverageUtils;
import org.junit.Test;
import utils.StageHelper;

public class FXEngineEarthCubeTest extends AbstractTestExecution {
    @Test
    public void testDisplayCSSStyler() throws Throwable {
        showNewStage(randomItem(CoverageUtils.getClasses(Application.class)), () -> {
            String[] list = new File("src/main/resources/").list((d, f) -> f.endsWith(".css"));
            interactNoWait(() -> StageHelper.displayCSSStyler(lookup(".root").query().getScene(), list[0]));
            tryClickButtons();
        });
    }
    @Test
    public void verifyEarthCubeMain() {
        showNewStage(EarthCubeMain.class, () -> {
            sleep(2000);
            CubeNode cube = lookupFirst(CubeNode.class);
            randomDrag(cube, 100);
            press(KeyCode.CONTROL);
            randomDrag(cube, 100);
            release(KeyCode.CONTROL);
            press(KeyCode.ALT);
            randomDrag(cube, 100);
            release(KeyCode.ALT);
            type(KeyCode.SPACE);
            sleep(1500);
            type(KeyCode.SPACE);
            sleep(1000);
            type(KeyCode.ESCAPE);
        });
    }

    @Test
    public void verifyFormValidation() {
        showNewStage(FormValidation.class, () -> {
            clickOn(lookupFirst(PasswordField.class));
            type(typeText(getRandomString()));
            type(KeyCode.ENTER);
            eraseText(4);
            type(typeText(getRandomString()));
            type(KeyCode.ENTER);
            eraseText(4);
            type(typeText("senha"));
            type(KeyCode.ENTER);
        });
    }

    @Test
    public void verifyWorkingListsViews() {
        showNewStage(WorkingListsViews.class, () -> {

            List<Node> lookup = lookup(ListView.class).stream().collect(Collectors.toList());
            List<Button> buttons = lookup(Button.class).stream().collect(Collectors.toList());
            for (int i = 0; i < lookup.size(); i++) {
                Node queryAs = from(lookup.get(i)).lookup(ListCell.class::isInstance).query();
                clickOn(buttons.get(i));
                clickOn(queryAs);
                clickOn(buttons.get(i));
            }
        });

    }
}
