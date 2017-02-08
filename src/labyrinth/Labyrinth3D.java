package labyrinth;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Labyrinth3D extends Application {
	private static String[][] mapa = {
		{ "_", "_", "_", "_", "_", "_" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "|", "_", "|", "_", "|" },
		{ "|", "|", "_", "|", "_", "|" }, 
		{ "|", "_", "_", "|", "_", "|" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "_", "_", "_", "_", "_" }, };
	private static final int SIZE = 50;

	private final double cameraModifier = 50.0;

	private final double cameraQuantity = 10.0;

	private Color color = Color.RED;
	private int i;
	private int j;
	private void initializeLabyrinth(Group root) {
		for (int k = mapa.length - 1; k >= 0; k--) {
			for (int l = mapa[k].length - 1; l >= 0; l--) {
				String string = mapa[k][l];
				if ("_".equals(string)) {
					LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
					rectangle.setTranslateX(k * SIZE);
					rectangle.setTranslateZ(l * SIZE);
					rectangle.getRy().setAngle(90);
					root.getChildren().add(rectangle);
				} else {
					LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
					rectangle.setTranslateX(k * SIZE);
					rectangle.setTranslateZ(l * SIZE);
					// Rectangle rectangle = new Rectangle(i * SIZE, j * SIZE,
					// SIZE, SIZE / 2);
					root.getChildren().add(rectangle);
				}
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();

		initializeLabyrinth(root);

		Scene scene = new Scene(root);
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-1000);
		scene.setCamera(camera);

		scene.setOnMouseClicked(event -> {
			String string = mapa[i][j];
			if ("_".equals(string)) {
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, color);
				rectangle.setTranslateX(i * SIZE);
				rectangle.setTranslateZ(j * SIZE);
				rectangle.getRy().setAngle(90);
				root.getChildren().add(rectangle);
			} else {
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, color);
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

	public static void main(String[] args) {
		launch(args);
	}

}
