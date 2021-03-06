package labyrinth;

import static labyrinth.GhostGenerator.getMapa;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Labyrinth3DAntiAliasing extends Application implements CommomLabyrinth {
	private static final int SIZE = 50;
	private PerspectiveCamera camera = new PerspectiveCamera(true);
	private List<LabyrinthWall> labyrinthWalls = new ArrayList<>();
	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public List<LabyrinthWall> getLabyrinthWalls() {
		return labyrinthWalls;
	}

	@Override
	public void start(Stage primaryStage) {

		Group root = new Group();

		initializeLabyrinth(root);
        SubScene subScene = new SubScene(root, 500, 500, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
        camera.setNearClip(1. / 10);
		camera.setTranslateZ(-1000);
		camera.setFarClip(1000.0);
		subScene.setCamera(camera);

        PointLight light = new PointLight(Color.GRAY);
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);
		Scene sc = new Scene(new Group(subScene));

		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);

        primaryStage.setTitle("Labyrinth 3D With Anti-Aliasing");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	private void initializeLabyrinth(Group root) {
        for (int k = getMapa().length - 1; k >= 0; k--) {
			for (int l = getMapa()[k].length - 1; l >= 0; l--) {
				String string = getMapa()[k][l];
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
				rectangle.setTranslateX(k * SIZE);
				rectangle.setTranslateZ(l * SIZE);
				if ("_".equals(string)) {
					rectangle.getRy().setAngle(90);
				}
				labyrinthWalls.add(rectangle);
				root.getChildren().add(rectangle);
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}