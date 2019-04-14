package fxtests;

import static fxtests.FXTesting.measureTime;

import ex.j8.Chapter4;
import fxpro.ch02.PongLauncher;
import fxsamples.PlayingAudio;
import gaming.ex01.SnakeLauncher;
import gaming.ex10.MinesweeperLauncher;
import gaming.ex10.MinesweeperSquare;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex13.CardStack;
import gaming.ex13.SolitaireCard;
import gaming.ex13.SolitaireLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex17.PuzzlePiece;
import gaming.ex18.Square2048Launcher;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import org.junit.Test;
import pdfreader.PdfReader;
import utils.RunnableEx;

public class FXEngineTest extends AbstractTestExecution {

	@Test
	public void verifyButtons() throws Exception {
		measureTime("Test.testButtons",
				() -> FXTesting.verifyAndRun(this, currentStage, () -> lookup(".button").queryAll().forEach(t -> {
					sleep(1000);
					clickOn(t);
					type(KeyCode.ESCAPE);
				}), Chapter4.Ex9.class, PlayingAudio.class, PdfReader.class));

	}

	@Test
	public void verifyDots() throws Exception {
		show(DotsLauncher.class);
		Set<Node> queryAll = lookup(e -> e instanceof DotsSquare).queryAll().stream().limit(20)
				.collect(Collectors.toSet());
		Random random = new Random();
		for (Node next : queryAll) {
			drag(next, MouseButton.PRIMARY);
            int a = random.nextBoolean() ? 1 : -1;
			if (random.nextBoolean()) {
                moveBy(a * DotsSquare.SQUARE_SIZE, 0);
			} else {
                moveBy(0, a * DotsSquare.SQUARE_SIZE);
			}
			drop();
		}
	}

    @Test
	public void verifyMinesweeper() throws Exception {
		show(MinesweeperLauncher.class);
        List<Node> queryAll = lookup(e -> e instanceof MinesweeperSquare).queryAll()
            .parallelStream()
            .collect(Collectors.toList());
		Collections.shuffle(queryAll);
        for (int i = 0; i < 30; i++) {
            Node next = queryAll.get(i);
            clickOn(next);
			tryClickButtons();
        }
	}

	@Test
	public void verifyPong() throws Exception {
		show(PongLauncher.class);
		tryClickButtons();
		for (Node next : lookup(e -> e instanceof Rectangle && e.isVisible()).queryAll()) {
			drag(next, MouseButton.PRIMARY);
			moveBy(0, DotsSquare.SQUARE_SIZE);
			moveBy(0, -DotsSquare.SQUARE_SIZE);
			drop();
		}
	}

	@Test
	public void verifyPuzzle() throws Exception {
	    show(PuzzleLauncher.class);
        interactNoWait(() -> currentStage.setMaximized(true));
        List<Node> queryAll = lookup(e -> e instanceof PuzzlePiece).queryAll().stream().filter(e -> e.isVisible())
            .collect(Collectors.toList());
        int squareSize = DotsSquare.SQUARE_SIZE;
        for (int i = 0; i < queryAll.size() / 5; i++) {
            Node next = queryAll.get(i);
            RunnableEx.makeRunnable(() -> drag(next, MouseButton.PRIMARY)).run();
            moveBy(Math.random() * squareSize - squareSize / 2, Math.random() * squareSize - squareSize / 2);
	        drop();
        }
        interactNoWait(() -> currentStage.setMaximized(false));
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
		Collections.shuffle(cardStacks);
		for (CardStack cardStack : cardStacks) {
			if (cardStack.getChildren().size() <= 1) {
				continue;
			}
			Node card = getLastCard(cardStack);
			clickOn(cardStack);
			for (CardStack stack : cardStacks) {
				drag(card, MouseButton.PRIMARY);
				moveTo(stack);
				drop();
				if (!cardStack.getChildren().contains(card)) {
					if (cardStack.getChildren().size() > 1) {
						card = getLastCard(cardStack);
						clickOn(cardStack);
					} else {
						continue;
					}
				}

			}
		}
	}

	@Test
	public void verifySquare() throws Exception {
		show(Square2048Launcher.class);
		type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
	}

	private Node getLastCard(CardStack cardStack) {

		ObservableList<Node> children = cardStack.getChildren();
		Optional<Node> findFirst = children.stream().filter(e -> e instanceof SolitaireCard)
				.filter(e -> ((SolitaireCard) e).isShown()).findFirst();
		if (findFirst.isPresent()) {
			return findFirst.get();
		}

		return children.get(children.size() - 1);
	}

}
