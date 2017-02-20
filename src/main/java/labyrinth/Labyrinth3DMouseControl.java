package labyrinth;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Labyrinth3DMouseControl extends Application implements CommomLabyrinth {

	private static final Color LIGHT_COLOR = Color.rgb(125, 125, 125);
	private static String[][] mapa = { 
			{ "_", "_", "_", "_", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, 
			{ "|", "|", "_", "|", "_", "|" },
			{ "_", "|", "_", "|", "_", "|" }, 
			{ "|", "|", "_", "|", "_", "|" },
			{ "|", "_", "_", "|", "_", "|" }, 
			{ "|", "_", "_", "_", "|", "_" },
			{ "_", "|", "_", "_", "_", "|" }, 
			{ "_", "_", "|", "|", "|", "_" },
			{ "_", "|", "_", "|", "_", "|" }, 
			{ "|", "|", "_", "_", "|", "_" },
			{ "_", "_", "_", "_", "_", "|" }, 
			{ "|", "_", "_", "_", "_", "_" },
			{ "|", "|", "_", "|", "_", "|" }, 
			{ "|", "_", "|", "_", "_", "|" },
			{ "|", "_", "_", "_", "_", "|" }, 
			{ "_", "_", "_", "|", "_", "|" },
			{ "_", "_", "_", "_", "_", "_" },
	};

	private final URL MESH_GHOST = newURL(
			Labyrinth3DMouseControl.class.getResource("ghost2.STL").toExternalForm());


	public static final String MESH_MINOTAUR = "Minotaur.stl";
	private static final int SIZE = 60;

	private final Image WALL_IMAGE = new Image(Labyrinth3DMouseControl.class.getResource("wall.jpg").toExternalForm());
	private final Image WALL_IMAGE2 = new Image(
			Labyrinth3DMouseControl.class.getResource("wall2.jpg").toExternalForm());

	private Sphere[][] balls = new Sphere[mapa.length][mapa[0].length];

	private PerspectiveCamera camera;

	private final IntegerProperty ghostCount = new SimpleIntegerProperty(
			mapa.length * mapa[0].length);

	private final List<LabyrinthWall> labyrinthWalls = new ArrayList<>();
	private MovimentacaoAleatoria movimentacao;

	public final Random random = new Random();
	private Group root = new Group();

	private Sphere checkBalls(Bounds boundsInParent) {
		return Stream.of(balls).flatMap(Stream::of)
				.filter(b -> b != null)
				.filter(b -> b.getBoundsInParent().intersects(boundsInParent))
				.findFirst().orElse(null);
	}


	private void createLabyrinth(Group root) {
		for (int i = 0; i < mapa.length; i++) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				LabyrinthWall wall = new LabyrinthWall(SIZE, Color.BLUE, WALL_IMAGE, WALL_IMAGE2);
				wall.setTranslateX(i * SIZE);
				wall.setTranslateZ(j * SIZE);
				if ("_".equals(string)) {
					wall.getRy().setAngle(90);
				}
				labyrinthWalls.add(wall);
				root.getChildren().add(wall);
				Sphere ball = new Sphere(SIZE / 20);
				balls[i][j] = ball;
				ball.setTranslateX(i * SIZE);
				ball.setTranslateZ(j * SIZE);
				ball.setMaterial(new PhongMaterial(Color.YELLOW));
				root.getChildren().add(ball);

			}
		}
	}

	@Override
	public void endKeyboard() {
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
			ghostCount.set(ghostCount.get() - 1);
			if (ghostCount.get() == 0) {
				movimentacao.stop();
				Stage dialogStage = new Stage();
				dialogStage.initModality(Modality.WINDOW_MODAL);
				Button button = new Button("Ok.");
				button.setOnAction(e -> {
					camera.setTranslateY(0);
					camera.setTranslateZ(0);
					camera.setTranslateX(0);
					movimentacao.start();
					dialogStage.close();
				});
				VBox vbox = new VBox();
				vbox.getChildren().addAll(new Text("Você Venceu"), button);
				vbox.setAlignment(Pos.CENTER);
				vbox.setPadding(new Insets(5));
				dialogStage.setScene(new Scene(vbox));
				dialogStage.show();
			}

		}
	}

	private static URL newURL(String arquivo) {
		try {
			return new URL(arquivo);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private MeshView generateGhost(URL arquivo, Color enemyColor) {
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(arquivo);
		Mesh mesh = importer.getImport();
		MeshView enemy = new MeshView(mesh);
		PhongMaterial sample = new PhongMaterial(enemyColor);
		sample.setSpecularColor(LIGHT_COLOR);
		enemy.setDrawMode(DrawMode.FILL);
		enemy.setTranslateY(14);
		enemy.setMaterial(sample);
		int posicaoInicialZ = random.nextInt(mapa[0].length * SIZE);
		enemy.setTranslateZ(posicaoInicialZ);
		int posicaoInicialX = random.nextInt(mapa.length * SIZE);
		enemy.setTranslateX(posicaoInicialX);
		while (checkColision(enemy.getBoundsInParent())) {
			enemy.setTranslateZ(enemy.getTranslateZ() + 1);
			enemy.setTranslateX(enemy.getTranslateX() + 1);
		}
		enemy.setScaleX(0.4);
		enemy.setScaleY(1);
		enemy.setScaleZ(0.4);
		return enemy;
	}

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public Collection<LabyrinthWall> getLabyrinthWalls() {
		return labyrinthWalls;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		createLabyrinth(root);
		SubScene subScene = new SubScene(root, 640, 480, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		camera.setRotationAxis(Rotate.Y_AXIS);
		camera.setNearClip(0.1);
		camera.setFieldOfView(40);
		subScene.setCamera(camera);
		PointLight light = new PointLight(Color.rgb(125, 125, 125));
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);

		MeshView[] fantasmas = { generateGhost(MESH_GHOST, Color.AQUAMARINE),
				generateGhost(MESH_GHOST, Color.BROWN),
				generateGhost(MESH_GHOST, Color.CHARTREUSE),
				generateGhost(MESH_GHOST, Color.DODGERBLUE),
				generateGhost(MESH_GHOST, Color.FUCHSIA),
				generateGhost(MESH_GHOST, Color.GREEN),
				generateGhost(MESH_GHOST, Color.HOTPINK),
				generateGhost(MESH_GHOST, Color.MIDNIGHTBLUE), generateGhost(MESH_GHOST, Color.WHITESMOKE),
				generateGhost(MESH_GHOST, Color.INDIGO),
				generateGhost(MESH_GHOST, Color.KHAKI),
				generateGhost(MESH_GHOST, Color.LIGHTSALMON),
				generateGhost(MESH_GHOST, Color.RED),
				generateGhost(MESH_GHOST, Color.NAVY),
				generateGhost(MESH_GHOST, Color.TRANSPARENT),
				generateGhost(MESH_GHOST, Color.ORCHID),
				generateGhost(MESH_GHOST, Color.PURPLE),
				generateGhost(MESH_GHOST, Color.SLATEBLUE),
				generateGhost(MESH_GHOST, Color.VIOLET),
				generateGhost(MESH_GHOST, Color.YELLOWGREEN), };

		movimentacao = new MovimentacaoAleatoria(this, fantasmas);
		movimentacao.start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		sc.setFill(Color.TRANSPARENT);
		sc.setOnKeyPressed(new MovimentacaoTeclado(this));
		sc.setOnMouseMoved(new EventHandler<MouseEvent>() {

			private double mouseOldX;
			private double mousePosX;

			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mousePosX = me.getX();
				double mouseDeltaX = mousePosX - mouseOldX;
				camera.setRotate(camera.getRotate() + mouseDeltaX * .5);
			}
		});

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
