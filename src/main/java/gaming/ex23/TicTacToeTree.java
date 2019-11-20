package gaming.ex23;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import utils.SupplierEx;

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
        return getChildren().stream().min(Comparator.comparing(e -> e.minValue(player))).orElse(this);
    }

    @Override
    public String toString() {
//        if (getWinner() != null) {
        return String.format("%s>%s %d", squares, getWinner(), action);
//        }
//        String collect = getChildren().stream().map(TicTacToeTree::toString)
//            .collect(Collectors.joining("\n-", "-", ""));
//        return String.format("%s%n%s", squares, collect.replaceAll("(-+)", "-$1"));

    }

    public int utility(TicTacToePlayer player) {
        TicTacToePlayer cur = getWinner();
        if (cur == null) {
            return 0;
        }
        return player == cur ? 1 : -1;
    }

    private int[] actions() {
        return IntStream.range(0, squares.size()).filter(i -> squares.get(i) == TicTacToePlayer.NONE).toArray();
    }

    private List<TicTacToeTree> getChildren() {
        return SupplierEx.orElse(children, () -> {
            children = new ArrayList<>();
            if (TicTacToeHelper.gameEnded(squares)) {
                getWinner();
                return children;
            }
            TicTacToePlayer player = player();
            int[] actions = actions();
            for (int j : actions) {
                TicTacToeTree ticTacToeTree = result(player, j);
                children.add(ticTacToeTree);
            }
            return children;
        });
    }

    private TicTacToePlayer getWinner() {
        return SupplierEx.orElse(winner,
            () -> winner = TicTacToeHelper.gameEnded(squares) ? TicTacToeHelper.getWinner(squares) : null);
    }

    private int maxValue(TicTacToePlayer player) {

        if (TicTacToeHelper.gameEnded(squares)) {
            return utility(player);
        }
        int v = -1000;
        for (TicTacToeTree a : getChildren()) {
            v = Math.max(v, a.minValue(player));
        }
        return v;
    }

    private int minValue(TicTacToePlayer player) {
        if (TicTacToeHelper.gameEnded(squares)) {
            return utility(player);
        }
        int v = 1000;
        for (TicTacToeTree a : getChildren()) {
            v = Math.min(v, a.maxValue(player));
        }
        return v;
    }

    private TicTacToePlayer player() {
        Map<TicTacToePlayer, Long> collect = squares.stream().filter(e -> e != TicTacToePlayer.NONE)
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        return Stream.of(TicTacToePlayer.X, TicTacToePlayer.O)
            .min(Comparator.comparing(e -> collect.getOrDefault(e, 0L))).orElse(TicTacToePlayer.X);
    }

    private TicTacToeTree result(TicTacToePlayer player, int j) {
        List<TicTacToePlayer> squares2 = new ArrayList<>(squares);
        squares2.set(j, player);
        TicTacToeTree ticTacToeTree = new TicTacToeTree(squares2, j);
        return ticTacToeTree;
    }

    public static void main(String[] args) {

        TicTacToeTree ticTacToeTree = new TicTacToeTree(
            IntStream.range(0, 9).mapToObj(i -> TicTacToePlayer.NONE).collect(Collectors.toList()));
        System.out.println(ticTacToeTree.makeDecision(TicTacToePlayer.X));
    }

}
