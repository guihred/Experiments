package fxtests;

import static fxtests.FXTesting.measureTime;

import ex.j8.Chapter4;
import fxpro.ch02.PongLauncher;
import fxsamples.PlayingAudio;
import gaming.ex01.SnakeLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex13.CardStack;
import gaming.ex13.SolitaireLauncher;
import gaming.ex18.Square2048Launcher;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import pdfreader.PdfReader;
import utils.ConsumerEx;

public class FXEngineTest extends AbstractTestExecution {


    @Test
    public void verifyButtons() throws Exception {
        measureTime("Test.testButtons", () -> FXTesting.verifyAndRun(e -> e.clickOn(".button"), PdfReader.class,
            Chapter4.Ex9.class, PlayingAudio.class));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void verifyDots() throws Exception {
        show(DotsLauncher.class);
        Set<Node> queryAll = lookup(e -> e instanceof DotsSquare).queryAll().stream().limit(20)
            .collect(Collectors.toSet());
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
    }

    @Test
    public void verifyPong() throws Exception {
        show(PongLauncher.class);
        lookup(".button").queryAll().stream().forEach(ConsumerEx.makeConsumer(t -> clickOn(t, MouseButton.PRIMARY)));
        for (Node next : lookup(e -> e instanceof Rectangle && e.isVisible()).queryAll()) {
            drag(next, MouseButton.PRIMARY);
            moveBy(0, DotsSquare.SQUARE_SIZE);
            moveBy(0, -DotsSquare.SQUARE_SIZE);
            drop();
        }
    }

    @Test
    public void verifySnake() throws Exception {
        show(SnakeLauncher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }

    @Test
    public void verifySolitaire() throws Exception {
        show(SolitaireLauncher.class);
        List<CardStack> cardStacks = lookup(".cardstack").queryAllAs(CardStack.class).stream()
            .collect(Collectors.toList());
        List<Node> cards = cardStacks.stream().filter(e -> !e.getChildren().isEmpty())
            .map(e -> e.getChildren().get(e.getChildren().size() - 1)).collect(Collectors.toList());
        for (Node node : cards) {
            for (CardStack cardStack : cardStacks) {
                drag(node, MouseButton.PRIMARY);
                moveTo(cardStack);
                drop();
            }
        }
    }

    @Test
    public void verifySquare() throws Exception {
        show(Square2048Launcher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }

}
