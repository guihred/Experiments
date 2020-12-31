package fxpro.earth;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.ExtractUtils;

public class EarthCubeMain extends Application {
	@Override
	public void start(Stage primaryStage) {
        ExtractUtils.insertProxyConfig();
		CubeNode cube = new CubeNode();
        Group root = new Group(cube);
		cube.setFocusTraversable(true);
        final int layout = 150;
        root.setLayoutX(layout);
        root.setLayoutY(layout);
        final int size = 800;
        Scene scene = new Scene(root, size, size, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.TRANSPARENT);
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
		perspectiveCamera.setFieldOfView(30);
		scene.setCamera(perspectiveCamera);
		primaryStage.setScene(scene);
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();
		cube.playShowMap();
	}

	public static void main(String[] args) {
		Application.launch(EarthCubeMain.class, args);
	}
}
