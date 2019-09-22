package fxtests;

import static fxtests.FXTesting.measureTime;
import static javafx.scene.input.KeyCode.*;
import static utils.RunnableEx.ignore;

import audio.mp3.FilesComparator;
import cubesystem.DeathStar;
import ethical.hacker.EthicalHackApp;
import ethical.hacker.ImageCrackerApp;
import ex.j8.Chapter4;
import fxpro.ch02.PongLauncher;
import fxpro.ch07.Chart3dSampleApp;
import fxsamples.*;
import gaming.ex01.SnakeLauncher;
import gaming.ex04.TronLauncher;
import gaming.ex05.TetrisLauncher;
import gaming.ex07.MazeLauncher;
import gaming.ex09.Maze3DLauncher;
import gaming.ex10.MinesweeperLauncher;
import gaming.ex10.MinesweeperSquare;
import gaming.ex11.DotsLauncher;
import gaming.ex11.DotsSquare;
import gaming.ex13.CardStack;
import gaming.ex13.SolitaireCard;
import gaming.ex13.SolitaireLauncher;
import gaming.ex14.PacmanLauncher;
import gaming.ex15.RubiksCubeLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex17.PuzzlePiece;
import gaming.ex18.Square2048Launcher;
import gaming.ex20.RoundMazeLauncher;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import labyrinth.Labyrinth3DMouseControl;
import labyrinth.Labyrinth3DWallTexture;
import ml.WordSuggetionApp;
import ml.WorldMapExample;
import ml.WorldMapExample2;
import org.junit.Test;
import paintexp.ColorChooser;
import pdfreader.PdfReader;
import schema.sngpc.SngpcViewer;
import utils.ConsoleUtils;
import utils.ConsumerEx;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXEngineTest extends AbstractTestExecution {

    // @Test
    public void verifyButtons() throws Exception {
        measureTime("Test.testButtons",
            () -> FXTesting.verifyAndRun(this, currentStage, () -> lookup(".button").queryAll().forEach(t -> {
                sleep(1000);
                RunnableEx.ignore(() -> clickOn(t));
                type(KeyCode.ESCAPE);
            }), Chapter4.Ex9.class, PdfReader.class));

    }

    @Test
    public void verifyColorChooser() throws Exception {
        show(ColorChooser.class);

        List<Node> queryAll = lookup(".slider").queryAll().stream().collect(Collectors.toList());
        for (int i = 0; i < queryAll.size(); i++) {
            if (i == 3) {
                lookup(".tab").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
            }
            Node m = queryAll.get(i);
            drag(m, MouseButton.PRIMARY);
            moveBy(Math.random() * 10 - 5, 0);
            drop();
        }
        tryClickButtons();
    }

    // @Test
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

    // @Test
    public void verifyEthicalHack() throws Exception { 
        show(EthicalHackApp.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText()))
            .forEach(ConsumerEx.ignore(this::clickOn));
        ConsoleUtils.waitAllProcesses();
    }

    // @Test
    public void verifyFileComparator() throws Exception {
        FilesComparator application = show(FilesComparator.class);
        TableView<File> query = lookup(e -> e instanceof TableView).query();
        File[] listFiles = ResourceFXUtils.getUserFolder("Music").listFiles(File::isDirectory);
        application.addSongsToTable(query, listFiles[0]);
    }

    // @Test
    public void verifyImageCracker() throws Exception {
        ImageCrackerApp show = show(ImageCrackerApp.class);
        show.setClickable(false);
        Platform.runLater(show::loadURL);
        ImageCrackerApp.waitABit();
    }

    // @Test
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

    // @Test
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

    // @Test
    public void verifyPlayingAudio() throws Exception {
        PlayingAudio show = show(PlayingAudio.class);
        interactNoWait(() -> show.playMedia(ResourceFXUtils.toExternalForm("TeenTitans.mp3")));
        tryClickButtons();
    }

    // @Test
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

    // @Test
    public void verifyPuzzle() throws Exception {
        show(PuzzleLauncher.class);
        interactNoWait(() -> currentStage.setMaximized(true));
        List<Node> queryAll = lookup(e -> e instanceof PuzzlePiece).queryAll().stream().filter(e -> e.isVisible())
            .collect(Collectors.toList());
        int squareSize = DotsSquare.SQUARE_SIZE;
        for (int i = 0; i < queryAll.size() / 5; i++) {
            Node next = queryAll.get(i);
            RunnableEx.ignore(() -> drag(next, MouseButton.PRIMARY));
            moveBy(Math.random() * squareSize - squareSize / 2, Math.random() * squareSize - squareSize / 2);
            drop();
        }
        interactNoWait(() -> currentStage.setMaximized(false));
    }

    // @Test
    public void verifyScroll() throws Exception {
        measureTime("Test.verifyScroll",
            () -> FXTesting.verifyAndRun(this, currentStage, () -> lookup(".button").queryAll().forEach(t -> {
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
            }), WorldMapExample.class, WorldMapExample2.class));

    }

    // @Test
    public void verifySnake() throws Exception {
        show(SnakeLauncher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }

    // @Test
    public void verifySngpcViewer() throws Exception {
        show(SngpcViewer.class);
        sleep(500);
        Node tree = lookup(e -> e instanceof TreeView).queryAll().stream().limit(1).findFirst().orElse(null);
        targetPos(Pos.TOP_CENTER);
        clickOn(tree);
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
    }

    // @Test
    public void verifySolitaire() throws Exception {
        show(SolitaireLauncher.class);
        List<CardStack> cardStacks = lookup(".cardstack").queryAllAs(CardStack.class).stream()
            .collect(Collectors.toList());
        Collections.shuffle(cardStacks);
        targetPos(Pos.TOP_CENTER);
        for (CardStack cardStack : cardStacks) {
            if (cardStack.getCards().isEmpty()) {
                continue;
            }
            Node card = getLastCard(cardStack);
            clickOn(cardStack);
            for (CardStack stack : cardStacks) {
                drag(card, MouseButton.PRIMARY);
                moveTo(stack);
                drop();
                if (!cardStack.getCards().contains(card)) {
                    if (cardStack.getCards().size() <= 1) {
                        continue;
                    }
                    card = getLastCard(cardStack);
                    clickOn(cardStack);
                }
            }
        }
        targetPos(Pos.CENTER);
    }

    // @Test
    public void verifySquare() throws Exception {
        show(Square2048Launcher.class);
        type(KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT);
    }

    // @Test
    public void verifyWordSuggetion() throws Exception {
        show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
            clickOn(t);
            write("new york ");
        }));
    }

    private static Node getLastCard(CardStack cardStack) {

        ObservableList<SolitaireCard> children = cardStack.getCards();
        Optional<SolitaireCard> findFirst = children.stream().filter(e -> e.isShown()).findFirst();
        return findFirst.orElseGet(() -> children.get(children.size() - 1));
    }

}
