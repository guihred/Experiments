/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex10;

import java.util.Random;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class MinesweeperModel {

    public static final int MAP_WIDTH = 8;
    public static final int MAP_HEIGHT = 8;

    MinesweeperSquare[][] map = new MinesweeperSquare[MAP_WIDTH][MAP_HEIGHT];
    IntegerProperty nPlayed = new SimpleIntegerProperty(0);
    GridPane gridPane;
    long startTime = 0;
    public MinesweeperModel(GridPane gridPane) {
        this.gridPane = gridPane;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new MinesweeperSquare(i, j);
            }
        }
        final Random random = new Random();
        for (int i = 0; i < 10; i++) {

            int j = random.nextInt(MAP_WIDTH);
            int k = random.nextInt(MAP_HEIGHT);

            final MinesweeperSquare mem = map[j][k];
            mem.minesweeperImage.set(MinesweeperImage.BOMB);
        }
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (map[i][j].minesweeperImage.get() == MinesweeperImage.BLANK) {
					int num = countBombsAround(i, j);
                    if (num != 0) {
                        map[i][j].num = num;
                        map[i][j].minesweeperImage.set(MinesweeperImage.NUMBER);
                    }

                }
                createMouseClickedEvent(map[i][j]);
            }
        }
        startTime = System.currentTimeMillis();

    }

	private int countBombsAround(int i, int j) {
		int num = 0;
		for (int k = -1; k <= 1; k++) {
		    for (int l = -1; l <= 1; l++) {
		        if (l == 0 && k == 0) {
		            continue;
		        }
		        if (i + k >= 0 && i + k < MAP_WIDTH) {
		            if (j + l >= 0 && j + l < MAP_HEIGHT) {
		                if (map[i + k][j + l].minesweeperImage.get().equals(MinesweeperImage.BOMB)) {
		                    num++;
		                }
		            }
		        }
		    }
		}
		return num;
	}

    final EventHandler<MouseEvent> createMouseClickedEvent(MinesweeperSquare mem) {
        EventHandler<MouseEvent> mouseClicked = (MouseEvent event) -> {
            if (mem.state.get() == MinesweeperSquare.State.HIDDEN) {
                nPlayed.set(nPlayed.get() + 1);
                mem.state.set(MinesweeperSquare.State.SHOWN);
                if (mem.minesweeperImage.get().equals(MinesweeperImage.BOMB)) {
                    if (nPlayed.get() == 0) {
                        reset();
                    }

                    final Text text = new Text("You exploded in " + " moves");
                    final Button button = new Button("Reset");
                    final Stage stage1 = new Stage();
                    button.setOnAction(a -> {
                        reset();
                        stage1.close();
                    });

                    final Group group = new Group(text, button);
                    group.setLayoutX(50);
                    group.setLayoutY(50);
                    stage1.setScene(new Scene(group));
                    stage1.show();
                }
                if (mem.minesweeperImage.get().equals(MinesweeperImage.BLANK)) {
                    showNeighbours(mem.i, mem.j);
                }
                if (verifyEnd()) {
                    final Text text = new Text("You won in " + (System.currentTimeMillis() - startTime) / 1000
                            + " seconds! ");
                    final Button button = new Button("Reset");
                    final Stage stage1 = new Stage();
                    button.setOnAction(a -> {
                        reset();
                        stage1.close();
                    });

                    final Group group = new Group(text, button);
                    group.setLayoutX(50);
                    group.setLayoutY(50);
                    stage1.setScene(new Scene(group));
                    stage1.show();
                }

            }
        };
        mem.getFinalShape().setOnMouseClicked(mouseClicked);
        mem.setOnMouseClicked(mouseClicked);
        return mouseClicked;
    }
    boolean verifyEnd() {
        return Stream.of(map).flatMap(Stream::of).noneMatch(s -> s.state.get().equals(MinesweeperSquare.State.HIDDEN) && !s.minesweeperImage.get().equals(MinesweeperImage.BOMB));
    }

    void reset() {
        nPlayed.set(0);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j].shape = null;
                map[i][j].minesweeperImage.set(MinesweeperImage.BLANK);
                map[i][j].state.set(MinesweeperSquare.State.HIDDEN);
            }
        }
        final Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final MinesweeperImage bomb = MinesweeperImage.BOMB;

            int j = random.nextInt(MAP_WIDTH);
            int k = random.nextInt(MAP_HEIGHT);

            final MinesweeperSquare mem = map[j][k];
            mem.minesweeperImage.set(bomb);
        }
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (map[i][j].minesweeperImage.get() == MinesweeperImage.BLANK) {
					int num = countBombsAround(i, j);
                    if (num != 0) {
                        map[i][j].num = num;
                        map[i][j].minesweeperImage.set(MinesweeperImage.NUMBER);
                    }

                }
                createMouseClickedEvent(map[i][j]);
            }
        }
        gridPane.getChildren().clear();
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {

                MinesweeperSquare map1 = map[i][j];
                gridPane.add(new StackPane(map1, map1.getFinalShape()), i, j);
            }
        }
        startTime = System.currentTimeMillis();
    }
    void showNeighbours(int i, int j) {
        map[i][j].state.set(MinesweeperSquare.State.SHOWN);
        for (int k = -1; k <= 1; k++) {
            for (int l = -1; l <= 1; l++) {
                if (l == 0 && k == 0) {
                    continue;
                }
				if (i + k >= 0 && i + k < MAP_WIDTH && j + l >= 0 && j + l < MAP_HEIGHT) {
					if (map[i + k][j + l].minesweeperImage.get().equals(MinesweeperImage.BLANK)
							&& map[i + k][j + l].state.get().equals(MinesweeperSquare.State.HIDDEN)) {
						showNeighbours(i + k, j + l);
					}
					map[i + k][j + l].state.set(MinesweeperSquare.State.SHOWN);
				}
            }
        }

    }

}
