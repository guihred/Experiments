package gaming.ex20;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import utils.HasLogging;

public class RoundMazeSquare implements HasLogging {
    public static final double SQUARE_SIZE = 20;

	public final int i;
	public final int j;
	private List<RoundMazeSquare> adjacents;
	private List<BoundingBox> bounds;
    private boolean center;
    private final BooleanProperty east = new SimpleBooleanProperty(false);
	private final BooleanProperty north = new SimpleBooleanProperty(false);
	private final BooleanProperty south = new SimpleBooleanProperty(false);
	private final BooleanProperty visited = new SimpleBooleanProperty(false);

    private final BooleanProperty west = new SimpleBooleanProperty(false);

	public RoundMazeSquare(int i, int j) {
		this.i = i;
		this.j = j;
		
	}

    public List<RoundMazeSquare> adjacents(RoundMazeSquare[][] map) {
		if (adjacents == null) {
			adjacents = new ArrayList<>();
			RoundMazeSquare el = map[i][j];
			if (el.east.get() && j + 1 < map.length) {
				adjacents.add(map[i][j + 1]);
			}
			if (el.west.get() && j > 0) {
				RoundMazeSquare e = map[i][j - 1];
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


	public BooleanProperty eastProperty() {
		return east;
	}

	public boolean isCenter() {
        return center;
    }

	public final boolean isEast() {
		return east.get();
	}

	public boolean isInBounds(double x, double y) {
		if (bounds == null) {

            double layoutX = i * RoundMazeSquare.SQUARE_SIZE;
            double layoutX2 = RoundMazeModel.MAZE_WIDTH * 2 * RoundMazeSquare.SQUARE_SIZE
                    - i * RoundMazeSquare.SQUARE_SIZE
                    - RoundMazeSquare.SQUARE_SIZE;
            double layoutY = j * RoundMazeSquare.SQUARE_SIZE;
            double layoutY2 = RoundMazeModel.MAZE_HEIGHT * 2 * RoundMazeSquare.SQUARE_SIZE
                    - j * RoundMazeSquare.SQUARE_SIZE
                    - RoundMazeSquare.SQUARE_SIZE;
			List<BoundingBox> arrayList = new ArrayList<>();
            arrayList.add(new BoundingBox(layoutX, layoutY, RoundMazeSquare.SQUARE_SIZE, RoundMazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX, layoutY2, RoundMazeSquare.SQUARE_SIZE, RoundMazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX2, layoutY, RoundMazeSquare.SQUARE_SIZE, RoundMazeSquare.SQUARE_SIZE));
            arrayList.add(new BoundingBox(layoutX2, layoutY2, RoundMazeSquare.SQUARE_SIZE, RoundMazeSquare.SQUARE_SIZE));

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

	public void setCenter(boolean center) {
        this.center = center;
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

}
