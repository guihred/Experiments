package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import paintexp.PaintModel;

public class EllipseTool extends PaintTool {

	private Ellipse icon;
	boolean pressed;

	private Ellipse area;
	private int initialX;
	private int initialY;

	public Ellipse getArea() {
		if (area == null) {
			area = new Ellipse(5, 10);
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new Ellipse(10, 5);
			icon.setFill(Color.TRANSPARENT);
			icon.setStroke(Color.BLACK);
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.DISAPPEAR;
	}

	@Override
	public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			ObservableList<Node> children = model.getImageStack().getChildren();
			if (!children.contains(getArea())) {

				children.add(getArea());
			}
			initialX = (int) e.getX();
			getArea().setLayoutX(initialX);
			initialY = (int) e.getY();
			getArea().setLayoutY(initialY);
			getArea().setRadiusX(1);
			getArea().setRadiusY(1);
			getArea().setStroke(model.getFrontColor());
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			getArea().setRadiusX(Math.abs(e.getX() - initialX));
			getArea().setRadiusY(Math.abs(e.getY() - initialY));
		}
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
			ObservableList<Node> children = model.getImageStack().getChildren();
			if (getArea().getRadiusX() > 2 && children.contains(getArea())) {
				double a = getArea().getRadiusX();
				double b = getArea().getRadiusY();
				Bounds bounds = getArea().getBoundsInParent();
				double width = bounds.getWidth();
				double height = bounds.getHeight();
				double nPoints = Double.max(width, height) * 4;
				for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
					int x = (int) Math.round(a * Math.cos(t));
					int y = (int) Math.round(b * Math.sin(t));
					drawPoint(model, x + initialX, y + initialY);
					drawPoint(model, x + initialX, y + initialY);
				}

			}
			children.remove(getArea());
		}

	}




}