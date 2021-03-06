package gaming.ex14;

import gaming.ex07.MazeSquare;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ex.HasLogging;

public class PacmanGhost extends Group {
    private static final Logger LOG = HasLogging.log();

    private ObjectProperty<GhostStatus> status = new SimpleObjectProperty<>(GhostStatus.ALIVE);

    private GhostDirection direction = GhostDirection.NORTH;
    @FXML
    private Circle leftEye;
    @FXML
    private Circle rightEye;
    private double startX;
    private double startY;
    private MazeSquare mazeSquare;
    private Circle circle = new Circle(2);
    private GhostColor color;

    private Random random = new Random();
    @FXML
    private Polygon polygon;

    @FXML
    private Ellipse ellipse;
    @FXML
    private Ellipse ellipse2;

    public PacmanGhost() {
        this(GhostColor.BLUE);
    }

    public PacmanGhost(@NamedArg("color") GhostColor color) {
        this.color = color;
        load();
    }

    public final Circle getCircle() {
        return circle;
    }

    public GhostColor getColor() {
        return color;
    }

    public GhostDirection getDirection() {
        return direction;
    }

    public final GhostStatus getStatus() {
        return status.get();
    }

    public void move(long now, Pacman pacman, ObservableList<Node> observableList, MazeSquare[][] maze) {
        if (status.get() == GhostStatus.ALIVE) {
            shortestMovement(now, observableList, pacman, maze);
            return;
        }
        if (status.get() == GhostStatus.DEAD) {
            int step = (int) Math.signum(startX - getLayoutX());
            setLayoutX(getLayoutX() + step);
            int stepY = (int) Math.signum(startY - getLayoutY());
            setLayoutY(getLayoutY() + stepY);
            if (Math.abs(startX - getLayoutX()) < 3 && Math.abs(startY - getLayoutY()) < 3) {
                setStatus(GhostStatus.ALIVE);
            }
            return;
        }
        randomMovement(now, observableList);
    }

    public void setDirection(GhostDirection direction) {
        if (color == GhostColor.RED) {
            LOG.trace("{} -> {}", color, direction);
        }
        adjustEyes(-1);
        this.direction = direction;
        adjustEyes(1);

    }

    public void setStartPosition(double startX, double startY) {
        setLayoutX(startX);
        setLayoutY(startY);
        this.startX = startX;
        this.startY = startY;
    }

    public final void setStatus(final GhostStatus status) {
        this.status.set(status);
    }



    private void addTranslate(final int step) {
        if (direction != null) {
            setLayoutY(getLayoutY() + step * direction.y);
            setLayoutX(getLayoutX() + step * direction.x);
        }
    }

    private void adjustEyes(int mul) {
        rightEye.setLayoutX(rightEye.getLayoutX() + mul * direction.x);
        rightEye.setLayoutY(rightEye.getLayoutY() + mul * direction.y);
        leftEye.setLayoutY(leftEye.getLayoutY() + mul * direction.y);
        leftEye.setLayoutX(leftEye.getLayoutX() + mul * direction.x);
    }

    private void getBestSquare(Pacman pacman, MazeSquare[][] maze) {
        int hxg = adjustedX(getLayoutX());
        int hyg = adjustedY(getLayoutY());
        MazeSquare ghostSquare = getSquareInBounds(maze, getLayoutX(), getLayoutY());
        if (ghostSquare != null) {
            hxg = ghostSquare.i;
            hyg = ghostSquare.j;
        }

        int hx = adjustedX(pacman.getLayoutX());
        int hy = adjustedY(pacman.getLayoutY());
        MazeSquare pacmanSquare = getSquareInBounds(maze, pacman.getLayoutX(), pacman.getLayoutY());
        if (pacmanSquare != null) {
            hx = pacmanSquare.i;
            hy = pacmanSquare.j;
        }

        mazeSquare = getBestMaze(maze, hx, hy, hxg, hyg);
        if (mazeSquare != null) {
            circle.setLayoutX(readjustedX(mazeSquare.i));
            circle.setLayoutY(readjustedY(mazeSquare.j));
        }

    }

