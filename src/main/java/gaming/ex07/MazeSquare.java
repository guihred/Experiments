package gaming.ex07;

import java.util.*;
import java.util.Map.Entry;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class MazeSquare extends BorderPane {
    public static final double SQUARE_SIZE = 20;
    private static Map<MazeSquare, Map<MazeSquare, MazeSquare>> paths; // <id,cell>

    private static final Logger LOG = HasLogging.log();
    public static final int MAZE_SIZE = 24;
    private final BooleanProperty visited = new SimpleBooleanProperty(false);
    private final BooleanProperty west = new SimpleBooleanProperty(false);
    private final BooleanProperty east = new SimpleBooleanProperty(false);
    private final BooleanProperty north = new SimpleBooleanProperty(false);
    private final BooleanProperty south = new SimpleBooleanProperty(false);
    public final int i;
    public final int j;

    private List<BoundingBox> bounds;
    private List<MazeSquare> adjacents;

    public MazeSquare(@NamedArg("i") int i, @NamedArg("j") int j) {
        this.i = i;
        this.j = j;
        setStyle("-fx-background-color:green;");
        visited.addListener((ob, old, n) -> setStyle(n ? "-fx-background-color:green;" : "-fx-background-color:gray;"));
        setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
        final Line line = new Line(0, 0, 0, SQUARE_SIZE);
        line.visibleProperty().bind(east.not());
        setRight(line);
        final Line line2 = new Line(0, 0, SQUARE_SIZE, 0);
        line2.visibleProperty().bind(north.not());
        setTop(line2);
        final Line line3 = new Line(0, 0, 0, SQUARE_SIZE);
        line3.visibleProperty().bind(west.not());
        setLeft(line3);
        final Line line4 = new Line(0, 0, SQUARE_SIZE, 0);
        line4.visibleProperty().bind(south.not());
        setBottom(line4);
    }

    public List<MazeSquare> adjacents(MazeSquare[][] map) {
        if (adjacents == null) {
            adjacents = new ArrayList<>();
            MazeSquare el = map[i][j];
            if (el.east.get() && j + 1 < map.length) {
                adjacents.add(map[i][j + 1]);
            }
            if (el.west.get() && j > 0) {
                MazeSquare e = map[i][j - 1];
                adjacents.add(e);

            }
            if (el.north.get() && i > 0) {
                adjacents.add(map[i - 1][j]);

            }
            if (el.south.get() && i + 1 < map.length) {
                adjacents.add(map[i + 1][j]);
            }
            LOG.trace("{} -> {}", this, adjacents);
        }

        return adjacents;
    }

    public Map<MazeSquare, Integer> dijkstra(final MazeSquare[][] map) {
        Map<MazeSquare, Integer> distance = new LinkedHashMap<>();
        Map<MazeSquare, Boolean> known = createDistanceMap(this, distance, map);
        while (known.entrySet().stream().anyMatch(e -> !e.getValue())) {
            Entry<MazeSquare, Integer> orElse = distance.entrySet().stream().filter(e -> !known.get(e.getKey()))
                .min(Comparator.comparing(Entry<MazeSquare, Integer>::getValue)).orElse(null);
            if (orElse == null) {
                break;
            }

            MazeSquare v = orElse.getKey();
            known.put(v, true);
            for (MazeSquare w : v.adjacents(map)) {
                if (!known.get(w)) {
                    Integer cvw = 1;
                    if (distance.get(v) + cvw < distance.get(w)) {
                        distance.put(w, distance.get(v) + cvw);
                        setPath(w, this, v);
                    }
                }
            }
        }
        return distance;
    }

    public BooleanProperty eastProperty() {
        return east;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public final boolean isEast() {
        return east.get();
    }

    public boolean isInBounds(double x, double y) {
        if (bounds == null) {

            double layoutX = i * MazeSquare.SQUARE_SIZE;
            double layoutX2 = MazeSquare.MAZE_SIZE * 2 * MazeSquare.SQUARE_SIZE - i * MazeSquare.SQUARE_SIZE
                - MazeSquare.SQUARE_SIZE;
            double layoutY = j * MazeSquare.SQUARE_SIZE;
            double layoutY2 = MazeSquare.MAZE_SIZE * 2 * MazeSquare.SQUARE_SIZE - j * MazeSquare.SQUARE_SIZE
                - MazeSquare.SQUARE_SIZE;
            List<BoundingBox> arrayList = new ArrayList<>();
            arrayList.add(new BoundingBox(layoutX, layoutY, MazeSquare.SQUARE_SIZE, MazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX, layoutY2, MazeSquare.SQUARE_SIZE, MazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX2, layoutY, MazeSquare.SQUARE_SIZE, MazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX2, layoutY2, MazeSquare.SQUARE_SIZE, MazeSquare.SQUARE_SIZE));

            bounds = arrayList;
        }
        return bounds.stream().anyMatch(e -> e.contains(x, y));

    }

    public final boolean isNorth() {
        return north.get();
    }

    public boolean isSouth() {
        return south.get();
    }

    public final boolean isVisited() {
        return visited.get();
    }

    public final boolean isWest() {
        return west.get();
    }

    public BooleanProperty northProperty() {
        return north;
    }

    public final void setEast(final boolean east) {
        this.east.set(east);
    }

    public final void setNorth(final boolean north) {
        this.north.set(north);
    }

    public void setSouth(boolean v) {
        south.set(v);
    }

    public final void setVisited(final boolean visited) {
        this.visited.set(visited);
    }

    public final void setWest(final boolean west) {
        this.west.set(west);
    }

    public BooleanProperty southProperty() {
        return south;
    }

    @Override
    public String toString() {
        return "(" + i + ", " + j + ")";
    }

    public BooleanProperty westProperty() {
        return west;
    }

    public static Map<MazeSquare, Map<MazeSquare, MazeSquare>> getPaths() {
        return paths;
    }

    public static void setPath(MazeSquare from, MazeSquare to, MazeSquare by) {
        if (paths == null) {
            paths = new LinkedHashMap<>();
        }
        paths.computeIfAbsent(from, f -> new LinkedHashMap<>()).put(to, by);
    }

    private static Map<MazeSquare, Boolean> createDistanceMap(MazeSquare source, Map<MazeSquare, Integer> distance,
        final MazeSquare[][] map) {
        Map<MazeSquare, Boolean> known = new LinkedHashMap<>();
        for (MazeSquare[] v : map) {
            for (MazeSquare el : v) {

                distance.put(el, Integer.MAX_VALUE);
                known.put(el, false);
            }
        }
        distance.put(source, 0);
        return known;
    }
}
