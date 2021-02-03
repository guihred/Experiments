package labyrinth;

import static labyrinth.GhostGenerator.getMapa;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class Labyrinth3DGhosts extends Application implements CommomLabyrinth {
    private static final Color lightColor = Color.grayRgb(125);
    private static final String MESH_GHOST = ResourceFXUtils.toFullPath("ghost2.STL");

    private static final int SIZE = 60;

    private PerspectiveCamera camera;

    private final List<LabyrinthWall> walls = new ArrayList<>();

    @Override
    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public List<LabyrinthWall> getLabyrinthWalls() {
        return walls;
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();

        initializeLabyrinth(root);
        SubScene subScene = new SubScene(root, 500, 500, true, SceneAntialiasing.BALANCED);
        subScene.heightProperty().bind(primaryStage.heightProperty());
        subScene.widthProperty().bind(primaryStage.widthProperty());
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1. / 10);
        camera.setTranslateZ(-100);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);
        PointLight light = new PointLight(lightColor);
        light.translateXProperty().bind(camera.translateXProperty());
        light.translateYProperty().bind(camera.translateYProperty());
        light.translateZProperty().bind(camera.translateZProperty());
        root.getChildren().add(light);

        MeshView[] ghosts = { gerarAnimal(MESH_GHOST, Color.AQUAMARINE), gerarAnimal(MESH_GHOST, Color.BEIGE),
                gerarAnimal(MESH_GHOST, Color.BLUEVIOLET), gerarAnimal(MESH_GHOST, Color.CORNFLOWERBLUE),
                gerarAnimal(MESH_GHOST, Color.DEEPPINK), gerarAnimal(MESH_GHOST, Color.DEEPSKYBLUE),
                gerarAnimal(MESH_GHOST, Color.LIGHTGREEN),

        };

        new MovimentacaoAleatoria(this, ghosts).start();

        root.getChildren().addAll(ghosts);

        Scene sc = new Scene(new Group(subScene));
        // End Step 2a
        // Step 2b: Add a Movement Keyboard Handler
        sc.setFill(Color.TRANSPARENT);
        MovimentacaoTeclado value = new MovimentacaoTeclado(this);
        sc.setOnKeyReleased(value::keyReleased);
        sc.setOnKeyPressed(value);
        primaryStage.setScene(sc);
        primaryStage.setTitle("Labyrinth 3D With Ghosts");
        primaryStage.show();
    }

    private MeshView gerarAnimal(String arquivo, Color jewelColor) {
        Mesh mesh = ResourceFXUtils.importStlMesh(arquivo);
        MeshView animal = new MeshView(mesh);
        PhongMaterial sample = new PhongMaterial(jewelColor);
        sample.setSpecularColor(lightColor);
        animal.setMaterial(sample);
        sample.setSpecularPower(16);
        animal.setTranslateY(15);

        animal.setTranslateZ(Math.random() * getMapa()[0].length * SIZE);
        animal.setTranslateX(Math.random() * getMapa().length * SIZE);
        while (checkColision(animal.getBoundsInParent())) {
            animal.setTranslateZ(animal.getTranslateZ() + 1);
            animal.setTranslateX(animal.getTranslateX() + 1);
        }

        animal.setScaleZ(1. / 4);
        animal.setScaleX(1. / 4);
        animal.setScaleY(1);

        return animal;
    }

    private void initializeLabyrinth(Group root) {
        for (int i = getMapa().length - 1; i >= 0; i--) {
            for (int j = getMapa()[i].length - 1; j >= 0; j--) {
                String string = getMapa()[i][j];
                LabyrinthWall rectangle = new LabyrinthWall(SIZE, Color.BLUE);
                rectangle.setTranslateX(i * (double) SIZE);
                rectangle.setTranslateZ(j * (double) SIZE);
                if ("_".equals(string)) {
                    final int straightAngle = 90;
                    rectangle.getRy().setAngle(straightAngle);
                }
                walls.add(rectangle);

                root.getChildren().add(rectangle);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
