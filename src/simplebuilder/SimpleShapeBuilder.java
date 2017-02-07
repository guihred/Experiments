package simplebuilder;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

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

	public Z strokeWidth(double value) {
		shape.setStrokeWidth(value);
		return (Z) this;
	}


}