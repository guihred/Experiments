package fxpro.earth;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EarthCubeMain extends Application {
	public static void main(String[] args) {
		Application.launch(EarthCubeMain.class, args);
	}

	@Override
	public void start(Stage primaryStage) {
		CubeNode cube = new CubeNode();
		cube.setFocusTraversable(true);
		Group root = new Group(cube);
		root.setLayoutX(150);
		root.setLayoutY(150);
		Scene scene = new Scene(root, 800, 800);
		scene.setFill(Color.TRANSPARENT);
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
		perspectiveCamera.setFieldOfView(30);
		scene.setCamera(perspectiveCamera);
		primaryStage.setScene(scene);
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();

		cube.playShowMap();
	}
}
