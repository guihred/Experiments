package ex.j9.ch4;

import java.util.Objects;

/**
 * 3. Make the instance variables x and y of the Point class in Exercise 1
 * protected. Show that the LabeledPoint class can access these variables only
 * in LabeledPoint instances.
 */
public class Point {
    protected double y;
    protected double x;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point copy() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point) || getClass() != obj.getClass()) {
            return false;
        }
        Point other = (Point) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        return Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[y=" + y + ", x=" + x + "]";
    }

}