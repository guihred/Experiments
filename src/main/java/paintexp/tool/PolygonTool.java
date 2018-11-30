package paintexp.tool;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.stream.DoubleStream.iterate;
import static java.util.stream.DoubleStream.of;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import paintexp.PaintModel;
import simplebuilder.SimpleToggleGroupBuilder;

public class PolygonTool extends PaintTool {

    private Polyline icon;
    private Polygon area;
	private Line line;
    private FillOption option = FillOption.STROKE;

	public Polygon getArea() {
		if (area == null) {
			area = new Polygon();
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
		}
		return area;
	}

	@Override
    public Polyline getIcon() {
		if (icon == null) {
			int pontas = 5;
			int radius = 5;
			double[] points = iterate(0, i -> i + 1).limit(pontas)
					.flatMap(i -> of(radius * cos(i * 2 % 5 * 2 * PI / pontas), radius * sin(i * 2 % 5 * 2 * PI / pontas)))
					.toArray();
            icon = new Polyline(points);
            icon.setStroke(Color.BLACK);
		}
		return icon;
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

	@Override
	public void onSelected(final PaintModel model) {
        icon = null;
        Shape icon2 = getIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        icon = null;
        Shape icon3 = getIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        icon = null;
        Shape icon4 = getIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
		icon = null;
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
                .addToggle(icon2, FillOption.STROKE).addToggle(icon3, FillOption.FILL)
                .addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option = newV == null ? FillOption.STROKE : (FillOption) newV.getUserData())
                .select(option)
                .getTogglesAs(Node.class);
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren().addAll(togglesAs);
	}

	@Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
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
        getArea().setStroke(Color.TRANSPARENT);
        getArea().setFill(Color.TRANSPARENT);
        if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
            getArea().setStroke(model.getFrontColor());
        }
        if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
            getArea().setFill(model.getBackColor());
        }
	}

	private void onMouseExited(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getBoundsInParent().getWidth() > 2 && children.contains(getArea())) {
            takeSnapshotFill(model, area);
            model.createImageVersion();
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



}