    private final void load() {
        CommonsFX.loadRoot("PacmanGhost.fxml", this);
        polygon.fillProperty()
                .bind(Bindings.when(status.isEqualTo(GhostStatus.ALIVE)).then(color.getColor())
                        .otherwise(Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.BLUEVIOLET)
                                .otherwise(Color.TRANSPARENT)));
        ellipse.fillProperty().bind(
                Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));
        ellipse2.fillProperty().bind(
                Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));
    }

    private void randomMovement(long now, ObservableList<Node> observableList) {
        final int step = 1;
        GhostDirection[] values = GhostDirection.values();
        addTranslate(step);
        if (checkColision(getBoundsInParent(), observableList)) {
            addTranslate(-step);
            setDirection(rndValue(values));
        }

        if (now % 500 == 0) {
            setDirection(rndValue(values));
        }
    }

    private double readjustedX(int i) {

        return PacmanBall.SQUARE_SIZE / 2 + (getLayoutX() > PacmanBall.SQUARE_SIZE * PacmanBall.MAZE_SIZE
                ? (PacmanBall.MAZE_SIZE * 2 - i - 1) * PacmanBall.SQUARE_SIZE
                : i * PacmanBall.SQUARE_SIZE);
    }

    private double readjustedY(int i) {

        return PacmanBall.SQUARE_SIZE / 2 + (getLayoutY() > PacmanBall.SQUARE_SIZE * PacmanBall.MAZE_SIZE
                ? (PacmanBall.MAZE_SIZE * 2 - i - 1) * PacmanBall.SQUARE_SIZE
                : i * PacmanBall.SQUARE_SIZE);
    }

    private GhostDirection rndValue(GhostDirection[] values) {
        return values[random.nextInt(values.length)];
    }

    private void shortestMovement(long now, ObservableList<Node> otherNodes, Pacman pacman, MazeSquare[][] maze) {

        if (pacman == null) {
            randomMovement(now, otherNodes);
            return;
        }
        int hx = 0;
        int hy = 0;

        if ((getLayoutY() + PacmanBall.SQUARE_SIZE / 2) % PacmanBall.SQUARE_SIZE / 2 == 0
                || (getLayoutX() + PacmanBall.SQUARE_SIZE / 2) % PacmanBall.SQUARE_SIZE / 2 == 0) {
            getBestSquare(pacman, maze);
            if (mazeSquare != null) {
                hx = (int) (-getLayoutX() + readjustedX(mazeSquare.i));
                hy = (int) (-getLayoutY() + readjustedY(mazeSquare.j));
                GhostDirection changeDirection = changeDirection(hx, hy);

                setDirection(changeDirection);
            }
        }

        final int step = 1;
        addTranslate(step);
        if (checkColision(getBoundsInParent(), otherNodes)) {
            addTranslate(-step);
            setDirection(changeDirection2(hx, hy));
            addTranslate(step);
            if (checkColision(getBoundsInParent(), otherNodes)) {

                getBestSquare(pacman, maze);
                addTranslate(-step);
                randomMovement(now, otherNodes);
            }

        }
    }

    private static int adjustedX(double layoutX) {
        double paci = layoutX / PacmanBall.SQUARE_SIZE - 1;
        return (int) (paci > PacmanBall.MAZE_SIZE ? -paci + 2 * PacmanBall.MAZE_SIZE - 1 : paci) % PacmanBall.MAZE_SIZE;
    }

    private static int adjustedY(double layoutX) {
        double paci = layoutX / PacmanBall.SQUARE_SIZE - 1;
        return (int) (paci > PacmanBall.MAZE_SIZE ? -paci - 1 + 2 * PacmanBall.MAZE_SIZE : paci) % PacmanBall.MAZE_SIZE;
    }

    private static GhostDirection changeDirection(final double hx, final double hy) {
        if (Math.abs(Math.abs(hx) - Math.abs(hy)) < PacmanBall.SQUARE_SIZE / 2) {
            if (hx < 0) {
                return hy < 0 ? GhostDirection.NORTHWEST : GhostDirection.SOUTHWEST;
            }
            return hy > 0 ? GhostDirection.SOUTHEAST : GhostDirection.NORTHEAST;
        }

        if (Math.abs(hx) > Math.abs(hy)) {

            return hx < 0 ? GhostDirection.WEST : GhostDirection.EAST;
        }
        return hy > 0 ? GhostDirection.NORTH : GhostDirection.SOUTH;
    }

    private static GhostDirection changeDirection2(double hx, double hy) {
        if (Math.abs(Math.abs(hx) - Math.abs(hy)) < PacmanBall.SQUARE_SIZE / 2) {
            if (hx > 0) {
                return hy < 0 ? GhostDirection.NORTHEAST : GhostDirection.SOUTHEAST;
            }
            return hy < 0 ? GhostDirection.SOUTHWEST : GhostDirection.NORTHWEST;
        }
        if (Math.abs(hx) < Math.abs(hy)) {
            return hy < 0 ? GhostDirection.NORTH : GhostDirection.SOUTH;
        }
        return hx < 0 ? GhostDirection.WEST : GhostDirection.EAST;
    }

    private static boolean checkColision(Bounds boundsInParent, ObservableList<Node> observableList) {
        Stream<Bounds> walls = observableList.stream().filter(Rectangle.class::isInstance).map(Node::getBoundsInParent);
        return walls.anyMatch(b -> b.intersects(boundsInParent.getMinX(), boundsInParent.getMinY(),
                boundsInParent.getWidth(), boundsInParent.getHeight()));
    }

    private static MazeSquare getBestMaze(MazeSquare[][] maze, int x, int y, int xg, int yg) {
        if (MazeSquare.getPaths() == null) {
            return null;
        }
        int hxg = Integer.max(xg, 0);
        int hyg = Integer.max(yg, 0);

        Map<MazeSquare, MazeSquare> map = MazeSquare.getPaths().get(maze[hxg][hyg]);
        if (map == null) {
            return null;
        }
        // LOG.info("f " + maze[hxg][hyg] + " t" + maze[hx][hy] + " b " +
        // mazeSquare)
        int hx = Integer.max(x, 0);
        int hy = Integer.max(y, 0);
        return map.get(maze[hx][hy]);
    }

    private static MazeSquare getSquareInBounds(MazeSquare[][] maze, double x, double y) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                boolean inBounds = maze[i][j].isInBounds(x, y);
                if (inBounds) {
                    return maze[i][j];
                }
            }
        }
        return null;
    }

    public enum GhostStatus {
        ALIVE,
        AFRAID,
        DEAD;
    }

    private enum GhostDirection {
        EAST(1, 0),
        NORTH(0, 1),
        SOUTH(0, -1),
        WEST(-1, 0),
        NORTHEAST(1, 1),
        SOUTHEAST(1, -1),
        NORTHWEST(-1, 1),
        SOUTHWEST(-1, -1);
        protected final int x;
        protected final int y;

        GhostDirection(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
