package gaming.ex24;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    List<CheckersSquare> squares = new ArrayList<>();

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

    private void highlightPossibleMovements(CheckersPlayer player, int i, int j) {
        if (i >= 0 && i < SIZE) {
            for (int j2 = -1; j2 <= 1; j2 += 2) {
                if (j + j2 >= 0 && j + j2 < SIZE) {
                    CheckersSquare checkersSquare = squares.get(i * SIZE + j + j2);
                    if (checkersSquare.getState() == CheckersPlayer.NONE) {
                        checkersSquare.setHighlight(true);
                    }
                    if (checkersSquare.getState() == player.opposite()) {
                        int index = (i + player.getDir()) * SIZE + j + j2 + j2;
                        if (index >= 0 && index < squares.size() && j + j2 + j2 >= 0 && j + j2 + j2 < SIZE) {
                            CheckersSquare square2 = squares.get(index);
                            if (square2.getState() == CheckersPlayer.NONE) {
                                checkersSquare.setMarked(true);
                                square2.setHighlight(true);
                            }
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
            int indexOf = squares.indexOf(target);
            int i = indexOf / SIZE;
            int j = indexOf % SIZE;
            i += player.getDir();
            highlightPossibleMovements(player, i, j);
            return;
        }
        if (target.getState() == CheckersPlayer.NONE && target.getHighlight()) {
            Optional<CheckersSquare> findFirst = squares.stream().filter(e -> e.getSelected()).findFirst();
            if (!findFirst.isPresent()) {
                return;
            }
            CheckersSquare selected = findFirst.get();
            replaceStates(target, selected);
            target.setState(player);
            selected.setState(CheckersPlayer.NONE);
            squares.forEach(e -> e.setSelected(false));
            squares.forEach(e -> e.setHighlight(false));
            squares.forEach(e -> e.setMarked(false));

            if (squares.stream().map(e -> e.getState()).distinct().count() < 3) {
//                TicTacToePlayer winner = getWinner(states);
//                String txt = winner!= TicTacToePlayer.NONE ? winner + " Won!" : "It's a draw!";
                new SimpleDialogBuilder().text("Game Finished").button("Reset", this::reset).bindWindow(gridPane)
                    .displayDialog();
//                return true;
            }
            currentPlayer++;
        }

    }

    private void replaceStates(CheckersSquare target, CheckersSquare selected) {
        int indexOf = squares.indexOf(target);
        int i = indexOf / SIZE;
        int j = indexOf % SIZE;
        int indexOf2 = squares.indexOf(selected);
        int i2 = indexOf2 / SIZE;
        int j2 = indexOf2 % SIZE;
        int diri = i - i2 > 0 ? 1 : -1;
        int dirj = j - j2 > 0 ? 1 : -1;
        for (int k = i2, k2 = j2; k != i && k2 != j; k += diri, k2 += dirj) {
            int index = k * SIZE + k2;
            if (index >= 0 && index < squares.size()) {
                CheckersSquare square2 = squares.get(index);
                square2.setState(CheckersPlayer.NONE);
            }
        }
    }

    private void reset() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int index = i * SIZE + j;
                CheckersSquare child = squares.get(index);
                child.setState(CheckersPlayer.NONE);
                if (child.isBlack()) {
                    if (i < 3) {
                        child.setState(CheckersPlayer.BLACK);
                    }
                    if (i > SIZE - 4) {
                        child.setState(CheckersPlayer.WHITE);
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}