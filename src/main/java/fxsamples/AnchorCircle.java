package fxsamples;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import utils.Delta;

// a draggable anchor displayed around a point.
public class AnchorCircle extends Circle {
	public AnchorCircle(Color color, DoubleProperty x, DoubleProperty y) {
		super(x.get(), y.get(), 10);
		setFill(color.deriveColor(1, 1, 1, 0.5));
		setStroke(color);
		setStrokeWidth(2);
		setStrokeType(StrokeType.OUTSIDE);

		x.bind(centerXProperty());
		y.bind(centerYProperty());
		enableDrag();
	}

	// make a node movable by dragging it around with the mouse.
	private void enableDrag() {
		final Delta dragDelta = new Delta();
		setOnMousePressed(mouseEvent -> {
			// record a delta distance for the drag and drop operation.
			dragDelta.setX(getCenterX() - mouseEvent.getX());
			dragDelta.setY(getCenterY() - mouseEvent.getY());
			getScene().setCursor(Cursor.MOVE);
		});
		setOnMouseReleased(mouseEvent -> getScene().setCursor(Cursor.HAND));
		setOnMouseDragged(mouseEvent -> {
			double newX = mouseEvent.getX() + dragDelta.getX();
			if (newX > 0 && newX < getScene().getWidth()) {
				setCenterX(newX);
			}
			double newY = mouseEvent.getY() + dragDelta.getY();
			if (newY > 0 && newY < getScene().getHeight()) {
				setCenterY(newY);
			}
		});
		setOnMouseEntered(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown()) {
				getScene().setCursor(Cursor.HAND);
			}
		});
		setOnMouseExited(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown()) {
				getScene().setCursor(Cursor.DEFAULT);
			}
		});
	}

	// records relative x and y co-ordinates.
}