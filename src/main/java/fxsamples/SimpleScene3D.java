package fxsamples;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import utils.RotateUtils;

public class SimpleScene3D extends Application {
    private static final double SCENE_WIDTH = 600;
    private static final double SCENE_HEIGHT = 600;
    private static final double CAMERA_Y_LIMIT = 15;
    private static final double ROTATE_MODIFIER = 25;
    private double mouseXold;
    private double mouseYold;
    private EventHandler<? super MouseEvent> mouseHandler = event -> {
        Node picked = event.getPickResult().getIntersectedNode();
        if (null != picked) {
            double scalar = 2;
            if (picked.getScaleX() > 1) {
                scalar = 1;
            }
            picked.setScaleX(scalar);
            picked.setScaleY(scalar);
            picked.setScaleZ(scalar);
        }
    };

    @Override
    public void start(Stage primaryStage) {

        final Cylinder cylinder = new Cylinder(50, 100);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        cylinder.setMaterial(blueMaterial);
        cylinder.setDrawMode(DrawMode.FILL);
        // Step 1c: Translate and Rotate primitive into position

        cylinder.setRotationAxis(Rotate.X_AXIS);
        final int defaultRotation = 45;
        cylinder.setRotate(defaultRotation);
        final int defaultDepth = 200;
        cylinder.setTranslateZ(-defaultDepth);
        // End Step 1c

        // Step 1d: Add and Transform more primitives

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final Box cube = new Box(50, 50, 50);
        cube.setMaterial(greenMaterial);
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final Sphere sphere = new Sphere(50);
        sphere.setMaterial(redMaterial);
        cube.setRotationAxis(Rotate.Y_AXIS);
        cube.setRotate(defaultRotation);
        final int distance = 150;
        cube.setTranslateX(-distance);
        cube.setTranslateY(-distance);
        cube.setTranslateZ(distance);
        sphere.setTranslateX(distance);
        sphere.setTranslateY(distance);
        sphere.setTranslateZ(-distance);

        // End Step 1d

        // Step 1e: All Together Now: Grouped Primitives

        Group primitiveGroup = new Group(cylinder, cube, sphere);
        primitiveGroup.setRotationAxis(Rotate.Z_AXIS);
        // Rotate the Group as a whole
        primitiveGroup.setRotate(180);
        Group sceneRoot = new Group();
        Scene scene = new Scene(sceneRoot, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setFill(Color.BLACK);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1. / 10);
        camera.setFarClip(1000. * 10);
        camera.setTranslateZ(-1000);
        scene.setCamera(camera);
        sceneRoot.getChildren().addAll(cylinder, cube, sphere);
        sceneRoot.getChildren().addAll(primitiveGroup);
        scene.setOnMouseClicked(mouseHandler);
        RotateUtils.setMovable(camera, scene);
        Rotate xRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        camera.getTransforms().addAll(xRotate, yRotate);
        scene.addEventHandler(MouseEvent.ANY, event -> {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double mouseXnew = event.getSceneX();
                double mouseYnew = event.getSceneY();
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    double pitchRotate = xRotate.getAngle() + (mouseYnew - mouseYold) / ROTATE_MODIFIER;
                    pitchRotate = pitchRotate > CAMERA_Y_LIMIT ? CAMERA_Y_LIMIT : pitchRotate;
                    pitchRotate = pitchRotate < -CAMERA_Y_LIMIT ? -CAMERA_Y_LIMIT : pitchRotate;
                    xRotate.setAngle(pitchRotate);
                    double yawRotate = yRotate.getAngle() - (mouseXnew - mouseXold) / ROTATE_MODIFIER;
                    yRotate.setAngle(yawRotate);
                }
                mouseXold = mouseXnew;
                mouseYold = mouseYnew;
            }
        });
        // End Step 3

        primaryStage.setTitle("Simple Scene 3D");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}