package ex.j9.ch4;

import static utils.FunctionEx.mapIf;

public class RectangleCh4 extends ShapeCh4 {

    private double width;

    private double height;
    public RectangleCh4(PointCh4 topLeft, double width, double height) {
        super(topLeft);
        this.width = width;
        this.height = height;
    }

    @Override
    public PointCh4 getCenter() {
        return mapIf(center, c -> new PointCh4(c.x + width / 2, c.y + height / 2));
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return String.format("RectangleCh4 [%s, %s, %s]", getCenter(), getHeight(), getWidth());
    }

}