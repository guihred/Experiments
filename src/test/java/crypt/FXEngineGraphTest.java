package crypt;

import java.util.Set;
import javaexercises.graphs.GraphModelLauncher;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;


public class FXEngineGraphTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new GraphModelLauncher().start(stage);
    }

    @Test
    public void verify() throws Exception {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }
        closeCurrentWindow();
    }

}
