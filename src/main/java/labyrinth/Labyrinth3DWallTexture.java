package labyrinth;

import static labyrinth.GhostGenerator.generateGhost;
import static labyrinth.GhostGenerator.getMapa;
import static labyrinth.LabyrinthWall.SIZE;
import static utils.ResourceFXUtils.toExternalForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;
import utils.MouseInScreenHandler;

public class Labyrinth3DWallTexture extends Application implements CommomLabyrinth {
    private static final Color lightColor = Color.grayRgb(125);


    public static final Image OOZE_IMAGE = new Image(toExternalForm("ooze.jpg"));

    private static final Image WALL_IMAGE = new Image(toExternalForm("wall.jpg"));

    private static final Image WALL_IMAGE2 = new Image(toExternalForm("wall2.jpg"));

    private Sphere[][] balls = new Sphere[getMapa().length][getMapa()[0].length];

    private final SimpleIntegerProperty ballsCount = new SimpleIntegerProperty(getMapa().length * getMapa()[0].length);

    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final List<LabyrinthWall> labyrinthWalls = new ArrayList<>();

    private MovimentacaoAleatoria movimentacao;

    private final Group root = new Group();

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
            ballsCount.set(ballsCount.get() - 1);
            if (ballsCount.get() == 0) {
                movimentacao.stop();
                new SimpleDialogBuilder().text("Você Venceu").button("Ok", () -> {
                    camera.setTranslateY(0);
                    camera.setTranslateZ(0);
                    camera.setTranslateX(0);
                    movimentacao.start();
                }).bindWindow(root).displayDialog();
            }
        }
    }

    @Override
    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public List<LabyrinthWall> getLabyrinthWalls() {
        return labyrinthWalls;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        createLabyrinth(root);
        SubScene subScene = new SubScene(root, 500, 500, true, SceneAntialiasing.BALANCED);
        subScene.heightProperty().bind(primaryStage.heightProperty());
        subScene.widthProperty().bind(primaryStage.widthProperty());

        camera.setNearClip(1. / 10);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-100);
        camera.setRotationAxis(Rotate.Y_AXIS);
        final int fieldView = 40;
        camera.setFieldOfView(fieldView);
        subScene.setCamera(camera);

        PointLight light = new PointLight(lightColor);
        light.translateXProperty().bind(camera.translateXProperty());
        light.translateYProperty().bind(camera.translateYProperty());
        light.translateZProperty().bind(camera.translateZProperty());
        root.getChildren().add(light);

        MeshView[] ghosts = { generateGhost( Color.AQUAMARINE), generateGhost( Color.BROWN),
            generateGhost( Color.CHARTREUSE), generateGhost( Color.DODGERBLUE),
            generateGhost( Color.FUCHSIA), generateGhost( Color.GREEN),
            generateGhost( Color.HOTPINK), generateGhost( Color.INDIGO),
            generateGhost( Color.KHAKI), generateGhost( Color.LIGHTSALMON),
            generateGhost( Color.MIDNIGHTBLUE), generateGhost( Color.NAVY),
            generateGhost( Color.ORCHID), generateGhost( Color.PURPLE),
            generateGhost( Color.RED), generateGhost( Color.SLATEBLUE),
            generateGhost( Color.TRANSPARENT), generateGhost( Color.VIOLET),
            generateGhost( Color.WHITESMOKE), generateGhost( Color.YELLOWGREEN), };

        movimentacao = new MovimentacaoAleatoria(this, ghosts);
        movimentacao.start();
        root.getChildren().addAll(ghosts);

        Scene sc = new Scene(new Group(subScene));
        // End Step 2a
        // Step 2b: Add a Movement Keyboard Handler
        sc.setCursor(Cursor.NONE);
        sc.setFill(Color.TRANSPARENT);
        MovimentacaoTeclado value = new MovimentacaoTeclado(this);
        sc.setOnKeyPressed(value);
        sc.setOnKeyReleased(value::keyReleased);
        sc.setOnMouseMoved(new MouseInScreenHandler(sc, camera));
        primaryStage.setFullScreen(true);
        primaryStage.setTitle("Labyrinth 3D With Wall Texture");
        primaryStage.setScene(sc);
        primaryStage.show();
    }

    private Sphere checkBalls(Bounds boundsInParent) {
        return Stream.of(balls).flatMap(Stream::of).filter(Objects::nonNull)
            .filter(b -> b.getBoundsInParent().intersects(boundsInParent)).findFirst().orElse(null);
    }

    private void createLabyrinth(Group root1) {
        for (int i = 0; i < getMapa().length; i++) {
            for (int j = getMapa()[i].length - 1; j >= 0; j--) {
                String string = getMapa()[i][j];
                LabyrinthWall wall = new LabyrinthWall(SIZE, Color.BLUE, WALL_IMAGE, WALL_IMAGE2);
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
                ball.setTranslateX(i * SIZE);
                ball.setTranslateZ(j * SIZE);
                root1.getChildren().add(ball);

            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
