package others;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class SimpleShapeBuilder<T extends Shape> implements SimpleBuilder<T> {

	private SimpleBuilder<T> builder;
	protected Shape shape;

	protected SimpleShapeBuilder(Shape shape) {
		this.shape = shape;
	}

	@Override
	public T build() {
		return builder.build();
	}

	public SimpleShapeBuilder<T> cursor(Cursor hand) {
		shape.setCursor(hand);
		return this;
	}

	public SimpleShapeBuilder<T> fill(Color lightblue) {
		shape.setFill(lightblue);
		return this;
	}

	public SimpleShapeBuilder<T> onMouseDragged(EventHandler<? super MouseEvent> value) {
		shape.setOnMouseDragged(value);
		return this;
	}

	public SimpleShapeBuilder<T> onMousePressed(EventHandler<? super MouseEvent> object) {
		shape.setOnMousePressed(object);
		return this;
	}

	protected void setBuilder(SimpleBuilder<T> builder) {
		this.builder = builder;
	}
}