package labyrinth;

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

public class Labyrinth3DKillerGhosts extends Application implements CommomLabyrinth {

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

	private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");
    private static final int SIZE = 60;

	private PerspectiveCamera camera;

	private final List<LabyrinthWall> cubes = new ArrayList<>();
	private void criarLabirinto(Group root) {
		for (int i = mapa.length - 1; i >= 0; i--) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
                rectangle.setTranslateZ(j * (double) SIZE);
                rectangle.setTranslateX(i * (double) SIZE);
				if ("_".equals(string)) {
					rectangle.getRy().setAngle(90);
				}
				cubes.add(rectangle);
				root.getChildren().add(rectangle);
			}
		}
	}

	private MeshView gerarFantasma(String arquivo, Color animalColor) {
        Mesh mesh = ResourceFXUtils.importStlMesh(arquivo);
		MeshView animal = new MeshView(mesh);
		PhongMaterial sample = new PhongMaterial(animalColor);
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


		animal.setScaleX(0.4);
        animal.setScaleZ(0.4);
		animal.setScaleY(1);

		return animal;
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

		criarLabirinto(root);
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

        MeshView[] ghosts = createGhosts();

        new MovimentacaoAleatoria(this, ghosts).start();

        root.getChildren().addAll(ghosts);

		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
        Scene sc = new Scene(new Group(subScene));
		sc.setFill(Color.TRANSPARENT);
		MovimentacaoTeclado value = new MovimentacaoTeclado(this);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
        primaryStage.setTitle("Labyrinth 3D With Killer Ghost");
		primaryStage.setScene(sc);
		primaryStage.show();
	}

    private MeshView[] createGhosts() {
        return new MeshView[] { 
				gerarFantasma(MESH_GHOST, Color.AQUAMARINE),
				gerarFantasma(MESH_GHOST, Color.BROWN),
				gerarFantasma(MESH_GHOST, Color.CHARTREUSE),
				gerarFantasma(MESH_GHOST, Color.DODGERBLUE),
				gerarFantasma(MESH_GHOST, Color.FUCHSIA),
				gerarFantasma(MESH_GHOST, Color.GREEN),
				gerarFantasma(MESH_GHOST, Color.HOTPINK),
				gerarFantasma(MESH_GHOST, Color.INDIGO),
				gerarFantasma(MESH_GHOST, Color.KHAKI),
				gerarFantasma(MESH_GHOST, Color.LIGHTSALMON),
				gerarFantasma(MESH_GHOST, Color.MIDNIGHTBLUE),
				gerarFantasma(MESH_GHOST, Color.NAVY),
				gerarFantasma(MESH_GHOST, Color.ORCHID),
				gerarFantasma(MESH_GHOST, Color.PURPLE),
				gerarFantasma(MESH_GHOST, Color.RED),
				gerarFantasma(MESH_GHOST, Color.SLATEBLUE),
				gerarFantasma(MESH_GHOST, Color.TRANSPARENT),
				gerarFantasma(MESH_GHOST, Color.VIOLET),
				gerarFantasma(MESH_GHOST, Color.WHITESMOKE),
                gerarFantasma(MESH_GHOST, Color.YELLOWGREEN)
		};
    }

	public static void main(String[] args) {
		launch(args);
	}

}
