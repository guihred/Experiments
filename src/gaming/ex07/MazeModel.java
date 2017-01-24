/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex07;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 *
 * @author Note
 */
public class MazeModel {

	public static final int MAZE_SIZE = 24;
	static final Random random = new Random();

	MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
	GridPane gridPane;
	int moves = 0;

	public MazeModel(GridPane gridPane, Scene scene) {
		this.gridPane = gridPane;
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				maze[i][j] = new MazeSquare();
				gridPane.add(maze[i][j], j, i);
				if (i == 0) {
					maze[i][j].north.set(false);
				}
				if (j == 0) {
					maze[i][j].west.set(false);
				}
				if (j == MAZE_SIZE - 1) {
					maze[i][j].east.set(false);
				}
				if (i == MAZE_SIZE - 1) {
					maze[i][j].south.set(false);
				}
			}
		}
		maze[0][0].setCenter(new Circle(5));
		List<MazeSquare> history = new ArrayList<>();
		history.add(maze[0][0]);
		List<String> check = new ArrayList<>();
		Timeline timeline = new Timeline();
		final EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			int r = 0, c = 0;

			@Override
			public void handle(ActionEvent event) {
				while (!history.isEmpty()) {
					maze[r][c].visited.set(true);
					check.clear();

					if (c > 0 && !maze[r][c - 1].visited.get()) {
						check.add("L");
					}
					if (r > 0 && !maze[r - 1][c].visited.get()) {
						check.add("U");
					}
					if (c < MAZE_SIZE - 1 && !maze[r][c + 1].visited.get()) {
						check.add("R");
					}
					if (r < MAZE_SIZE - 1 && !maze[r + 1][c].visited.get()) {
						check.add("D");
					}
					if (!check.isEmpty()) {
						history.add(maze[r][c]);
						final String direction = check.get(random.nextInt(check.size()));
						if (direction.equals("L")) {
							maze[r][c].west.set(true);
							c = c - 1;
							maze[r][c].east.set(true);
						}
						if (direction.equals("U")) {
							maze[r][c].north.set(true);
							r = r - 1;
							maze[r][c].south.set(true);
						}
						if (direction.equals("R")) {
							maze[r][c].east.set(true);
							c = c + 1;
							maze[r][c].west.set(true);
						}
						if (direction.equals("D")) {
							maze[r][c].south.set(true);
							r = r + 1;
							maze[r][c].north.set(true);
						}
					} else {
						final MazeSquare remove = history.remove(history.size() - 1);
						for (int i = 0; i < MAZE_SIZE; i++) {
							for (int j = 0; j < MAZE_SIZE; j++) {
								if (maze[i][j] == remove) {
									r = i;
									c = j;
									return;
								}
							}
						}
					}
				}
				timeline.stop();
			}
		};
		final KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.001), eventHandler);
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

		final Circle circle = new Circle(MazeSquare.SQUARE_SIZE / 3, Color.RED);
		maze[0][0].setCenter(circle);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			int i = 0, j = 0;

			@Override
			public void handle(KeyEvent event) {
				maze[i][j].setCenter(null);
				final KeyCode code = event.getCode();
				switch (code) {
				case W:
				case UP:
					if (i > 0 && maze[i][j].north.get()) {
						i--;
					}
					break;
				case S:
				case DOWN:
					if (i < MazeModel.MAZE_SIZE - 1 && maze[i][j].south.get()) {
						i++;
					}
					break;
				case D:
				case RIGHT:
					if (j < MazeModel.MAZE_SIZE - 1 && maze[i][j].east.get()) {
						j++;
					}
					break;
				case A:
				case LEFT:
					if (j > 0 && maze[i][j].west.get()) {
						j--;
					}
					break;
				default:
					break;

				}
				maze[i][j].setCenter(circle);

			}
		});

	}

}
