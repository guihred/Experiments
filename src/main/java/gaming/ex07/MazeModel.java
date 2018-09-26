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

	private MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
    private int x;
    private int y;
	private Circle circle;
	public MazeModel(GridPane gridPane, Scene scene) {
		initializeMaze(gridPane);
		maze[0][0].setCenter(new Circle(5));
		Timeline timeline = new Timeline();
		final EventHandler<ActionEvent> eventHandler = new CreateMazeHandler(timeline, maze);
		final KeyFrame keyFrame = new KeyFrame(Duration.seconds(.001), eventHandler);
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();

		circle = new Circle(MazeSquare.SQUARE_SIZE / 3, Color.RED);
		maze[0][0].setCenter(circle);
		scene.setOnKeyPressed(this::handleKey);
	}
	private void goDown() {
        if (x < MazeModel.MAZE_SIZE - 1 && maze[x][y].isSouth()) {
            x++;
        }
    }

	private void goLeft() {
        if (y > 0 && maze[x][y].isWest()) {
            y--;
        }
    }

    private void goRight() {
        if (y < MazeModel.MAZE_SIZE - 1 && maze[x][y].isEast()) {
            y++;
        }
    }

    private void goUp() {
        if (x > 0 && maze[x][y].isNorth()) {
        	x--;
        }
    }

    private void handleKey(KeyEvent event) {
		maze[x][y].setCenter(null);
		final KeyCode code = event.getCode();
		switch (code) {
            case W:
            case UP:
                goUp();
                break;
            case S:
            case DOWN:
                goDown();
                break;
            case D:
            case RIGHT:
                goRight();
                break;
            case A:
            case LEFT:
                goLeft();
                break;
            default:
                break;

		}
		maze[x][y].setCenter(circle);
	}

    private void initializeMaze(GridPane gridPane) {
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				maze[i][j] = new MazeSquare(i, j);
				gridPane.add(maze[i][j], j, i);
				if (i == 0) {
					maze[i][j].setNorth(false);
				}
				if (j == 0) {
					maze[i][j].setWest(false);
				}
				if (j == MAZE_SIZE - 1) {
					maze[i][j].setEast(false);
				}
				if (i == MAZE_SIZE - 1) {
					maze[i][j].setSouth(false);
				}
			}
		}
	}
	public static MazeModel create(GridPane gridPane, Scene scene) {
		return new MazeModel(gridPane, scene);
	}

}
