package others;

import javafx.scene.shape.Rectangle;

public class SimpleRectangleBuilder extends SimpleShapeBuilder<Rectangle> implements SimpleBuilder<Rectangle> {

	Rectangle rectangle;

	public SimpleRectangleBuilder() {
		super(new Rectangle());
		rectangle = (Rectangle) shape;
		setBuilder(this);
	}

	public SimpleRectangleBuilder arcHeight(double x) {
		rectangle.setArcHeight(x);
		return this;
	}

	public SimpleRectangleBuilder arcWidth(double x) {
		rectangle.setArcWidth(x);
		return this;
	}

	@Override
	public javafx.scene.shape.Rectangle build() {
		return rectangle;
	}

	public SimpleRectangleBuilder height(double x) {
		rectangle.setHeight(x);
		return this;
	}

	public SimpleRectangleBuilder width(double x) {
		rectangle.setWidth(x);
		return this;
	}

	public SimpleRectangleBuilder x(double x) {
		rectangle.setX(x);
		return this;
	}

	public SimpleRectangleBuilder y(double x) {
		rectangle.setY(x);
		return this;
	}

}