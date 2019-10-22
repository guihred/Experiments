package ex.j9.ch4;

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
        return new PointCh4(center.x + width / 2, center.y + height / 2);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

}