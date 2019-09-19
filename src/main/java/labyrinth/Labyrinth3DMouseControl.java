package labyrinth;

import static labyrinth.GhostGenerator.generateGhost;
import static labyrinth.GhostGenerator.mapa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import utils.MouseInScreenHandler;
import utils.ResourceFXUtils;

public class Labyrinth3DMouseControl extends Application implements CommomLabyrinth {

    private static final Color LIGHT_COLOR = Color.grayRgb(125);

    private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");


    private Sphere[][] balls = new Sphere[mapa.length][mapa[0].length];

	private PerspectiveCamera camera;

    private final SimpleIntegerProperty ghostCount = new SimpleIntegerProperty(
        mapa.length * mapa[0].length);

	private final List<LabyrinthWall> labyrinthWalls = new ArrayList<>();
	private MovimentacaoAleatoria movimentacao;

	private Group root = new Group();

	@Override
	public void endKeyboard() {
        Labyrinth3DKillerGhostsAndBalls.end(camera, root, balls, ghostCount, movimentacao);
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
        Labyrinth3DKillerGhostsAndBalls.createLabyrinth(root, labyrinthWalls, balls, mapa);
        SubScene subScene = new SubScene(root, 500, 500, true,
				SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(primaryStage.heightProperty());
		subScene.widthProperty().bind(primaryStage.widthProperty());
		camera = new PerspectiveCamera(true);
		camera.setFarClip(1000.0);
		camera.setTranslateZ(-100);
		camera.setRotationAxis(Rotate.Y_AXIS);
        camera.setNearClip(1. / 10);
        final int fieldView = 40;
        camera.setFieldOfView(fieldView);
		subScene.setCamera(camera);
        PointLight light = new PointLight(LIGHT_COLOR);
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

		movimentacao = new MovimentacaoAleatoria(this, ghosts);
		movimentacao.start();
		root.getChildren().addAll(ghosts);

		Scene sc = new Scene(new Group(subScene));
		sc.setCursor(Cursor.NONE);
		sc.setFill(Color.TRANSPARENT);
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
		primaryStage.setFullScreen(true);
        sc.setOnMouseMoved(new MouseInScreenHandler(sc, camera));

        primaryStage.setTitle("Labyrinth 3D With Mouse Control");
		primaryStage.setScene(sc);
		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
