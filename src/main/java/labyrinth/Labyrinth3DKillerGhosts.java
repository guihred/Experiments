package labyrinth;

import static labyrinth.GhostGenerator.SIZE;
import static labyrinth.GhostGenerator.generateGhost;
import static labyrinth.GhostGenerator.mapa;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;

public class Labyrinth3DKillerGhosts extends Application implements CommomLabyrinth {

    private PerspectiveCamera camera;
    private final List<LabyrinthWall> cubes = new ArrayList<>();

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
        SubScene subScene = new SubScene(root, 500, 500, true, SceneAntialiasing.BALANCED);
        subScene.heightProperty().bind(primaryStage.heightProperty());
        subScene.widthProperty().bind(primaryStage.widthProperty());
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1. / 10);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-100);
        subScene.setCamera(camera);
        PointLight light = new PointLight(GhostGenerator.LIGHT_COLOR);
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

    public static void main(String[] args) {
        launch(args);
    }

    private static MeshView[] createGhosts() {
        return new MeshView[] { generateGhost(Color.AQUAMARINE), generateGhost(Color.BROWN),
            generateGhost(Color.CHARTREUSE), generateGhost(Color.DODGERBLUE), generateGhost(Color.FUCHSIA),
            generateGhost(Color.GREEN), generateGhost(Color.HOTPINK), generateGhost(Color.INDIGO),
            generateGhost(Color.KHAKI), generateGhost(Color.LIGHTSALMON), generateGhost(Color.MIDNIGHTBLUE),
            generateGhost(Color.NAVY), generateGhost(Color.ORCHID), generateGhost(Color.PURPLE),
            generateGhost(Color.RED), generateGhost(Color.SLATEBLUE), generateGhost(Color.TRANSPARENT),
            generateGhost(Color.VIOLET), generateGhost(Color.WHITESMOKE), generateGhost(Color.YELLOWGREEN) };
    }

}
