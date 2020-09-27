package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Circle;

public class SimpleCircleBuilder extends SimpleShapeBuilder<Circle, SimpleCircleBuilder> {


	public SimpleCircleBuilder() {
		super(new Circle());
	}


	public SimpleCircleBuilder centerX(double d) {
        node.setCenterX(d);
		return this;
	}

    public SimpleCircleBuilder centerX(ObservableValue<? extends Number> d) {
        node.centerXProperty().bind(d);
        return this;
    }

    public SimpleCircleBuilder centerY(double d) {
        node.setCenterY(d);
		return this;
	}

	public SimpleCircleBuilder centerY(ObservableValue<? extends Number> d) {
        node.centerYProperty().bind(d);
	    return this;
	}

	public SimpleCircleBuilder radius(double d) {
        node.setRadius(d);
		return this;
	}

}