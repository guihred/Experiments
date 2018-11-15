package paintexp.tool;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.stream.DoubleStream.iterate;
import static java.util.stream.DoubleStream.of;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import paintexp.PaintModel;

public class PolygonTool extends PaintTool {

	private Polygon icon;
	boolean pressed;

	private Polygon area;
	private Line line;

	public Polygon getArea() {
		if (area == null) {
			area = new Polygon();
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
		}
		return area;
	}

	public Line getLine() {
		if (line == null) {
			line = new Line();
			line.setStroke(Color.BLACK);
			line.setManaged(false);
		}
		return line;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			int pontas = 5;
			double[] points = iterate(0, i -> i + 1).limit(pontas)
					.flatMap(i -> of(10 * cos(i * 2 % 5 * 2 * PI / pontas), 10 * sin(i * 2 % 5 * 2 * PI / pontas)))
					.toArray();
			icon = new Polygon(points);
			icon.setFill(Color.GRAY);
			icon.setStroke(Color.GRAY);
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
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
			onMouseMoved(e);
		}
		if (MouseEvent.MOUSE_EXITED.equals(eventType)) {
			onMouseExited(model);
		}

	}

	private void onMouseExited(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getBoundsInParent().getWidth() > 2 && children.contains(getArea())) {
			ObservableList<Double> points = getArea().getPoints();
			for (int i = 0; i < points.size() + 2; i += 2) {
				Double startX = points.get(i % points.size());
				Double startY = points.get((i + 1) % points.size());
				Double endX = points.get((i + 2) % points.size());
				Double endY = points.get((i + 3) % points.size());
				drawLine(model, startX, startY, endX, endY);
			}

		}
		children.remove(getArea());
		children.remove(getLine());
		getArea().getPoints().clear();
	}

	private void onMouseMoved(final MouseEvent e) {
		ObservableList<Double> points = getArea().getPoints();
		if (points.size() > 1) {
			Double x = points.get(points.size() - 2);
			Double y = points.get(points.size() - 1);
			getLine().setStartX(x);
			getLine().setStartY(y);
			getLine().setEndX(e.getX());
			getLine().setEndY(e.getY());
		}
	}

	private void onMousePressed(final MouseEvent e, final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getArea())) {
			children.add(getArea());

		}
		if (!children.contains(getLine())) {
			children.add(getLine());
		}
		double x = e.getX();
		double y = e.getY();
		getLine().setStartX(x);
		getLine().setStartY(y);
		getLine().setEndX(x);
		getLine().setEndY(y);
		getArea().getPoints().addAll(x, y);
		getArea().setStroke(model.getFrontColor());
	}




}