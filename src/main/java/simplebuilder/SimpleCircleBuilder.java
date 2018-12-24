package simplebuilder;

import javafx.beans.value.ObservableValue;
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

    public SimpleCircleBuilder centerX(ObservableValue<? extends Number> d) {
        circle.centerXProperty().bind(d);
        return this;
    }

    public SimpleCircleBuilder centerY(double d) {
		circle.setCenterY(d);
		return this;
	}

	public SimpleCircleBuilder centerY(ObservableValue<? extends Number> d) {
	    circle.centerYProperty().bind(d);
	    return this;
	}

	public SimpleCircleBuilder radius(double d) {
		circle.setRadius(d);
		return this;
	}

}