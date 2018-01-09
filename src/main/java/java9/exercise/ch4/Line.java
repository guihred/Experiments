package java9.exercise.ch4;

public class Line extends Shape {

    private Point from;
    private Point to;

    public Line(Point from, Point to) {
        super(from);
        this.from = from;
        this.to = to;
    }

    @Override
    public Point getCenter() {
        return new Point((from.x + to.x) / 2, (from.y + to.y) / 2);
    }

}