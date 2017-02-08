package simplebuilder;

import javafx.scene.shape.Line;

public class SimpleLineBuilder extends SimpleShapeBuilder<Line, SimpleLineBuilder> {

	protected Line circle;

	public SimpleLineBuilder() {
		super(new Line());
		circle = shape;
	}


	public SimpleLineBuilder startX(double d) {
		circle.setStartX(d);
		return this;
	}

	public SimpleLineBuilder endX(double d) {
		circle.setEndX(d);
		return this;
	}

	public SimpleLineBuilder startY(double d) {
		circle.setStartY(d);
		return this;
	}

	public SimpleLineBuilder endY(double d) {
		circle.setEndY(d);
		return this;
	}

}