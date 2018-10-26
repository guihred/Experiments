package gaming.ex20;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import utils.HasLogging;

public class RoundMazeSquare implements HasLogging {
    public final int i;
    public final int j;
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

    public boolean isCenter() {
        return center;
    }

    public final boolean isEast() {
        return east.get();
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

    @Override
    public String toString() {
        return "(" + i + ", " + j + ")";
    }

}
