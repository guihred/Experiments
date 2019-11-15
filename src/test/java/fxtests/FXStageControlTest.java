package fxtests;

import static fxtests.FXTesting.measureTime;

import fxpro.ch02.StageControlExample;
import fxsamples.DraggingRectangle;
import java.util.Arrays;
import javafx.geometry.VerticalDirection;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.junit.Test;
import utils.ConsumerEx;

public class FXStageControlTest extends AbstractTestExecution {
    @Test
	public void testLaunch() throws Exception {
        launch(StageControlExample.class,
            new String[] { randomItem(Arrays.asList("TRANSPARENT", "UNDECORATED", "UTILITY")) });
    }


    @Test
    public void verifyDrag() {
        measureTime("Test.verifyDrag", () -> verifyAndRun(() -> {
            lookup(Circle.class).forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
                randomDrag(t, 50);
            });
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
        }, Arrays.asList(DraggingRectangle.class)));
    }

    @Test
    public void verifyStageControlExample() {
        show(StageControlExample.class);
        lookup(StackPane.class).forEach(t -> randomDrag(t, 100));
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            currentStage.toFront();
        }));

    }
}
