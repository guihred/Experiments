package crypt;

import exercise.java8.RunnableEx;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import labyrinth.*;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

public class LabyrinthTest extends ApplicationTest {

    private Stage currentStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.currentStage = stage;
        new Labyrinth3DKillerGhostsAndBalls().start(stage);
    }

    @Test
    public void testLabyrinth() throws Exception {
        type(KeyCode.UP, 10);
        type(KeyCode.DOWN, 10);
    }

    @Test
    public void testLabyrinth2() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DKillerGhosts().start(currentStage);
        }));
        
        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth3() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DGhosts().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth4() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DCollisions().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth5() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DMouseControl().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth6() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DWallTexture().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth7() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3DAntiAliasing().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth8() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth3D().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

    @Test
    public void testLabyrinth9() throws Exception {
        Platform.runLater(RunnableEx.makeRunnable(() -> {
            currentStage.close();
            new Labyrinth2D().start(currentStage);
        }));

        type(KeyCode.A, 5);
        type(KeyCode.W, 20);
        type(KeyCode.D, 20);
        type(KeyCode.S, 20);
        type(KeyCode.A, 20);
    }

}
