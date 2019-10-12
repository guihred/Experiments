/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package gaming.ex05;

import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.Timeline;
import javafx.scene.layout.GridPane;
import simplebuilder.SimpleDialogBuilder;

public class TetrisModel {

    public static final int MAP_HEIGHT = 20;
    public static final int MAP_WIDTH = 10;
    private int currentI;
    private int currentJ;
    private TetrisDirection direction = TetrisDirection.UP;
    private final TetrisSquare[][] map = new TetrisSquare[MAP_WIDTH][MAP_HEIGHT];
    private TetrisPiece piece = TetrisPiece.L;

    private Map<TetrisPiece, Map<TetrisDirection, int[][]>> pieceDirection = new EnumMap<>(TetrisPiece.class);

    private SecureRandom random = new SecureRandom();
    public TetrisModel(GridPane gridPane) {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j] = new TetrisSquare();
                gridPane.add(map[i][j], i, j);
            }
        }

        for (TetrisPiece value : TetrisPiece.values()) {
            pieceDirection.put(value, new HashMap<>());
            pieceDirection.get(value).put(TetrisDirection.UP, value.getMap());

            int[][] right = rotateMap(value.getMap());
            pieceDirection.get(value).put(TetrisDirection.RIGHT, right);
            int[][] down = rotateMap(right);
            pieceDirection.get(value).put(TetrisDirection.DOWN, down);
            int[][] left = rotateMap(down);
            pieceDirection.get(value).put(TetrisDirection.LEFT, left);
        }

    }

    public void changeDirection() {
        TetrisDirection a = direction;
        direction = direction.next();
        if (checkCollision(getCurrentI(), getCurrentJ())) {
            direction = a;
        } else {
            clearMovingPiece();
            drawPiece();
        }
    }

    public boolean checkCollision(int nextI, int nextJ) {
        final int[][] get = pieceDirection.get(piece).get(direction);
        for (int i = 0; i < get.length; i++) {
            for (int j = 0; j < get[i].length; j++) {
                if (get[i][j] == 1) {
                    if (nextI + i >= MAP_WIDTH || nextI < 0) {
                        return true;
                    }
                    if (nextJ + j >= MAP_HEIGHT) {
                        return true;
                    }
                    if (map[nextI + i][nextJ + j].getState() == TetrisPieceState.SETTLED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void clearMovingPiece() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (map[i][j].getState() == TetrisPieceState.TRANSITION) {
                    map[i][j].setState(TetrisPieceState.EMPTY);
                }
            }
        }

    }

    public void drawPiece() {
        drawPiece(TetrisPieceState.TRANSITION);
    }

    public int getCurrentI() {
        return currentI;
    }

    public int getCurrentJ() {
        return currentJ;
    }

    public void movePiecesTimeline(Timeline timeline) {
        clearMovingPiece();
        if (!checkCollision(getCurrentI(), getCurrentJ() + 1)) {
            drawPiece();
        } else {
            drawPiece(TetrisPieceState.SETTLED);
            final TetrisPiece[] values = TetrisPiece.values();
            piece = values[random.nextInt(values.length)];
            setCurrentJ(0);
            setCurrentI(MAP_WIDTH / 2);
            if (checkCollision(getCurrentI(), getCurrentJ())) {
                timeline.stop();
                new SimpleDialogBuilder().text("You Got " + 0 + " points").button("Reset", () -> {
                    reset();
                    timeline.play();
                }).bindWindow(map[0][0]).displayDialog();
            }
            for (int i = 0; i < MAP_HEIGHT; i++) {
                boolean clearLine = isLineClear(i);
                if (clearLine) {
                    removeLine(i);
                }
            }

        }
        setCurrentJ(getCurrentJ() + 1);
    }

    public void setCurrentI(int currentI) {
        this.currentI = currentI;
    }

    public void setCurrentJ(int currentJ) {
        this.currentJ = currentJ;
    }

    private void drawPiece(TetrisPieceState state) {
        final int[][] get = pieceDirection.get(piece).get(direction);
        for (int i = 0; i < get.length; i++) {
            for (int j = 0; j < get[i].length; j++) {
                if (get[i][j] == 1) {
                    map[getCurrentI() + i][getCurrentJ() + j].setState(state);
                }
            }
        }
    }

    private boolean isLineClear(int i) {
        boolean clearLine = true;
        for (int j = 0; j < MAP_WIDTH; j++) {
            if (map[j][i].getState() != TetrisPieceState.SETTLED) {
                clearLine = false;
            }
        }
        return clearLine;
    }

    private void removeLine(int i) {
        for (int k = i; k >= 0; k--) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (k == 0) {
                    map[j][k].setState(TetrisPieceState.EMPTY);
                } else {
                    map[j][k].setState(map[j][k - 1].getState());
                }
            }
        }
    }

    private void reset() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j].setState(TetrisPieceState.EMPTY);
            }
        }
    }

    private static final int[][] rotateMap(int[][] pieceMap) {
        int width = pieceMap.length;
        int height = pieceMap[0].length;
        int[][] left = new int[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                left[j][i] = pieceMap[i][height - j - 1];
            }
        }
        return left;
    }

}
