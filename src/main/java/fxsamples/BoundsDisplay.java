package fxsamples;

import fxsamples.BoundsPlayground.BoundsType;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

// a translucent overlay display rectangle to show the bounds of a Shape.
class BoundsDisplay extends Rectangle {
	/**
	 * 
	 */
	private final BoundsPlayground boundsPlayground;
	// the shape to which the bounds display has been type.
	private final Shape monitoredShape;
	private ChangeListener<Bounds> boundsChangeListener;

	BoundsDisplay(BoundsPlayground boundsPlayground, final Shape shape) {
		this.boundsPlayground = boundsPlayground;
		setFill(Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.35));
		setStroke(Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.5));
		setStrokeType(StrokeType.INSIDE);
		setStrokeWidth(3);

		monitoredShape = shape;

		monitorBounds(BoundsType.LAYOUT_BOUNDS);
	}

	// set the type of the shape's bounds to monitor for the bounds display.
	void monitorBounds(final BoundsType boundsType) {
		// remove the shape's previous boundsType.
		if (boundsChangeListener != null) {
			final ReadOnlyObjectProperty<Bounds> oldBounds;
			switch (boundsPlayground.getSelectedBoundsType().get()) {
			case LAYOUT_BOUNDS:
				oldBounds = monitoredShape.layoutBoundsProperty();
				break;
			case BOUNDS_IN_LOCAL:
				oldBounds = monitoredShape.boundsInLocalProperty();
				break;
			case BOUNDS_IN_PARENT:
				oldBounds = monitoredShape.boundsInParentProperty();
				break;
			default:
				oldBounds = null;
			}
			if (oldBounds != null) {
				oldBounds.removeListener(boundsChangeListener);
			}
		}

		// determine the shape's bounds for the given boundsType.
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

	// update this bounds display to match a new set of bounds.
	private void updateBoundsDisplay(Bounds newBounds) {
		setX(newBounds.getMinX());
		setY(newBounds.getMinY());
		setWidth(newBounds.getWidth());
		setHeight(newBounds.getHeight());
	}
}