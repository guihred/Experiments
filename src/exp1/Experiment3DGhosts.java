package exp1;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.util.Random;
import java.util.stream.Stream;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Experiment3DGhosts extends Application {
	private final double cameraModifier = 50.0;
	private final double cameraQuantity = 5.0;
	private static final Color lightColor = Color.rgb(125, 125, 125);

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

	private Cube[][] cubes = new Cube[mapa.length][mapa[0].length];
	private static final int SIZE = 60;

	public static void main(String[] args) {
		launch(args);
	}

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

		MeshView[] fantasmas = { 
				gerarAnimal(MESH_GHOST, Color.AQUAMARINE),
				gerarAnimal(MESH_GHOST, Color.BEIGE),
				gerarAnimal(MESH_GHOST, Color.BLUEVIOLET),
				gerarAnimal(MESH_GHOST, Color.CORNFLOWERBLUE),
				gerarAnimal(MESH_GHOST, Color.DEEPPINK),
				gerarAnimal(MESH_GHOST, Color.DEEPSKYBLUE),
				gerarAnimal(MESH_GHOST, Color.LIGHTGREEN),

		};

		new MovimentacaoAleatoria(fantasmas).start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
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
				if (checkColision(camera.getBoundsInParent())) {
					camera.setTranslateX(camera.getTranslateX() - sin);
				}
				camera.setTranslateZ(camera.getTranslateZ() + cos);
				if (checkColision(camera.getBoundsInParent())) {
					camera.setTranslateZ(camera.getTranslateZ() - cos);
				}
			}
			if (keycode == KeyCode.S) {
				double sin = Math.sin(camera.getRotate() * Math.PI / 180)
						* change;
				double cos = Math.cos(camera.getRotate() * Math.PI / 180)
						* change;

				camera.setTranslateX(camera.getTranslateX() - sin);
				if (checkColision(camera.getBoundsInParent())) {
					camera.setTranslateX(camera.getTranslateX() + sin);
					// camera.setTranslateZ(camera.getTranslateZ() - change);
				}
				camera.setTranslateZ(camera.getTranslateZ() - cos);
				if (checkColision(camera.getBoundsInParent())) {
					camera.setTranslateZ(camera.getTranslateZ() + cos);
				}
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setRotationAxis(Rotate.Y_AXIS);
				camera.setRotate(camera.getRotate() - change);
			}
			if (keycode == KeyCode.UP) {
				camera.setTranslateY(camera.getTranslateY() - change);
			}
			if (keycode == KeyCode.DOWN) {
				camera.setTranslateY(camera.getTranslateY() + change);
			}
			if (keycode == KeyCode.D) {
				camera.setRotationAxis(Rotate.Y_AXIS);
				camera.setRotate(camera.getRotate() + change);
			}
		});

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
//		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();
	}

	boolean checkColision(Bounds boundsInParent) {
		Stream<Bounds> walls = Stream.of(cubes).flatMap(l -> Stream.of(l))
				.map(Cube::getBoundsInParent);
		return walls.anyMatch(b -> b.intersects(boundsInParent));
	}


	static final String MESH_MINOTAUR = Experiment3DWallTexture.class.getResource("Minotaur.stl").getFile();
	private static final String MESH_GHOST = Experiment3DWallTexture.class.getResource("ghost2.STL").getFile();

	private MeshView gerarAnimal(String arquivo, Color jewelColor) {
		File file = new File(arquivo);
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(file);
		Mesh mesh = importer.getImport();
		MeshView animal = new MeshView(mesh);
		PhongMaterial sample = new PhongMaterial(jewelColor);
		sample.setSpecularColor(lightColor);
		sample.setSpecularPower(16);
		animal.setMaterial(sample);
		animal.setTranslateY(14);

		int posicaoInicialZ = new Random().nextInt(mapa[0].length * SIZE);
		animal.setTranslateZ(posicaoInicialZ);
		int posicaoInicialX = new Random().nextInt(mapa.length * SIZE);
		animal.setTranslateX(posicaoInicialX);
		while (checkColision(animal.getBoundsInParent())) {
			animal.setTranslateZ(animal.getTranslateZ() + 1);
			animal.setTranslateX(animal.getTranslateX() + 1);
		}


		animal.setScaleX(0.25);
		animal.setScaleY(1);
		animal.setScaleZ(0.25);

		return animal;
	}

	private final class MovimentacaoAleatoria extends AnimationTimer {
		int direction[];// EAST, WEST, NORTH, SOUTH
		private MeshView[] animais;

		public MovimentacaoAleatoria(MeshView... animais) {
			this.animais = animais;
			direction = new int[animais.length];
		}

		@Override
		public void handle(long now) {
			for (int i = 0; i < animais.length; i++) {
				MeshView animal = animais[i];

				final int STEP = 1;
				if (direction[i] == 3) {// NORTH
					animal.setTranslateZ(animal.getTranslateZ() + STEP);
				}
				if (direction[i] == 2) {// WEST
					animal.setTranslateX(animal.getTranslateX() - STEP);
				}
				if (direction[i] == 1) {// SOUTH
					animal.setTranslateZ(animal.getTranslateZ() - STEP);
				}
				if (direction[i] == 0) {// EAST
					animal.setTranslateX(animal.getTranslateX() + STEP);
				}
				if (checkColision(animal.getBoundsInParent())
						|| animal.getTranslateZ() < 0
						|| animal.getTranslateZ() > mapa[0].length * SIZE

						|| animal.getTranslateX() < 0
						|| animal.getTranslateX() > mapa.length * SIZE

				) {
					if (direction[i] == 3) {// NORTH
						animal.setTranslateZ(animal.getTranslateZ() - STEP);
					}
					if (direction[i] == 2) {// WEST
						animal.setTranslateX(animal.getTranslateX() + STEP);
					}
					if (direction[i] == 1) {// SOUTH
						animal.setTranslateZ(animal.getTranslateZ() + STEP);
					}
					if (direction[i] == 0) {// EAST
						animal.setTranslateX(animal.getTranslateX() - STEP);
					}
					animal.setRotationAxis(Rotate.Y_AXIS);
					// animal.setRotate(direction[i] * 90);
					direction[i] = new Random().nextInt(4);

				}
				if (now % 1000 == 0) {
					direction[i] = new Random().nextInt(4);
				}
			}
		}
	}

}
