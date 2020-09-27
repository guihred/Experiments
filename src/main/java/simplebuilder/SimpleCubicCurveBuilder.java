package simplebuilder;

import javafx.scene.shape.CubicCurve;

public class SimpleCubicCurveBuilder extends SimpleShapeBuilder<CubicCurve, SimpleCubicCurveBuilder> {

	public SimpleCubicCurveBuilder() {
		super(new CubicCurve());
	}

	public SimpleCubicCurveBuilder controlX1(double value) {
        node.setControlX1(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlX2(double value) {
        node.setControlX2(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlY1(double value) {
        node.setControlY1(value);
		return this;
	}

	public SimpleCubicCurveBuilder controlY2(double value) {
        node.setControlY2(value);
		return this;
	}

	public SimpleCubicCurveBuilder endX(double value) {
        node.setEndX(value);
		return this;
	}

	public SimpleCubicCurveBuilder endY(double value) {
        node.setEndY(value);
		return this;
	}

	public SimpleCubicCurveBuilder startX(double value) {
        node.setStartX(value);
		return this;
	}

	public SimpleCubicCurveBuilder startY(double value) {
        node.setStartY(value);
		return this;
	}
}