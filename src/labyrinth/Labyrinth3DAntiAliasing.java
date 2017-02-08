package labyrinth;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Labyrinth3DAntiAliasing extends Application {
	private static String[][] mapa = {
		{ "_", "_", "_", "_", "_", "_" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "|", "_", "|", "_", "|" },
		{ "|", "|", "_", "|", "_", "|" }, 
		{ "|", "_", "_", "|", "_", "|" },
		{ "|", "_", "_", "_", "_", "|" }, 
		{ "|", "_", "_", "_", "_", "_" }, };
	private static final int SIZE = 50;

	PerspectiveCamera camera = new PerspectiveCamera(true);

	private final double cameraModifier = 50.0;

	private final double cameraQuantity = 10.0;
	private EventHandler<? super KeyEvent> keyboardHandler = event -> {
		double change = cameraQuantity;
		// Add shift modifier to simulate "Running Speed"
		if (event.isShiftDown()) {
			change = cameraModifier;
		}
		// What key did the user press?
		KeyCode keycode = event.getCode();
		// Step 2c: Add Zoom controls
		if (keycode == KeyCode.W) {
			double sin = Math.sin(camera.getRotate() * Math.PI / 180) * change;
			double cos = Math.cos(camera.getRotate() * Math.PI / 180) * change;

			camera.setTranslateX(camera.getTranslateX() + sin);
			camera.setTranslateZ(camera.getTranslateZ() + cos);
			// camera.setTranslateZ(camera.getTranslateZ() + change);
		}
		if (keycode == KeyCode.S) {
			double sin = Math.sin(camera.getRotate() * Math.PI / 180) * change;
			double cos = Math.cos(camera.getRotate() * Math.PI / 180) * change;

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
	};
	private void initializeLabyrinth(Group root) {
		for (int k = mapa.length - 1; k >= 0; k--) {
			for (int l = mapa[k].length - 1; l >= 0; l--) {
				String string = mapa[k][l];
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
				rectangle.setTranslateX(k * SIZE);
				rectangle.setTranslateZ(l * SIZE);
				if ("_".equals(string)) {
					rectangle.getRy().setAngle(90);
				}
				root.getChildren().add(rectangle);
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();

		initializeLabyrinth(root);
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-1000);
		subScene.setCamera(camera);

		PointLight light = new PointLight(Color.rgb(125, 125, 125));
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);
		Scene sc = new Scene(new Group(subScene));

		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setOnKeyPressed(keyboardHandler);


		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}