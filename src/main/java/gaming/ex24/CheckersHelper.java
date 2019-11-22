package gaming.ex24;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import simplebuilder.SimpleDialogBuilder;

public final class CheckersHelper {

    public static final int SIZE = 8;
    public static final List<CheckersPlayer> PLAYERS = Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK);
    private CheckersHelper() {
    }

    public static void clearEaten(List<CheckersSquare> squares, CheckersSquare selected, CheckersSquare target,
        CheckersPlayer player) {
        int indexOf = squares.indexOf(selected);
        int i = indexOf / SIZE;
        int j = indexOf % SIZE;
        for (int dirI = player.getDir(); dirI == player.getDir()
            || selected.getQueen() && dirI == -player.getDir(); dirI -= 2 * player.getDir()) {
            for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
                int iterations = !selected.getQueen() ? 1 : SIZE - 1;
                for (int k = 1; k <= iterations; k++) {
                    if (!withinBounds(j + dirJ * k) || !withinBounds(i + dirI * k)) {
                        continue;
                    }
                    List<CheckersSquare> markPossibleKills = markPossibleKills(squares, player, i + dirI * k,
                        j + dirJ * k, dirI, dirJ);
                    if (!markPossibleKills.isEmpty() && markPossibleKills.contains(target)) {
                        for (CheckersSquare square2 : markPossibleKills) {
                            square2.setState(CheckersPlayer.NONE);
                            square2.setQueen(false);
                            if (Objects.equals(target, square2)) {
                                break;
                            }
                        }
                        return;
                    }
                }
            }
        }
        for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
            List<CheckersSquare> markPossibleKills = markPossibleKills(squares, player, i - player.getDir(), j + dirJ,
                -player.getDir(), dirJ);
            if (!markPossibleKills.isEmpty() && markPossibleKills.contains(target)) {
                for (CheckersSquare square2 : markPossibleKills) {
                    square2.setState(CheckersPlayer.NONE);
                    square2.setQueen(false);
                    if (Objects.equals(target, square2)) {
                        break;
                    }
                }
                return;
            }
        }

    }

    public static boolean gameOver(List<CheckersPlayer> squares) {
        return squares.stream().distinct().count() < 3;
    }
    public static CheckersPlayer getPlayer(int currentPlayer) {
        return CheckersHelper.PLAYERS.get(currentPlayer % CheckersHelper.PLAYERS.size());
    }
    public static CheckersPlayer getWinner(List<CheckersSquare> squares) {
        return getWinner2(squares.stream().map(CheckersSquare::getState).collect(Collectors.toList()));
    }

    public static CheckersPlayer getWinner2(List<CheckersPlayer> squares) {
        return squares.stream().filter(e -> e != CheckersPlayer.NONE).findFirst().orElse(CheckersPlayer.NONE);
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
                for (int k = 1; k <= iterations; k++) {
                    if (!withinBounds(j + dirJ * k) || !withinBounds(i + dirI * k)) {
                        continue;
                    }
                    CheckersSquare checkersSquare = squares.get(toIndex(i + dirI * k, j + dirJ * k));
                    if (checkersSquare.getState() == CheckersPlayer.NONE) {
                        checkersSquare.setHighlight(true);
                        highlighted.add(checkersSquare);
                    }
                    List<CheckersSquare> kills = markPossibleKills(squares, player, i + dirI * k, j + dirJ * k, dirI,
                        dirJ);

                    kills.stream().filter(e -> e.getHighlight()).forEach(highlighted::add);

                    if (checkersSquare.getState() == player) {
                        break;
                    }
                }
            }
        }
        for (int dirJ = -1; dirJ <= 1; dirJ += 2) {
            List<CheckersSquare> kills = markPossibleKills(squares, player, i - player.getDir(), j + dirJ,
                -player.getDir(), dirJ);
            kills.stream().filter(e -> e.getHighlight()).forEach(highlighted::add);
        }
        return highlighted;
    }

    public static boolean isGameOver(List<CheckersSquare> squares) {
        return squares.stream().map(CheckersSquare::getState).distinct().count() < 3;
    }

    public static List<CheckersSquare> markPossibleKills(List<CheckersSquare> squares, CheckersPlayer player, int i,
        int j, int dirI, int dirJ) {
        return markPossibleKills(squares, player, i, j, dirI, dirJ, new ArrayList<>());
    }

    public static List<CheckersSquare> markPossibleKills(List<CheckersSquare> squares, CheckersPlayer player, int i,
        int j, int dirI, int dirJ, List<CheckersSquare> marked) {
        if (!withinBounds(i) || !withinBounds(j)) {
            return marked;
        }
        CheckersSquare checkersSquare = squares.get(toIndex(i, j));
        if (checkersSquare.getState() == player.opposite()) {
            if (withinBounds(i + dirI) && withinBounds(j + dirJ)) {
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
                    for (int dirI2 = -1; dirI2 <= 1; dirI2 += 2) {
                        for (int dirJ2 = -1; dirJ2 <= 1; dirJ2 += 2) {
                            markPossibleKills(squares, player, i + dirI + dirI2, j + dirJ + dirJ2, dirI2, dirJ2,
                                marked);
                        }
                    }
                }
            }
        }

        return marked;
    }

    public static boolean onClick(AtomicInteger currentPlayer, List<CheckersSquare> squares, CheckersSquare target) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        if (target.getState() == player) {
            squares.forEach(e -> e.setSelected(false));
            squares.forEach(e -> e.setHighlight(false));
            squares.forEach(e -> e.setMarked(false));
            target.setSelected(true);
            highlightPossibleMovements(squares, player, target);
            return false;
        }
        if (target.getState() == CheckersPlayer.NONE && target.getHighlight()) {
            Optional<CheckersSquare> findFirst = squares.stream().filter(CheckersSquare::getSelected).findFirst();
            if (!findFirst.isPresent()) {
                return false;
            }
            CheckersSquare selected = findFirst.get();
            replaceStates(squares, target, selected, player);

            squares.forEach(e -> e.setSelected(false));
            squares.forEach(e -> e.setHighlight(false));
            squares.forEach(e -> e.setMarked(false));

            if (isGameOver(squares)) {
                displayDialog(currentPlayer, squares);
            }
            return true;

        }
        return false;
    }

    public static void replaceStates(List<CheckersSquare> squares, CheckersSquare target, CheckersSquare selected,
        CheckersPlayer player) {
        clearEaten(squares, selected, target, player);

        target.setState(player);
        target.setQueen(selected.getQueen());
        selected.setState(CheckersPlayer.NONE);
        selected.setQueen(false);
        int i2 = squares.indexOf(target) / SIZE;
        if (i2 == 0 && player.getDir() == -1 || i2 == SIZE - 1 && player.getDir() == 1) {
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

    public static void runIfAI(List<CheckersSquare> squares, AtomicInteger currentPlayer) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        if (player == CheckersPlayer.BLACK) {
            boolean verifyWin = CheckersHelper.isGameOver(squares);
            if (!verifyWin) {
                Platform.runLater(() -> runAI(squares,currentPlayer));
            }
        }
    }

    public static int toIndex(int k, int k2) {
        return k * SIZE + k2;
    }

    public static boolean withinBounds(int k) {
        return k >= 0 && k < SIZE;
    }

    private static void displayDialog(AtomicInteger currentPlayer, List<CheckersSquare> squares) {
        CheckersPlayer winner = getWinner(squares);
        String txt = winner != CheckersPlayer.NONE ? winner + " Won!" : "It's a draw!";
        new SimpleDialogBuilder().text(txt).button("Reset", () -> {
            reset(squares);
            CheckersHelper.runIfAI(squares, currentPlayer);
        }).bindWindow(squares.get(0))
            .displayDialog();
    }

    private static void runAI(List<CheckersSquare> squares, AtomicInteger currentPlayer) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        CheckersTree CheckersTree = new CheckersTree(squares, currentPlayer.get(), null);
        CheckersTree makeDecision = CheckersTree.makeDecision(player);
        if (makeDecision == null) {
            displayDialog(currentPlayer, squares);
            return;
        }

        Entry<CheckersSquare, CheckersSquare> j = makeDecision.getAction();
        CheckersHelper.replaceStates(squares, j.getValue(), j.getKey(), player);
        currentPlayer.incrementAndGet();
    }
}