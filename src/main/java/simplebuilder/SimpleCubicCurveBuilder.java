package simplebuilder;

import javafx.scene.shape.CubicCurve;

public class SimpleCubicCurveBuilder extends SimpleShapeBuilder<CubicCurve, SimpleCubicCurveBuilder> {

	public SimpleCubicCurveBuilder() {
		super(new CubicCurve());
	}

	public SimpleCubicCurveBuilder controlX1(double value) {
		shape.setControlX1(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlX2(double value) {
		shape.setControlX2(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlY1(double value) {
		shape.setControlY1(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlY2(double value) {
		shape.setControlY2(value);
		return this;
	}

	public SimpleCubicCurveBuilder endX(double value) {
		shape.setEndX(value);
		return this;
	}

	public SimpleCubicCurveBuilder endY(double value) {
		shape.setEndY(value);
		return this;
	}

	public SimpleCubicCurveBuilder startX(double value) {
		shape.setStartX(value);
		return this;
	}

	public SimpleCubicCurveBuilder startY(double value) {
		shape.setStartY(value);
		return this;
	}
}