package simplebuilder;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

@SuppressWarnings("unchecked")
public class SimpleShapeBuilder<T extends Shape, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {

	protected T shape;

	protected SimpleShapeBuilder(T shape) {
		super(shape);
		this.shape = shape;

	}


	public Z fill(Paint lightblue) {
		shape.setFill(lightblue);
		return (Z) this;
	}

	public Z stroke(Paint value) {
		shape.setStroke(value);
		return (Z) this;
	}
	public Z strokeLineCap(StrokeLineCap value) {
		shape.setStrokeLineCap(value);
		return (Z) this;
	}
	public Z strokeLineJoin(StrokeLineJoin value) {
		shape.setStrokeLineJoin(value);
		return (Z) this;
	}

	public Z strokeType(StrokeType value) {
		shape.setStrokeType(value);
		return (Z) this;
	}

	public Z strokeWidth(double value) {
		shape.setStrokeWidth(value);
		return (Z) this;
	}


}