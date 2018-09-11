package crypt;

import javafx.scene.Node;
import javafx.stage.Stage;
import ml.WordSuggetionApp;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.ResourceFXUtils;


public class FXEngineWordSearchTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();

        new WordSuggetionApp().start(stage);
    }

    @Test
    public void verify() throws Exception {

        Node query = lookup(".text-field").query();
        clickOn(query);
        write("new york ");
    }

}
