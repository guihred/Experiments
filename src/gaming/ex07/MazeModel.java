/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex07;

import javafx.animation.Animation;
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

	MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
	GridPane gridPane;
	int moves = 0;

	public static MazeModel create(GridPane gridPane, Scene scene) {
		return new MazeModel(gridPane, scene);
	}
	public MazeModel(GridPane gridPane, Scene scene) {
		this.gridPane = gridPane;
		initializeMaze(gridPane);
		maze[0][0].setCenter(new Circle(5));
		Timeline timeline = new Timeline();
		final EventHandler<ActionEvent> eventHandler = new CreateMazeHandler(timeline, maze);
		final KeyFrame keyFrame = new KeyFrame(Duration.seconds(.001), eventHandler);
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
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

	private void initializeMaze(GridPane gridPane) {
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
	}

}
