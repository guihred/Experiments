package ex.j9.ch4;

public class Rectangle extends Shape {

    private double width;
    private double height;

    public Rectangle(Point topLeft, double width, double height) {
        super(topLeft);
        this.width = width;
        this.height = height;
    }

    @Override
    public Point getCenter() {
        return new Point(center.x + width / 2, center.y + height / 2);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

}