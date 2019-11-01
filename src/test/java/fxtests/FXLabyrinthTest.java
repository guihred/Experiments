package fxtests;

import static fxtests.FXTesting.measureTime;
import static javafx.scene.input.KeyCode.*;

import fxpro.ch07.Chart3dDemo;
import fxsamples.PhotoViewer;
import japstudy.JapaneseLessonApplication;
import java.util.Arrays;
import java.util.Set;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import labyrinth.*;
import ml.graph.Chart3dGraph;
import org.junit.Test;
import utils.ConsumerEx;

public class FXLabyrinthTest extends AbstractTestExecution {
    @Test
    public void verifyJapaneseLessonApplication() {
        show(JapaneseLessonApplication.class);
        Set<Button> lookup = lookup(Button.class);
        doubleClickOn(randomItem(lookup(Cell.class)));
        lookup(Button.class).stream().filter(t -> !lookup.contains(t)).forEach(this::clickOn);
        clickOn(randomItem(lookup(Cell.class)));
        type(KeyCode.SHIFT);
        clickButtonsWait();
        type(KeyCode.ENTER);
        type(KeyCode.SHIFT);
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

    @Test
    public void verifyScroll() {
        measureTime("Test.verifyScroll", () -> FXTesting.verifyAndRun(this, currentStage, () -> {
            lookup(".root").queryAll().forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
                randomDrag(t, 50);
            });
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
        }, Chart3dDemo.class, Chart3dGraph.class));
    }
}
