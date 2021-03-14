package utils.fx;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class Xform extends Group {
    private Translate ip = new Translate();
    private Translate p = new Translate();
    private Rotate rx = new Rotate(0, Rotate.X_AXIS);
    private Rotate ry = new Rotate(0, Rotate.Y_AXIS);
    private Rotate rz = new Rotate(0, Rotate.Z_AXIS);
    private Scale s = new Scale();
    private Translate t = new Translate();

    public Xform() {
        getTransforms().addAll(t, rz, ry, rx, s);
    }

    public Xform(Node... nodes) {
        getTransforms().addAll(t, rz, ry, rx, s);
        getChildren().addAll(nodes);
    }

    public double getRotateX() {
        return rx.getAngle();
    }

    public double getRotateY() {
        return ry.getAngle();
    }

    public double getRotateZ() {
        return rz.getAngle();
    }

    public double getRx() {
        return rx.getAngle();
    }

    public double getRy() {
        return ry.getAngle();
    }

    public double getRz() {
        return rz.getAngle();
    }

    public double getTx() {
        return t.getX();
    }

    public double getTy() {
        return t.getY();
    }

    public double getTz() {
        return t.getZ();
    }

    public DoubleProperty rotateXProperty() {
        return rx.angleProperty();
    }

    public DoubleProperty rotateYProperty() {
        return ry.angleProperty();
    }

    public DoubleProperty rotateZProperty() {
        return rz.angleProperty();
    }

    public void setRotateX(double x) {
        rx.setAngle(x);
    }

    public void setRotateY(double y) {
        ry.setAngle(y);
    }

    public void setRotateZ(double z) {
        rz.setAngle(z);
    }

    public void setRx(double x) {
        rx.setAngle(x);
    }

    public void setRy(double y) {
        ry.setAngle(y);
    }

    public void setRz(double z) {
        rz.setAngle(z);
    }

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setSx(double x) {
        s.setX(x);
    }

    public void setSy(double y) {
        s.setY(y);
    }

    public void setSz(double z) {
        s.setZ(z);
    }

    public void setTx(double x) {
        t.setX(x);
    }

    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }

    @Override
    public String toString() {
        return String.format(
                "Xform[t = (%.1f, %.1f, %.1f)  r = (%.1f, %.1f, %.1f)  s = (%.1f, %.1f, %.1f) "
                        + " p = (%.1f, %.1f, %.1f)  ip = (%.1f, %.1f, %.1f)]",
                t.getX(), t.getY(), t.getZ(), rx.getAngle(), ry.getAngle(), rz.getAngle(), s.getX(), s.getY(), s.getZ(),
                p.getX(), p.getY(), p.getZ(), ip.getX(), ip.getY(), ip.getZ());
    }
}
