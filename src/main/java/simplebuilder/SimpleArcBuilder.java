package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class SimpleArcBuilder extends SimpleShapeBuilder<Arc, SimpleArcBuilder> {

	protected Arc arc;

	public SimpleArcBuilder() {
		super(new Arc());
		arc = shape;
	}


	public SimpleArcBuilder centerX(double value) {
		arc.setCenterX(value);
		return this;
	}

	public SimpleArcBuilder centerX(ObservableValue<? extends Number> observable) {
		arc.centerXProperty().bind(observable);
		return this;
	}

	public SimpleArcBuilder centerY(double value) {
		arc.setCenterY(value);
		return this;
	}

	public SimpleArcBuilder centerY(ObservableValue<? extends Number> observable) {
		arc.centerYProperty().bind(observable);
		return this;
	}
	public SimpleArcBuilder length(double value) {
		arc.setLength(value);
		return this;
	}

	public SimpleArcBuilder radiusX(double value) {
		arc.setRadiusX(value);
		return this;
	}

	public SimpleArcBuilder radiusX(ObservableValue<? extends Number> observable) {
		arc.radiusXProperty().bind(observable);
		return this;
	}

	public SimpleArcBuilder radiusY(double value) {
		arc.setRadiusY(value);
		return this;
	}

	public SimpleArcBuilder radiusY(ObservableValue<? extends Number> observable) {
		arc.radiusYProperty().bind(observable);
		return this;
	}

	public SimpleArcBuilder startAngle(double value) {
		arc.setStartAngle(value);
		return this;
	}

	public SimpleArcBuilder type(ArcType value) {
		arc.setType(value);
		return this;
	}


}