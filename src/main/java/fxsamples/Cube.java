package fxsamples;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

public class Cube extends Group {
    private static final int STRAIGHT_ANGLE = 90;

	protected final Rotate rx = new Rotate(0, Rotate.X_AXIS);
	protected final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
	protected final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

    public Cube(double size, Color color, double shade) {
        getTransforms().addAll(rz, ry, rx);
        // back face
        Rectangle rec1 = new Rectangle(size, size, color.deriveColor(0, 1, 1 - shade * 5 / 10, 1));
        rec1.setTranslateX(-size / 2);
        rec1.setTranslateY(-size / 2);
        rec1.setTranslateZ(size / 2);
        // bottom face
        Rectangle rec2 = new Rectangle(size, size, color.deriveColor(0, 1, 1 - shade * 4 / 10, 1));
        rec2.setTranslateX(-size / 2);
        rec2.setTranslateY(0);
        rec2.setRotationAxis(Rotate.X_AXIS);
        rec2.setRotate(STRAIGHT_ANGLE);
        // right face
        Rectangle rec3 = new Rectangle(size, size, color.deriveColor(0, 1, 1 - shade * 3 / 10, 1));
        rec3.setTranslateX(-1 * size);
        rec3.setTranslateY(-size / 2);
        rec3.setRotationAxis(Rotate.Y_AXIS);
        rec3.setRotate(STRAIGHT_ANGLE);
        // left face
        Rectangle rec4 = new Rectangle(size, size, color.deriveColor(0, 1, 1 - shade * 2 / 10, 1));
        rec4.setTranslateX(0);
        rec4.setTranslateY(-size / 2);
        rec4.setRotationAxis(Rotate.Y_AXIS);
        rec4.setRotate(STRAIGHT_ANGLE);
        // top face
        Rectangle rec5 = new Rectangle(size, size, color.deriveColor(0, 1, 1 - shade * 1 / 10, 1));
        rec5.setTranslateX(-size / 2);
        rec5.setTranslateY(-1 * size);
        rec5.setRotationAxis(Rotate.X_AXIS);
        rec5.setRotate(STRAIGHT_ANGLE);
        // front face
        Rectangle rec6 = new Rectangle(size, size, color);
        rec6.setTranslateX(-size / 2);
        rec6.setTranslateY(-size / 2);
        rec6.setTranslateZ(-size / 2);

        getChildren().addAll(rec1, rec2, rec3, rec4, rec5, rec6);
    }
}
