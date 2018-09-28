package gaming.ex20;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import utils.HasLogging;

public class RoundMazeSquare extends Group implements HasLogging {
    public static final double SQUARE_SIZE = 20;

	private final BooleanProperty visited = new SimpleBooleanProperty(false);
	private final BooleanProperty west = new SimpleBooleanProperty(false);
	private final BooleanProperty east = new SimpleBooleanProperty(false);
	private final BooleanProperty north = new SimpleBooleanProperty(false);
    private final BooleanProperty south = new SimpleBooleanProperty(false);
    public final int i;
	public final int j;
	private List<BoundingBox> bounds;
	private List<RoundMazeSquare> adjacents;

	public RoundMazeSquare(int i, int j) {
		this.i = i;
		this.j = j;
		setStyle("-fx-background-color:green;");
		styleProperty().bind(
				Bindings.when(visited).then("-fx-background-color:green;").otherwise("-fx-background-color:gray;"));
		// final Line line = new Line(0, 0, 0, SQUARE_SIZE);
		// line.visibleProperty().bind(east.not());
		// setRight(line);
		// final Line line2 = new Line(0, 0, SQUARE_SIZE, 0);
		// line2.visibleProperty().bind(north.not());
		// setTop(line2);
		// final Line line3 = new Line(0, 0, 0, SQUARE_SIZE);
		// line3.visibleProperty().bind(west.not());
		// setLeft(line3);
		// final Line line4 = new Line(0, 0, SQUARE_SIZE, 0);
		// line4.visibleProperty().bind(south.not());
		// setBottom(line4);
		
	}

	public void setCenter(Node node) {
		// node.setTranslateX(getBoundsInLocal().getWidth() / 2);
		// node.setTranslateY(getBoundsInLocal().getHeight() / 2);
		//
		// getChildren().add(node);
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

	public final boolean isEast() {
		return east.get();
	}

	public boolean isInBounds(double x, double y) {
		if (bounds == null) {

            double layoutX = i * RoundMazeSquare.SQUARE_SIZE;
            double layoutX2 = RoundMazeModel.MAZE_SIZE * 2 * RoundMazeSquare.SQUARE_SIZE - i * RoundMazeSquare.SQUARE_SIZE
                    - RoundMazeSquare.SQUARE_SIZE;
            double layoutY = j * RoundMazeSquare.SQUARE_SIZE;
            double layoutY2 = RoundMazeModel.MAZE_SIZE * 2 * RoundMazeSquare.SQUARE_SIZE - j * RoundMazeSquare.SQUARE_SIZE
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

	public void draw(GraphicsContext gc) {
		double length = 360.0 / RoundMazeModel.MAZE_SIZE;
		int center = RoundMazeModel.CANVAS_WIDTH / 2;
		double angle = length * j;
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		int m = i * RoundMazeModel.CANVAS_WIDTH / RoundMazeModel.MAZE_SIZE;
		if (!south.get()) {
			gc.strokeArc(center - m / 2, center - m / 2, m, m, length * j, length, ArcType.OPEN);
		}
		if (!east.get()) {
			int b = (i - 1) * RoundMazeModel.CANVAS_WIDTH / RoundMazeModel.MAZE_SIZE;
			gc.strokeLine(center + cos * m, center + sin * m, center + cos * b, center + sin * b);
		}
	}

}
