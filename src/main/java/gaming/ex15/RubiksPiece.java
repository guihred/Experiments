package gaming.ex15;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

public class RubiksPiece extends Group {

	Map<RubiksCubeFaces, Rotate> rotations = new HashMap<>();

	public RubiksPiece(double size) {
		// back face
		Box rec1 = new Box(size, size, 1);
		rec1.setMaterial(new PhongMaterial(Color.GREEN));
		rec1.setTranslateX(-0.5 * size);
		rec1.setTranslateY(-0.5 * size);
		rec1.setTranslateZ(0.5 * size);
		// bottom face
		Box rec2 = new Box(size, size, 1);
		rec2.setMaterial(new PhongMaterial(Color.ORANGERED));
		rec2.setTranslateX(-0.5 * size);
		rec2.setTranslateY(0);
		rec2.setRotationAxis(Rotate.X_AXIS);
		rec2.setRotate(90);
		// right face
		Box rec3 = new Box(size, size, 1);
		rec3.setMaterial(new PhongMaterial(Color.WHITE));
		rec3.setTranslateX(-1 * size);
		rec3.setTranslateY(-0.5 * size);
		rec3.setRotationAxis(Rotate.Y_AXIS);
		rec3.setRotate(90);
		// left face
		Box rec4 = new Box(size, size, 1);
		rec4.setMaterial(new PhongMaterial(Color.BLUE));
		rec4.setTranslateX(0);
		rec4.setTranslateY(-0.5 * size);
		rec4.setRotationAxis(Rotate.Y_AXIS);
		rec4.setRotate(90);
		// top face
		Box rec5 = new Box(size, size, 1);
		rec5.setMaterial(new PhongMaterial(Color.YELLOW));
		rec5.setTranslateX(-0.5 * size);
		rec5.setTranslateY(-1 * size);
		rec5.setRotationAxis(Rotate.X_AXIS);
		rec5.setRotate(90);
		// front face
		Box rec6 = new Box(size, size, 1);
		rec6.setMaterial(new PhongMaterial(Color.RED));
		rec6.setTranslateX(-0.5 * size);
		rec6.setTranslateY(-0.5 * size);
		rec6.setTranslateZ(-0.5 * size);

		getChildren().addAll(rec1, rec2, rec3, rec4, rec5, rec6);
	}

	public void rotate(RubiksCubeFaces face, RubiksPiece pivot, DoubleProperty angle) {
		if (rotations.containsKey(face)) {
			rotations.get(face).angleProperty().bind(angle.add(rotations.get(face).getAngle()));
		} else {
			Rotate rotate = new Rotate(0, face.getAxis());
			rotate.pivotXProperty().bind(
					pivot.translateXProperty().subtract(translateXProperty())
							.subtract(RubiksCubeLauncher.RUBIKS_CUBE_SIZE / 2));
			rotate.pivotYProperty().bind(
					pivot.translateYProperty().subtract(translateYProperty())
							.subtract(RubiksCubeLauncher.RUBIKS_CUBE_SIZE / 2));
			rotate.pivotZProperty().bind(pivot.translateZProperty().subtract(translateZProperty()));
			rotate.angleProperty().bind(angle);
			getTransforms().add(rotate);
			rotations.put(face, rotate);
		}
	}
}
