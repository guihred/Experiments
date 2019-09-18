package fxsamples.bounds;

import javafx.scene.shape.Shape;

// records a pair of (possibly) intersecting shapes.
class ShapePair {
    protected Shape a;
    protected Shape b;

    public ShapePair(Shape src, Shape dest) {
        a = src;
        b = dest;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !getClass().isInstance(other)) {
            return false;
        }
        ShapePair o = (ShapePair) other;
        return a == o.a && b == o.b || a == o.b && b == o.a;
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    public boolean intersects(BoundsType boundsType) {
        if (a == b) {
            return false;
        }

        a.intersects(b.getBoundsInLocal());
        switch (boundsType) {
            case LAYOUT_BOUNDS:
                return a.getLayoutBounds().intersects(b.getLayoutBounds());
            case BOUNDS_IN_LOCAL:
                return a.getBoundsInLocal().intersects(b.getBoundsInLocal());
            case BOUNDS_IN_PARENT:
                return a.getBoundsInParent().intersects(b.getBoundsInParent());
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return a.getId() + " : " + b.getId();
    }
}