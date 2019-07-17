package gaming.ex14;

import gaming.ex07.MazeSquare;
import java.security.SecureRandom;
import java.util.Map;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import utils.HasLogging;

public class PacmanGhost extends Group implements HasLogging {
    private ObjectProperty<GhostStatus> status = new SimpleObjectProperty<>(GhostStatus.ALIVE);

    private GhostDirection direction = GhostDirection.NORTH;

    private Circle leftEye = new Circle(2);

    private Circle rightEye = new Circle(2);
    private double startX;
    private double startY;
    private MazeSquare mazeSquare;
    private final Circle circle = new Circle(2);
    private GhostColor color;
    private SecureRandom random = new SecureRandom();

    public PacmanGhost(GhostColor color) {
        this.color = color;
        Polygon polygon = new Polygon();
        final double radius = 12;
        for (int i = 180; i <= 360; i += 5) {
            double x = Math.cos(Math.toRadians(i)) * radius;
            double y = Math.sin(Math.toRadians(i)) * radius;
            polygon.getPoints().addAll(x, y);
        }
        getCircle().setFill(color.color);
        polygon.setFill(color.color);
        polygon.fillProperty()
                .bind(Bindings.when(status.isEqualTo(GhostStatus.ALIVE)).then(color.color)
                        .otherwise(Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.BLUEVIOLET)
                                .otherwise(Color.TRANSPARENT)));
        polygon.getPoints().addAll(-radius, 0D, -radius, 20D, -8D, 10D, -4D, 20D, 0D, 10D, 4D, 20D, 8D, 10D, radius,
                20D, radius, 0D);
        Ellipse ellipse = new Ellipse(4, 6);
        ellipse.setFill(Color.WHITE);
        ellipse.setLayoutX(-5);
        Ellipse ellipse2 = new Ellipse(4, 6);
        ellipse2.setFill(Color.WHITE);
        ellipse2.setLayoutX(5);
        ellipse.fillProperty().bind(
                Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));
        ellipse2.fillProperty().bind(
                Bindings.when(status.isEqualTo(GhostStatus.AFRAID)).then(Color.TRANSPARENT).otherwise(Color.WHITE));

        rightEye.setLayoutX(5);
        rightEye.setLayoutY(2);
        leftEye.setLayoutX(-5);
        leftEye.setLayoutY(2);

        getChildren().add(polygon);
        getChildren().add(ellipse);
        getChildren().add(ellipse2);
        getChildren().add(rightEye);
        getChildren().add(leftEye);
    }

    public final Circle getCircle() {
        return circle;
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
        } else if (status.get() == GhostStatus.DEAD) {
            int step = 1;
            if (startX > getLayoutX()) {
                setLayoutX(getLayoutX() + step);
            } else if (startX < getLayoutX()) {
                setLayoutX(getLayoutX() - step);
            }
            if (startY > getLayoutY()) {
                setLayoutY(getLayoutY() + step);
            } else if (startY < getLayoutY()) {
                setLayoutY(getLayoutY() - step);
            }
            if (Math.abs(startX - getLayoutX()) < 3 && Math.abs(startY - getLayoutY()) < 3) {
                setStatus(GhostStatus.ALIVE);
            }
        } else {
            randomMovement(now, observableList);
        }
    }

    public void setDirection(GhostDirection direction) {
        if (color == GhostColor.RED) {
            getLogger().trace("{} -> {}", color, direction);
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

    public final ObjectProperty<GhostStatus> statusProperty() {
        return status;
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

    private boolean checkColision(Bounds boundsInParent, ObservableList<Node> observableList) {
        Stream<Bounds> walls = observableList.stream().filter(Rectangle.class::isInstance).map(Node::getBoundsInParent);
        return walls.anyMatch(b -> b.intersects(boundsInParent.getMinX(), boundsInParent.getMinY(),
                boundsInParent.getWidth(), boundsInParent.getHeight()));
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
            getCircle().setLayoutX(readjustedX(mazeSquare.i));
            getCircle().setLayoutY(readjustedY(mazeSquare.j));
        }

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

        return PacmanModel.SQUARE_SIZE / 2 + (getLayoutX() > PacmanModel.SQUARE_SIZE * PacmanModel.MAZE_SIZE
                ? (PacmanModel.MAZE_SIZE * 2 - i - 1) * PacmanModel.SQUARE_SIZE
                : i * PacmanModel.SQUARE_SIZE);
    }

    private double readjustedY(int i) {

        return PacmanModel.SQUARE_SIZE / 2 + (getLayoutY() > PacmanModel.SQUARE_SIZE * PacmanModel.MAZE_SIZE
                ? (PacmanModel.MAZE_SIZE * 2 - i - 1) * PacmanModel.SQUARE_SIZE
                : i * PacmanModel.SQUARE_SIZE);
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

        if ((getLayoutY() + PacmanModel.SQUARE_SIZE / 2) % PacmanModel.SQUARE_SIZE / 2 == 0
                || (getLayoutX() + PacmanModel.SQUARE_SIZE / 2) % PacmanModel.SQUARE_SIZE / 2 == 0) {
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
        double paci = layoutX / PacmanModel.SQUARE_SIZE - 1;
        return (int) (paci > PacmanModel.MAZE_SIZE ? -paci + 2 * PacmanModel.MAZE_SIZE - 1 : paci)
                % PacmanModel.MAZE_SIZE;
    }

    private static int adjustedY(double layoutX) {
        double paci = layoutX / PacmanModel.SQUARE_SIZE - 1;
        return (int) (paci > PacmanModel.MAZE_SIZE ? -paci - 1 + 2 * PacmanModel.MAZE_SIZE : paci)
                % PacmanModel.MAZE_SIZE;
    }

    private static GhostDirection changeDirection(final double hx, final double hy) {
        if (Math.abs(Math.abs(hx) - Math.abs(hy)) < PacmanModel.SQUARE_SIZE / 2) {
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
        if (Math.abs(Math.abs(hx) - Math.abs(hy)) < PacmanModel.SQUARE_SIZE / 2) {
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
        // getLogger().info("f " + maze[hxg][hyg] + " t" + maze[hx][hy] + " b " +
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

    public enum GhostColor {
        RED(Color.RED),
        BLUE(Color.BLUE),
        ORANGE(Color.ORANGE),
        GREEN(Color.GREEN);

        private final transient Color color;

        GhostColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    public enum GhostDirection {
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

    public enum GhostStatus {
        ALIVE,
        AFRAID,
        DEAD;
    }

}
