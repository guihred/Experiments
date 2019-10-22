package utils;

import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;

public class ZoomableScrollPane extends ScrollPane {
    protected static final double DELTA_ZOOM = 0.1;
    private Group zoomGroup;
    private Scale scaleTransform;
    private DoubleProperty scaleValue = new SimpleDoubleProperty(1);

    public ZoomableScrollPane(@NamedArg("content") Node content) {
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(getScaleValue(), getScaleValue(), 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);

        zoomGroup.setOnScroll(this::handle);
    }

    public double getScaleValue() {
        return scaleValue.get();
    }

    public void handle(ScrollEvent scrollEvent) {
        double s = getScaleValue();
        if (scrollEvent.getDeltaY() < 0) {
            setScaleValue(getScaleValue() - DELTA_ZOOM);
        } else {
            setScaleValue(getScaleValue() + DELTA_ZOOM);
        }
        if (getScaleValue() <= DELTA_ZOOM) {
            setScaleValue(s);

        }
        zoomTo(getScaleValue());
        scrollEvent.consume();
    }
    public DoubleProperty scaleValueProperty() {
        return scaleValue;
    }

    public void setScaleValue(double scaleValue) {
        this.scaleValue.set(scaleValue);
    }




    public void zoomTo(double scaleValue1) {

        setScaleValue(scaleValue1);

        scaleTransform.setX(scaleValue1);
        scaleTransform.setY(scaleValue1);

    }



}