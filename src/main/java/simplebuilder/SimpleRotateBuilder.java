package simplebuilder;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

public class SimpleRotateBuilder extends SimpleTransformBuilder<Rotate> {


	public SimpleRotateBuilder() {
		super(new Rotate());
	}

	public SimpleRotateBuilder angle(double d) {
        transform.setAngle(d);
		return this;
	}

	public SimpleRotateBuilder axis(Point3D d) {
        transform.setAxis(d);
		return this;
	}

	public SimpleRotateBuilder pivotX(double d) {
        transform.setPivotX(d);
		return this;
	}

	public SimpleRotateBuilder pivotY(double d) {
        transform.setPivotY(d);
		return this;
	}

	public SimpleRotateBuilder pivotZ(double d) {
        transform.setPivotZ(d);
		return this;
	}

}