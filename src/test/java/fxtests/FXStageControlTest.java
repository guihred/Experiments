package fxtests;

import fxpro.ch02.StageControlExample;
import java.util.Arrays;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import utils.ConsumerEx;

public class FXStageControlTest extends AbstractTestExecution {
    @Test
	public void testLaunch() throws Exception {
        launch(StageControlExample.class,
            new String[] { randomItem(Arrays.asList("TRANSPARENT", "UNDECORATED", "UTILITY")) });
    }


    @Test
    public void verifyStageControlExample() {
        show(StageControlExample.class);
        lookup(StackPane.class).forEach(ConsumerEx.ignore(t -> randomDrag(t, 100)));
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            currentStage.toFront();
        }));

    }
}
