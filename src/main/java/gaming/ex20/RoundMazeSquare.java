package gaming.ex20;

public class RoundMazeSquare {
    public final int i;
    public final int j;
    private boolean center;
    private boolean east;
    private boolean north;
    private boolean south;
    private boolean visited;
    private boolean west;

    public RoundMazeSquare(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public boolean isCenter() {
        return center;
    }

    public final boolean isEast() {
        return east;
    }

    public final boolean isNorth() {
        return north;
    }

    public boolean isSouth() {
        return south;
    }

    public final boolean isVisited() {
        return visited;
    }

    public final boolean isWest() {
        return west;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    public final void setEast(final boolean east) {
        this.east=east;
    }

    public final void setNorth(final boolean north) {
        this.north=north;
    }

    public void setSouth(boolean v) {
        south=v;
    }

    public final void setVisited(final boolean visited) {
        this.visited=visited;
    }

    public final void setWest(final boolean west) {
        this.west=west;
    }

    @Override
    public String toString() {
        return "(" + i + ", " + j + ")";
    }

}
