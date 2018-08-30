package exercise.java9.ch4;

class Circle extends Shape {

    private double radius;

    public Circle(Point center, double radius) {
        super(center);
        this.radius = radius;
    }

    @Override
    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}