package paintexp.tool;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class RectangleTool extends PaintTool {

	private Rectangle icon;
	private Rectangle area;
	private double initialX;
	private double initialY;
    private FillOption option = FillOption.STROKE;

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
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        if (!e.getEventType().equals(KeyEvent.KEY_RELEASED)) {
            return;
        }
        KeyCode code = e.getCode();
        if(code==KeyCode.UP) {
            getArea().arcWidthProperty().set(Math.min(getArea().getArcWidth() + 1, 100));
        }
    
        if (code == KeyCode.DOWN) {
            getArea().arcWidthProperty().set(Math.max(getArea().getArcWidth() - 1, 0));
        }

    }

	@Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren()
				.add(new SimpleSliderBuilder(0, 100, 0).bindBidirectional(getArea().arcWidthProperty()).maxWidth(60)
						.build());
        getArea().arcHeightProperty().bind(getArea().arcWidthProperty());
        Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
        rectangle.setStroke(Color.grayRgb(128));
        rectangle.strokeProperty().bind(model.frontColorProperty());
        rectangle.arcWidthProperty().bind(getArea().arcWidthProperty());
        rectangle.arcHeightProperty().bind(getArea().arcWidthProperty());
        model.getToolOptions().getChildren().add(rectangle);
        icon = null;
        Shape icon2 = (Shape) getIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        icon = null;
        Shape icon3 = (Shape) getIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        icon = null;
        Shape icon4 = (Shape) getIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        icon = null;
        List<Node> togglesAs = new SimpleToggleGroupBuilder().addToggle(icon2, FillOption.STROKE)
                .addToggle(icon3, FillOption.FILL).addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option = newV == null ? FillOption.STROKE : (FillOption) newV.getUserData())
                .select(option).getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);

	}

	protected void dragTo(final MouseEvent e, final double x, final double y) {
		double layoutX = initialX;
		double layoutY = initialY;
        double min = Math.min(x, layoutX);
		getArea().setLayoutX(min);
        double min2 = Math.min(y, layoutY);
		getArea().setLayoutY(min2);
		double width = Math.abs(x - layoutX);
		getArea().setWidth(width);
		double height = Math.abs(y - layoutY);
		getArea().setHeight(height);
		if (e.isShiftDown()) {
            double max = Math.max(width, height);
			getArea().setWidth(max);
			getArea().setHeight(max);
		}
	}

	@Override
    protected  void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = e.getX();
		double y = e.getY();
		dragTo(e, x, y);
	}

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
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
        getArea().setStroke(Color.TRANSPARENT);
        getArea().setFill(Color.TRANSPARENT);
        if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
            getArea().setFill(model.getBackColor());
        }
        if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
            getArea().setStroke(model.getFrontColor());
        }

	}

	@Override
    protected  void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() > 2 && children.contains(getArea())) {
            Bounds boundsInLocal = getArea().getBoundsInParent();
            double startX = boundsInLocal.getMinX();
            double endX = boundsInLocal.getMaxX();
            double startY = boundsInLocal.getMinY();
            double endY = boundsInLocal.getMaxY();
            double height = boundsInLocal.getHeight();
            double width = boundsInLocal.getWidth();
            double arc = getArea().getArcWidth() / 2;
            double radiusX = Math.min(arc, width / 2);
            double radiusY = Math.min(arc, height / 2);
            double centerY1 = Math.min(startY + radiusY, startY + height / 2);
            double centerY2 = Math.max(endY - radiusY, startY - height / 2);
            double centerX1 = Math.min(startX + radiusX, startX + width / 2);
            double centerX2 = Math.max(endX - radiusX, endX - width / 2);
            if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
                drawFill(model, startX, endX, startY, endY, radiusX, radiusY, centerY1, centerY2, centerX1, centerX2);
            }
            if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
                drawStroke(model, startX, endX, startY, endY, radiusX, radiusY, centerY1, centerY2, centerX1, centerX2);
            }

        }
        children.remove(getArea());
    }

    private void drawFill(final PaintModel model, final double startX, final double endX, final double startY, final double endY,
	        final double radiusX, final double radiusY, final double centerY1, final double centerY2, final double centerX1, final double centerX2) {
        drawRect(model, centerX1, startY, centerX2 - centerX1, endY - startY);
        drawRect(model, startX, centerY1, endX - startX, centerY2 - centerY1);
        for (int i = 0; i < radiusX; i++) {
            drawCirclePart(model, centerX1, centerY1, i, radiusY, Math.PI, model.getBackColor());
            drawCirclePart(model, centerX2, centerY1, i, radiusY, Math.PI * 3 / 2, model.getBackColor());
            drawCirclePart(model, centerX1, centerY2, i, radiusY, Math.PI / 2, model.getBackColor());
            drawCirclePart(model, centerX2, centerY2, i, radiusY, 0, model.getBackColor());
        }
	}

    private void drawStroke(final PaintModel model, final double startX, final double endX, final double startY, final double endY,
            final double radiusX, final double radiusY, final double centerY1, final double centerY2,
            final double centerX1, final double centerX2) {
        drawLine(model, startX, centerY1, startX, centerY2);//LEFT
        drawLine(model, endX, centerY1, endX, centerY2);//RIGHT
        drawLine(model, centerX1, startY - 1, centerX2, startY - 1);//TOP
        drawLine(model, centerX1, endY, centerX2, endY);// BOTTOM
        drawCircle(model, centerX1, centerY1, radiusX, radiusY, Math.PI);//TOP-LEFT
        drawCircle(model, centerX2, centerY1, radiusX, radiusY, Math.PI * 3 / 2);//
        drawCircle(model, centerX1, centerY2, radiusX, radiusY, Math.PI / 2);
        drawCircle(model, centerX2, centerY2, radiusX, radiusY, 0);
    }

}