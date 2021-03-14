package gaming.ex24;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.IntStream;

public final class CheckersHelper {

    private static final int SIZE = 8;
    private static final List<CheckersPlayer> PLAYERS = Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK);

    private CheckersHelper() {
    }

    public static CheckersPlayer getPlayer(int currentPlayer) {
        return CheckersHelper.PLAYERS.get(currentPlayer % CheckersHelper.PLAYERS.size());
    }

    public static CheckersPlayer getWinner(Collection<CheckersSquare> squares) {
        return getWinner2(squares.stream().map(CheckersSquare::getState).collect(toList()));
    }

    public static List<CheckersSquare> highlightPossibleMovements(List<CheckersSquare> squares, CheckersPlayer player,
        CheckersSquare target) {
        int indexOf = squares.indexOf(target);
        final int i = indexOf / SIZE;
        final int j = indexOf % SIZE;
        List<CheckersSquare> highlighted = new ArrayList<>();
        for (int dirI = player.getDir(); dirI == player.getDir()
            || target.getQueen() && dirI == -player.getDir(); dirI -= 2 * player.getDir()) {
            for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
                int iterations = !target.getQueen() ? 1 : SIZE - 1;
                highlight(squares, player, i, j, highlighted, iterations, dirI, dirJ);
            }
        }
        for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
            List<CheckersSquare> kills = markPossibleKills(squares, player, i - player.getDir(), j + dirJ,
                -player.getDir(), dirJ);
            highlighted.addAll(kills.stream().filter(CheckersSquare::getHighlight).collect(toList()));
        }
        return highlighted;
    }

    public static void replaceStates(List<CheckersSquare> squares, CheckersSquare target, CheckersSquare selected,
        CheckersPlayer player) {
        clearEaten(squares, selected, target, player);

        target.setState(player);
        target.setQueen(selected.getQueen());
        selected.setState(CheckersPlayer.NONE);
        selected.setQueen(false);
        int i = squares.indexOf(target) / SIZE;
        if (i == 0 && player.getDir() == -1 || i == SIZE - 1 && player.getDir() == 1) {
            target.setQueen(true);
        }

    }

    public static void reset(List<CheckersSquare> squares) {
        IntStream.range(0, squares.size()).filter(e -> squares.get(e).isBlack()).forEach(index -> {
            CheckersSquare child = squares.get(index);
            child.setState(CheckersPlayer.NONE);
            child.setQueen(false);
            int i = index / SIZE;
            if (i < SIZE / 2 - 1) {
                child.setState(CheckersPlayer.BLACK);
            }
            if (i > SIZE / 2) {
                child.setState(CheckersPlayer.WHITE);
            }
        });
    }

    private static void clearEaten(List<CheckersSquare> squares, CheckersSquare selected, CheckersSquare target,
        CheckersPlayer player) {
        int indexOf = squares.indexOf(selected);
        int i = indexOf / SIZE;
        int j = indexOf % SIZE;
        for (int dirI = player.getDir(); dirI == player.getDir()
            || selected.getQueen() && dirI == -player.getDir(); dirI -= 2 * player.getDir()) {
            for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
                int iterations = !selected.getQueen() ? 1 : SIZE - 1;
                for (int k = 1; k <= iterations && withinBounds(j + dirJ * k) && withinBounds(i + dirI * k); k++) {
                    List<CheckersSquare> markPossibleKills = markPossibleKills(squares, player, i + dirI * k,
                        j + dirJ * k, dirI, dirJ);
                    clearPossibleKills(target, markPossibleKills);
                }
            }
        }
        for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
            List<CheckersSquare> markPossibleKills = markPossibleKills(squares, player, i - player.getDir(), j + dirJ,
                -player.getDir(), dirJ);
            clearPossibleKills(target, markPossibleKills);
        }

    }

    private static void clearPossibleKills(CheckersSquare target, List<CheckersSquare> markPossibleKills) {
        if (!markPossibleKills.isEmpty() && markPossibleKills.contains(target)) {
            for (CheckersSquare square2 : markPossibleKills) {
                square2.setState(CheckersPlayer.NONE);
                square2.setQueen(false);
                if (Objects.equals(target, square2)) {
                    break;
                }
            }
        }
    }

    private static CheckersPlayer getWinner2(Collection<CheckersPlayer> squares) {
        return squares.stream().filter(e -> e != CheckersPlayer.NONE).findFirst().orElse(CheckersPlayer.NONE);
    }

    private static void highlight(List<CheckersSquare> squares, CheckersPlayer player, final int i, final int j,
        List<CheckersSquare> highlighted, int iterations, int... dirs) {
        int dirI = dirs[0];
        int dirJ = dirs[1];

        for (int k = 1; k <= iterations && withinBounds(j + dirJ * k) && withinBounds(i + dirI * k); k++) {
            CheckersSquare checkersSquare = squares.get(toIndex(i + dirI * k, j + dirJ * k));
            if (checkersSquare.getState() == CheckersPlayer.NONE) {
                checkersSquare.setHighlight(true);
                highlighted.add(checkersSquare);
            }
            List<CheckersSquare> kills = markPossibleKills(squares, player, i + dirI * k, j + dirJ * k, dirI, dirJ);
            highlighted.addAll(kills.stream().filter(CheckersSquare::getHighlight).collect(toList()));
            if (checkersSquare.getState() == player) {
                break;
            }
        }
    }

    private static void markAllDirections(List<CheckersSquare> squares, CheckersPlayer player, int i, int j, int dirI,
        int dirJ, List<CheckersSquare> marked) {
        for (int dirI2 = -1; dirI2 <= 1; dirI2 += 2) {
            for (int dirJ2 = -1; dirJ2 <= 1; dirJ2 += 2) {
                markPossibleKills(squares, player, i + dirI + dirI2, j + dirJ + dirJ2, dirI2, dirJ2, marked);
            }
        }
    }

    private static List<CheckersSquare> markPossibleKills(List<CheckersSquare> squares, CheckersPlayer player, int i,
        int j, int dirI, int dirJ) {
        return markPossibleKills(squares, player, i, j, dirI, dirJ, new ArrayList<>());
    }

    private static List<CheckersSquare> markPossibleKills(List<CheckersSquare> squares, CheckersPlayer player, int i,
        int j, int dirI, int dirJ, List<CheckersSquare> marked) {
        if (!withinBounds(i) || !withinBounds(j)) {
            return marked;
        }
        CheckersSquare checkersSquare = squares.get(toIndex(i, j));
        if (checkersSquare.getState() == player.opposite() && withinBounds(i + dirI) && withinBounds(j + dirJ)) {
            int index = toIndex(i + dirI, j + dirJ);
            CheckersSquare square2 = squares.get(index);
            if (square2.getState() == CheckersPlayer.NONE) {
                checkersSquare.setMarked(true);
                square2.setHighlight(true);
                if (marked.contains(checkersSquare)) {
                    return marked;
                }
                marked.add(checkersSquare);
                marked.add(square2);
                markAllDirections(squares, player, i, j, dirI, dirJ, marked);
            }
        }

        return marked;
    }

    private static int toIndex(int k, int k2) {
        return k * SIZE + k2;
    }

    private static boolean withinBounds(int k) {
        return k >= 0 && k < SIZE;
    }
}