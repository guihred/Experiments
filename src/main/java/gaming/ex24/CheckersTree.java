package gaming.ex24;

import java.util.*;
import java.util.stream.Collectors;
import utils.SupplierEx;

public class CheckersTree {

    private static final int MAX_DEPTH = 10;
    private final List<CheckersSquare> squares;
    private List<CheckersTree> children;
    private Map.Entry<CheckersSquare, CheckersSquare> action;
    private int depth;
    private String toStr;

    public CheckersTree(List<CheckersSquare> squares, int depth, Map.Entry<CheckersSquare, CheckersSquare> action) {
        this.squares = squares;
        this.depth = depth;
        this.action = action;
    }

    public Map.Entry<CheckersSquare, CheckersSquare> getAction() {
        return action;
    }

    public CheckersTree makeDecision(CheckersPlayer player) {
        double max = Double.NEGATIVE_INFINITY;
        CheckersTree decision = this;
        for (CheckersTree e : getChildren()) {
            double maxValue = e.maxValue(player, depth + MAX_DEPTH);
            if (maxValue > max) {
                max = maxValue;
                decision = e;
            }
        }
        return decision;
    }

    @Override
    public String toString() {
        return SupplierEx.orElse(toStr, this::getStrRepresentation);

    }

    public double utility(CheckersPlayer player) {
        Map<CheckersPlayer, Long> collect = squares.stream()
            .collect(Collectors.groupingBy(CheckersSquare::getState, Collectors.counting()));
        return collect.getOrDefault(player, 0L) - collect.getOrDefault(player.opposite(), 0L);
    }

    private List<Map.Entry<CheckersSquare, CheckersSquare>> actions(CheckersPlayer player) {

        return squares.stream().filter(e -> e.getState() == player).flatMap(e -> {
            List<CheckersSquare> highlightPossibleMovements = CheckersHelper.highlightPossibleMovements(squares, player,
                e);
            squares.forEach(e0 -> e0.setHighlight(false));
            squares.forEach(e0 -> e0.setMarked(false));
            return highlightPossibleMovements.stream().map(h -> new AbstractMap.SimpleEntry<>(e, h));
        }).collect(Collectors.toList());
    }

    private List<CheckersTree> getChildren() {
        return SupplierEx.orElse(children, () -> {
            children = new ArrayList<>();
            CheckersPlayer player = player();
            List<Map.Entry<CheckersSquare, CheckersSquare>> actions = actions(player);
            for (Map.Entry<CheckersSquare, CheckersSquare> j : actions) {
                children.add(result(player, j));
            }
            Collections.shuffle(children);
            return children;
        });
    }

    private String getStrRepresentation() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < squares.size(); i++) {
            CheckersSquare sq = squares.get(i);
            CheckersPlayer state = sq.getState();
            if (!sq.isBlack()) {
                s.append(" ");
            } else if (state == CheckersPlayer.BLACK) {
                s.append("B");
            } else if (state == CheckersPlayer.WHITE) {
                s.append("W");
            } else {
                s.append("_");
            }
            if (i % 8 == 0) {
                s.append("\n");
            }
        }
        return toStr = s.toString();
    }

    private double maxValue(CheckersPlayer player, int i) {

        if (i > depth) {
            return utility(player);
        }
        double v = 0;
        List<CheckersTree> children2 = getChildren();
        for (CheckersTree a : children2) {
            v += a.maxValue(player, i);
        }
        return v / children2.size();
    }

    private CheckersPlayer player() {
        return CheckersHelper.getPlayer(depth);
    }

    private CheckersTree result(CheckersPlayer player, Map.Entry<CheckersSquare, CheckersSquare> j) {
        List<CheckersSquare> squares2 = squares.stream().map(e -> new CheckersSquare(e.getState()))
            .collect(Collectors.toList());
        CheckersSquare selected = squares2.get(squares.indexOf(j.getKey()));
        CheckersSquare target = squares2.get(squares.indexOf(j.getValue()));
        CheckersHelper.replaceStates(squares2, target, selected, player);
        return new CheckersTree(squares2, depth + 1, j);
    }

}
