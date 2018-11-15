package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;

public class SelectRectTool extends PaintTool {

	private Rectangle icon;
	private Rectangle area;
	private double initialX;
	private double initialY;

	public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}

	@Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
			if (getArea().getWidth() < 2 && children.contains(getArea())) {
				children.remove(getArea());
			}
            area.setStroke(Color.BLUE);
		}
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            ObservableList<Node> children = model.getImageStack().getChildren();
			if (!children.contains(getArea())) {
				children.add(getArea());
            }
            area.setStroke(Color.BLACK);
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
	}

	@Override
	public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
		KeyCode code = e.getCode();
		switch (code) {
			case DELETE:
				Bounds bounds = getArea().getBoundsInParent();
				drawRect(paintModel, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
				break;
			default:
				break;
		}
	}
}