package simplebuilder;

import javafx.scene.shape.Line;

public class SimpleLineBuilder extends SimpleShapeBuilder<Line, SimpleLineBuilder> {


	public SimpleLineBuilder() {
		super(new Line());
	}


	public SimpleLineBuilder endX(double d) {
        node.setEndX(d);
		return this;
	}

	public SimpleLineBuilder endY(double d) {
        node.setEndY(d);
		return this;
	}

	public SimpleLineBuilder startX(double d) {
        node.setStartX(d);
		return this;
	}

	public SimpleLineBuilder startY(double d) {
        node.setStartY(d);
		return this;
	}

}