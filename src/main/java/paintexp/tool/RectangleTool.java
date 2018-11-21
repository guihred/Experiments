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
import simplebuilder.SimpleSliderBuilder;

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
    public void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        if (!e.getEventType().equals(KeyEvent.KEY_TYPED)) {
            return;
        }
        KeyCode code = e.getCode();
        if(code==KeyCode.UP) {
            getArea().arcWidthProperty().set(Double.min(getArea().getArcWidth() + 1, 100));
        }
    
        if (code == KeyCode.DOWN) {
            getArea().arcWidthProperty().set(Double.max(getArea().getArcWidth() - 1, 0));
        }

    }

	@Override
	public void onSelected(PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(0, 100, 0).bindBidirectional(getArea().arcWidthProperty()).build());
        getArea().arcHeightProperty().bind(getArea().arcWidthProperty());
        Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
        rectangle.setStroke(Color.grayRgb(128));
        rectangle.strokeProperty().bind(model.frontColorProperty());
        rectangle.arcWidthProperty().bind(getArea().arcWidthProperty());
        rectangle.arcHeightProperty().bind(getArea().arcWidthProperty());

        model.getToolOptions().getChildren().add(rectangle);
	}

	private void onMouseDragged(final MouseEvent e) {
		double layoutX = initialX;
		double layoutY = initialY;
		double x = e.getX();
		double min = Double.min(x, layoutX);
		getArea().setLayoutX(min);
		double y = e.getY();
		double min2 = Double.min(y, layoutY);
		getArea().setLayoutY(min2);
		double width = Math.abs(e.getX() - layoutX);
		getArea().setWidth(width);
		double height = Math.abs(e.getY() - layoutY);
		getArea().setHeight(height);
		if (e.isShiftDown()) {
			double max = Double.max(width, height);
			getArea().setWidth(max);
			getArea().setHeight(max);
		}
	}

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
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
		getArea().setStroke(model.getFrontColor());
	}

    private void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() > 2 && children.contains(getArea())) {
            Bounds boundsInLocal = getArea().getBoundsInParent();
            double startX = boundsInLocal.getMinX();
            double endX = boundsInLocal.getMaxX();
            double startY = boundsInLocal.getMinY();
            double endY = boundsInLocal.getMaxY();
            double height = boundsInLocal.getHeight();
            double width = boundsInLocal.getWidth();
            double max = Double.max(width, height);
            double arc = getArea().getArcWidth() / 2;
            double radiusX = Double.min(arc, width / 2);
            double radiusY = Double.min(arc, height / 2);
            double centerY1 = Double.min(startY + radiusY, startY + height / 2);
            double centerY2 = Double.max(endY - radiusY, startY - height / 2);
            drawLine(model, startX, centerY1, startX, centerY2);//LEFT
            drawLine(model, endX, centerY1, endX, centerY2);//RIGHT
            double centerX1 = Double.min(startX + radiusX, startX + width / 2);
            double centerX2 = Double.max(endX - radiusX, endX - width / 2);
            drawLine(model, centerX1, startY - 1, centerX2, startY - 1);//TOP
            drawLine(model, centerX1, endY, centerX2, endY);// BOTTOM

            double nPoints = max * 4;
            drawCircle(model, centerX1, centerY1, radiusX, radiusY, nPoints, Math.PI, Math.PI / 2);//TOP-LEFT
            drawCircle(model, centerX2, centerY1, radiusX, radiusY, nPoints, Math.PI * 3 / 2, Math.PI / 2);//
            drawCircle(model, centerX1, centerY2, radiusX, radiusY, nPoints, Math.PI / 2, Math.PI / 2);
            drawCircle(model, centerX2, centerY2, radiusX, radiusY, nPoints, 0, Math.PI / 2);

        }
        children.remove(getArea());
    }

}