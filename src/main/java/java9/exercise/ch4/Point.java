package java9.exercise.ch4;

import java.util.Objects;

/**
 * 3. Make the instance variables x and y of the Point class in Exercise 1
 * protected. Show that the LabeledPoint class can access these variables only
 * in LabeledPoint instances.
 */
public class Point implements Cloneable {
    protected double y;
    protected double x;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    protected Point clone() throws CloneNotSupportedException {
        return (Point) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Point other = (Point) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[y=" + y + ", x=" + x + "]";
    }

    public double getX() {
        return x;
    }

}