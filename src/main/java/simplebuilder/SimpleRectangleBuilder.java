package simplebuilder;

import javafx.scene.shape.Rectangle;

public class SimpleRectangleBuilder extends SimpleShapeBuilder<Rectangle, SimpleRectangleBuilder> {


	public SimpleRectangleBuilder() {
		super(new Rectangle());
	}

	public SimpleRectangleBuilder arcHeight(double x) {
        node.setArcHeight(x);
		return this;
	}

	public SimpleRectangleBuilder arcWidth(double x) {
        node.setArcWidth(x);

		return this;
	}

    public SimpleRectangleBuilder height(double x) {
        node.setHeight(x);
		return this;
	}



	public SimpleRectangleBuilder width(double x) {
        node.setWidth(x);
		return this;
	}

	public SimpleRectangleBuilder x(double x) {
        node.setX(x);
		return this;
	}

	public SimpleRectangleBuilder y(double x) {
        node.setY(x);
		return this;
	}

}