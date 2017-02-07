package labyrinth;

import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

public class Cube extends Group {

	final Rotate rx = new Rotate(0, Rotate.X_AXIS);
	final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
	final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

	public Cube(double size, Color color) {
		PhongMaterial value = new PhongMaterial(color);
		create(size, value);
	}

	public Cube(double size, Color color, Image diffuse, Image specular) {
		PhongMaterial value = new PhongMaterial(color);
		value.setDiffuseMap(diffuse);
		value.setSpecularMap(specular);
		create(size, value);
	}

	private void create(double size, PhongMaterial value) {
		getTransforms().addAll(rz, ry, rx);
		Box cube = new Box(size, size / 2, 5);
		cube.setMaterial(value);
		cube.setBlendMode(BlendMode.DARKEN);
		cube.setDrawMode(DrawMode.FILL);
		cube.setRotationAxis(Rotate.Y_AXIS);
		cube.setTranslateX(-0.5 * size);
		cube.setTranslateY(0);
		cube.setTranslateZ(-0.5 * size);
		getChildren().addAll(cube);
	}
}