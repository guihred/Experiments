package exercise.java9.ch4;

import java.util.Objects;

/**
 * Exercises
 * 
 * 1. Define a class Point with a constructor public Point(double x, double y)
 * and accessor methods getX, getY. Define a subclass LabeledPoint with a
 * constructor public LabeledPoint(String label, double x, double y) and an
 * accessor method getLabel.
 */
/**
 * 2. Define toString, equals, and hashCode methods for the classes of the
 * preceding exercise.
 */

public class LabeledPoint extends Point {

    private String label;

    public LabeledPoint(String label, double x, double y) {
        super(x, y);
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LabeledPoint other = (LabeledPoint) obj;
        return Objects.equals(label, other.label);
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, label);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[label=" + label + ", y=" + y + ", x=" + x + "]";
    }
}