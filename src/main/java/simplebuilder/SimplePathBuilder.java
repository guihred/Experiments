package simplebuilder;

import javafx.scene.shape.*;

public class SimplePathBuilder extends SimpleShapeBuilder<Path, SimplePathBuilder> {

    protected Path path;

	public SimplePathBuilder() {
        super(new Path());
        path = shape;
	}

    public SimplePathBuilder add(PathElement e) {
        path.getElements().add(e);
        return this;
    }

    public SimplePathBuilder arcTo(double radiusX, double radiusY, double xAxisRotation, double x, double y,
            boolean largeArcFlag, boolean sweepFlag) {
        return add(new ArcTo(radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag));
    }

    public SimplePathBuilder closePath() {
        return add(new ClosePath());
    }

    public SimplePathBuilder cubicCurveTo(double controlX1, double controlY1, double controlX2, double controlY2,
        double x, double y) {
        return add(new CubicCurveTo(controlX1, controlY1, controlX2, controlY2, x, y));
    }

    public SimplePathBuilder hLineTo(double x) {
        return add(new HLineTo(x));
    }

    public SimplePathBuilder lineTo(double x, double y) {
        return add(new LineTo(x, y));
    }

    public SimplePathBuilder moveTo(double x, double y) {
        return add(new MoveTo(x, y));
    }

    public SimplePathBuilder quadCurveTo(double controlX, double controlY, double x, double y) {
        return add(new QuadCurveTo(controlX, controlY, x, y));
    }

    public SimplePathBuilder vLineTo(double x) {
        return add(new VLineTo(x));
    }
}