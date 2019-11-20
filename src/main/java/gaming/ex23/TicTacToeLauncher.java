package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;
import utils.CommonsFX;

public class TicTacToeLauncher extends Application {

    private static final int SIZE = 3;
    @FXML
    private GridPane gridPane;
    private int currentPlayer;
    private List<TicTacToePlayer> players = Arrays.asList(TicTacToePlayer.O, TicTacToePlayer.X);
    private List<TicTacToeSquare> squares;

    public void initialize() {
        squares = gridPane.getChildren().stream().map(TicTacToeSquare.class::cast).collect(Collectors.toList());
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void onMouseClickedTicTacToeSquare0(MouseEvent e) {
        TicTacToeSquare square = (TicTacToeSquare) e.getTarget();
        if (square.getState() == TicTacToePlayer.NONE) {
            square.setState(players.get(currentPlayer++ % players.size()));
        }
        verifyWin();
    }

    @Override
    public void start(Stage stage) {
        CommonsFX.loadFXML("Tic-Tac-Toe", "TicTacToeLauncher.fxml", this, stage);
    }

    private boolean anyWin(int i) {
        if (anyWinner(j -> getSquare(j, j).getState())) {
            return true;
        }
        if (anyWinner(j -> getSquare(j, 2 - j).getState())) {
            return true;
        }
        if (anyWinner(j -> getSquare(i, j).getState())) {
            return true;
        }
        return anyWinner(j -> getSquare(j, i).getState());
    }

    private TicTacToeSquare getSquare(int i, int j) {
        return squares.get(i * SIZE + j);
    }

    private TicTacToePlayer getWinner() {
        List<BiFunction<Integer, Integer, TicTacToePlayer>> a = Arrays.asList((i, j) -> getSquare(j, j).getState(),
            (i, j) -> getSquare(j, 2 - j).getState(), (i, j) -> getSquare(i, j).getState(),
            (i, j) -> getSquare(j, j).getState(), (i, j) -> getSquare(j, i).getState());
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
        for (TicTacToeSquare sq : squares) {
            sq.setState(TicTacToePlayer.NONE);
        }
    }

    private void verifyWin() {
        if (IntStream.range(0, SIZE).anyMatch(this::anyWin)) {
            TicTacToePlayer winner = getWinner();
            new SimpleDialogBuilder().text(winner + " Won!").button("Reset", this::reset).bindWindow(gridPane)
                .displayDialog();
            return;
        }
        if (IntStream.range(0, SIZE).mapToObj(i -> IntStream.range(0, SIZE).mapToObj(j -> getSquare(i, j).getState()))
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