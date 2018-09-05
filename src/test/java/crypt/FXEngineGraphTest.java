package crypt;

import java.util.Set;
import javaexercises.graphs.GraphModelLauncher;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;


public class FXEngineGraphTest extends ApplicationTest {
    JFXPanel jfxPanel = new JFXPanel();

    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        new GraphModelLauncher().start(stage);
    }

    @Test
    public void verify() throws Exception {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }
    }

}
