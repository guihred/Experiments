package fxtests;

import ethical.hacker.EthicalHackApp;
import java.util.Set;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.ResourceFXUtils;


public class FXEngineEthicalHackTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        new EthicalHackApp().start(stage);
    }

    @Test
    public void verify() throws Exception {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }

    }





}
