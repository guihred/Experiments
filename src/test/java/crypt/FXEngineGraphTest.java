package crypt;

import java.util.Set;
import javaexercises.graphs.Cell;
import javaexercises.graphs.GraphModelLauncher;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.ResourceFXUtils;


public class FXEngineGraphTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        new GraphModelLauncher().start(stage);
    }
    @Test
    public void verifyZoomable() throws Exception {
        Set<Node> queryButtons = lookup(Cell.class::isInstance).queryAll();
        queryButtons.forEach(e -> {
            clickOn(e);
            drag(e, MouseButton.PRIMARY);
            moveBy(100, 100);
            drop();
        });
        scroll(2, VerticalDirection.UP);
        scroll(2, VerticalDirection.DOWN);
    }

    @Test
    public void verify() throws Exception {
        Set<Node> queryButtons = lookup(".button").queryAll();
        for (Node e : queryButtons) {
            clickOn(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void verifyAllTopologies() throws Exception {
        Set<ChoiceBox> queryButtons = lookup(".choice-box").queryAllAs(ChoiceBox.class);
        Set<Node> queryAll = lookup("Go").queryAll();
        for (ChoiceBox e : queryButtons) {

            ObservableList<?> items = e.getItems();
            for (int i = 0; i < items.size(); i++) {
                int j = i;
                interact(() -> e.getSelectionModel().select(j));
                queryAll.forEach(b -> clickOn(b));
            }

        }
    }




}
