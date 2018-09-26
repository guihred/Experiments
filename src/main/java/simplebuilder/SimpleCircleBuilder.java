package simplebuilder;

import javafx.scene.shape.Circle;

public class SimpleCircleBuilder extends SimpleShapeBuilder<Circle, SimpleCircleBuilder> {

	protected Circle circle;

	public SimpleCircleBuilder() {
		super(new Circle());
		circle = shape;
	}


	public SimpleCircleBuilder centerX(double d) {
		circle.setCenterX(d);
		return this;
	}

	public SimpleCircleBuilder centerY(double d) {
		circle.setCenterY(d);
		return this;
	}

	public SimpleCircleBuilder radius(double d) {
		circle.setRadius(d);
		return this;
	}

}