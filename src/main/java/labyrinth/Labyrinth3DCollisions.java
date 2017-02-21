package labyrinth;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Labyrinth3DCollisions extends Application implements CommomLabyrinth {
	private static String[][] mapa = { { "_", "_", "_", "_", "_", "_" },
			{ "|", "_", "_", "_", "_", "|" }, { "|", "|", "_", "|", "_", "|" },
			{ "|", "|", "_", "|", "_", "|" }, { "|", "_", "_", "|", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, { "|", "_", "_", "_", "_", "_" }, };
	private static final int SIZE = 50;

	private PerspectiveCamera camera = new PerspectiveCamera(true);

	private Color color = Color.RED;

	private List<LabyrinthWall> cubes = new ArrayList<>();
	private int i;
	private int j;
	private Group root = new Group();


	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public List<LabyrinthWall> getLabyrinthWalls() {
		return cubes;
	}


	private void handleMouseClick() {
		String string = mapa[i][j];
		LabyrinthWall rectangle = new LabyrinthWall(SIZE, color);
		rectangle.setTranslateX(i * SIZE);
		rectangle.setTranslateZ(j * SIZE);
		if ("_".equals(string)) {
			rectangle.getRy().setAngle(90);
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
	}

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
				cubes.add(rectangle);
				root.getChildren().add(rectangle);
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initializeLabyrinth(root);
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		camera.setNearClip(0.1);
		subScene.setCamera(camera);

		PointLight light = new PointLight(Color.rgb(125, 125, 125));
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);
		Scene sc = new Scene(new Group(subScene));
		sc.setOnMouseClicked(event -> handleMouseClick());
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
