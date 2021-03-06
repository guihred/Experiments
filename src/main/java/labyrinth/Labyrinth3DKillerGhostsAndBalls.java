package labyrinth;

import static labyrinth.GhostGenerator.generateGhost;
import static labyrinth.GhostGenerator.getMapa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;
import utils.ResourceFXUtils;

public class Labyrinth3DKillerGhostsAndBalls extends Application implements CommomLabyrinth {


	private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");

    private static final double SIZE = 60;

    private Sphere[][] balls = new Sphere[getMapa().length][getMapa()[0].length];

	private PerspectiveCamera camera;

    private final SimpleIntegerProperty ghostCount = new SimpleIntegerProperty(getMapa().length * getMapa()[0].length);
	private final List<LabyrinthWall> labyrinthWalls = new ArrayList<>();
	private MovimentacaoAleatoria movimentacao;
	private Group root = new Group();

    @Override
	public void endKeyboard() {
        end(camera, root, balls, ghostCount, movimentacao);
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
	public void start(Stage primaryStage) {
        createLabyrinth(root, labyrinthWalls, balls, getMapa());
        SubScene subScene = new SubScene(root, 500, 500, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
        camera.setNearClip(1. / 10);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
        camera.setFieldOfView(30);
		subScene.setCamera(camera);


        PointLight light = new PointLight(Color.GRAY);
		light.translateXProperty().bind(camera.translateXProperty());
		light.translateYProperty().bind(camera.translateYProperty());
		light.translateZProperty().bind(camera.translateZProperty());
		root.getChildren().add(light);

		MeshView[] ghosts = { generateGhost(MESH_GHOST, Color.AQUAMARINE),
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

		movimentacao = new MovimentacaoAleatoria(this, ghosts);
		movimentacao.start();

		root.getChildren().addAll(ghosts);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
        primaryStage.setTitle("Labyrinth 3D With Balls");
		primaryStage.setScene(sc);
		primaryStage.show();
	}


	public static void createLabyrinth(Group root1, Collection<LabyrinthWall> labyrinthWalls, Sphere[][] balls,
        String[][] mapa1) {
        for (int i = 0; i < mapa1.length; i++) {
            for (int j = mapa1[i].length - 1; j >= 0; j--) {
                String string = mapa1[i][j];
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

	public static void end(PerspectiveCamera camera, Group root, Sphere[][] balls, SimpleIntegerProperty ghostCount,
            MovimentacaoAleatoria movimentacao) {
        Sphere ballGot = CommomLabyrinth.checkBalls(camera.getBoundsInParent(), balls);
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
			new SimpleDialogBuilder().text("Você Venceu").button("Ok", () -> {
                movimentacao.start();
                camera.setTranslateZ(0);
                camera.setTranslateY(0);
                camera.setTranslateX(0);
            }).bindWindow(root).displayDialog();
        }
    }

	public static void main(String[] args) {
		launch(args);
	}
}
