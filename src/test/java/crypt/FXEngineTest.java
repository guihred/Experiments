package crypt;

import exercise.java8.RunnableEx;
import gaming.ex01.SnakeLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.query.NodeQuery;
import simplebuilder.ResourceFXUtils;

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

        interactNoWait(RunnableEx.makeRunnable(() -> new DotsLauncher().start(currentStage)));
        NodeQuery lookup = lookup(e -> e instanceof DotsSquare);
        Set<Node> queryAll = lookup.queryAll().stream().limit(20).collect(Collectors.toSet());
        Random random = new Random();
        for (Node next : queryAll) {
            drag(next, MouseButton.PRIMARY);
            if (random.nextBoolean()) {
                moveBy(DotsSquare.SQUARE_SIZE, 0);
            } else {
                moveBy(0,DotsSquare.SQUARE_SIZE);
            }
            drop();
        }
    }

}
