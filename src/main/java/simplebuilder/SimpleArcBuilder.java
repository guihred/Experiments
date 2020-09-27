package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class SimpleArcBuilder extends SimpleShapeBuilder<Arc, SimpleArcBuilder> {

    public SimpleArcBuilder() {
        super(new Arc());
    }

    public SimpleArcBuilder centerX(ObservableValue<? extends Number> observable) {
        node.centerXProperty().bind(observable);
        return this;
    }

    public SimpleArcBuilder centerY(ObservableValue<? extends Number> observable) {
        node.centerYProperty().bind(observable);
        return this;
    }

    public SimpleArcBuilder length(double value) {
        node.setLength(value);
        return this;
    }

    public SimpleArcBuilder radiusX(double value) {
        node.setRadiusX(value);
        return this;
    }

    public SimpleArcBuilder radiusX(ObservableValue<? extends Number> observable) {
        node.radiusXProperty().bind(observable);
        return this;
    }

    public SimpleArcBuilder radiusY(double value) {
        node.setRadiusY(value);
        return this;
    }

    public SimpleArcBuilder startAngle(double value) {
        node.setStartAngle(value);
        return this;
    }

    public SimpleArcBuilder type(ArcType value) {
        node.setType(value);
        return this;
    }

}