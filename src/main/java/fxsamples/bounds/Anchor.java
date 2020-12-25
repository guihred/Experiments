package fxsamples.bounds;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

// an anchor displayed around a point.
public class Anchor extends Circle {
    public Anchor() {
    }

    public Anchor(String id, DoubleProperty x, DoubleProperty y) {
        super(x.get(), y.get(), 10);
        setId(id);
        setFill(Color.GOLD.deriveColor(1, 1, 1, 1. / 2));
        setStroke(Color.GOLD);
        setStrokeWidth(2);
        setStrokeType(StrokeType.OUTSIDE);

        x.bind(centerXProperty());
        y.bind(centerYProperty());
    }
}