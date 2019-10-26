package fxtests;

import fxpro.ch02.StageControlExample;
import org.junit.Test;
import utils.ConsumerEx;

public class FXStageControlTest extends AbstractTestExecution {
    @Test
    public void verifyStageControlExample() {
        show(StageControlExample.class);
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(t -> {
            clickOn(t);
            currentStage.toFront();
        }));
    }
}
