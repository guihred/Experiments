package utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public final class RotateUtils {
    private static final double CAMERA_MODIFIER = 50.0;

    private static final double CAMERA_QUANTITY = 10.0;

    private RotateUtils() {

    }
    public static void makeZoomable(Node control) {

        final double MAX_SCALE = 20.0;
        final double MIN_SCALE = 0.1;
        final double DELTA = 1.2;

        control.addEventFilter(ScrollEvent.ANY, event -> {
            double scale = control.getScaleX();
            if (event.getDeltaY() < 0) {
                scale /= DELTA;
            } else {
                scale *= DELTA;
            }
            scale = ResourceFXUtils.clamp(scale, MIN_SCALE, MAX_SCALE);
            control.setScaleX(scale);
            control.setScaleY(scale);
            event.consume();
        });

    }

    public static void setMovable(Node node) {
        setMovable(node, node.getScene());
    }

    public static void setMovable(Node node, Scene scene) {
        scene.setOnKeyPressed(event -> {
            double change = event.isShiftDown() ? CAMERA_MODIFIER : CAMERA_QUANTITY;
            // What key did the user press?
            KeyCode keycode = event.getCode();
            // Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                node.setTranslateZ(node.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                node.setTranslateZ(node.getTranslateZ() - change);
            }
            // Step 2d: Add Strafe controls
            if (keycode == KeyCode.A) {
                node.setTranslateX(node.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                node.setTranslateX(node.getTranslateX() + change);
            }
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
