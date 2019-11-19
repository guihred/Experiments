package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;

public class TicTacToeLauncher extends Application {

    private static final int SIZE = 3;
    private TicTacToeSquare[][] map = new TicTacToeSquare[SIZE][SIZE];
    private int currentPlayer;
    private List<TicTacToePlayer> players = Arrays.asList(TicTacToePlayer.O, TicTacToePlayer.X);

    @Override
    public void start(Stage stage) {
        final GridPane gridPane = new GridPane();
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                map[i][j] = new TicTacToeSquare();
                TicTacToeSquare square = map[i][j];
                gridPane.add(square, i, j);
                square.setOnMouseClicked(e -> {
                    if (square.getState() == TicTacToePlayer.NONE) {
                        square.setState(players.get(currentPlayer++ % players.size()));
                    }
                    verifyWin(gridPane);
                });
            }
        }

        final BorderPane borderPane = new BorderPane(gridPane);
        final Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
    }

    private boolean anyWin(int i) {
        if (anyWinner(j -> map[j][j].getState())) {
            return true;
        }
        if (anyWinner(j -> map[j][2 - j].getState())) {
            return true;
        }
        if (anyWinner(j -> map[i][j].getState())) {
            return true;
        }
        return anyWinner(j -> map[j][i].getState());
    }

    private TicTacToePlayer getWinner() {
        List<BiFunction<Integer, Integer, TicTacToePlayer>> a = Arrays.asList((i, j) -> map[j][j].getState(),
            (i, j) -> map[j][2 - j].getState(), (i, j) -> map[i][j].getState(), (i, j) -> map[j][j].getState(),
            (i, j) -> map[j][i].getState());
        for (BiFunction<Integer, Integer, TicTacToePlayer> intFunction : a) {
            Optional<TicTacToePlayer> findFirst = IntStream.range(0, SIZE)
                .mapToObj(i -> getWinner(j -> intFunction.apply(i, j))).filter(Optional<TicTacToePlayer>::isPresent)
                .map(Optional<TicTacToePlayer>::get).findFirst();
            if (findFirst.isPresent()) {
                return findFirst.get();
            }
        }
        return TicTacToePlayer.NONE;
    }

    private void reset() {
        for (TicTacToeSquare[] ticTacToeSquares : map) {
            for (TicTacToeSquare sq : ticTacToeSquares) {
                sq.setState(TicTacToePlayer.NONE);
            }
        }
    }

    private void verifyWin(GridPane gridPane) {
        if (IntStream.range(0, SIZE).anyMatch(this::anyWin)) {
            TicTacToePlayer winner = getWinner();
            new SimpleDialogBuilder().text(winner + " Won!").button("Reset", this::reset).bindWindow(gridPane)
                .displayDialog();
            return;
        }
        if (IntStream.range(0, SIZE).mapToObj(i -> IntStream.range(0, SIZE).mapToObj(j -> map[i][j].getState()))
            .flatMap(e -> e).noneMatch(e -> e == TicTacToePlayer.NONE)) {
            new SimpleDialogBuilder().text("It's a draw!").button("Reset", this::reset).bindWindow(gridPane)
                .displayDialog();

        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static boolean anyWinner(IntFunction<TicTacToePlayer> mapper) {
        long count = IntStream.range(0, SIZE).mapToObj(mapper).distinct().count();
        return count == 1 && IntStream.range(0, SIZE).mapToObj(mapper).noneMatch(e -> e == TicTacToePlayer.NONE);
    }

    private static Optional<TicTacToePlayer> getWinner(IntFunction<TicTacToePlayer> mapper) {
        if (anyWinner(mapper)) {
            return IntStream.range(0, SIZE).mapToObj(mapper).findFirst();
        }
        return Optional.empty();
    }

}