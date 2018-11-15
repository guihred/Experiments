package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;

public class RectangleTool extends PaintTool {

	private Rectangle icon;
	private Rectangle area;
	private double initialX;
	private double initialY;

	public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.build();
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.build();
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.DEFAULT;
	}

	@Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
			if (!children.contains(getArea())) {
				children.add(getArea());
            }
			getArea().setManaged(false);
			initialX = e.getX();
			getArea().setLayoutX(initialX);
			initialY = e.getY();
			getArea().setLayoutY(initialY);
			getArea().setWidth(1);
			getArea().setHeight(1);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			double layoutX = initialX;
			double layoutY = initialY;
			double x = e.getX();
			double min = Double.min(x, layoutX);
			getArea().setLayoutX(min);
			double y = e.getY();
			double min2 = Double.min(y, layoutY);
			getArea().setLayoutY(min2);
			getArea().setWidth(Math.abs(e.getX() - layoutX));
			getArea().setHeight(Math.abs(e.getY() - layoutY));
		}
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
			ObservableList<Node> children = model.getImageStack().getChildren();
			if (getArea().getWidth() > 2 && children.contains(getArea())) {
				Bounds boundsInLocal = getArea().getBoundsInParent();
				double startX = boundsInLocal.getMinX();
				double endX = boundsInLocal.getMaxX();
				double startY = boundsInLocal.getMinY();
				double endY = boundsInLocal.getMaxY();
				drawLine(model, startX, startY, startX, endY);
				drawLine(model, endX, startY, endX, endY);
				drawLine(model, startX, startY - 1, endX, startY - 1);
				drawLine(model, startX, endY, endX, endY);
			}
			children.remove(getArea());
		}

	}
}