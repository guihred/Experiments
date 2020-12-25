package fxsamples.bounds;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

// a translucent overlay display rectangle to show the bounds of a Shape.
public class BoundsDisplay extends Rectangle {
    private ObjectProperty<BoundsType> boundsPlayground;
    // the shape to which the bounds display has been type.
    private Shape monitoredShape;
    private ChangeListener<Bounds> boundsChangeListener;

    public BoundsDisplay() {
        makeShape();
    }

    public BoundsDisplay(@NamedArg("boundsPlayground") ObjectProperty<BoundsType> boundsPlayground,
        @NamedArg("monitoredShape") final Shape shape) {
        this.boundsPlayground = boundsPlayground;
        monitoredShape = shape;

        makeShape();
    }

    public ObjectProperty<BoundsType> getBoundsPlayground() {
        return boundsPlayground;
    }

    public Shape getMonitoredShape() {
        return monitoredShape;
    }

    // set the type of the shape's bounds to monitor for the bounds display.
    public final void monitorBounds(final BoundsType boundsType) {
        // remove the shape's previous boundsType.
        if (boundsChangeListener != null && boundsPlayground != null) {
            final ReadOnlyObjectProperty<Bounds> oldBounds = getOldBounds();
            if (oldBounds != null) {
                oldBounds.removeListener(boundsChangeListener);
            }
        }
        // determine the shape's bounds for the given boundsType.
        if (monitoredShape != null) {
            final ReadOnlyObjectProperty<Bounds> bounds;
            switch (boundsType) {
                case LAYOUT_BOUNDS:
                    bounds = monitoredShape.layoutBoundsProperty();
                    break;
                case BOUNDS_IN_LOCAL:
                    bounds = monitoredShape.boundsInLocalProperty();
                    break;
                case BOUNDS_IN_PARENT:
                    bounds = monitoredShape.boundsInParentProperty();
                    break;
                default:
                    return;
            }

            // set the visual bounds display based upon the new bounds and keep
            // it in sync.
            updateBoundsDisplay(bounds.get());

            // keep the visual bounds display based upon the new bounds and keep
            // it in sync.
            boundsChangeListener = (observableValue, oldBounds, newBounds) -> updateBoundsDisplay(newBounds);
            bounds.addListener(boundsChangeListener);
        }
    }

	public void setBoundsPlayground(ObjectProperty<BoundsType> boundsPlayground) {
		this.boundsPlayground = boundsPlayground;
	}

    public void setMonitoredShape(Shape monitoredShape) {
        this.monitoredShape = monitoredShape;
    }

    private ReadOnlyObjectProperty<Bounds> getOldBounds() {
		switch (boundsPlayground.get()) {
		    case LAYOUT_BOUNDS:
				return monitoredShape.layoutBoundsProperty();
		    case BOUNDS_IN_LOCAL:
				return monitoredShape.boundsInLocalProperty();
		    case BOUNDS_IN_PARENT:
				return monitoredShape.boundsInParentProperty();
		    default:
				return null;
		}
	}

    private void makeShape() {
        setFill(Color.LIGHTGRAY.deriveColor(1, 1, 1, 7. / 20));
        setStroke(Color.LIGHTGRAY.deriveColor(1, 1, 1, 1. / 2));
        setStrokeType(StrokeType.INSIDE);
        setStrokeWidth(3);

        monitorBounds(BoundsType.LAYOUT_BOUNDS);
    }

    // update this bounds display to match a new set of bounds.
    private void updateBoundsDisplay(Bounds newBounds) {
        setX(newBounds.getMinX());
        setY(newBounds.getMinY());
        setWidth(newBounds.getWidth());
        setHeight(newBounds.getHeight());
    }
}