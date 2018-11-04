package fxtests;

import fxpro.ch02.PongLauncher;
import gaming.ex01.SnakeLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex18.Square2048Launcher;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineTest extends ApplicationTest {

    private Stage currentStage;

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage;
    }

    @Test
    public void verify() throws Exception {
        interactNoWait(RunnableEx.makeRunnable(() -> new SnakeLauncher().start(currentStage)));
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
        interactNoWait(RunnableEx.makeRunnable(() -> new Square2048Launcher().start(currentStage)));
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
        interactNoWait(RunnableEx.makeRunnable(() -> new DotsLauncher().start(currentStage)));
        Set<Node> queryAll = lookup(e -> e instanceof DotsSquare).queryAll().stream().limit(20).collect(Collectors.toSet());
        Random random = new Random();
        for (Node next : queryAll) {
            drag(next, MouseButton.PRIMARY);
            if (random.nextBoolean()) {
                moveBy(DotsSquare.SQUARE_SIZE, 0);
            } else {
                moveBy(0, DotsSquare.SQUARE_SIZE);
            }
            drop();
        }
        interactNoWait(RunnableEx.makeRunnable(() -> new PongLauncher().start(currentStage)));
        interactNoWait(RunnableEx.makeRunnable(() -> currentStage.setMaximized(true)));
        lookup(".button").queryAll().forEach(this::clickOn);
        for (Node next : lookup(e -> e instanceof Rectangle && e.isVisible()).queryAll()) {
            drag(next, MouseButton.PRIMARY);
            moveBy(0, DotsSquare.SQUARE_SIZE);
            moveBy(0, -DotsSquare.SQUARE_SIZE);
            drop();
        }
    }

}
