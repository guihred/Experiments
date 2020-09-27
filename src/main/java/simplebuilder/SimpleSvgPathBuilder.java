package simplebuilder;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

public class SimpleSvgPathBuilder extends SimpleShapeBuilder<SVGPath, SimpleSvgPathBuilder> {

	public SimpleSvgPathBuilder() {
		super(new SVGPath());
	}

	public SimpleSvgPathBuilder content(final String value) {
        node.setContent(value);
		return this;
	}

	public SimpleSvgPathBuilder fillRule(final FillRule value) {
        node.setFillRule(value);
		return this;

	}
}