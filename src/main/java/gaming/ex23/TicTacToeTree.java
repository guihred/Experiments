package gaming.ex23;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class TicTacToeTree {

    private final List<TicTacToePlayer> squares;
    private List<TicTacToeTree> children;
    private TicTacToePlayer winner;
    private final int action;

    public TicTacToeTree(List<TicTacToePlayer> squares) {
        this.squares = squares;
        action = -1;
    }

    public TicTacToeTree(List<TicTacToePlayer> squares, int action) {
        this.squares = squares;
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public TicTacToeTree makeDecision(TicTacToePlayer player) {
        double max = Double.NEGATIVE_INFINITY;
        TicTacToeTree decision = this;
        for (TicTacToeTree e : getChildren()) {
            double maxValue = e.maxValue(player);
            if (maxValue > max) {
                max = maxValue;
                decision = e;
            }
        }
        return decision;
    }

    @Override
    public String toString() {
        return String.format("%s>%s %d", squares, getWinner(), action);

    }

    public double utility(TicTacToePlayer player) {
        TicTacToePlayer cur = getWinner();
        if (cur == null || cur == TicTacToePlayer.NONE) {
            return 0;
        }
        return player != cur ? -1 : 1;
    }

    private int[] actions() {
        return IntStream.range(0, squares.size()).filter(i -> squares.get(i) == TicTacToePlayer.NONE).toArray();
    }

    private List<TicTacToeTree> getChildren() {
        return SupplierEx.orElse(children, () -> {
            children = new ArrayList<>();
            TicTacToePlayer player = player();
            int[] actions = actions();
            for (int j : actions) {
                TicTacToeTree ticTacToeTree = result(player, j);
                children.add(ticTacToeTree);
            }
            Collections.shuffle(children);
            return children;
        });
    }

    private TicTacToePlayer getWinner() {
        return SupplierEx.orElse(winner,
            () -> winner = TicTacToeHelper.gameEnded(squares) ? TicTacToeHelper.getWinner(squares) : null);
    }

    private double maxValue(TicTacToePlayer player) {

        if (TicTacToeHelper.gameEnded(squares)) {
            return utility(player);
        }
        double v = 0;
        List<TicTacToeTree> children2 = getChildren();
        for (TicTacToeTree a : children2) {
            v += a.maxValue(player);
        }
        return v / children2.size();
    }

    private TicTacToePlayer player() {
        Map<TicTacToePlayer, Long> nonBlankPoints = squares.stream().filter(e -> e != TicTacToePlayer.NONE)
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Long x = nonBlankPoints.getOrDefault(TicTacToePlayer.X, 0L);
        Long o = nonBlankPoints.getOrDefault(TicTacToePlayer.O, 0L);
        if (Objects.equals(x, o) && action != -1) {
            return squares.get(action).opposite();
        }
        return Stream.of(TicTacToePlayer.X, TicTacToePlayer.O)
                .min(Comparator.comparing(e -> nonBlankPoints.getOrDefault(e, 0L))).orElse(TicTacToePlayer.X);

    }

    private TicTacToeTree result(TicTacToePlayer player, int j) {
        List<TicTacToePlayer> squares2 = new ArrayList<>(squares);
        squares2.set(j, player);
        return new TicTacToeTree(squares2, j);
    }

    public static void main(String[] args) {

        TicTacToeTree ticTacToeTree = new TicTacToeTree(
            IntStream.range(0, 9).mapToObj(i -> TicTacToePlayer.NONE).collect(Collectors.toList()));
        TicTacToeTree makeDecision = ticTacToeTree.makeDecision(TicTacToePlayer.X);
        HasLogging.log().info("{}", makeDecision);
    }

}
