package fxtests;

import static javafx.scene.input.KeyCode.*;

import fxpro.ch07.Chart3dSampleApp;
import fxsamples.JewelViewer;
import fxsamples.SimpleScene3D;
import gaming.ex04.TronLauncher;
import gaming.ex05.TetrisLauncher;
import gaming.ex06.MoleculeSampleApp;
import gaming.ex07.MazeLauncher;
import gaming.ex09.Maze3DLauncher;
import gaming.ex15.RubiksCubeLauncher;
import java.util.Arrays;
import javafx.scene.input.KeyCode;
import labyrinth.Labyrinth3DMouseControl;
import labyrinth.Labyrinth3DWallTexture;
import org.junit.Test;

public class FXLabyrinthTest extends AbstractTestExecution {
	@Test
	public void verifyMouseMovements() throws Exception {
		FXTesting.verifyAndRun(this, currentStage, () -> {
			moveTo(200, 200);
			moveBy(-1000, 0);
			moveBy(1000, 0);
			type(W, 20);
            for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z)) {
                press(keyCode).release(keyCode);
                press(CONTROL, keyCode).release(keyCode);
                press(ALT, keyCode).release(keyCode);
                press(SHIFT, keyCode).release(keyCode);
                release(CONTROL, ALT, SHIFT);
            }
        }, RubiksCubeLauncher.class, TetrisLauncher.class, SimpleScene3D.class, Maze3DLauncher.class,
            Labyrinth3DMouseControl.class, TronLauncher.class, JewelViewer.class, MoleculeSampleApp.class,
            Chart3dSampleApp.class, MazeLauncher.class, Labyrinth3DWallTexture.class);
		interactNoWait(currentStage::close);
	}
}
