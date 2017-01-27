package javafxpert.earthcubex;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 
 */
public class EarthCubeMain extends Application {

	/**
	 
	 */
	CubeModel cubeModel;

	/**
	 
	 */
	Scene scene;

	/**
	 
	 */
	CubeNode cube;

	/**
	 
	 
	 */
	public static void main(String[] args) {
		Application.launch(EarthCubeMain.class, args);
	}

	@Override
	public void start(Stage primaryStage) {
		cubeModel = new CubeModel();
		CubeModel.instance = cubeModel;
		cube = new CubeNode(cubeModel);
		cube.setFocusTraversable(true);

		Group root = new Group(cube);
		root.setLayoutX(150);
		root.setLayoutY(150);
		scene = new Scene(root, 800, 800);
		scene.setFill(Color.TRANSPARENT);
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
		perspectiveCamera.setFieldOfView(30);
		scene.setCamera(perspectiveCamera);
		primaryStage.setScene(scene);
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();

		cube.showMapTimeline.playFromStart();
	}
}
