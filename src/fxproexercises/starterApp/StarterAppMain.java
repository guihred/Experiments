/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.starterApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StarterAppMain extends Application {


    @Override
	public void start(Stage stage) throws Exception {
		final GridPane gridPane = new GridPane();
		gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		MazeModel mazeModel = new MazeModel(gridPane);
		final Scene scene = new Scene(gridPane);
        stage.setScene(scene);
		stage.setWidth(MazeModel.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 30);
		stage.setHeight(MazeModel.MAZE_SIZE * MazeSquare.SQUARE_SIZE + 30);
        stage.show();

		final Circle circle = new Circle(MazeSquare.SQUARE_SIZE / 3, Color.RED);
		mazeModel.maze[0][0].setCenter(circle);
		scene.setOnKeyPressed((KeyEvent event) -> {
			final KeyCode code = event.getCode();
			switch (code) {
			case W:
			case UP:
				circle.setCenterX(circle.getCenterX() + 1);
				break;
			case S:
			case DOWN:
				circle.setCenterX(circle.getCenterX() - 1);
				break;
			case D:
			case RIGHT:
				circle.setCenterY(circle.getCenterY() + 1);
				break;
			case A:
			case LEFT:
				circle.setCenterY(circle.getCenterY() - 1);
				break;
			default:

			}
		});

    }

	public static void main(String[] args) {
		launch(args);
    }
}
class MazeModel {

	public static final int MAZE_SIZE = 24;
	static final Random random = new Random();

	MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
	GridPane gridPane;
	int moves = 0;

	public MazeModel(GridPane gridPane) {
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
		List<MazeSquare> history = new ArrayList<>();
		history.add(maze[0][0]);
		List<String> check = new ArrayList<>();
		Timeline timeline = new Timeline();
		final EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			int r = 0, c = 0;

			@Override
			public void handle(ActionEvent event) {
				if (!history.isEmpty()) {
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
				} else {
					timeline.stop();
				}
            }
		};
		final KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.005), eventHandler);
		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();



        
    }

}
class MazeSquare extends BorderPane {
	public static final int SQUARE_SIZE = 20;

	BooleanProperty visited = new SimpleBooleanProperty(false);
	BooleanProperty west = new SimpleBooleanProperty(false);
	BooleanProperty east = new SimpleBooleanProperty(false);
	BooleanProperty north = new SimpleBooleanProperty(false);
	BooleanProperty south = new SimpleBooleanProperty(false);

	public MazeSquare() {
		setStyle("-fx-background-color:green;");
		setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
		final Line line = new Line(0, 0, 0, SQUARE_SIZE);
		line.visibleProperty().bind(east.not());
		setLeft(line);
		final Line line2 = new Line(0, 0, SQUARE_SIZE, 0);
		line2.visibleProperty().bind(north.not());
		setTop(line2);
		final Line line3 = new Line(0, 0, 0, SQUARE_SIZE);
		line3.visibleProperty().bind(west.not());
		setRight(line3);
		final Line line4 = new Line(0, 0, SQUARE_SIZE, 0);
		line4.visibleProperty().bind(south.not());
		setBottom(line4);
    }

}


