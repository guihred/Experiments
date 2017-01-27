package exp1;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Experiment3D extends Application {
	private final double cameraModifier = 50.0;
	private final double cameraQuantity = 10.0;

	private static String[][] mapa = {
		{ "_", "_", "_", "_", "_", "_" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "|", "_", "|", "_", "|" },
		{ "|", "|", "_", "|", "_", "|" }, 
		{ "|", "_", "_", "|", "_", "|" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "_", "_", "_", "_", "_" }, };

	private static final int SIZE = 50;

	public static void main(String[] args) {
		launch(args);
	}
	private int i;
	private int j;
	Color color = Color.RED;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();

		for (int i = mapa.length - 1; i >= 0; i--) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				if ("_".equals(string)) {
					Cube rectangle = new Cube(SIZE, Color.BLUE);
					rectangle.setTranslateX(i * SIZE);
					rectangle.setTranslateZ(j * SIZE);
					rectangle.ry.setAngle(90);
					root.getChildren().add(rectangle);
				} else {
					Cube rectangle = new Cube(SIZE, Color.BLUE);
					rectangle.setTranslateX(i * SIZE);
					rectangle.setTranslateZ(j * SIZE);
					// Rectangle rectangle = new Rectangle(i * SIZE, j * SIZE,
					// SIZE, SIZE / 2);
					root.getChildren().add(rectangle);
				}
			}
		}

		Scene scene = new Scene(root);
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-1000);
		scene.setCamera(camera);

		scene.setOnMouseClicked(event -> {
			String string = mapa[i][j];
			if ("_".equals(string)) {
				Cube rectangle = new Cube(SIZE, color);
				rectangle.setTranslateX(i * SIZE);
				rectangle.setTranslateZ(j * SIZE);
				rectangle.ry.setAngle(90);
				root.getChildren().add(rectangle);
			} else {
				Cube rectangle = new Cube(SIZE, color);
				rectangle.setTranslateX(i * SIZE);
				rectangle.setTranslateZ(j * SIZE);
				root.getChildren().add(rectangle);
			}
			j++;
			if (j >= mapa[i].length) {
				j = 0;
				i++;
			}
			if (i >= mapa.length) {
				i = 0;
				j = 0;
				color = color == Color.RED ? Color.BLACK : Color.RED;
			}

		});
		camera.setTranslateY(camera.getTranslateY() - cameraQuantity);
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		scene.setOnKeyPressed(event -> {
			double change = cameraQuantity;
			// Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = cameraModifier;
			}
			// What key did the user press?
			KeyCode keycode = event.getCode();
			// Step 2c: Add Zoom controls
			if (keycode == KeyCode.W) {
				double sin = Math.sin(camera.getRotate() * Math.PI / 180)
						* change;
				double cos = Math.cos(camera.getRotate() * Math.PI / 180)
						* change;

				camera.setTranslateX(camera.getTranslateX() + sin);
				camera.setTranslateZ(camera.getTranslateZ() + cos);
				// camera.setTranslateZ(camera.getTranslateZ() + change);
			}
			if (keycode == KeyCode.S) {
				double sin = Math.sin(camera.getRotate() * Math.PI / 180)
						* change;
				double cos = Math.cos(camera.getRotate() * Math.PI / 180)
						* change;

				camera.setTranslateX(camera.getTranslateX() - sin);
				camera.setTranslateZ(camera.getTranslateZ() - cos);
				// camera.setTranslateZ(camera.getTranslateZ() - change);
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setRotationAxis(Rotate.Y_AXIS);
				camera.setRotate(camera.getRotate() - change);
				// camera.setTranslateX(camera.getTranslateX() - change);
			}
			if (keycode == KeyCode.UP) {
				// root.setRotationAxis(Rotate.Y_AXIS);
				// root.setRotate(root.getRotate() - change);
				camera.setTranslateY(camera.getTranslateY() - change);
			}
			if (keycode == KeyCode.DOWN) {
				// root.setRotationAxis(Rotate.Y_AXIS);
				// root.setRotate(root.getRotate() - change);
				camera.setTranslateY(camera.getTranslateY() + change);
			}
			if (keycode == KeyCode.D) {
				camera.setRotationAxis(Rotate.Y_AXIS);
				camera.setRotate(camera.getRotate() + change);
				// camera.setTranslateX(camera.getTranslateX() + change);
			}
		});

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public class Cube extends Group {

		final Rotate rx = new Rotate(0, Rotate.X_AXIS);
		final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
		final Rotate rz = new Rotate(0, Rotate.Z_AXIS);


		public Cube(float size, Color color) {
			getTransforms().addAll(rz, ry, rx);
			final Box cube = new Box(size, size / 2, 5);
			cube.setMaterial(new PhongMaterial(color));
			cube.setBlendMode(BlendMode.DARKEN);
			cube.setDrawMode(DrawMode.FILL);
			cube.setRotationAxis(Rotate.Y_AXIS);
			cube.setTranslateX(-0.5 * size);
			cube.setTranslateY(0);
			cube.setTranslateZ(-0.5 * size);

			getChildren().addAll(cube);
			// rx.setAngle(90);
		}
	}
}
