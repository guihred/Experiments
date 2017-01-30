 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex05;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class TetrisModel {

    public static final int MAP_WIDTH = 10;
    public static final int MAP_HEIGHT = 20;

    TetrisSquare[][] map = new TetrisSquare[MAP_WIDTH][MAP_HEIGHT];
    GridPane gridPane;
    int currentI, currentJ;
    Random random = new Random();
    Piece piece = Piece.L;
    Map<Piece, Map<Direction, int[][]>> pieceDirection;
    Direction direction = Direction.UP;

    public TetrisModel(GridPane gridPane) {
        this.gridPane = gridPane;
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j] = new TetrisSquare();
                gridPane.add(map[i][j], i, j);
            }
        }

        pieceDirection = new HashMap<>();
        for (Piece value : Piece.values()) {
            pieceDirection.put(value, new HashMap<>());
            pieceDirection.get(value).put(Direction.UP, value.map);

            int[][] right = rotateMap(value.map);
            pieceDirection.get(value).put(Direction.RIGHT, right);
            int[][] down = rotateMap(right);
            pieceDirection.get(value).put(Direction.DOWN, down);
            int[][] left = rotateMap(down);
            pieceDirection.get(value).put(Direction.LEFT, left);
        }

    }

    void clearMovingPiece() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (map[i][j].state.get() == TetrisPieceState.TRANSITION) {
                    map[i][j].state.set(TetrisPieceState.EMPTY);
                }
            }
        }

    }

    EventHandler<ActionEvent> getEventHandler(Timeline timeline) {
        return (ActionEvent t) -> {
            clearMovingPiece();
            if (!checkCollision(currentI, currentJ + 1)) {
                drawPiece();
            } else {
                drawPiece(TetrisPieceState.SETTLED);
                final Piece[] values = Piece.values();
                piece = values[random.nextInt(values.length)];
                currentJ = 0;
                currentI = MAP_WIDTH / 2;
                if (checkCollision(currentI, currentJ)) {
                    timeline.stop();
                    final Text text = new Text("You Got " + 0 + " points");
                    final Button button = new Button("Reset");
                    final Stage stage1 = new Stage();
                    button.setOnAction(a -> {
                       reset();
                        timeline.play();
                        stage1.close();
                    });

                    final Group group = new Group(text, button);
                    group.setLayoutX(50);
                    group.setLayoutY(50);
                    stage1.setScene(new Scene(group));
                    stage1.show();
                }
                for (int i = 0; i < MAP_HEIGHT; i++) {
                    boolean clearLine = true;
                    for (int j = 0; j < MAP_WIDTH; j++) {
                        if (map[j][i].state.get() != TetrisPieceState.SETTLED) {
                            clearLine = false;
                        }
                    }
                    if (clearLine) {
                        for (int k = i; k >= 0; k--) {
                            for (int j = 0; j < MAP_WIDTH; j++) {
                                if (k == 0) {
                                    map[j][k].state.set(TetrisPieceState.EMPTY);
                                } else {
                                    map[j][k].state.set(map[j][k - 1].state.get());
                                }
                            }
                        }
                    }
                }

            }
            currentJ++;

        };
    }
    void reset() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j].state.set(TetrisPieceState.EMPTY);
            }
        }
    }


    final int[][] rotateMap(int[][] pieceMap) {
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


    boolean checkCollision(int nextI, int nextJ) {
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
                    if (map[nextI + i][nextJ + j].state.get() == TetrisPieceState.SETTLED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void changeDirection() {
        Direction a = direction;
        direction = direction.next();
        if (checkCollision(currentI, currentJ)) {
            direction = a;
        } else {
            clearMovingPiece();
            drawPiece();
        }
    }

    void drawPiece() {
        drawPiece(TetrisPieceState.TRANSITION);
    }

    void drawPiece(TetrisPieceState state) {
        final int[][] get = pieceDirection.get(piece).get(direction);
        for (int i = 0; i < get.length; i++) {
            for (int j = 0; j < get[i].length; j++) {
                if (get[i][j] == 1) {
                    map[currentI + i][currentJ + j].state.set(state);
                }
            }
        }
    }
    enum Direction {
        UP, RIGHT, DOWN, LEFT;

        Direction next() {
            final Direction[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    enum Piece {

        S(new int[][]{
            {1, 0},
            {1, 1},
            {0, 1}
        }),
        Z(new int[][]{
            {0, 1},
            {1, 1},
            {1, 0}
        }),
        I(new int[][]{
            {1, 1, 1, 1}
        }),
        O(new int[][]{
            {1, 1},
            {1, 1}}),
        J(new int[][]{
            {0, 1},
            {0, 1},
            {1, 1}
        }),
        L(new int[][]{
            {1, 0},
            {1, 0},
            {1, 1}
        }),
        T(new int[][]{
            {0, 1},
            {1, 1},
            {0, 1}
        });
        int[][] map;

        private Piece(int[][] map) {
            this.map = map;
        }

    }

}
