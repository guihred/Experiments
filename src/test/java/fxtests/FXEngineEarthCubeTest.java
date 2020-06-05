package fxtests;

import fxpro.earth.CubeNode;
import fxpro.earth.EarthCubeMain;
import fxsamples.GlobeSphereApp;
import fxsamples.WorkingListsViews;
import fxsamples.person.FormValidation;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import ml.data.CoverageUtils;
import org.junit.Test;
import simplebuilder.StageHelper;
import utils.ResourceFXUtils;

public class FXEngineEarthCubeTest extends AbstractTestExecution {
    @Test
    public void testDisplayCSSStyler() throws Throwable {
        showNewStage(randomItem(CoverageUtils.getClasses(Application.class)), () -> {
            String[] list = ResourceFXUtils.getPathByExtension(new File("src/main/resources/"), ".css").stream()
                    .map(e -> e.toFile().getName()).toArray(String[]::new);
            interactNoWait(() -> StageHelper.displayCSSStyler(lookup(".root").query().getScene(), randomItem(list)));
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
    public void verifyGlobeSphereApp() {
        showNewStage(GlobeSphereApp.class, t -> t.setSpecularColorNull(false));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void verifyWorkingListsViews() {
        showNewStage(WorkingListsViews.class, () -> {

            List<ListView> lookup = lookupList(ListView.class);
            List<Button> buttons = lookupList(Button.class);
            for (int i = 0; i < lookup.size(); i++) {
                Node queryAs = from(lookup.get(i)).lookup(ListCell.class::isInstance).query();
                clickOn(buttons.get(i));
                clickOn(queryAs);
                clickOn(buttons.get(i));
            }
        });

    }
}
