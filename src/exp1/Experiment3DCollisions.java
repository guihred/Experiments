package exp1;

import java.util.stream.Stream;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Experiment3DCollisions extends Application {
	private final double cameraModifier = 50.0;
	private final double cameraQuantity = 5.0;

	private static String[][] mapa = { { "_", "_", "_", "_", "_", "_" },
			{ "|", "_", "_", "_", "_", "|" }, { "|", "|", "_", "|", "_", "|" },
			{ "|", "|", "_", "|", "_", "|" }, { "|", "_", "_", "|", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, { "|", "_", "_", "_", "_", "_" }, };

	private static Cube[][] cubes = new Cube[mapa.length][mapa[0].length];
	private static final int SIZE = 50;

	public static void main(String[] args) {
		launch(args);
	}

	private int i;
	private int j;
	Color color = Color.RED;
	private PerspectiveCamera camera;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();

		for (int i = mapa.length - 1; i >= 0; i--) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				Cube rectangle = new Cube(SIZE, Color.BLUE);
				rectangle.setTranslateX(i * SIZE);
				rectangle.setTranslateZ(j * SIZE);
				if ("_".equals(string)) {
					rectangle.ry.setAngle(90);
				}
				cubes[i][j] = rectangle;

				root.getChildren().add(rectangle);
			}
		}
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		subScene.setCamera(camera);

		PointLight sun = new PointLight(Color.rgb(125, 125, 125));
		sun.translateXProperty().bind(camera.translateXProperty());
		sun.translateYProperty().bind(camera.translateYProperty());
		sun.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(sun);
		Scene sc = new Scene(new Group(subScene));
		sc.setOnMouseClicked(event -> {
			String string = mapa[i][j];
			Cube rectangle = new Cube(SIZE, color);
			rectangle.setTranslateX(i * SIZE);
			rectangle.setTranslateZ(j * SIZE);
			if ("_".equals(string)) {
				rectangle.ry.setAngle(90);
			}
			root.getChildren().add(rectangle);
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
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setOnKeyPressed(event -> {
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
				if (checkColision()) {
					camera.setTranslateX(camera.getTranslateX() - sin);
					// camera.setTranslateZ(camera.getTranslateZ() + change);
				}
				camera.setTranslateZ(camera.getTranslateZ() + cos);
				if (checkColision()) {
					camera.setTranslateZ(camera.getTranslateZ() - cos);
				}
			}
			if (keycode == KeyCode.S) {
				double sin = Math.sin(camera.getRotate() * Math.PI / 180)
						* change;
				double cos = Math.cos(camera.getRotate() * Math.PI / 180)
						* change;

				camera.setTranslateX(camera.getTranslateX() - sin);
				if (checkColision()) {
					camera.setTranslateX(camera.getTranslateX() + sin);
					// camera.setTranslateZ(camera.getTranslateZ() - change);
				}
				camera.setTranslateZ(camera.getTranslateZ() - cos);
				if (checkColision()) {
					camera.setTranslateZ(camera.getTranslateZ() + cos);
				}
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
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	boolean checkColision() {

		Bounds boundsInParent = camera.getBoundsInParent();
		return Stream.of(cubes).flatMap(l -> Stream.of(l))
				.map(Cube::getBoundsInParent)
				.anyMatch(b -> b.intersects(boundsInParent));
	}

	public class Cube extends Group {
		// private TriangleMesh mesh = new TriangleMesh();

		final Rotate rx = new Rotate(0, Rotate.X_AXIS);
		final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
		final Rotate rz = new Rotate(0, Rotate.Z_AXIS);
		private Box cube;

		public Cube(float size, Color color) {
			getTransforms().addAll(rz, ry, rx);
			cube = new Box(size, size / 2, 5);
			PhongMaterial value = new PhongMaterial(color);

			cube.setMaterial(value);
			cube.setBlendMode(BlendMode.DARKEN);
			cube.setDrawMode(DrawMode.FILL);
			cube.setRotationAxis(Rotate.Y_AXIS);
			cube.setTranslateX(-0.5 * size);
			cube.setTranslateY(0);
			cube.setTranslateZ(-0.5 * size);

			getChildren().addAll(cube);
			// rx.setAngle(90);
		}

		public Bounds getBoundaries() {
			return cube.getBoundsInParent();
		}

	}
}
