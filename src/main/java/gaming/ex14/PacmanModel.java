
package gaming.ex14;

import gaming.ex07.CreateMazeHandler;
import gaming.ex07.MazeSquare;
import gaming.ex14.Pacman.PacmanDirection;
import gaming.ex14.PacmanGhost.GhostStatus;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class PacmanModel {

    private static final Logger LOG = HasLogging.log();

    private final List<PacmanBall> balls = DoubleStream
        .iterate(PacmanBall.SQUARE_SIZE / 2, d -> d + PacmanBall.SQUARE_SIZE).limit(PacmanBall.MAZE_SIZE * 2L)
        .mapToObj(d -> DoubleStream.iterate(PacmanBall.SQUARE_SIZE / 2, e -> e + PacmanBall.SQUARE_SIZE)
            .limit(PacmanBall.MAZE_SIZE * 2L).mapToObj(e -> new PacmanBall(d, e)))
        .flatMap(e -> e).collect(Collectors.toList());
    private final List<PacmanGhost> ghosts = Stream
        .of(GhostColor.RED, GhostColor.BLUE, GhostColor.ORANGE, GhostColor.GREEN).map(PacmanGhost::new)
        .collect(Collectors.toList());

    private final Pacman pacman = new Pacman();

    private final IntegerProperty points = new SimpleIntegerProperty(0);

    private long time;

    private PacmanModel(Group group, Scene scene) {
        Timeline timeline = new Timeline();
        MazeSquare[][] maze = initializeMaze();
        final EventHandler<ActionEvent> eventHandler = new CreateMazeHandler(timeline, maze);
        final KeyFrame keyFrame = new KeyFrame(Duration.seconds(.001), eventHandler);
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.statusProperty().addListener((ob, old, value) -> {
            if (value == Animation.Status.STOPPED) {
                createLabyrinth(maze, group);
            }
        });
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameLoop(group, now, maze);
            }
        };
        animationTimer.start();
        List<PacmanBall> ballCopy = balls.stream().collect(Collectors.toList());
        Collections.shuffle(ballCopy);
        for (int i = 0; i < 5; i++) {
            PacmanBall pacmanBall = ballCopy.remove(0);
            pacmanBall.setSpecial(true);
        }
        group.getChildren().addAll(balls);
        group.getChildren().add(pacman);
        group.getChildren().addAll(ghosts);
        pacman.setLayoutY(30);
        pacman.setLayoutX(30);
        for (int i = 0; i < ghosts.size(); i++) {
            PacmanGhost ghost = ghosts.get(i);
            int location = i / 2;
            final int initialPos = 265;
            ghost.setStartPosition(initialPos + i % 2 * PacmanBall.SQUARE_SIZE,
                initialPos + location * PacmanBall.SQUARE_SIZE);
            group.getChildren().add(ghost.getCircle());
        }
        scene.setOnKeyPressed(this::handleKeyPressed);

    }

    public IntegerProperty getPoints() {
        return points;
    }

    private void gameLoop(Group group, long now, MazeSquare[][] maze) {
        ghosts.forEach(g -> g.move(now, pacman, group.getChildren(), maze));
        pacman.move(group.getChildren());
        List<PacmanBall> bal = balls.stream().filter(b -> b.getBoundsInParent().intersects(pacman.getBoundsInParent()))
            .collect(Collectors.toList());
        if (!bal.isEmpty()) {
            getPoints().set(getPoints().get() + bal.size());
            balls.removeAll(bal);
            group.getChildren().removeAll(bal);
            if (bal.stream().anyMatch(PacmanBall::isSpecial)) {
                ghosts.stream().filter(g -> g.getStatus() == GhostStatus.ALIVE)
                    .forEach(g -> g.setStatus(GhostStatus.AFRAID));
                time = 500;
            }
        }
        List<PacmanGhost> gh = ghosts.stream().filter(b -> b.getBoundsInParent().intersects(pacman.getBoundsInParent()))
            .collect(Collectors.toList());
        if (!gh.isEmpty()) {
            if (gh.stream().anyMatch(g -> g.getStatus() == GhostStatus.ALIVE)) {
                pacman.die();
            } else {
                gh.forEach(g -> g.setStatus(GhostStatus.DEAD));
            }
        }

        if (time > 0) {
            time--;
            if (time == 0) {
                ghosts.stream().filter(g -> g.getStatus() == GhostStatus.AFRAID)
                    .forEach(g -> g.setStatus(GhostStatus.ALIVE));
            }
        }
    }

    private void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case DOWN:
                pacman.turn(PacmanDirection.DOWN);
                break;
            case UP:
                pacman.turn(PacmanDirection.UP);
                break;
            case LEFT:
                pacman.turn(PacmanDirection.LEFT);
                break;
            case RIGHT:
                pacman.turn(PacmanDirection.RIGHT);
                break;
            case SPACE:
                pacman.turn(null);
                break;
            default:
                break;

        }
    }



    public static PacmanModel create(Group group, Scene scene) {
        return new PacmanModel(group, scene);
    }

    private static void addRectangle(Group group, double value, double value2, double width, double height) {
        Rectangle rectangle = new Rectangle(width, height, Color.BLUE);
        rectangle.setLayoutX(value);
        rectangle.setLayoutY(value2);

        group.getChildren().add(rectangle);
    }

    private static MazeSquare[][] createLabyrinth(MazeSquare[][] maze, Group group) {
        for (int i = 0; i < PacmanBall.MAZE_SIZE; i++) {
            for (int j = 0; j < PacmanBall.MAZE_SIZE; j++) {
                double layoutX = i * PacmanBall.SQUARE_SIZE;
                double layoutX2 = PacmanBall.MAZE_SIZE * 2 * PacmanBall.SQUARE_SIZE - i * PacmanBall.SQUARE_SIZE
                    - PacmanBall.SQUARE_SIZE;
                double layoutY = j * PacmanBall.SQUARE_SIZE;
                double layoutY2 = PacmanBall.MAZE_SIZE * 2 * PacmanBall.SQUARE_SIZE - j * PacmanBall.SQUARE_SIZE
                    - PacmanBall.SQUARE_SIZE;

                if (!maze[i][j].isWest()) {
                    addRectangle(group, layoutX, layoutY, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX2, layoutY, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX, layoutY2 + PacmanBall.SQUARE_SIZE, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX2, layoutY2 + PacmanBall.SQUARE_SIZE, PacmanBall.SQUARE_SIZE, 2);
                }
                if (!maze[i][j].isNorth()) {
                    addRectangle(group, layoutX, layoutY, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX2 + PacmanBall.SQUARE_SIZE, layoutY, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX, layoutY2, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX2 + PacmanBall.SQUARE_SIZE, layoutY2, 2, PacmanBall.SQUARE_SIZE);
                }
                if (!maze[i][j].isEast()) {
                    addRectangle(group, layoutX, layoutY + PacmanBall.SQUARE_SIZE, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX2, layoutY + PacmanBall.SQUARE_SIZE, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX, layoutY2, PacmanBall.SQUARE_SIZE, 2);
                    addRectangle(group, layoutX2, layoutY2, PacmanBall.SQUARE_SIZE, 2);
                }
                if (!maze[i][j].isSouth()) {
                    addRectangle(group, layoutX + PacmanBall.SQUARE_SIZE, layoutY, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX2, layoutY, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX + PacmanBall.SQUARE_SIZE, layoutY2, 2, PacmanBall.SQUARE_SIZE);
                    addRectangle(group, layoutX2, layoutY2, 2, PacmanBall.SQUARE_SIZE);
                }
                maze[i][j].dijkstra(maze);
            }
        }
        MazeSquare.getPaths()
            .forEach((from, map) -> map.forEach((to, by) -> LOG.trace("from {} to {} by {}", from, to, by)));

        return maze;
    }

    private static MazeSquare[][] initializeMaze() {
        MazeSquare[][] maze = new MazeSquare[PacmanBall.MAZE_SIZE][PacmanBall.MAZE_SIZE];
        for (int i = 0; i < PacmanBall.MAZE_SIZE; i++) {
            for (int j = 0; j < PacmanBall.MAZE_SIZE; j++) {
                maze[i][j] = new MazeSquare(i, j);
                MazeSquare mazeSquare = maze[i][j];
                setOusideWalls(i, j, mazeSquare);
            }
        }
        return maze;
    }

    private static void setOusideWalls(int i, int j, MazeSquare mazeSquare) {
        if (i == 0) {
            mazeSquare.setNorth(false);
        }
        if (j == 0) {
            mazeSquare.setWest(false);
        }
        if (PacmanBall.MAZE_SIZE - 1 == j && i % 3 == 0) {
            mazeSquare.setEast(true);
        }
        if (PacmanBall.MAZE_SIZE - 1 == i && j % 3 == 0) {
            mazeSquare.setSouth(true);
        }
    }

}
