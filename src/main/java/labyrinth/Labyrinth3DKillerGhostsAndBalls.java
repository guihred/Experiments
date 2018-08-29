package labyrinth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;

public class Labyrinth3DKillerGhostsAndBalls extends Application implements CommomLabyrinth {


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

	private static final int SIZE = 60;

	private Sphere[][] balls = new Sphere[mapa.length][mapa[0].length];

	private PerspectiveCamera camera;

    private final SimpleIntegerProperty ghostCount = new SimpleIntegerProperty(mapa.length * mapa[0].length);
	private final List<LabyrinthWall> labyrinthWalls = new ArrayList<>();

	private final Color lightColor = Color.rgb(125, 125, 125);

	private MovimentacaoAleatoria movimentacao;

	private final Random random = new Random();
	private Group root = new Group();

	private Sphere checkBalls(Bounds boundsInParent) {
        return checkBalls(boundsInParent, balls);
	}

	private void createLabyrinth(Group root1) {
		for (int i = 0; i < mapa.length; i++) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				LabyrinthWall wall = new LabyrinthWall(SIZE, Color.BLUE);
				wall.setTranslateX(i * SIZE);
				wall.setTranslateZ(j * SIZE);
				if ("_".equals(string)) {
					wall.getRy().setAngle(90);
				}
				labyrinthWalls.add(wall);
				root1.getChildren().add(wall);
				Sphere ball = new Sphere(SIZE / 20);
				balls[i][j] = ball;
				ball.setMaterial(new PhongMaterial(Color.YELLOW));
				ball.setTranslateZ(j * SIZE);
				ball.setTranslateX(i * SIZE);
				root1.getChildren().add(ball);

			}
		}
	}

	@Override
	public void endKeyboard() {
		Sphere ballGot = checkBalls(camera.getBoundsInParent());
        if (ballGot == null) {
            return;
        }
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
                camera.setTranslateZ(0);
                camera.setTranslateY(0);
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

	private MeshView generateGhost(String arquivo, Color animalColor) {
        MeshView animal = new MeshView(ResourceFXUtils.importStlMesh(arquivo));
		PhongMaterial sample = new PhongMaterial(animalColor);
		sample.setSpecularColor(lightColor);
		sample.setSpecularPower(16);
		animal.setMaterial(sample);
		animal.setTranslateY(14);

		int posicaoInicialZ = random.nextInt(mapa[0].length * SIZE);
		animal.setTranslateZ(posicaoInicialZ);
		int posicaoInicialX = random.nextInt(mapa.length * SIZE);
		animal.setTranslateX(posicaoInicialX);
		while (checkColision(animal.getBoundsInParent())) {
			animal.setTranslateZ(animal.getTranslateZ() + 1);
			animal.setTranslateX(animal.getTranslateX() + 1);
		}
		animal.setScaleX(0.4);
		animal.setScaleY(1);
		animal.setScaleZ(0.4);
		return animal;
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
		camera.setNearClip(0.1);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
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
				generateGhost(MESH_GHOST, Color.INDIGO),
				generateGhost(MESH_GHOST, Color.KHAKI),
				generateGhost(MESH_GHOST, Color.LIGHTSALMON),
				generateGhost(MESH_GHOST, Color.MIDNIGHTBLUE),
				generateGhost(MESH_GHOST, Color.NAVY),
				generateGhost(MESH_GHOST, Color.ORCHID),
				generateGhost(MESH_GHOST, Color.PURPLE),
				generateGhost(MESH_GHOST, Color.RED),
				generateGhost(MESH_GHOST, Color.SLATEBLUE),
				generateGhost(MESH_GHOST, Color.AZURE),
				generateGhost(MESH_GHOST, Color.VIOLET),
				generateGhost(MESH_GHOST, Color.WHITESMOKE),
				generateGhost(MESH_GHOST, Color.YELLOWGREEN), };

		movimentacao = new MovimentacaoAleatoria(this, fantasmas);
		movimentacao.start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
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
