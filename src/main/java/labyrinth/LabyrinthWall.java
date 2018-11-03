package labyrinth;

import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

public class LabyrinthWall extends Group {
	public static final int SIZE = 60;
	private final Rotate rx = new Rotate(0, Rotate.X_AXIS);
	private final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
	private final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

	public LabyrinthWall(double size, Color color) {
		PhongMaterial value = new PhongMaterial(color);
		create(size, value);
	}

	public LabyrinthWall(double size, Color color, Image diffuse, Image specular) {
		PhongMaterial value = new PhongMaterial(color);
		value.setDiffuseMap(diffuse);
		value.setSpecularMap(specular);
		create(size, value);
	}

	public Rotate getRx() {
		return rx;
	}

	public Rotate getRy() {
		return ry;
	}

	public Rotate getRz() {
		return rz;
	}

	private void create(double size, PhongMaterial value) {
		getTransforms().addAll(getRz(), getRy(), getRx());
		Box cube = new Box(size, size / 2, 5);
		cube.setMaterial(value);
		cube.setBlendMode(BlendMode.DARKEN);
		cube.setDrawMode(DrawMode.FILL);
		cube.setRotationAxis(Rotate.Y_AXIS);
        cube.setTranslateX(-size / 2);
		cube.setTranslateY(0);
        cube.setTranslateZ(-size / 2);
		getChildren().addAll(cube);
	}
}