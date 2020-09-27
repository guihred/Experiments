package simplebuilder;

import javafx.scene.shape.Ellipse;

public class SimpleEllipseBuilder extends SimpleShapeBuilder<Ellipse, SimpleEllipseBuilder> {


	public SimpleEllipseBuilder() {
		super(new Ellipse());
	}

	public SimpleEllipseBuilder centerX(double value) {
        node.setCenterX(value);
		return this;
	}

	public SimpleEllipseBuilder centerY(double value) {
        node.setCenterY(value);
		return this;
	}
	public SimpleEllipseBuilder radiusX(double value) {
        node.setRadiusX(value);
		return this;
	}

	public SimpleEllipseBuilder radiusY(double value) {
        node.setRadiusY(value);
		return this;

	}


}