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
import gaming.ex04.TronLauncher;
import gaming.ex05.TetrisLauncher;
import gaming.ex07.MazeLauncher;
import gaming.ex09.Maze3DLauncher;
import gaming.ex10.MinesweeperLauncher;
import gaming.ex10.MinesweeperSquare;
import gaming.ex11.DotsSquare;
import gaming.ex14.PacmanLauncher;
import gaming.ex15.RubiksCubeLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex17.PuzzlePiece;
import gaming.ex18.Square2048Launcher;
import gaming.ex20.RoundMazeLauncher;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import labyrinth.Labyrinth3DMouseControl;
import labyrinth.Labyrinth3DWallTexture;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineGamingTest extends AbstractTestExecution {



    @Test
    public void verifyMinesweeper() throws Exception {
		show(MinesweeperLauncher.class);
        List<Node> queryAll = lookup(e -> e instanceof MinesweeperSquare).queryAll().parallelStream()
            .collect(Collectors.toList());
        Collections.shuffle(queryAll);
        for (int i = 0; i < 30; i++) {
            Node next = queryAll.get(i);
            ignore(() -> clickOn(next));
            tryClickButtons();
        }
    }

    @Test
    public void verifyMouseMovements() throws Exception {
        FXTesting.verifyAndRun(this, currentStage, () -> {
            moveTo(200, 200);
            moveBy(-1000, 0);
            moveBy(1000, 0);
            type(W, 20);
            for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z, X, LEFT, RIGHT)) {
                press(keyCode).release(keyCode);
                press(CONTROL, keyCode).release(keyCode);
                press(ALT, keyCode).release(keyCode);
                press(SHIFT, keyCode).release(keyCode);
                release(CONTROL, ALT, SHIFT);
            }
        }, RubiksCubeLauncher.class, TetrisLauncher.class, SimpleScene3D.class, Maze3DLauncher.class,
            Labyrinth3DMouseControl.class, TronLauncher.class, JewelViewer.class, MoleculeSampleApp.class,
            DeathStar.class, Chart3dSampleApp.class, PacmanLauncher.class, RoundMazeLauncher.class, MazeLauncher.class,
            Labyrinth3DWallTexture.class, RaspiCycle.class);
        interactNoWait(currentStage::close);
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
        List<Node> queryAll = lookup(e -> e instanceof PuzzlePiece).queryAll().stream().filter(e -> e.isVisible())
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
    public void verifySnake() throws Exception {
        show(SnakeLauncher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }


    @Test
    public void verifySquare() throws Exception {
        show(Square2048Launcher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }


}
