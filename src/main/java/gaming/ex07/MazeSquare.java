package gaming.ex07;

import gaming.ex14.PacmanModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import simplebuilder.HasLogging;

public class MazeSquare extends BorderPane implements HasLogging {
	public static final int SQUARE_SIZE = 20;
    public static Map<MazeSquare, Map<MazeSquare, MazeSquare>> paths; // <id,cell>

	private final BooleanProperty visited = new SimpleBooleanProperty(false);
	private final BooleanProperty west = new SimpleBooleanProperty(false);
	private final BooleanProperty east = new SimpleBooleanProperty(false);
	private final BooleanProperty north = new SimpleBooleanProperty(false);
    private final BooleanProperty south = new SimpleBooleanProperty(false);
    public final int i;
	public final int j;
	private List<BoundingBox> bounds;
	private List<MazeSquare> adjacents;

	public MazeSquare(int i, int j) {
		this.i = i;
		this.j = j;
		setStyle("-fx-background-color:green;");
		styleProperty().bind(
				Bindings.when(visited).then("-fx-background-color:green;").otherwise("-fx-background-color:gray;"));
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
            getLogger().trace("{} -> {}", this, adjacents);
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

	public final boolean isEast() {
		return east.get();
	}

	public boolean isInBounds(double x, double y) {
		if (bounds == null) {

			double layoutX = i * PacmanModel.SQUARE_SIZE;
			double layoutX2 = PacmanModel.MAZE_SIZE * 2 * PacmanModel.SQUARE_SIZE - i * PacmanModel.SQUARE_SIZE
					- PacmanModel.SQUARE_SIZE;
			double layoutY = j * PacmanModel.SQUARE_SIZE;
			double layoutY2 = PacmanModel.MAZE_SIZE * 2 * PacmanModel.SQUARE_SIZE - j * PacmanModel.SQUARE_SIZE
					- PacmanModel.SQUARE_SIZE;
			List<BoundingBox> arrayList = new ArrayList<>();
			arrayList.add(new BoundingBox(layoutX, layoutY, PacmanModel.SQUARE_SIZE, PacmanModel.SQUARE_SIZE));
			arrayList.add(new BoundingBox(layoutX, layoutY2, PacmanModel.SQUARE_SIZE, PacmanModel.SQUARE_SIZE));
			arrayList.add(new BoundingBox(layoutX2, layoutY, PacmanModel.SQUARE_SIZE, PacmanModel.SQUARE_SIZE));
			arrayList.add(new BoundingBox(layoutX2, layoutY2, PacmanModel.SQUARE_SIZE, PacmanModel.SQUARE_SIZE));

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

	private Map<MazeSquare, Boolean> createDistanceMap(MazeSquare source, Map<MazeSquare, Integer> distance,
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

	public static void setPath(MazeSquare from, MazeSquare to, MazeSquare by) {
        if (paths == null) {
            paths = new LinkedHashMap<>();
        }
        if (!paths.containsKey(from)) {
            paths.put(from, new LinkedHashMap<>());
		}
        paths.get(from).put(to, by);
	}

}
