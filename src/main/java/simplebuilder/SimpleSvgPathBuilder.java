package simplebuilder;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

public class SimpleSvgPathBuilder extends SimpleShapeBuilder<SVGPath, SimpleSvgPathBuilder> {

	protected SVGPath svgPath;

	public SimpleSvgPathBuilder() {
		super(new SVGPath());
		svgPath = shape;
	}

	public SimpleSvgPathBuilder content(final String value) {
		svgPath.setContent(value);
		return this;
	}

	public SimpleSvgPathBuilder fillRule(final FillRule value) {
		svgPath.setFillRule(value);
		return this;

	}
}