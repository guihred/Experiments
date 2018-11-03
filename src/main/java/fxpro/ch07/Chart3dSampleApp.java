package fxpro.ch07;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;
import utils.Xform;

public class Chart3dSampleApp extends Application {
    private static final int DEPTH = 300;
    private static final double CAMERA_DISTANCE = 1450;
    private static final double CONTROL_MULTIPLIER = 10.1;
    private static final double SHIFT_MULTIPLIER = 0.1;
    private static final double ALT_MULTIPLIER = 0.5;

    private final Group root = new Group();
    private final Group axisGroup = new Group();
    private final Xform world = new Xform();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

    @Override
    public void start(Stage primaryStage) {
        buildScene();
        buildCamera();
        buildAxes();
        buildChart();

        Scene scene = new Scene(root, 1600, 900, true);
        scene.setFill(Color.GREY);
        scene.setOnKeyPressed(this::handleKeyPressed);
        handleMouse(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setCamera(camera);

    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        final Box xAxis = new Box(DEPTH, 1, DEPTH);
        final Box yAxis = new Box(1, DEPTH, DEPTH);
        final Box zAxis = new Box(DEPTH, DEPTH, 1);
        yAxis.setTranslateY(-DEPTH / 2.0);
        yAxis.setTranslateX(DEPTH / 2.0);
        zAxis.setTranslateY(-DEPTH / 2.0);
        zAxis.setTranslateZ(DEPTH / 2.0);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-CAMERA_DISTANCE);
        cameraXform.setRotateY(0);
        cameraXform.setRotateX(0);
    }

    private void buildChart() {

        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

        float h = 200; // Height
        float s = 200; // Side

        TriangleMesh pyramidMesh = new TriangleMesh();

        pyramidMesh.getTexCoords().addAll(0, 0);
        pyramidMesh.getPoints().addAll(0, 0, 0, // Point 0 - Top
                0, h, -s / 2, // Point 1 - Front
                -s / 2, h, 0, // Point 2 - Left
                s / 2, h, 0, // Point 3 - Back
                0, h, s / 2 // Point 4 - Right
        );

        pyramidMesh.getFaces().addAll(0, 0, 2, 0, 1, 0, // Front left face
                0, 0, 1, 0, 3, 0, // Front right face
                0, 0, 3, 0, 4, 0, // Back right face
                0, 0, 4, 0, 2, 0, // Back left face
                4, 0, 1, 0, 2, 0, // Bottom rear face
                4, 0, 3, 0, 1, 0 // Bottom front face
        );

        MeshView pyramid = new MeshView(pyramidMesh);
        pyramid.setDrawMode(DrawMode.FILL);
        pyramid.setMaterial(whiteMaterial);
        pyramid.setTranslateY(-h);

        world.getChildren().addAll(pyramid);
    }

    private void buildScene() {
        root.getChildren().add(world);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case Z:
                resetPosition(event);
                break;
            case X:
                toggleVisible(event);
                break;
      
            case UP:
                moveUp(event);
                break;
            case DOWN:
                moveDown(event);
                break;
            case RIGHT:
                moveRight(event);
                break;
            case LEFT:
                moveLeft(event);
                break;
        default:
        	break;
        }
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged(this::onMouseDragged);
    }

    private void moveDown(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTy(cameraXform2.getTy() + 10.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRotateX(cameraXform.getRotateX() + 10.0 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTy(cameraXform2.getTy() + 1.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRotateX(cameraXform.getRotateX() + 2.0 * ALT_MULTIPLIER);
        } else if (event.isShiftDown()) {
            double z2 = camera.getTranslateZ();
            double newZ2 = z2 - 5.0 * SHIFT_MULTIPLIER;
            camera.setTranslateZ(newZ2);
        }
    }

    private void moveLeft(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTx(cameraXform2.getTx() - 10.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRotateY(cameraXform.getRotateY() + 10.0 * ALT_MULTIPLIER); // -
        } else if (event.isControlDown()) {
            cameraXform2.setTx(cameraXform2.getTx() - 1.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRotateY(cameraXform.getRotateY() + 2.0 * ALT_MULTIPLIER); // -
        }
    }

    private void moveRight(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + 10.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRotateY(cameraXform.getRotateY() - 10.0 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + 1.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRotateY(cameraXform.getRotateY() - 2.0 * ALT_MULTIPLIER);
        }
    }

    private void moveUp(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTy(cameraXform2.getTy() - 10.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRotateX(cameraXform.getRotateX() - 10.0 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTy(cameraXform2.getTy() - 1.0 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRotateX(cameraXform.getRotateX() - 2.0 * ALT_MULTIPLIER);
        } else if (event.isShiftDown()) {
            double z1 = camera.getTranslateZ();
            double newZ1 = z1 + 5.0 * SHIFT_MULTIPLIER;
            camera.setTranslateZ(newZ1);
        }
    }

    private void onMouseDragged(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();

        mousePosY = me.getSceneY();
        double mouseDeltaX = mousePosX - mouseOldX;
        double mouseDeltaY = mousePosY - mouseOldY;

        double modifier = 1.0;
        double modifierFactor = 0.1;

        if (me.isControlDown()) {
            modifier = 0.1;
        }
        if (me.isShiftDown()) {
            modifier = 10.0;
        }
        if (me.isPrimaryButtonDown()) {
            cameraXform.setRotateY(cameraXform.getRotateY() - mouseDeltaX * modifierFactor * modifier * 2.0); // +
            cameraXform.setRotateX(cameraXform.getRotateX() + mouseDeltaY * modifierFactor * modifier * 2.0); // -
        } else if (me.isSecondaryButtonDown()) {
            double z = camera.getTranslateZ();
            double newZ = z + mouseDeltaX * modifierFactor * modifier;
            camera.setTranslateZ(newZ);
        } else if (me.isMiddleButtonDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
            cameraXform2.setTy(cameraXform2.getTy() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
        }
    }

    private void resetPosition(KeyEvent event) {
        if (event.isShiftDown()) {
            cameraXform.setRotateY(0.0);
            cameraXform.setRotateX(0.0);
            camera.setTranslateZ(-DEPTH);
        }
        cameraXform2.setTx(0.0);
        cameraXform2.setTy(0.0);
    }

    private void toggleVisible(KeyEvent event) {
        if (event.isControlDown()) {
            if (axisGroup.isVisible()) {
                axisGroup.setVisible(false);
            } else {
                axisGroup.setVisible(true);
            }
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main()
     * serves only as fallback in case the application can not be launched through
     * deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores
     * main().
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }



}