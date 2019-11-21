package gaming.ex24;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;

public class CheckersLauncher extends Application {

    private static final int SIZE = 8;
    private GridPane gridPane;
    private int currentPlayer;

    private List<CheckersPlayer> players = Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK);
    private List<CheckersSquare> squares = new ArrayList<>();

    public void initilize() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boolean black = (i + j) % 2 == 0;
                CheckersSquare child = new CheckersSquare(black);
                if (black) {
                    if (i < 3) {
                        child.setState(CheckersPlayer.BLACK);
                    }
                    if (i > SIZE - 4) {
                        child.setState(CheckersPlayer.WHITE);
                    }
                }
                squares.add(child);
                child.setOnMouseClicked(e0 -> {
                    Object target = e0.getSource();
                    if (target instanceof CheckersSquare) {
                        onClick((CheckersSquare) target);
                    }
                });
                gridPane.add(child, j, i);
            }
        }
    }

    @Override
    public void start(Stage stage) {
        gridPane = new GridPane();
        initilize();

        Scene scene = new Scene(gridPane);
        stage.setTitle("Checkers");
        stage.setScene(scene);
        stage.show();
    }

    private boolean gameOver() {
        return squares.stream().map(CheckersSquare::getState).distinct().count() < 3;
    }

    private CheckersPlayer getWinner() {
        return squares.stream().map(e -> e.getState()).filter(e -> e != CheckersPlayer.NONE).findFirst()
            .orElse(CheckersPlayer.NONE);
    }

    private void highlightPossibleMovements(CheckersPlayer player, CheckersSquare target) {
        int indexOf = squares.indexOf(target);
        final int i = indexOf / SIZE;
        final int j = indexOf % SIZE;
        for (int dirI = player.getDir(); dirI == player.getDir()
            || target.getQueen() && dirI == -player.getDir(); dirI += 2) {
            for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
                int iterations = !target.getQueen() ? 1 : SIZE - 1;
                for (int k = 1; k <= iterations; k++) {
                    if (!withinBounds(j + dirJ * k) || !withinBounds(i + dirI * k)) {
                        continue;
                    }
                    CheckersSquare checkersSquare = squares.get(toIndex(i + dirI * k, j + dirJ * k));
                    if (checkersSquare.getState() == CheckersPlayer.NONE) {
                        checkersSquare.setHighlight(true);
                    }
                    markPossibleKills(player, i + dirI * k, j + dirJ * k, dirI * k, dirJ * k);
                    if (checkersSquare.getState() == player) {
                        break;
                    }
                }
            }
        }
        for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
            markPossibleKills(player, i - player.getDir(), j + dirJ, -player.getDir(), dirJ);
        }
    }

    private void markPossibleKills(CheckersPlayer player, int i, int j, int dirI, int dirJ) {
        if (!withinBounds(i) || !withinBounds(j)) {
            return;
        }
        CheckersSquare checkersSquare = squares.get(toIndex(i, j));
        if (checkersSquare.getState() == player.opposite()) {
            if (withinBounds(i + dirI) && withinBounds(j + dirJ)) {
                int index = toIndex(i + dirI, j + dirJ);
                CheckersSquare square2 = squares.get(index);
                if (square2.getState() == CheckersPlayer.NONE) {
                    checkersSquare.setMarked(true);
                    square2.setHighlight(true);

                    for (int dirI2 = -1; dirI2 == -1
//                        || target.getQueen() && dirI == -player.getDir()
                    ; dirI2 += 2) {
                        for (int dirJ2 = -1; dirJ2 <= 1; dirJ2 += 2) {
                            markPossibleKills(player, i + dirI + dirI2, j + dirJ + dirJ2, dirI2, dirJ2);
                        }
                    }
                }
            }
        }
    }

    private void onClick(CheckersSquare target) {
        CheckersPlayer player = players.get(currentPlayer % players.size());
        if (target.getState() == player) {
            squares.forEach(e -> e.setSelected(false));
            squares.forEach(e -> e.setHighlight(false));
            squares.forEach(e -> e.setMarked(false));
            target.setSelected(true);

            highlightPossibleMovements(player, target);
            return;
        }
        if (target.getState() == CheckersPlayer.NONE && target.getHighlight()) {
            Optional<CheckersSquare> findFirst = squares.stream().filter(CheckersSquare::getSelected).findFirst();
            if (!findFirst.isPresent()) {
                return;
            }
            CheckersSquare selected = findFirst.get();
            replaceStates(target, selected, player);

            squares.forEach(e -> e.setSelected(false));
            squares.forEach(e -> e.setHighlight(false));
            squares.forEach(e -> e.setMarked(false));

            if (gameOver()) {
                CheckersPlayer winner = getWinner();
                String txt = winner != CheckersPlayer.NONE ? winner + " Won!" : "It's a draw!";
                new SimpleDialogBuilder().text(txt).button("Reset", this::reset).bindWindow(gridPane).displayDialog();
            }
            currentPlayer++;
        }
    }

    private void replaceStates(CheckersSquare target, CheckersSquare selected, CheckersPlayer player) {
        int indexOf = squares.indexOf(target);
        int i = indexOf / SIZE;
        int j = indexOf % SIZE;
        int indexOf2 = squares.indexOf(selected);
        int i2 = indexOf2 / SIZE;
        int j2 = indexOf2 % SIZE;
        int diri = i - i2 > 0 ? 1 : -1;
        int dirj = j - j2 > 0 ? 1 : -1;
        boolean queen = selected.getQueen();
        for (int k = i2, k2 = j2; k != i && k2 != j; k += diri, k2 += dirj) {
            if (withinBounds(k) && withinBounds(k2)) {
                CheckersSquare square2 = squares.get(toIndex(k, k2));
                square2.setState(CheckersPlayer.NONE);
            }
        }
        target.setState(player);
        target.setQueen(queen);
        selected.setState(CheckersPlayer.NONE);
        selected.setQueen(false);
        if (i == 0 && player.getDir() == -1 || i == SIZE - 1 && player.getDir() == 1) {
            target.setQueen(true);
        }

    }

    private void reset() {
        IntStream.range(0, squares.size()).filter(e -> squares.get(e).isBlack()).forEach(i -> {
            CheckersSquare child = squares.get(i);
            child.setState(CheckersPlayer.NONE);
            if (i < SIZE / 2 - 1) {
                child.setState(CheckersPlayer.BLACK);
            }
            if (i > SIZE / 2) {
                child.setState(CheckersPlayer.WHITE);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static int toIndex(int k, int k2) {
        return k * SIZE + k2;
    }

    private static boolean withinBounds(int k) {
        return k >= 0 && k < SIZE;
    }
}