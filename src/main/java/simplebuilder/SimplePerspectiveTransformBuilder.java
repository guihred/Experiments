package simplebuilder;

import javafx.scene.effect.PerspectiveTransform;

public class SimplePerspectiveTransformBuilder implements SimpleBuilder<PerspectiveTransform> {

	protected PerspectiveTransform perspectiveTransform = new PerspectiveTransform();

	@Override
	public PerspectiveTransform build() {
		return perspectiveTransform;
	}

	public SimplePerspectiveTransformBuilder llx(double d) {
		perspectiveTransform.setLlx(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder lly(double d) {
		perspectiveTransform.setLly(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder lrx(double d) {
		perspectiveTransform.setLrx(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder lry(double d) {
		perspectiveTransform.setLry(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder ulx(double d) {
		perspectiveTransform.setUlx(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder uly(double d) {
		perspectiveTransform.setUly(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder urx(double d) {
		perspectiveTransform.setUrx(d);
		return this;
	}

	public SimplePerspectiveTransformBuilder ury(double d) {
		perspectiveTransform.setUry(d);
		return this;
	}


}