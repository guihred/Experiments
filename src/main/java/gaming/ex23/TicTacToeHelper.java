package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.layout.GridPane;
import simplebuilder.SimpleDialogBuilder;

public final class TicTacToeHelper {
    private static final int SIZE = 3;

    private TicTacToeHelper() {
    }

    public static boolean gameEnded(List<TicTacToePlayer> squares) {
        return gameWin(squares) || allFilled(squares);
    }

    public static TicTacToePlayer getSquare(List<TicTacToePlayer> squares, int i, int j) {
        return squares.get(i * SIZE + j);
    }

    public static TicTacToePlayer getWinner(List<TicTacToePlayer> squares) {
        List<BiFunction<Integer, Integer, TicTacToePlayer>> mappers = Arrays.asList(
            (i, j) -> getSquare(squares, j, j), (i, j) -> getSquare(squares, j, 2 - j),
            (i, j) -> getSquare(squares, i, j), (i, j) -> getSquare(squares, j, j), (i, j) -> getSquare(squares, j, i));
        for (BiFunction<Integer, Integer, TicTacToePlayer> intFunction : mappers) {
            Optional<TicTacToePlayer> winner = IntStream.range(0, SIZE)
                .mapToObj(i -> getWinner(j -> intFunction.apply(i, j))).filter(Optional<TicTacToePlayer>::isPresent)
                .map(Optional<TicTacToePlayer>::get).findFirst();
            if (winner.isPresent()) {
                return winner.get();
            }
        }
        return TicTacToePlayer.NONE;
    }

    public static boolean verifyWin(List<TicTacToeSquare> squares, GridPane gridPane) {

        List<TicTacToePlayer> states = squares.stream().map(TicTacToeSquare::getState).collect(Collectors.toList());
        if (gameEnded(states)) {
            TicTacToePlayer winner = getWinner(states);
            String txt = winner!= TicTacToePlayer.NONE ? winner + " Won!" : "It's a draw!";
            new SimpleDialogBuilder().text(txt).button("Reset", () -> reset(squares)).bindWindow(gridPane)
                .displayDialog();
            return true;
        }
        return false;
    }

    private static boolean allFilled(List<TicTacToePlayer> squares) {
        return IntStream.range(0, SIZE)
            .mapToObj(i -> IntStream.range(0, SIZE).mapToObj(j -> getSquare(squares, i, j))).flatMap(e -> e)
            .noneMatch(e -> e == TicTacToePlayer.NONE);
    }

    @SafeVarargs
    private static boolean anyWinner(IntFunction<TicTacToePlayer>... mapper) {
        for (IntFunction<TicTacToePlayer> intFunction : mapper) {
            long count = IntStream.range(0, SIZE).mapToObj(intFunction).distinct().count();
            boolean anyWin = count == 1
                && IntStream.range(0, SIZE).mapToObj(intFunction).noneMatch(e -> e == TicTacToePlayer.NONE);
            if (anyWin) {
                return anyWin;
            }
        }
        return false;
    }

    private static boolean gameWin(List<TicTacToePlayer> squares) {
        if (anyWinner(j -> getSquare(squares, j, j), j -> getSquare(squares, j, SIZE - 1 - j))) {
            return true;
        }
        return IntStream.range(0, SIZE).anyMatch(
            i -> anyWinner(j -> getSquare(squares, i, j), j -> getSquare(squares, j, i)));
    }

    private static Optional<TicTacToePlayer> getWinner(IntFunction<TicTacToePlayer> mapper) {
        if (anyWinner(mapper)) {
            return IntStream.range(0, SIZE).mapToObj(mapper).findFirst();
        }
        return Optional.empty();
    }

    private static void reset(List<TicTacToeSquare> squares) {
        for (TicTacToeSquare sq : squares) {
            sq.setState(TicTacToePlayer.NONE);
        }
    }
}
