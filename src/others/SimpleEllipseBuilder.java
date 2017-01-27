package others;

import javafx.scene.shape.Ellipse;

public class SimpleEllipseBuilder extends SimpleShapeBuilder<Ellipse, SimpleEllipseBuilder> {

	Ellipse ellipse;

	public SimpleEllipseBuilder() {
		super(new Ellipse());
		ellipse = shape;
	}

	public SimpleEllipseBuilder centerX(double value) {
		ellipse.setCenterX(value);
		return this;
	}

	public SimpleEllipseBuilder centerY(double value) {
		ellipse.setCenterY(value);
		return this;
	}
	public SimpleEllipseBuilder radiusX(double value) {
		ellipse.setRadiusX(value);
		return this;
	}

	public SimpleEllipseBuilder radiusY(double value) {
		ellipse.setRadiusY(value);
		return this;

	}


}