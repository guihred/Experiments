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

	public static final int MAP_HEIGHT = 20;
	public static final int MAP_WIDTH = 10;
	private int currentI, currentJ;
	private TetrisDirection direction = TetrisDirection.UP;
	private final TetrisSquare[][] map = new TetrisSquare[MAP_WIDTH][MAP_HEIGHT];
	private TetrisPiece piece = TetrisPiece.L;

    private Map<TetrisPiece, Map<TetrisDirection, int[][]>> pieceDirection;

    private Random random = new Random();

    public TetrisModel(GridPane gridPane) {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j] = new TetrisSquare();
                gridPane.add(map[i][j], i, j);
            }
        }

        pieceDirection = new HashMap<>();
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

	void changeDirection() {
        TetrisDirection a = direction;
        direction = direction.next();
        if (checkCollision(getCurrentI(), getCurrentJ())) {
            direction = a;
        } else {
            clearMovingPiece();
            drawPiece();
        }
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
					if (map[nextI + i][nextJ + j].getState() == TetrisPieceState.SETTLED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    void clearMovingPiece() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
				if (map[i][j].getState() == TetrisPieceState.TRANSITION) {
					map[i][j].setState(TetrisPieceState.EMPTY);
                }
            }
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
					map[getCurrentI() + i][getCurrentJ() + j].setState(state);
                }
            }
        }
    }

    public int getCurrentI() {
		return currentI;
	}

    public int getCurrentJ() {
		return currentJ;
	}

    EventHandler<ActionEvent> getEventHandler(Timeline timeline) {
        return (ActionEvent t) -> {
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
					boolean clearLine = isLineClear(i);
                    if (clearLine) {
						removeLine(i);
                    }
                }

            }
            setCurrentJ(getCurrentJ() + 1);

        };
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
	void reset() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
				map[i][j].setState(TetrisPieceState.EMPTY);
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
	public void setCurrentI(int currentI) {
		this.currentI = currentI;
	}

    public void setCurrentJ(int currentJ) {
		this.currentJ = currentJ;
	}

}
