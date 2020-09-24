package gaming.ex24;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import simplebuilder.SimpleDialogBuilder;
import utils.CommonsFX;

public final class CheckersAI {
    private CheckersAI() {
    }

    public static void displayDialog(AtomicInteger currentPlayer, List<CheckersSquare> squares) {
        CheckersPlayer winner = CheckersHelper.getWinner(squares);
        String txt = winner != CheckersPlayer.NONE ? winner + " Won!" : "It's a draw!";
        new SimpleDialogBuilder().text(txt).button("Reset", () -> {
            CheckersHelper.reset(squares);
            runIfAI(squares, currentPlayer);
        }).bindWindow(squares.get(0)).displayDialog();
    }

    public static boolean isGameOver(Collection<CheckersSquare> squares) {
        return squares.stream().map(CheckersSquare::getState).distinct().count() < 3;
    }

    public static boolean onClick(AtomicInteger currentPlayer, List<CheckersSquare> squares, CheckersSquare target) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        if (target.getState() == player) {
            CheckersAI.resetSquares(squares);
            target.setSelected(true);
            CheckersHelper.highlightPossibleMovements(squares, player, target);
            return false;
        }
        if (target.getState() != CheckersPlayer.NONE || !target.getHighlight()) {
            return false;
        }
        Optional<CheckersSquare> anySelected = squares.stream().filter(CheckersSquare::getSelected).findFirst();
        if (!anySelected.isPresent()) {
            return false;
        }
        CheckersSquare selected = anySelected.get();
        CheckersHelper.replaceStates(squares, target, selected, player);
        CheckersAI.resetSquares(squares);
        if (CheckersAI.isGameOver(squares)) {
            displayDialog(currentPlayer, squares);
        }
        return true;
    }

    public static void runAI(List<CheckersSquare> squares, AtomicInteger currentPlayer) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        CheckersTree checkersTree = new CheckersTree(squares, currentPlayer.get(), null);
        CheckersTree makeDecision = checkersTree.makeDecision(player);
        if (makeDecision == null) {
            CheckersAI.displayDialog(currentPlayer, squares);
            return;
        }

        Entry<CheckersSquare, CheckersSquare> j = makeDecision.getAction();
        CheckersHelper.replaceStates(squares, j.getValue(), j.getKey(), player);
        currentPlayer.incrementAndGet();
    }

    public static void runIfAI(List<CheckersSquare> squares, AtomicInteger currentPlayer) {
        CheckersPlayer player = CheckersHelper.getPlayer(currentPlayer.get());
        if (player == CheckersPlayer.BLACK) {
            if (CheckersAI.isGameOver(squares)) {
                return;
            }
            CommonsFX.runInPlatform(() -> runAI(squares, currentPlayer));
        }
    }

    private static void resetSquares(List<CheckersSquare> squares) {
        squares.forEach(e -> {
            e.setSelected(false);
            e.setHighlight(false);
            e.setMarked(false);
        });
    }

}
