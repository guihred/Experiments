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

	private static final String MESH_GHOST = Labyrinth3DWallTexture.class.getResource("ghost2.STL").getFile();

	public static final String MESH_MINOTAUR = Labyrinth3DWallTexture.class.getResource("Minotaur.stl").getFile();

	private static final int SIZE = 60;

	private PerspectiveCamera camera;

	private final List<LabyrinthWall> cubes = new ArrayList<>();
	private MovimentacaoAleatoria movimentacao;
	private void criarLabirinto(Group root) {
		for (int i = mapa.length - 1; i >= 0; i--) {
			for (int j = mapa[i].length - 1; j >= 0; j--) {
				String string = mapa[i][j];
				LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
				rectangle.setTranslateZ(j * SIZE);
				rectangle.setTranslateX(i * SIZE);
				if ("_".equals(string)) {
					rectangle.getRy().setAngle(90);
				}
				cubes.add(rectangle);
				root.getChildren().add(rectangle);
			}
		}
	}

	private MeshView gerarFantasma(String arquivo, Color animalColor) {
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(new File(arquivo));
		Mesh mesh = importer.getImport();
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
		animal.setScaleY(1);
		animal.setScaleZ(0.4);

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

		MeshView[] fantasmas = { 
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
				gerarFantasma(MESH_GHOST, Color.YELLOWGREEN),
		};

		movimentacao = new MovimentacaoAleatoria(this, fantasmas);
		movimentacao.start();

		root.getChildren().addAll(fantasmas);

		Scene sc = new Scene(new Group(subScene));
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		sc.setFill(Color.TRANSPARENT);
		sc.setOnKeyPressed(new MovimentacaoTeclado(this));

		primaryStage.setTitle("EXP 1: Labyrinth");
		primaryStage.setScene(sc);
		// primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}