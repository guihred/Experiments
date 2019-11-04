package fxtests;

import static javafx.scene.input.KeyCode.*;

import fxsamples.PhotoViewer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import labyrinth.*;
import org.junit.Test;
import utils.ConsumerEx;

public class FXLabyrinthTest extends AbstractTestExecution {



    @Test
    public void verifyLabyrinth3DKillerGhostsAndBalls() {
        List<Class<? extends Application>> asList = Arrays.asList(Labyrinth3DWallTexture.class,
            Labyrinth3DKillerGhostsAndBalls.class);
        for (Class<? extends Application> cl : asList) {
            show(cl);
            type(W, 15);
            type(A, 4);
            type(W, 2);
            type(D, 4);
            type(W, 20);
        }
        
    }

    @Test
    public void verifyMouseMovements() {
        FXTesting.verifyAndRun(this, currentStage, () -> {
            moveTo(200, 200);
            moveBy(-1000, 0);
            moveBy(1000, 0);
            type(W, 20);
            clickOn(".root");
            for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z, X, LEFT, RIGHT)) {
                press(keyCode).release(keyCode);
                press(CONTROL, keyCode).release(keyCode);
                press(ALT, keyCode).release(keyCode);
                press(SHIFT, keyCode).release(keyCode);
                release(CONTROL, ALT, SHIFT);
            }
        }, Labyrinth3DMouseControl.class, Labyrinth3DCollisions.class, Labyrinth3D.class, Labyrinth2D.class,
            Labyrinth3DWallTexture.class);
        interactNoWait(currentStage::close);

    }

    @Test
    public void verifyPhotoViewer() {
        show(PhotoViewer.class);
        Set<Node> queryAll = lookup(".button").queryAll();
        queryAll.forEach(ConsumerEx.ignore(t -> {
            for (int i = 0; i < 10; i++) {
                clickOn(t);
            }
        }));
    }


}
