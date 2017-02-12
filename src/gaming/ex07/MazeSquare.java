package gaming.ex07;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;

public class MazeSquare extends BorderPane {
    public static final int SQUARE_SIZE = 20;

	private final BooleanProperty visited = new SimpleBooleanProperty(false);
	private final BooleanProperty west = new SimpleBooleanProperty(false);
	private final BooleanProperty east = new SimpleBooleanProperty(false);
	private final BooleanProperty north = new SimpleBooleanProperty(false);
	private final BooleanProperty south = new SimpleBooleanProperty(false);

    public MazeSquare() {
        setStyle("-fx-background-color:green;");
        styleProperty().bind(Bindings.when(visited).then("-fx-background-color:green;").otherwise("-fx-background-color:gray;"));
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

	public BooleanProperty northProperty() {
		return north;
	}

	public BooleanProperty southProperty() {
		return south;
	}

	public BooleanProperty westProperty() {
		return west;
	}

	public BooleanProperty eastProperty() {
		return east;
	}

	public void setSouth(boolean v) {
		south.set(v);
	}

	public final boolean isVisited() {
		return visited.get();
	}

	public final void setVisited(final boolean visited) {
		this.visited.set(visited);
	}

	public boolean isSouth() {
		return south.get();
	}

	public final boolean isWest() {
		return west.get();
	}

	public final void setWest(final boolean west) {
		this.west.set(west);
	}

	public final boolean isEast() {
		return east.get();
	}


	public final void setEast(final boolean east) {
		this.east.set(east);
	}

	public final boolean isNorth() {
		return north.get();
	}


	public final void setNorth(final boolean north) {
		this.north.set(north);
	}


}
