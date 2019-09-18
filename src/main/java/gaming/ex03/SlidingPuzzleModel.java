/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex03;

import java.security.SecureRandom;
import java.util.Objects;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import utils.StageHelper;

/**
 *
 * @author Note
 */
public class SlidingPuzzleModel {

    private GridPane gridPane;
    private SlidingPuzzleSquare[][] map = new SlidingPuzzleSquare[SlidingPuzzleSquare.MAP_SIZE][SlidingPuzzleSquare.MAP_SIZE];
    private int moves;

    private final SecureRandom random = new SecureRandom();

    public SlidingPuzzleModel(GridPane gridPane) {
        this.gridPane = gridPane;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new SlidingPuzzleSquare(i * SlidingPuzzleSquare.MAP_SIZE + j + 1);
                map[i][j].setOnMouseClicked(createMouseClickedEvento(map[i][j]));
            }
        }
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                SlidingPuzzleSquare map1 = map[i][j];
                gridPane.add(map1.getStack(), j, i);
            }
        }
        reset();
    }

    private final EventHandler<MouseEvent> createMouseClickedEvento(SlidingPuzzleSquare mem) {
        return e -> slideIfPossible(mem);
    }

    private boolean isNeighborEmpty(int i, int j, int h, int v) {
        if (i + h >= 0 && i + h < SlidingPuzzleSquare.MAP_SIZE && j + v >= 0 && j + v < SlidingPuzzleSquare.MAP_SIZE) {
            return map[i + h][j + v].isEmpty();
        }
        return false;
    }

    private boolean neighborEmpty(int i, int j) {
        return isNeighborEmpty(i, j, 0, -1) || isNeighborEmpty(i, j, 0, 1) || isNeighborEmpty(i, j, 1, 0)
            || isNeighborEmpty(i, j, -1, 0);
    }

    private final void reset() {

        for (int i = 0; i < 100; i++) {
            int nextI = random.nextInt(SlidingPuzzleSquare.MAP_SIZE);
            int nextJ = random.nextInt(SlidingPuzzleSquare.MAP_SIZE);
            swapEmptyNeighbor(nextI, nextJ);
        }

    }

    private void slideIfPossible(SlidingPuzzleSquare mem) {
        for (int i = 0; i < SlidingPuzzleSquare.MAP_SIZE; i++) {
            for (int j = 0; j < SlidingPuzzleSquare.MAP_SIZE; j++) {
                if (Objects.equals(map[i][j], mem) && neighborEmpty(i, j)) {
                    swapEmptyNeighbor(i, j);
                    moves++;
                    if (verifyEnd()) {
                        final String text = "You ended in " + moves + " moves";
                        final Runnable c = () -> {
                            reset();
                            moves = 0;
                        };
                        StageHelper.displayDialog(text, "Reset", c);
                    }
                    return;
                }
            }
        }
    }

    private final void swapEmptyNeighbor(int i, int j) {
        for (int k = -1; k < 2; k++) {
            for (int l = -1; l < 2; l++) {
                if ((k == 0 || l == 0) && k != l && isNeighborEmpty(i, j, k, l)) {
                    gridPane.getChildren().remove(map[i][j].getStack());
                    gridPane.getChildren().remove(map[i + k][j + l].getStack());
                    gridPane.add(map[i][j].getStack(), j + l, i + k);
                    gridPane.add(map[i + k][j + l].getStack(), j, i);
                    final SlidingPuzzleSquare empty = map[i + k][j + l];
                    map[i + k][j + l] = map[i][j];
                    map[i][j] = empty;
                }
            }
        }
    }

    private boolean verifyEnd() {
        for (int i = 0; i < SlidingPuzzleSquare.MAP_SIZE; i++) {
            for (int j = 0; j < SlidingPuzzleSquare.MAP_SIZE; j++) {
                if (map[i][j].getNumber() != i * SlidingPuzzleSquare.MAP_SIZE + j + 1) {
                    return false;
                }
            }
        }

        return true;
    }

    public static SlidingPuzzleModel create(GridPane gridPane) {
        return new SlidingPuzzleModel(gridPane);
    }

}
