package simplebuilder;

import java.util.stream.IntStream;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

@SuppressWarnings("unchecked")
public class SimpleShapeBuilder<T extends Shape, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {

    protected SimpleShapeBuilder(T node) {
        super(node);
    }

    public Z fill(ObservableValue<? extends Paint> fill) {
        node.fillProperty().bind(fill);
        return (Z) this;
    }

    public Z fill(Paint lightblue) {
        node.setFill(lightblue);
        return (Z) this;
    }

    public Z smooth(boolean value) {
        node.setSmooth(value);
        return (Z) this;
    }

    public Z stroke(Paint value) {
        node.setStroke(value);
        return (Z) this;
    }

    public Z strokeDashArray(int... elements) {
        node.getStrokeDashArray().clear();
        node.getStrokeDashArray().addAll(IntStream.of(elements).mapToDouble(e -> e).boxed().toArray(Double[]::new));
        return (Z) this;

    }

    public Z strokeLineCap(StrokeLineCap value) {
        node.setStrokeLineCap(value);
        return (Z) this;
    }

    public Z strokeLineJoin(StrokeLineJoin value) {
        node.setStrokeLineJoin(value);
        return (Z) this;
    }

    public Z strokeType(StrokeType value) {
        node.setStrokeType(value);
        return (Z) this;
    }

    public Z strokeWidth(double value) {
        node.setStrokeWidth(value);
        return (Z) this;
    }

}