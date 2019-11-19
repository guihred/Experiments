package simplebuilder;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

public class SimpleRotateBuilder extends SimpleTransformBuilder<Rotate> {

	protected Rotate rotate;

	public SimpleRotateBuilder() {
		super(new Rotate());
		rotate = transform;
	}

	public SimpleRotateBuilder angle(double d) {
		rotate.setAngle(d);
		return this;
	}

	public SimpleRotateBuilder axis(Point3D d) {
		rotate.setAxis(d);
		return this;
	}

	public SimpleRotateBuilder pivotX(double d) {
		rotate.setPivotX(d);
		return this;
	}

	public SimpleRotateBuilder pivotY(double d) {
		rotate.setPivotY(d);
		return this;
	}

	public SimpleRotateBuilder pivotZ(double d) {
		rotate.setPivotZ(d);
		return this;
	}

}