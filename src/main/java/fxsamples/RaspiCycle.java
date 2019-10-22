package fxsamples;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

/**
 *
 * @author Mark Heckler, @MkHeck
 */
public class RaspiCycle extends Application {
    private static final Color DARK_BLUE = Color.rgb(2, 2, 47);
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    private static final double SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
    private static final Logger LOG = HasLogging.log();
    private int speed = 1;
    private GraphicsContext gc;
    private Point2D startPos;
    private Point2D curPos;
    private Point2D newPos;

    private final List<Line> walls = new ArrayList<>();

    private Direction curDir = Direction.UP;

    private AnimationTimer animTimer;

    public void handleKeyPress(KeyEvent event) {
        boolean isNewDir = getNewDir(event);
        seSpeed(event);

        if (event.getCode() == KeyCode.ESCAPE) {
            animTimer.stop();
        }
        if (isNewDir) {
            // User sent Light Cycle in a new direction...
            // add the wall (light trail) to the list & start a new one
            walls.add(getLine(startPos.getX(), startPos.getY(), curPos.getX(), curPos.getY()));
            startPos = curPos;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        // Add screen boundaries to list of walls (coordinates)
        // to check for collisions
        // Top wall
        walls.add(getLine(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY()));
        // Right wall
        walls.add(getLine(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
        // Bottom wall
        walls.add(getLine(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY()));
        // Left wall
        walls.add(getLine(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY()));
        // Define starting point for our Light Cycle (bottom center)
        startPos = curPos = new Point2D(SCREEN_WIDTH / 2, SCREEN_HEIGHT - 10);
        // Prepare the Game Grid (screen)
        Canvas canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(DARK_BLUE);
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        drawGameGrid();
        drawWalls(bounds);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setCursor(Cursor.NONE);
        scene.setOnKeyPressed(this::handleKeyPress);
        primaryStage.setTitle("Raspi Cycle");
        primaryStage.setScene(scene);
        primaryStage.show();

        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                runLightCycle();
            }
        };
        animTimer.start();
    }

    private void calculateDestination() {
        double newX = 0;
        double newY = 0;
        switch (curDir) {
            case UP:
                // X doesn't change, only Y
                newX = curPos.getX();
                newY = curPos.getY() - speed;
                break;
            case DOWN:
                // X doesn't change, only Y
                newX = curPos.getX();
                newY = curPos.getY() + speed;
                break;
            case LEFT:
                // Y doesn't change, only X
                newY = curPos.getY();
                newX = curPos.getX() - speed;
                break;
            case RIGHT:
                // Y doesn't change, only X
                newY = curPos.getY();
                newX = curPos.getX() + speed;
                break;
            default:
                break;
        }
        newPos = new Point2D(newX, newY);
    }

    private void checkCollision() {
        LOG.trace("Current Position: ({}, {})", curPos.getX(), curPos.getY());
        walls.stream().filter(line -> line.getStartX() <= curPos.getX() && curPos.getX() <= line.getEndX()
            && line.getStartY() <= curPos.getY() && curPos.getY() <= line.getEndY()).forEach(line -> {
                animTimer.stop();
                LOG.info("COLLISION!");
            });
    }

    private void drawGameGrid() {
        int boxSize = (int) (Math.min(SCREEN_WIDTH, SCREEN_HEIGHT) / 10);
        int i;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);

        // Draw horizontal lines
        i = boxSize;
        while (i < SCREEN_HEIGHT) {
            gc.strokeLine(0, i, SCREEN_WIDTH, i);
            i += boxSize;
        }
        // Draw vertical lines
        i = boxSize;
        while (i < SCREEN_WIDTH) {
            gc.strokeLine(i, 0, i, SCREEN_HEIGHT);
            i += boxSize;
        }
    }

    private void drawWalls(Rectangle2D bounds) {
        gc.setStroke(Color.LIGHTBLUE);
        gc.setLineWidth(2);
        // Top wall
        gc.strokeLine(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY());
        // Right wall
        gc.strokeLine(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
        // Bottom wall
        gc.strokeLine(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY());
        // Left wall
        gc.strokeLine(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY());
    }
    private boolean getNewDir(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT && curDir != Direction.LEFT) {
            curDir = Direction.LEFT;
            return true;
        } else if (event.getCode() == KeyCode.RIGHT && curDir != Direction.RIGHT) {
            curDir = Direction.RIGHT;
            return true;
        } else if (event.getCode() == KeyCode.UP && curDir != Direction.UP) {
            curDir = Direction.UP;
            return true;
        } else if (event.getCode() == KeyCode.DOWN && curDir != Direction.DOWN) {
            curDir = Direction.DOWN;
            return true;
        }
        return false;
    }

    private void runLightCycle() {
        calculateDestination();
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(5);
        gc.strokeLine(curPos.getX(), curPos.getY(), newPos.getX(), newPos.getY());
        curPos = newPos;
        checkCollision();
    }

    private void seSpeed(KeyEvent event) {
        if (event.getCode() == KeyCode.DIGIT1) {
            speed = 1;
        } else if (event.getCode() == KeyCode.DIGIT2) {
            speed = 2;
        } else if (event.getCode() == KeyCode.DIGIT3) {
            speed = 3;
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main()
     * serves only as fallback in case the application can not be launched through
     * deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores
     * main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static Line getLine(double x1, double y1, double x2, double y2) {
        return new Line(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}