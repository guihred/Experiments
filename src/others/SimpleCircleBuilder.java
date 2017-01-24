package others;

import javafx.scene.shape.Circle;

public class SimpleCircleBuilder extends SimpleShapeBuilder<Circle> implements SimpleBuilder<Circle> {

	Circle rectangle;

	public SimpleCircleBuilder() {
		super(new Circle());
		rectangle = (Circle) shape;
		setBuilder(this);
	}

	@Override
	public Circle build() {
		return rectangle;
	}

	public SimpleCircleBuilder radius(double d) {

		return this;
	}

}