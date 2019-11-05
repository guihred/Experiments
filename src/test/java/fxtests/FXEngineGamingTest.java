package fxtests;

import static javafx.scene.input.KeyCode.*;
import static utils.RunnableEx.ignore;

import cubesystem.DeathStar;
import fxpro.ch02.PongLauncher;
import fxpro.ch07.Chart3dSampleApp;
import fxsamples.JewelViewer;
import fxsamples.MoleculeSampleApp;
import fxsamples.RaspiCycle;
import fxsamples.SimpleScene3D;
import gaming.ex01.SnakeLauncher;
import gaming.ex01.SnakeSquare;
import gaming.ex01.SnakeState;
import gaming.ex02.MemoryLauncher;
import gaming.ex02.MemorySquare;
import gaming.ex03.SlidingPuzzleLauncher;
import gaming.ex03.SlidingPuzzleSquare;
import gaming.ex04.TronLauncher;
import gaming.ex05.TetrisLauncher;
import gaming.ex07.MazeLauncher;
import gaming.ex09.Maze3DLauncher;
import gaming.ex10.MinesweeperLauncher;
import gaming.ex10.MinesweeperSquare;
import gaming.ex11.DotsSquare;
import gaming.ex12.PlatformMain;
import gaming.ex14.PacmanLauncher;
import gaming.ex15.RubiksCubeLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex17.PuzzlePiece;
import gaming.ex18.Square2048Launcher;
import gaming.ex19.NumberButton;
import gaming.ex19.SudokuLauncher;
import gaming.ex19.SudokuSquare;
import gaming.ex20.RoundMazeLauncher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import labyrinth.*;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineGamingTest extends AbstractTestExecution {
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
	public void verifyLabyrinthMouseMovements() {
		verifyAndRun(() -> {
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
		}, Arrays.asList(Labyrinth3DMouseControl.class, Labyrinth3DCollisions.class, Labyrinth3D.class,
				Labyrinth2D.class, Labyrinth3DWallTexture.class));
	}

	@Test
	public void verifyMemoryLauncher() {
		show(MemoryLauncher.class);
		Set<MemorySquare> lookup = lookup(MemorySquare.class);
		for (int i = 0; i < 5; i++) {
			MemorySquare randomItem = randomItem(lookup);
			ignore(() -> clickOn(randomItem));
		}
	}

	@Test
	public void verifyMinesweeper() {
		show(MinesweeperLauncher.class);
		List<MinesweeperSquare> queryAll = lookup(MinesweeperSquare.class).parallelStream()
				.collect(Collectors.toList());
		Collections.shuffle(queryAll);
		for (int i = 0; i < 30; i++) {
			Node next = queryAll.get(i);
			ignore(() -> clickOn(next));
			if (tryClickButtons()) {
				return;
			}
		}
	}

	@Test
	public void verifyMouseMovements() {
		verifyAndRun(() -> {
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
		}, Arrays.asList(Maze3DLauncher.class, JewelViewer.class, MoleculeSampleApp.class, Chart3dSampleApp.class,
				SimpleScene3D.class));
	}

	@Test
	public void verifyPlatformMain() {
		for (Class<? extends Application> class1 : Arrays.asList(RoundMazeLauncher.class, DeathStar.class,
				RaspiCycle.class, PacmanLauncher.class, PlatformMain.class, MazeLauncher.class, TronLauncher.class,
				TetrisLauncher.class)) {
			show(class1);
			for (KeyCode keyCode : Arrays.asList(W, S, A, D, DOWN, UP, LEFT, RIGHT, SPACE)) {
				type(keyCode, nextInt(5));
			}
		}
	}

	@Test
	public void verifyPong() {
		show(PongLauncher.class);
		tryClickButtons();
		for (Node next : lookup(Rectangle.class).stream().filter(e -> e.isVisible()).collect(Collectors.toSet())) {
			drag(next, MouseButton.PRIMARY);
			moveBy(0, DotsSquare.SQUARE_SIZE);
			moveBy(0, -DotsSquare.SQUARE_SIZE);
			drop();
		}
		type(KeyCode.A, KeyCode.COMMA, KeyCode.Z, KeyCode.L, KeyCode.F);
		sleep(1000);
		type(KeyCode.A, KeyCode.COMMA, KeyCode.Z, KeyCode.L, KeyCode.F);
	}

	@Test
	public void verifyPuzzle() {
		show(PuzzleLauncher.class);
		List<Node> queryAll = lookup(PuzzlePiece.class).stream().filter(PuzzlePiece::isVisible)
				.collect(Collectors.toList());
		double squareSize = DotsSquare.SQUARE_SIZE;
		for (int i = 0; i < queryAll.size() / 5; i++) {
			Node next = queryAll.get(i);
			RunnableEx.ignore(() -> drag(next, MouseButton.PRIMARY));
			moveBy(Math.random() * squareSize - squareSize / 2, Math.random() * squareSize - squareSize / 2);
			drop();
		}
	}

	@Test
	public void verifyRubiksCube() {
		show(RubiksCubeLauncher.class);
		moveBy(1000, 0);
		type(W, 20);
		moveTo(".root");
		for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z, X, LEFT, RIGHT)) {
			press(keyCode).release(keyCode);
			press(CONTROL, keyCode).release(keyCode);
			press(ALT, keyCode).release(keyCode);
			press(SHIFT, keyCode).release(keyCode);
			release(CONTROL, ALT, SHIFT);
		}
	}

	@Test
	public void verifySimpleScene3D() {
		show(SimpleScene3D.class);
		Node query = lookup(".root").query();
		randomDrag(query, 100);
	}

	@Test
	public void verifySlidingPuzzleLauncher() {
		show(SlidingPuzzleLauncher.class);
		lookup(SlidingPuzzleSquare.class).forEach(this::clickOn);
	}

	@Test
	public void verifySnake() {
		SnakeLauncher show = show(SnakeLauncher.class);
		List<SnakeSquare> lookup = lookup(SnakeSquare.class).stream().collect(Collectors.toList());
		while (true) {
			SnakeSquare food = lookup.stream().filter(e -> e.getState() == SnakeState.FOOD).findFirst()
					.orElse(lookup.get(0));
			SnakeSquare snake = show.getSnake().get(0);
			if (snake.getI() != food.getI()) {
				type(snake.getI() > food.getI() ? KeyCode.LEFT : KeyCode.RIGHT);
			}
			sleep(50);
			if (snake.getJ() != food.getJ()) {
				type(snake.getJ() > food.getJ() ? KeyCode.UP : KeyCode.DOWN);
			}
			if (randomNumber(5) == 0) {
				type(KeyCode.F);
			}
			sleep(50);
			if (tryClickButtons() || show.getSnake().size() == 10) {
				break;
			}
		}
	}

	@Test
	public void verifySquare() {
		show(Square2048Launcher.class);
		type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
	}

	@Test
	public void verifySudokuLauncher() {
		show(SudokuLauncher.class);
		Set<Node> queryAll = lookup(SudokuSquare.class).stream().filter(e -> !e.isPermanent()).limit(20)
				.collect(Collectors.toSet());
		for (Node next : queryAll) {
			drag(next, MouseButton.PRIMARY);
			List<NumberButton> buttons = lookup(NumberButton.class).stream().collect(Collectors.toList());
			NumberButton randomItem = randomItem(buttons);
			moveTo(randomItem);
			drop();
		}
	}
}
