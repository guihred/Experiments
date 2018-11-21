package paintexp.tool;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import paintexp.PaintModel;
import simplebuilder.SimpleToggleGroupBuilder;

public class EllipseTool extends PaintTool {

	private Ellipse icon;
	boolean pressed;

	private Ellipse area;
	private int initialX;
	private int initialY;

    private FillOption option = FillOption.STROKE;

	public Ellipse getArea() {
		if (area == null) {
            area = new Ellipse(3, 5);
			area.setFill(Color.TRANSPARENT);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
		}
		return area;
	}

	@Override
	public Ellipse getIcon() {
		if (icon == null) {
            icon = new Ellipse(6, 4);
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
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			onMouseDragged(e);
		}
		if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
			onMouseReleased(model);
		}

	}

	@Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();
        icon = null;
        Ellipse icon2 = getIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        icon = null;
        Ellipse icon3 = getIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        icon = null;
        Ellipse icon4 = getIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        icon = null;
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
                .addToggle(icon2, FillOption.STROKE).addToggle(icon3, FillOption.FILL)
                .addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option = (FillOption) newV.getUserData())
                .getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
    }

    private void onMouseDragged(final MouseEvent e) {
		double radiusX = Math.abs(e.getX() - initialX);
		getArea().setRadiusX(radiusX);
		double radiusY = Math.abs(e.getY() - initialY);
		getArea().setRadiusY(radiusY);
		if (e.isShiftDown()) {
			double max = Double.max(radiusX, radiusY);
			getArea().setRadiusX(max);
			getArea().setRadiusY(max);
		}
	}

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
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
        getArea().setStroke(Color.TRANSPARENT);
        getArea().setFill(Color.TRANSPARENT);
        if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
            getArea().setStroke(model.getFrontColor());
        }
        if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
            getArea().setFill(model.getBackColor());
        }
    }

    private void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getRadiusX() > 2 && children.contains(getArea())) {
            double a = getArea().getRadiusX();
            double b = getArea().getRadiusY();
            Bounds bounds = getArea().getBoundsInParent();
            double width = bounds.getWidth();
            double height = bounds.getHeight();
            double nPoints = Double.max(width, height) * 4;
            if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
                for (int i = 0; i < a; i++) {
                    drawCircle(model, initialX, initialY, i, b, nPoints, model.getBackColor());
                }
            }
            if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
                drawCircle(model, initialX, initialY, a, b, nPoints, model.getFrontColor());
            }
        }
        children.remove(getArea());
    }



}