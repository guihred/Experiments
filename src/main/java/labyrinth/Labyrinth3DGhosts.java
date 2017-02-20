package labyrinth;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;

public class Labyrinth3DGhosts extends Application implements CommomLabyrinth {
	private static final Color lightColor = Color.rgb(125, 125, 125);
	private Random random = new Random();
	private static String[][] mapa = { { "_", "_", "_", "_", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, { "|", "|", "_", "|", "_", "|" },
			{ "_", "|", "_", "|", "_", "|" }, { "|", "|", "_", "|", "_", "|" },
			{ "|", "_", "_", "|", "_", "|" }, { "|", "_", "_", "_", "|", "_" },
			{ "_", "|", "_", "_", "_", "|" }, { "_", "_", "|", "|", "|", "_" },
			{ "_", "|", "_", "|", "_", "|" }, { "|", "|", "_", "_", "|", "_" },
			{ "_", "_", "_", "_", "_", "|" }, { "|", "_", "_", "_", "_", "_" },
			{ "|", "|", "_", "|", "_", "|" }, { "|", "_", "|", "_", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, { "_", "_", "_", "|", "_", "|" },
			{ "_", "_", "_", "_", "_", "_" },

	};

	private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");
	public static final String MESH_MINOTAUR = ResourceFXUtils.toFullPath("Minotaur.stl");
	private static final int SIZE = 60;

	private PerspectiveCamera camera;

	private final List<LabyrinthWall> cubes = new ArrayList<>();


	private MeshView gerarAnimal(String arquivo, Color jewelColor) {
		File file = new File(arquivo);
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(file);
		Mesh mesh = importer.getImport();
		MeshView animal = new MeshView(mesh);
		PhongMaterial sample = new PhongMaterial(jewelColor);
		sample.setSpecularColor(lightColor);
		animal.setMaterial(sample);
		sample.setSpecularPower(16);
		animal.setTranslateY(14);

		animal.setTranslateZ(random.nextInt(mapa[0].length * SIZE));
		animal.setTranslateX(random.nextInt(mapa.length * SIZE));
		while (checkColision(animal.getBoundsInParent())) {
			animal.setTranslateZ(animal.getTranslateZ() + 1);
			animal.setTranslateX(animal.getTranslateX() + 1);
		}


		animal.setScaleZ(0.25);
		animal.setScaleX(0.25);
		animal.setScaleY(1);

		return animal;
	}



	private void initializeLabyrinth(Group root) {
		for (int i = mapa.length - 1; i >= 0; i--) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
				rectangle.setTranslateX(i * SIZE);
				rectangle.setTranslateZ(j * SIZE);
				if ("_".equals(string)) {
					rectangle.getRy().setAngle(90);
				}
				cubes.add(rectangle);

				root.getChildren().add(rectangle);
			}
		}
	}

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public List<LabyrinthWall> getLabyrinthWalls() {
		return cubes;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Group root = new Group();

		initializeLabyrinth(root);
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		subScene.setCamera(camera);
		PointLight light = new PointLight(Color.rgb(125, 125, 125));
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);

		MeshView[] fantasmas = { 
				gerarAnimal(MESH_GHOST, Color.AQUAMARINE),
				gerarAnimal(MESH_GHOST, Color.BEIGE),
				gerarAnimal(MESH_GHOST, Color.BLUEVIOLET),
				gerarAnimal(MESH_GHOST, Color.CORNFLOWERBLUE),
				gerarAnimal(MESH_GHOST, Color.DEEPPINK),
				gerarAnimal(MESH_GHOST, Color.DEEPSKYBLUE),
				gerarAnimal(MESH_GHOST, Color.LIGHTGREEN),

		};

		new MovimentacaoAleatoria(this, fantasmas).start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
		sc.setOnKeyPressed(new MovimentacaoTeclado(this));

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
