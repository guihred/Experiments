package ex.j9.ch4;

import java.util.Objects;

/**
 * 3. Make the instance variables x and y of the Point class in Exercise 1
 * protected. Show that the Labeled Point class can access these variables only
 * in Labeled Point instances.
 */
public class PointCh4 {
    protected double y;
    protected double x;

    public PointCh4(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointCh4 copy() {
        return new PointCh4(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !this.getClass().isInstance(obj)) {
            return false;
        }
        PointCh4 other = (PointCh4) obj;
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