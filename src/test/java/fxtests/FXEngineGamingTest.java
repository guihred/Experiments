package fxtests;

import static java.util.stream.Collectors.toList;
import static javafx.scene.input.KeyCode.*;

import cubesystem.DeathStar;
import cubesystem.GolfBall;
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
import gaming.ex14.PacmanBall;
import gaming.ex14.PacmanLauncher;
import gaming.ex15.RubiksCubeLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex17.PuzzlePiece;
import gaming.ex18.Square2048Launcher;
import gaming.ex19.NumberButton;
import gaming.ex19.SudokuLauncher;
import gaming.ex19.SudokuSquare;
import gaming.ex20.RoundMazeLauncher;
import gaming.ex23.TicTacToeLauncher;
import gaming.ex23.TicTacToeSquare;
import gaming.ex24.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import labyrinth.*;
import org.junit.Test;

public class FXEngineGamingTest extends AbstractTestExecution {
    @Test
    public void verifyCheckers() {
        show(CheckersLauncher.class);
        for (int i = 0; i < 12; i++) {
            List<CheckersSquare> queryAll = lookupList(CheckersSquare.class, CheckersSquare::isBlack);
            Collections.shuffle(queryAll);
            List<CheckersSquare> whitePieces = queryAll.stream().filter(e -> e.getState() == CheckersPlayer.WHITE)
                .collect(toList());
            while (!whitePieces.isEmpty() && queryAll.stream().noneMatch(CheckersSquare::getHighlight)) {
                whitePieces = queryAll.stream().filter(e -> e.getState() == CheckersPlayer.WHITE).collect(toList());
                tryClickOn(randomRemoveItem(whitePieces));
            }
            List<CheckersSquare> collect = queryAll.stream().filter(CheckersSquare::getHighlight).collect(toList());
            tryClickOn(randomItem(collect));
        }
        List<CheckersSquare> collect = IntStream.range(0, 8 * 8).mapToObj(i -> new CheckersSquare(i % 2 == i / 8 % 2))
            .collect(Collectors.toList());
        CheckersHelper.reset(collect);
        getLogger().info("{}", new CheckersTree(collect, 0, null));

    }

    @Test
    public void verifyDeathStar() {
        for (Class<? extends Application> class1 : Arrays.asList(DeathStar.class,
                GolfBall.class)) {
            show(class1);
            for (KeyCode keyCode : Arrays.asList(W, S, A, D, DOWN, UP, LEFT, RIGHT, SPACE)) {
                type(keyCode, nextInt(10));
            }
        }
    }

    @Test
    public void verifyDirections() {
        for (Class<? extends Application> class1 : Arrays.asList(RoundMazeLauncher.class, 
            MazeLauncher.class, TronLauncher.class, TetrisLauncher.class)) {
            show(class1);
            for (KeyCode keyCode : Arrays.asList(W, S, A, D, DOWN, UP, LEFT, RIGHT, SPACE)) {
                type(keyCode, nextInt(20));
            }
        }
    }

    @Test
    public void verifyLabyrinth3DKillerGhostsAndBalls() {
        show(Labyrinth3DKillerGhostsAndBalls.class);
        type(W, 15);
        type(A, 4);
        type(W, 2);
        type(D, 4);
        type(W, 20);
    }

    @Test
    public void verifyLabyrinth3DWallTexture() {
        show(Labyrinth3DWallTexture.class);
        type(W, 15);
        type(A, 4);
        type(W, 2);
        type(D, 4);
        type(W, 20);
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
            Labyrinth2D.class));
    }

    @Test
    public void verifyMemoryLauncher() {
        show(MemoryLauncher.class);
        Set<MemorySquare> lookup = lookup(MemorySquare.class);
        for (int i = 0; i < 5; i++) {
            MemorySquare randomItem = randomItem(lookup);
            tryClickOn(randomItem);
        }
    }

    @Test
    public void verifyMinesweeper() {
        show(MinesweeperLauncher.class);
        List<MinesweeperSquare> queryAll = lookupList(MinesweeperSquare.class);
        Collections.shuffle(queryAll);
        for (int i = 0; i < 30; i++) {
            Node next = queryAll.get(i);
            tryClickOn(next);
            if (tryClickButtons()) {
                return;
            }
        }
    }

    @Test
    public void verifyMouseMovements() {
        show(Maze3DLauncher.class);
        moveTo(200, 200);
        moveBy(-1000, 0);
        moveBy(1000, 0);
        type(W, 20);
        clickOn(".root");
        clickOn(".root");
        for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z, X, LEFT, RIGHT)) {
            press(keyCode).release(keyCode);
            press(CONTROL, keyCode).release(keyCode);
            press(ALT, keyCode).release(keyCode);
            press(SHIFT, keyCode).release(keyCode);
            release(CONTROL, ALT, SHIFT);
        }
    }

    @Test
    public void verifyPacman() {
        for (Class<? extends Application> class1 : Arrays.asList(PacmanLauncher.class)) {
            show(class1);
            for (KeyCode keyCode : Arrays.asList(DOWN, UP, LEFT, RIGHT, SPACE)) {
                type(keyCode, nextInt(10));
            }
        }
        interactNoWait(() -> {
            PacmanBall pacmanBall = new PacmanBall(2, 2);
            pacmanBall.setSpecial(false);
            pacmanBall.setSpecial(true);
            pacmanBall.setSpecial(false);
        });

    }

    @Test
    public void verifyPlatformMain() {
        show(PlatformMain.class);
        for (KeyCode keyCode : Arrays.asList(W, S, A, D, DOWN, UP, LEFT, RIGHT, SPACE)) {
            type(keyCode, 5);
        }
        press(RIGHT);
        sleep(5000);
    }

    @Test
    public void verifyPuzzle() {
        show(PuzzleLauncher.class);
        List<PuzzlePiece> queryAll = lookupList(PuzzlePiece.class, PuzzlePiece::isVisible);
        int squareSize = (int) DotsSquare.SQUARE_SIZE;
        for (int i = 0; i < queryAll.size() / 5; i++) {
            Node next = queryAll.get(i);
            randomDrag(next, squareSize * 3);
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
    public void verifySlidingPuzzleLauncher() {
        show(SlidingPuzzleLauncher.class);
        lookup(SlidingPuzzleSquare.class).forEach(this::clickOn);
    }

    @Test
    public void verifySnake() {
        SnakeLauncher show = show(SnakeLauncher.class);
        List<SnakeSquare> lookup = lookupList(SnakeSquare.class);
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
        Set<Node> queryAll = lookupList(SudokuSquare.class, e -> !e.isPermanent()).stream().limit(20)
            .collect(Collectors.toSet());
        for (Node next : queryAll) {
            drag(next, MouseButton.PRIMARY);
            List<NumberButton> buttons = lookupList(NumberButton.class);
            NumberButton randomItem = randomItem(buttons);
            moveTo(randomItem);
            drop();
        }
    }

    @Test
    public void verifyTicTacToe() {
        show(TicTacToeLauncher.class);
        List<TicTacToeSquare> queryAll = lookupList(TicTacToeSquare.class);
        for (int i = 0; i < 6; i++) {
            Collections.shuffle(queryAll);
            for (TicTacToeSquare ticTacToeSquare : queryAll) {
                tryClickOn(ticTacToeSquare);
                if (tryClickButtons()) {
                    break;
                }
            }
        }
    }
}
