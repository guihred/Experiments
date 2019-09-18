package ex.j9.ch4;

/**
 * 4. Define an abstract class Shape with an instance variable of class Point, a
 * constructor, a concrete method public void moveBy(double dx, double dy) that
 * moves the point by the given amount, and an abstract method public Point
 * getCenter(). Provide concrete subclasses circle, rectangle, line with
 * constructors public circle(Point center, double radius), public
 * rectangle(Point topLeft, double width, double height), and public line(Point
 * from, Point to).
 */
/**
 * 5. Define clone methods for the classes of the preceding exercise.
 */
public abstract class Shape {
    protected Point center;

    public Shape(Point center) {
        this.center = center;
    }

    public abstract Point getCenter();

    public void moveBy(double dx, double dy) {
        center.x += dx;
        center.y += dy;
    }

}