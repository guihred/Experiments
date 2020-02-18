package utils;

import static utils.ResourceFXUtils.clamp;

import com.sun.javafx.scene.control.skin.ScrollPaneSkin;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        skinProperty().addListener((ob, old, val) -> {
            ScrollPaneSkin scrollPaneSkin = (ScrollPaneSkin) val;
            scrollPaneSkin.getBehavior().dispose();
            addEventHandler(KeyEvent.ANY, e -> normalScroll(scrollPaneSkin, e));
        });
    }

    public double getScaleValue() {
        return scaleValue.get();
    }

    public void handle(ScrollEvent scrollEvent) {

        double mul = scrollEvent.getDeltaY() < 0 ? -1 : 1;
        double scale = getScaleValue();
        zoomTo(scale + mul * DELTA_ZOOM > DELTA_ZOOM ? scale + mul * DELTA_ZOOM : scale);
        double x = scrollEvent.getX();
        double y = scrollEvent.getY();
        Bounds boundsInLocal = zoomGroup.getBoundsInLocal();
        double a = x / boundsInLocal.getWidth();
        double b = y / boundsInLocal.getHeight();
        setHvalue(clamp(a, getHmin(), getHmax()));
        setVvalue(clamp(b, getVmin(), getVmax()));
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

    private static void normalScroll(ScrollPaneSkin scrollPaneSkin, KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case PAGE_UP:
                scrollPaneSkin.vsbPageDecrement();
                break;
            case SPACE:
            case PAGE_DOWN:
                scrollPaneSkin.vsbPageIncrement();
                break;
            default:
                break;
        }
    }

}