/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex20;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 *
 * @author Note
 */
public class RoundMazeModel {

    public static final int MAZE_HEIGHT = 40;
    public static final int MAZE_WIDTH = 7;
    public static final double CANVAS_WIDTH = 500;
    private RoundMazeSquare[][] maze = new RoundMazeSquare[MAZE_WIDTH][MAZE_HEIGHT];
    private int x = MAZE_WIDTH - 1;
    private int y = MAZE_HEIGHT - 1;
	private GraphicsContext gc;
    private Canvas canvas;

	public RoundMazeModel(Scene scene, Canvas canvas) {
        this.canvas = canvas;
        initializeMaze();
		gc = canvas.getGraphicsContext2D();
        maze[MAZE_WIDTH - 1][MAZE_HEIGHT - 1].setCenter(true);
        RoundMazeHandler.createMaze(maze);
		scene.setOnKeyPressed(this::handleKey);
	}

	public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < MAZE_WIDTH; i++) {
            for (int j = 0; j < MAZE_HEIGHT; j++) {
                draw(maze[i][j], gc);
            }
        }
	}


    public static void draw(RoundMazeSquare sq, GraphicsContext gc) {
        double length = -360.0 / MAZE_HEIGHT;
        double center = CANVAS_WIDTH / 2 + CANVAS_WIDTH / MAZE_WIDTH / 2;
        double angle = length * (sq.j + 1);
        double m = (sq.i + 2) * CANVAS_WIDTH / 2 / MAZE_WIDTH;
        double b = (sq.i + 1) * CANVAS_WIDTH / 2 / MAZE_WIDTH;
        if (!sq.isSouth()) {
            gc.strokeArc(center - m, center - m, m * 2, m * 2, -angle, length, ArcType.OPEN);
        }
        if (!sq.isNorth()) {
            gc.strokeArc(center - b, center - b, b * 2, b * 2, -angle, length, ArcType.OPEN);
        }
        if (!sq.isEast()) {
            double sin = Math.sin(Math.toRadians(angle));
            double cos = Math.cos(Math.toRadians(angle));
            gc.strokeLine(center + cos * m, center + sin * m, center + cos * b, center + sin * b);
        }
        if (sq.isCenter()) {
            angle = length * sq.j;
            double sin = Math.sin(Math.toRadians(angle + length / 2));
            double cos = Math.cos(Math.toRadians(angle + length / 2));
            m = (sq.i + 1.5) * CANVAS_WIDTH / 2 / MAZE_WIDTH;
            gc.setFill(Color.RED);

            gc.fillOval(center + cos * m, center + sin * m, 5, 5);
        }
    }
	private void initializeMaze() {
        for (int i = 0; i < MAZE_WIDTH; i++) {
            for (int j = 0; j < MAZE_HEIGHT; j++) {
				maze[i][j] = new RoundMazeSquare(i, j);
				if (i == 0) {
					maze[i][j].setNorth(false);
				}
                if (i == MAZE_WIDTH - 1) {
					maze[i][j].setSouth(false);
				}
			}
		}
	}

	private void handleKey(KeyEvent event) {
        maze[x][y].setCenter(false);
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
        maze[x][y].setCenter(true);
		draw();
	}

    private void goLeft() {
        if (maze[x][y].isWest()) {
            y = (y - 1 + RoundMazeModel.MAZE_HEIGHT) % RoundMazeModel.MAZE_HEIGHT;
        }
    }

    private void goRight() {
        if (maze[x][y].isEast()) {
            y = (y + 1) % RoundMazeModel.MAZE_HEIGHT;
        }
    }

    private void goDown() {
        if (x < RoundMazeModel.MAZE_WIDTH - 1 && maze[x][y].isSouth()) {
            x = (x + 1) % RoundMazeModel.MAZE_WIDTH;
        }
    }

    private void goUp() {
        if (x > 0 && maze[x][y].isNorth()) {
            x = (x - 1 + RoundMazeModel.MAZE_WIDTH) % RoundMazeModel.MAZE_WIDTH;
        }
    }

	public static RoundMazeModel create(Scene scene, Canvas canvas) {
		return new RoundMazeModel(scene, canvas);
	}

}
