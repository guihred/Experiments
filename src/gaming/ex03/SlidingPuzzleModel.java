/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex03;

import java.util.Random;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class SlidingPuzzleModel {

    public static final int MAP_SIZE = 4;

    SlidingPuzzleSquare[][] map = new SlidingPuzzleSquare[MAP_SIZE][MAP_SIZE];
    GridPane gridPane;
    int moves = 0;
    public SlidingPuzzleModel(GridPane gridPane) {
        this.gridPane = gridPane;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new SlidingPuzzleSquare(i * MAP_SIZE + j + 1);
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

	final void reset() {

        final Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int nextI = random.nextInt(MAP_SIZE);
            int nextJ = random.nextInt(MAP_SIZE);
            swapEmptyNeighbor(nextI, nextJ);
        }

    }
    boolean verifyEnd() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (map[i][j].number.get() != i * MAP_SIZE + j + 1) {
                    return false;
                }
            }
        }

        return true;
    }

    final EventHandler<MouseEvent> createMouseClickedEvento(SlidingPuzzleSquare mem) {
        EventHandler<MouseEvent> mouseClicked = (MouseEvent event) -> {
            for (int i = 0; i < MAP_SIZE; i++) {
                for (int j = 0; j < MAP_SIZE; j++) {
					if (map[i][j] == mem && (
							isNeighborEmpty(i, j, 0, -1) 
							|| isNeighborEmpty(i, j, 0, 1)
							|| isNeighborEmpty(i, j, 1, 0) 
							|| isNeighborEmpty(i, j, -1, 0))) {
						swapEmptyNeighbor(i, j);
						moves++;
						if (verifyEnd()) {
							final Text text = new Text("You ended in " + moves + " moves");
							final Button button = new Button("Reset");
							final Stage stage1 = new Stage();
							button.setOnAction(a -> {
								reset();
								stage1.close();
								moves = 0;
							});

							final Group group = new Group(text, button);
							group.setLayoutX(50);
							group.setLayoutY(50);
							stage1.setScene(new Scene(group));
							stage1.show();
						}
						return;
					}
                }
            }


        };
        return mouseClicked;
    }

    boolean isNeighborEmpty(int i, int j, int h, int v) {
        if (i + h >= 0 && i + h < MAP_SIZE) {
            if (j + v >= 0 && j + v < MAP_SIZE) {
                return map[i + h][j + v].isEmpty();
            }
        }
        return false;
    }
    final void swapEmptyNeighbor(int i, int j) {
        for (int k = -1; k < 2; k++) {
            for (int l = -1; l < 2; l++) {
                if ((k == 0 || l == 0) && k != l) {
                    if (isNeighborEmpty(i, j, k, l)) {
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
    }

}
