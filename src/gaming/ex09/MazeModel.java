/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex09;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    final Circle circle;
    MazeSquare[][] maze = new MazeSquare[MAZE_SIZE][MAZE_SIZE];
    GridPane gridPane;
    int moves = 0;

	public static MazeModel create(GridPane gridPane) {
		return new MazeModel(gridPane);
	}
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
						if ("L".equals(direction)) {
                            maze[r][c].west.set(true);
                            c = c - 1;
                            maze[r][c].east.set(true);
                        }
						if ("U".equals(direction)) {
                            maze[r][c].north.set(true);
                            r = r - 1;
                            maze[r][c].south.set(true);
                        }
						if ("R".equals(direction)) {
                            maze[r][c].east.set(true);
                            c = c + 1;
                            maze[r][c].west.set(true);
                        }
						if ("D".equals(direction)) {
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
        final KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), eventHandler);
        timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        
        circle = new Circle(MazeSquare.SQUARE_SIZE / 3, Color.RED);
        maze[0][0].setCenter(circle);
        
    }

}
