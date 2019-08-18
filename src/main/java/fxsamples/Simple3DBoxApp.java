package fxsamples;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Simple3DBoxApp extends Application {


    private static final int SIZE = 300;

    @Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setResizable(false);
		Scene scene = new Scene(createContent());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static Parent createContent() {

		// Box
        Box testBox = new Box(5, 5, 5);

        PhongMaterial material = new PhongMaterial(Color.RED);
		testBox.setMaterial(material);

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll(new Rotate(-20, Rotate.Y_AXIS),
                new Rotate(-20, Rotate.X_AXIS), new Translate(0, 0, -20));

		// Build the Scene Graph
		Group root = new Group();
		root.getChildren().add(camera);
		root.getChildren().add(testBox);

		// Use a SubScene
        SubScene subScene = new SubScene(root, SIZE, SIZE, true,
				SceneAntialiasing.BALANCED);
		subScene.setFill(Color.TRANSPARENT);
		subScene.setCamera(camera);

		return new Group(subScene);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
