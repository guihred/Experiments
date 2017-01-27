package exp1;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.util.Random;
import java.util.stream.Stream;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Experiment3DWallTexture extends Application {
	private static final Image WALL_IMAGE = new Image(Experiment3DWallTexture.class.getResource("wall.jpg").toString());
	private static final Image WALL_IMAGE2 = new Image(
			Experiment3DWallTexture.class.getResource("wall2.jpg").toString());
	public static final Image OOZE_IMAGE = new Image(Experiment3DWallTexture.class.getResource("ooze.jpg").toString());
	public class Cube extends Group {

		final Rotate rx = new Rotate(0, Rotate.X_AXIS);
		final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
		final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

		public Cube(float size, Color color) {
			getTransforms().addAll(rz, ry, rx);
			PhongMaterial value = new PhongMaterial(color);
			value.setDiffuseMap(WALL_IMAGE);
			value.setSpecularMap(WALL_IMAGE2);
			Box cube = new Box(size, size / 2, 5);
			cube.setMaterial(value);
			cube.setBlendMode(BlendMode.DARKEN);
			cube.setDrawMode(DrawMode.FILL);
			cube.setRotationAxis(Rotate.Y_AXIS);
			cube.setTranslateX(-0.5 * size);
			cube.setTranslateY(0);
			cube.setTranslateZ(-0.5 * size);
			getChildren().addAll(cube);
		}
	}

	private class MovimentacaoAleatoria extends AnimationTimer {
		private MeshView[] animais;
		int direction[];// EAST, WEST, NORTH, SOUTH

		public MovimentacaoAleatoria(MeshView... animais) {
			this.animais = animais;
			direction = new int[animais.length];
		}

		@Override
		public void handle(long now) {
			for (int i = 0; i < animais.length; i++) {
				MeshView enemy = animais[i];

				final int STEP = 1;
				if (direction[i] == 3) {// NORTH
					enemy.setTranslateZ(enemy.getTranslateZ() + STEP);
				}
				if (direction[i] == 2) {// WEST
					enemy.setTranslateX(enemy.getTranslateX() - STEP);
				}
				if (direction[i] == 1) {// SOUTH
					enemy.setTranslateZ(enemy.getTranslateZ() - STEP);
				}
				if (direction[i] == 0) {// EAST
					enemy.setTranslateX(enemy.getTranslateX() + STEP);
				}
				if (checkColision(enemy.getBoundsInParent())
						|| enemy.getTranslateZ() < 0
						|| enemy.getTranslateZ() > mapa[0].length * SIZE

						|| enemy.getTranslateX() < 0
						|| enemy.getTranslateX() > mapa.length * SIZE

				) {
					if (direction[i] == 3) {// NORTH
						enemy.setTranslateZ(enemy.getTranslateZ() - STEP);
					}
					if (direction[i] == 2) {// WEST
						enemy.setTranslateX(enemy.getTranslateX() + STEP);
					}
					if (direction[i] == 1) {// SOUTH
						enemy.setTranslateZ(enemy.getTranslateZ() + STEP);
					}
					if (direction[i] == 0) {// EAST
						enemy.setTranslateX(enemy.getTranslateX() - STEP);
					}
					enemy.setRotationAxis(Rotate.Y_AXIS);
					enemy.setRotate(direction[i] * 90);
					direction[i] = random.nextInt(4);

				}
				if (now % 1000 == 0) {
					direction[i] = random.nextInt(4);
				}
				if (camera.getBoundsInParent().intersects(
						enemy.getBoundsInParent())) {
					movimentacao.stop();
					Stage dialogStage = new Stage();
					dialogStage.initModality(Modality.WINDOW_MODAL);
					Button button = new Button("Ok.");
					button.setOnAction(e -> {
						camera.setTranslateX(0);
						camera.setTranslateY(0);
						camera.setTranslateZ(0);
						movimentacao.start();
						dialogStage.close();
					});
					VBox vbox = new VBox();
					vbox.getChildren().addAll(new Text("Voc� Morreu"), button);
					vbox.setAlignment(Pos.CENTER);
					vbox.setPadding(new Insets(5));
					dialogStage.setScene(new Scene(vbox));
					dialogStage.show();
				}

			}
		}
	}

	private class MovimentacaoTeclado implements EventHandler<KeyEvent> {
		private PerspectiveCamera camera;
		private final double cameraModifier = 50.0;
		private final double cameraQuantity = 5.0;
		private Group root;
		private IntegerProperty ghostcount;

		public MovimentacaoTeclado(Group root, PerspectiveCamera camera,
				IntegerProperty ghostcount) {
			this.root = root;
			this.camera = camera;
			this.ghostcount = ghostcount;
		}

		@Override
		public void handle(KeyEvent event) {
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

			Sphere ballGot = checkBalls(camera.getBoundsInParent());
			if (ballGot != null) {
				root.getChildren().remove(ballGot);
				for (int i = 0; i < balls.length; i++) {
					for (int j = 0; j < balls[i].length; j++) {
						if (ballGot == balls[i][j]) {
							balls[i][j] = null;
						}
					}
				}
				ghostcount.set(ghostcount.get() - 1);
				if (ghostcount.get() == 0) {
					movimentacao.stop();
					Stage dialogStage = new Stage();
					dialogStage.initModality(Modality.WINDOW_MODAL);
					Button button = new Button("Ok.");
					button.setOnAction(e -> {
						camera.setTranslateX(0);
						camera.setTranslateY(0);
						camera.setTranslateZ(0);
						movimentacao.start();
						dialogStage.close();
					});
					VBox vbox = new VBox();
					vbox.getChildren().addAll(new Text("Voc� Venceu"), button);
					vbox.setAlignment(Pos.CENTER);
					vbox.setPadding(new Insets(5));
					dialogStage.setScene(new Scene(vbox));
					dialogStage.show();
				}

			}
		}
	}

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
	private static Sphere[][] balls = new Sphere[mapa.length][mapa[0].length];
	private static Cube[][] labirynthWalls = new Cube[mapa.length][mapa[0].length];

	private static final String MESH_GHOST = Experiment3DWallTexture.class.getResource("ghost2.STL").getFile();

	static final String MESH_MINOTAUR = Experiment3DWallTexture.class.getResource("Minotaur.stl").getFile();

	public static final Random random = new Random();

	private static final int SIZE = 60;

	private static final IntegerProperty ghostCount = new SimpleIntegerProperty(
			mapa.length * mapa[0].length);

	public static void main(String[] args) {
		launch(args);
	}

	private PerspectiveCamera camera;
	private MovimentacaoAleatoria movimentacao;

	private Sphere checkBalls(Bounds boundsInParent) {
		return Stream.of(balls).flatMap(l -> Stream.of(l))
				.filter(b -> b != null)
				.filter(b -> b.getBoundsInParent().intersects(boundsInParent))
				.findFirst().orElse(null);
	}

	private boolean checkColision(Bounds boundsInParent) {
		Stream<Bounds> walls = Stream.of(labirynthWalls)
				.flatMap(l -> Stream.of(l)).parallel()
				.map(Cube::getBoundsInParent);
		return walls.anyMatch(b -> b.intersects(boundsInParent));
	}

	private void createLabirynth(Group root) {
		for (int i = 0; i < mapa.length; i++) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				Cube wall = new Cube(SIZE, Color.BLUE);
				wall.setTranslateX(i * SIZE);
				wall.setTranslateZ(j * SIZE);
				if ("_".equals(string)) {
					wall.ry.setAngle(90);
				}
				labirynthWalls[i][j] = wall;
				root.getChildren().add(wall);
				Sphere ball = new Sphere(SIZE / 20);
				balls[i][j] = ball;
				ball.setMaterial(new PhongMaterial(Color.YELLOW));
				ball.setTranslateX(i * SIZE);
				ball.setTranslateZ(j * SIZE);
				root.getChildren().add(ball);

			}
		}
	}

	private MeshView generateGhost(String arquivo, Color enemyColor) {
		File file = new File(arquivo);
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(file);
		Mesh mesh = importer.getImport();
		MeshView enemy = new MeshView(mesh);

		PhongMaterial sample = new PhongMaterial(enemyColor);
		sample.setSpecularColor(lightColor);
		enemy.setMaterial(sample);
		enemy.setTranslateY(14);
		enemy.setDrawMode(DrawMode.FILL);
		int posicaoInicialZ = random.nextInt(mapa[0].length * SIZE);
		enemy.setTranslateZ(posicaoInicialZ);
		int posicaoInicialX = random.nextInt(mapa.length * SIZE);
		enemy.setTranslateX(posicaoInicialX);
		for (int i = 0; checkColision(enemy.getBoundsInParent()); i++) {
			enemy.setTranslateZ(enemy.getTranslateZ() + i);
			enemy.setTranslateX(enemy.getTranslateX() + i);
		}
		enemy.setScaleX(0.4);
		enemy.setScaleY(1);
		enemy.setScaleZ(0.4);
		return enemy;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Group root = new Group();

		createLabirynth(root);
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		camera.setFieldOfView(40);
		subScene.setCamera(camera);

		// Rectangle rectangle = new Rectangle(10,
									// 10);

		// rectangle.setFill(Color.GREEN);
		// rectangle.translateYProperty().bind(
		// camera.translateYProperty().add(-10));
		//
		// camera.translateXProperty().addListener(
		// (observable, oldValue, newValue) -> {
		//
		// });
		// root.getChildren().add(rectangle);

		PointLight sun = new PointLight(Color.rgb(125, 125, 125));
		sun.translateXProperty().bind(camera.translateXProperty());
		sun.translateYProperty().bind(camera.translateYProperty());
		sun.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(sun);

		MeshView[] fantasmas = { generateGhost(MESH_GHOST, Color.AQUAMARINE),
				generateGhost(MESH_GHOST, Color.BROWN),
				generateGhost(MESH_GHOST, Color.CHARTREUSE),
				generateGhost(MESH_GHOST, Color.DODGERBLUE),
				generateGhost(MESH_GHOST, Color.FUCHSIA),
				generateGhost(MESH_GHOST, Color.GREEN),
				generateGhost(MESH_GHOST, Color.HOTPINK),
				generateGhost(MESH_GHOST, Color.INDIGO),
				generateGhost(MESH_GHOST, Color.KHAKI),
				generateGhost(MESH_GHOST, Color.LIGHTSALMON),
				generateGhost(MESH_GHOST, Color.MIDNIGHTBLUE),
				generateGhost(MESH_GHOST, Color.NAVY),
				generateGhost(MESH_GHOST, Color.ORCHID),
				generateGhost(MESH_GHOST, Color.PURPLE),
				generateGhost(MESH_GHOST, Color.RED),
				generateGhost(MESH_GHOST, Color.SLATEBLUE),
				generateGhost(MESH_GHOST, Color.TRANSPARENT),
				generateGhost(MESH_GHOST, Color.VIOLET),
				generateGhost(MESH_GHOST, Color.WHITESMOKE),
				generateGhost(MESH_GHOST, Color.YELLOWGREEN), };

		movimentacao = new MovimentacaoAleatoria(fantasmas);
		movimentacao.start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
		sc.setOnKeyPressed(new MovimentacaoTeclado(root, camera, ghostCount));

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		primaryStage.show();
	}
}
