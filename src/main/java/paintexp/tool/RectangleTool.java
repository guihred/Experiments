package paintexp.tool;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class RectangleTool extends PaintTool {

	private Rectangle area;
	private double initialX;
	private double initialY;
	private FillOption option = FillOption.STROKE;
	private Slider arcWidthSlider;

	@Override
	public Node createIcon() {
		return new SimpleRectangleBuilder().width(30).height(30).fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.build();
	}

	public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).build();
		}
		return area;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.DEFAULT;
	}

	@Override
	public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
		PaintToolHelper.handleSlider(e, getArea().arcWidthProperty(), arcWidthSlider);
	}

	@Override
	public void onSelected(final PaintModel model) {
		model.getToolOptions().getChildren().clear();
		model.getToolOptions().getChildren().add(getArcWidthSlider());
		getArea().arcHeightProperty().bind(getArea().arcWidthProperty());
		Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
		rectangle.setStroke(Color.GRAY);
		rectangle.strokeProperty().bind(model.frontColorProperty());
		rectangle.arcWidthProperty().bind(getArea().arcWidthProperty());
		rectangle.arcHeightProperty().bind(getArea().arcWidthProperty());
		model.getToolOptions().getChildren().add(rectangle);
		Shape icon2 = (Shape) createIcon();
		icon2.strokeProperty().bind(model.frontColorProperty());
		icon2.setFill(Color.TRANSPARENT);
		Shape icon3 = (Shape) createIcon();
		icon3.setStroke(Color.TRANSPARENT);
		icon3.fillProperty().bind(model.backColorProperty());
		Shape icon4 = (Shape) createIcon();
		icon4.strokeProperty().bind(model.frontColorProperty());
		icon4.fillProperty().bind(model.backColorProperty());
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
	protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = e.getX();
		double y = e.getY();
		dragTo(e, x, y);
	}

	@Override
	protected void onMousePressed(final MouseEvent e, final PaintModel model) {
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
	protected void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getWidth() > 2 && children.contains(getArea())) {
			Bounds boundsInLocal = getArea().getBoundsInParent();
			RectBuilder rect = new RectBuilder();
			rect.bound(boundsInLocal).arc(getArea().getArcWidth() / 2);
			if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
				rect.drawFill(model);
			}
			if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
				rect.drawStroke(model);
			}

		}
		children.remove(getArea());
	}

	private Slider getArcWidthSlider() {
		if (arcWidthSlider == null) {
			arcWidthSlider = new SimpleSliderBuilder(0, 100, 0).bindBidirectional(getArea().arcWidthProperty())
					.maxWidth(60).build();
		}
		return arcWidthSlider;
	}

}