package fxtests;

import fxpro.ch02.StageControlExample;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import utils.ConsumerEx;

public class FXStageControlTest extends AbstractTestExecution {
    @Test
    public void verifyStageControlExample() {
        show(StageControlExample.class);
        lookup(StackPane.class).forEach(ConsumerEx.ignore(t -> {
            drag(t, MouseButton.PRIMARY);
            moveRandom(100);
            drop();
        }));
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            currentStage.toFront();
        }));

    }
}
