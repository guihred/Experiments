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
import javafx.animation.Timeline;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

/**
 *
 * @author Note
 */
public class Maze3DModel {

    private static final int MAZE_SIZE = 24;
	private final Circle circle;
	private Maze3DSquare[][] maze = new Maze3DSquare[MAZE_SIZE][MAZE_SIZE];
    private final Random random = new Random();
	private List<Maze3DSquare> history = new ArrayList<>();
    private int row;
    private int column;
    private Timeline timeline;

    public Maze3DModel(GridPane gridPane) {
		initializeMaze(gridPane);
        maze[0][0].setCenter(new Circle(5));
        history.add(maze[0][0]);
        List<String> check = new ArrayList<>();
        timeline = new SimpleTimelineBuilder()
                .addKeyFrame(Duration.millis(10), event -> createMazeLoop(check))
                .cycleCount(Animation.INDEFINITE)
                .build();
        timeline.play();
        circle = new Circle(Maze3DSquare.SQUARE_SIZE / 3, Color.RED);
        maze[0][0].setCenter(circle);
        
    }

    private void addSides(List<String> check) {
        if (column > 0 && !maze[row][column - 1].isVisited()) {
        	check.add("L");
        }
        if (row > 0 && !maze[row - 1][column].isVisited()) {
        	check.add("U");
        }
        if (column < MAZE_SIZE - 1 && !maze[row][column + 1].isVisited()) {
        	check.add("R");
        }
        if (row < MAZE_SIZE - 1 && !maze[row + 1][column].isVisited()) {
        	check.add("D");
        }
    }

    private void createMazeLoop(List<String> check) {
		while (!history.isEmpty()) {
			maze[row][column].setVisited(true);
			check.clear();
            addSides(check);
			if (!check.isEmpty()) {
				history.add(maze[row][column]);
				final String direction = check.get(random.nextInt(check.size()));
                setWalls(direction);
            } else if (goBackIn(history)) {
				return;
			}
		}
		timeline.stop();
	}

    private boolean goBackIn(List<Maze3DSquare> history1) {
		final Maze3DSquare remove = history1.remove(history1.size() - 1);
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze.length; j++) {
				if (maze[i][j] == remove) {
					row = i;
					column = j;
					return true;
				}
			}
		}
		return false;
	}

    private void initializeMaze(GridPane gridPane) {
		for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                maze[i][j] = new Maze3DSquare();
                maze[i][j].setVisited(false);
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

	private void setWalls(final String direction) {
        if ("L".equals(direction)) {
        	maze[row][column].setWest(true);
        	column = column - 1;
        	maze[row][column].setEast(true);
        }
        if ("U".equals(direction)) {
        	maze[row][column].setNorth(true);
        	row = row - 1;
        	maze[row][column].setSouth(true);
        }
        if ("R".equals(direction)) {
        	maze[row][column].setEast(true);
        	column = column + 1;
        	maze[row][column].setWest(true);
        }
        if ("D".equals(direction)) {
        	maze[row][column].setSouth(true);
        	row = row + 1;
        	maze[row][column].setNorth(true);
        }
    }

}
