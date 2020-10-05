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
import javafx.scene.transform.Rotate;
import simplebuilder.SimpleDialogBuilder;

/**
 *
 * @author Note
 */
public class RoundMazeModel {

    public static final double CANVAS_WIDTH = 500;
    private RoundMazeSquare[][] maze = new RoundMazeSquare[RoundMazeHandler.MAZE_WIDTH][RoundMazeHandler.MAZE_HEIGHT];
    private int x = RoundMazeHandler.MAZE_WIDTH - 1;
    private int y = RoundMazeHandler.MAZE_HEIGHT - 1;
    private GraphicsContext gc;
    private Canvas canvas;
    private Rotate angle = new Rotate(90);

    public RoundMazeModel(Scene scene, Canvas canvas) {
        this.canvas = canvas;
        initializeMaze();
        gc = canvas.getGraphicsContext2D();
        maze[RoundMazeHandler.MAZE_WIDTH - 1][RoundMazeHandler.MAZE_HEIGHT - 1].setCenter(true);
        RoundMazeHandler.createMaze(maze);
        scene.setOnKeyPressed(this::handleKey);
        angle.setAxis(Rotate.Z_AXIS);
        double center = CANVAS_WIDTH / 2 + CANVAS_WIDTH / RoundMazeHandler.MAZE_WIDTH / 2;
        angle.setPivotX(center);
        angle.setPivotY(center);
        canvas.getTransforms().add(angle);
    }

    public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < RoundMazeHandler.MAZE_WIDTH; i++) {
            for (int j = 0; j < RoundMazeHandler.MAZE_HEIGHT; j++) {
                draw(maze[i][j], gc);
            }
        }
    }

    private void goCenter() {
        if (maze[x][y].isNorth()) {
            if (x > 0) {
                x = (x - 1 + RoundMazeHandler.MAZE_WIDTH) % RoundMazeHandler.MAZE_WIDTH;
            } else {
                maze[x][y].setCenter(false);
                draw();
                new SimpleDialogBuilder().text("You Won").button("_Reset", this::reset).bindWindow(canvas)
                        .displayDialog();
            }
        }
    }

    private void goLeft() {
        if (maze[x][y].isWest()) {
            y = (y - 1 + RoundMazeHandler.MAZE_HEIGHT) % RoundMazeHandler.MAZE_HEIGHT;
            angle.setAngle(angle.getAngle() - 360. / RoundMazeHandler.MAZE_HEIGHT);
        }
    }

    private void goOutter() {
        if (x < RoundMazeHandler.MAZE_WIDTH - 1 && maze[x][y].isSouth()) {
            x = (x + 1) % RoundMazeHandler.MAZE_WIDTH;
        }
    }

    private void goRight() {
        if (maze[x][y].isEast()) {
            y = (y + 1) % RoundMazeHandler.MAZE_HEIGHT;
            angle.setAngle(angle.getAngle() + 360. / RoundMazeHandler.MAZE_HEIGHT);
        }
    }

    private void handleKey(KeyEvent event) {
        maze[x][y].setCenter(false);
        final KeyCode code = event.getCode();
        switch (code) {
            case W:
            case UP:
                goCenter();
                break;
            case S:
            case DOWN:
                goOutter();
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

    private void initializeMaze() {
        for (int i = 0; i < RoundMazeHandler.MAZE_WIDTH; i++) {
            for (int j = 0; j < RoundMazeHandler.MAZE_HEIGHT; j++) {
                maze[i][j] = new RoundMazeSquare(i, j);
                if (i == 0) {
                    maze[i][j].setNorth(false);
                }
                if (i == RoundMazeHandler.MAZE_WIDTH - 1) {
                    maze[i][j].setSouth(false);
                }
            }
        }
    }

    private void reset() {
        initializeMaze();
        angle.setAngle(90);
        x = RoundMazeHandler.MAZE_WIDTH - 1;
        y = RoundMazeHandler.MAZE_HEIGHT - 1;
        maze[RoundMazeHandler.MAZE_WIDTH - 1][RoundMazeHandler.MAZE_HEIGHT - 1].setCenter(true);
        RoundMazeHandler.createMaze(maze);
        draw();
    }

    public static RoundMazeModel create(Scene scene, Canvas canvas) {
        return new RoundMazeModel(scene, canvas);
    }

    public static void draw(RoundMazeSquare sq, GraphicsContext gc) {
        double length = -360.0 / RoundMazeHandler.MAZE_HEIGHT;
        double center = CANVAS_WIDTH / 2 + CANVAS_WIDTH / RoundMazeHandler.MAZE_WIDTH / 2;
        double angle = length * (sq.j + 1);
        double m = (sq.i + 2) * CANVAS_WIDTH / 2 / RoundMazeHandler.MAZE_WIDTH;
        double b = (sq.i + 1) * CANVAS_WIDTH / 2 / RoundMazeHandler.MAZE_WIDTH;
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
            m = (sq.i + 3. / 2) * CANVAS_WIDTH / 2 / RoundMazeHandler.MAZE_WIDTH;
            gc.setFill(Color.RED);

            gc.fillOval(center + cos * m, center + sin * m, 5, 5);
        }
    }

}
