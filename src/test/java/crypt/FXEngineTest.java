package crypt;

import gaming.ex01.SnakeLauncher;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.ResourceFXUtils;


public class FXEngineTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();

        new SnakeLauncher().start(stage);
    }

    @Test
    public void verify() throws Exception {
        KeyCode[] keys = { KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT };
        type(keys);
    }

}
