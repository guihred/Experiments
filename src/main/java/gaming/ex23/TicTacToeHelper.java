package gaming.ex23;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import javafx.scene.layout.GridPane;
import simplebuilder.SimpleDialogBuilder;

public final class TicTacToeHelper {
    private static final int SIZE = 3;

    private TicTacToeHelper() {
    }

    public static TicTacToeSquare getSquare(List<TicTacToeSquare> squares, int i, int j) {
        return squares.get(i * SIZE + j);
    }

    public static void verifyWin(List<TicTacToeSquare> squares, GridPane gridPane) {
        if (gameWin(squares)) {
            TicTacToePlayer winner = getWinner(squares);
            new SimpleDialogBuilder().text(winner + " Won!").button("Reset", () -> reset(squares)).bindWindow(gridPane)
                .displayDialog();
            return;
        }
        if (allFilled(squares)) {
            new SimpleDialogBuilder().text("It's a draw!").button("Reset", () -> reset(squares)).bindWindow(gridPane)
                .displayDialog();
        }
    }

    private static boolean allFilled(List<TicTacToeSquare> squares) {
        return IntStream.range(0, SIZE)
            .mapToObj(i -> IntStream.range(0, SIZE).mapToObj(j -> getSquare(squares, i, j).getState())).flatMap(e -> e)
            .noneMatch(e -> e == TicTacToePlayer.NONE);
    }

    @SafeVarargs
    private static boolean anyWinner(IntFunction<TicTacToePlayer>... mapper) {
        for (IntFunction<TicTacToePlayer> intFunction : mapper) {
            long count = IntStream.range(0, SIZE).mapToObj(intFunction).distinct().count();
            if (count == 1
                && IntStream.range(0, SIZE).mapToObj(intFunction).noneMatch(e -> e == TicTacToePlayer.NONE)) {
                return true;
            }
        }
        return false;
    }

    private static boolean gameWin(List<TicTacToeSquare> squares) {
        if (anyWinner(j -> getSquare(squares, j, j).getState(), j -> getSquare(squares, j, 2 - j).getState())) {
            return true;
        }
        return IntStream.range(0, SIZE).anyMatch(i -> {
            if (anyWinner(j -> getSquare(squares, i, j).getState())) {
                return true;
            }
            return anyWinner(j -> getSquare(squares, j, i).getState());
        });
    }

    private static Optional<TicTacToePlayer> getWinner(IntFunction<TicTacToePlayer> mapper) {
        if (anyWinner(mapper)) {
            return IntStream.range(0, SIZE).mapToObj(mapper).findFirst();
        }
        return Optional.empty();
    }

    private static TicTacToePlayer getWinner(List<TicTacToeSquare> squares) {
        List<BiFunction<Integer, Integer, TicTacToePlayer>> mappers = Arrays.asList(
            (i, j) -> getSquare(squares, j, j).getState(), (i, j) -> getSquare(squares, j, 2 - j).getState(),
            (i, j) -> getSquare(squares, i, j).getState(), (i, j) -> getSquare(squares, j, j).getState(),
            (i, j) -> getSquare(squares, j, i).getState());
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

    private static void reset(List<TicTacToeSquare> squares) {
        for (TicTacToeSquare sq : squares) {
            sq.setState(TicTacToePlayer.NONE);
        }
    }
}
