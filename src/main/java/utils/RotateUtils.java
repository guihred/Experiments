package utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class RotateUtils {
    public static void makeZoomable(Node control) {

        final double MAX_SCALE = 20.0;
        final double MIN_SCALE = 0.1;

        control.addEventFilter(ScrollEvent.ANY, event -> {
            double delta = 1.2;
            double scale = control.getScaleX();
            if (event.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            scale = ResourceFXUtils.clamp(scale, MIN_SCALE, MAX_SCALE);
            control.setScaleX(scale);
            control.setScaleY(scale);
            event.consume();
        });

    }

    public static void setSpinnable(Node cube, Scene scene) {
        DoubleProperty mousePosX = new SimpleDoubleProperty();
        DoubleProperty mousePosY = new SimpleDoubleProperty();
        DoubleProperty mouseOldX = new SimpleDoubleProperty();
        DoubleProperty mouseOldY = new SimpleDoubleProperty();
        final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
        final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

        cube.getTransforms().addAll(rotateX, rotateY);
        scene.setOnMousePressed(me -> {
            mouseOldY.set(me.getSceneY());
            mouseOldX.set(me.getSceneX());
        });
        scene.setOnMouseDragged(me -> {
            mousePosX.set(me.getSceneX());
            mousePosY.set(me.getSceneY());
            rotateX.setAngle(rotateX.getAngle() - (mousePosY.get() - mouseOldY.get()));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX.get() - mouseOldX.get()));
            mouseOldX.set(mousePosX.get());
            mouseOldY.set(mousePosY.get());
        });
    }

    public static void setZoomable(Node node) {

        setZoomable(node, false);
    }

    public static void setZoomable(Node node, boolean onlyClose) {
        Scale scale = new Scale(1, 1);
        Translate translate = new Translate(0, 0);
        node.getTransforms().addAll(scale, translate);
        double delta = 0.1;
        DoubleProperty iniX = new SimpleDoubleProperty(0);
        DoubleProperty iniY = new SimpleDoubleProperty(0);

        node.setOnScroll(scrollEvent -> {
            double scaleValue = scale.getX();
            double s = scaleValue;
            if (scrollEvent.getDeltaY() < 0) {
                scaleValue -= delta;
            } else {
                scaleValue += delta;
            }
            if (onlyClose && scaleValue < 1) {
                scaleValue = s;
            }

            if (scaleValue <= 0.1) {
                scaleValue = s;
            }
            scale.setX(scaleValue);
            scale.setY(scaleValue);
            scrollEvent.consume();
        });

        node.setOnMousePressed(evt -> {
            iniX.set(evt.getX());
            iniY.set(evt.getY());
        });

        node.setOnMouseDragged(evt -> {
            double deltaX = evt.getX() - iniX.get();
            double deltaY = evt.getY() - iniY.get();
            translate.setX(translate.getX() + deltaX);
            translate.setY(translate.getY() + deltaY);
        });
    }
}
