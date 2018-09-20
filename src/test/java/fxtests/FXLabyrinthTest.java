package fxtests;

import static javafx.scene.input.KeyCode.*;

import gaming.ex15.RubiksCubeLauncher;
import javafx.stage.Stage;
import labyrinth.Labyrinth3DMouseControl;
import labyrinth.Labyrinth3DWallTexture;
import language.FXTesting;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.ResourceFXUtils;

public class FXLabyrinthTest extends ApplicationTest {
    private Stage currentStage;

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage;
    }

    @Test
    public void verifyMouseMovements() throws Exception {
        FXTesting.verifyAndRun(this, currentStage, () -> {
            moveTo(200, 200);
            moveBy(-1000, 0);
            moveBy(1000, 0);
            type(W, 20);
            press(W, S, A, DOWN, D, UP, R, L, U, D, B, F);
            sleep(1000);
        }, RubiksCubeLauncher.class, Labyrinth3DMouseControl.class, Labyrinth3DWallTexture.class);
    }



}
