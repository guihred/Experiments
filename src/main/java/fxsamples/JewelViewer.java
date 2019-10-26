package fxsamples;

import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;
import utils.Xform;

public class JewelViewer extends Application {
    private static final Logger LOGGER = HasLogging.log();
    private static final Color JEWEL_COLOR = Color.BURLYWOOD;
    private static final Color LIGHT_COLOR = Color.rgb(125, 125, 125);
    private static final String ORIGINAL_FILENAME = "original.stl";
    private static final double MODEL_SCALE_FACTOR = 16;
    private static final double VIEWPORT_SIZE = 800;
    private static final double CAMERA_MODIFIER = 50.0;
    private static final double CAMERA_QUANTITY = 10.0;

    private static final Color AMBIENT_COLOR = Color.grayRgb(80);
    private PerspectiveCamera camera;

    private Group root;
    private Xform obj3d;

    @Override
    public void start(Stage primaryStage) {
        Group group = buildScene();
        group.setScaleX(2);
        group.setScaleY(2);
        group.setScaleZ(2);

        Scene scene = new Scene(group, VIEWPORT_SIZE, VIEWPORT_SIZE, true);
        scene.setFill(Color.grayRgb(10));
        addCamera(scene);
        scene.setOnMouseClicked(event -> {
            Node picked = event.getPickResult().getIntersectedNode();
            if (null != picked) {
                double scalar = MODEL_SCALE_FACTOR;
                if (picked.getScaleX() > MODEL_SCALE_FACTOR / 2) {
                    scalar = MODEL_SCALE_FACTOR / 2;
                }
                picked.setScaleX(scalar);
                picked.setScaleY(scalar);
                picked.setScaleZ(scalar);
            }
        });
        // End Step 2a
        // Step 2b: Add a Movement Keyboard Handler
        camera.setRotationAxis(Rotate.Y_AXIS);
        scene.setOnKeyPressed(this::handleKeyPressed);
        // End Step 2b-d
        initFileDragNDrop(scene);
        primaryStage.setTitle("Jewel Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private PerspectiveCamera addCamera(Scene scene) {
        camera = new PerspectiveCamera();
        LOGGER.trace("Near Clip: {}", camera.getNearClip());
        LOGGER.trace("Far Clip:  {}", camera.getFarClip());
        LOGGER.trace("FOV:       {}", camera.getFieldOfView());

        scene.setCamera(camera);
        return camera;
    }

    private Group buildScene() {
        loadMeshViews(ResourceFXUtils.toFile(ORIGINAL_FILENAME));
        return root;
    }

    private void handleKeyPressed(KeyEvent event) {
        // Add shift modifier to simulate "Running Speed"
        double change = event.isShiftDown() ? CAMERA_MODIFIER : CAMERA_QUANTITY;
        // What key did the user press?
        KeyCode keycode = event.getCode();
        // Step 2c: Add Zoom controls
        if (keycode == KeyCode.W) {
            obj3d.setRx(obj3d.getRx() - change);
        }
        if (keycode == KeyCode.S) {
            obj3d.setRx(obj3d.getRx() + change);
        }
        // Step 2d: Add Strafe controls
        if (keycode == KeyCode.Z) {
            obj3d.setRz(obj3d.getRz() - change);
        }
        if (keycode == KeyCode.X) {
            obj3d.setRz(obj3d.getRz() + change);
        }
        if (keycode == KeyCode.A) {
            obj3d.setRy(obj3d.getRy() - change);
        }
        if (keycode == KeyCode.D) {
            obj3d.setRy(obj3d.getRy() + change);
        }
    }

    private void initFileDragNDrop(Scene scene) {
        scene.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (db.hasFiles() || db.hasUrl()) {
                dragEvent.acceptTransferModes(TransferMode.LINK);
                return;
            }
            dragEvent.consume();
        });
        // Dropping over surface
        scene.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                if (!db.getFiles().isEmpty()) {
                    tryLoadMeshViews(db);
                }
            } else {
                // audio file from some host or jar
                tryLoadMeshViews(db.getUrl());
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
    }

    private void loadMeshViews(File file) {
        Mesh mesh = ResourceFXUtils.importStlMesh(file);
        MeshView meshViews1 = new MeshView(mesh);
        meshViews1.setScaleX(MODEL_SCALE_FACTOR);
        meshViews1.setScaleY(MODEL_SCALE_FACTOR);
        meshViews1.setScaleZ(MODEL_SCALE_FACTOR);
        PhongMaterial sample = new PhongMaterial(JEWEL_COLOR);
        sample.setSpecularColor(LIGHT_COLOR);
        sample.setSpecularPower(16);
        meshViews1.setMaterial(sample);
        obj3d = new Xform(meshViews1);
        obj3d.setTx(VIEWPORT_SIZE / 2);
        obj3d.setTy(VIEWPORT_SIZE / 2);
        if (root == null) {
            root = new Group(obj3d);
        } else {
            root.getChildren().clear();
            root.getChildren().add(obj3d);
        }
        PointLight pointLight = new PointLight(LIGHT_COLOR);
        Rotate e = new Rotate();
        e.setAxis(Rotate.Y_AXIS);
        e.setPivotX(VIEWPORT_SIZE / 4);
        e.setPivotY(VIEWPORT_SIZE / 4);
        pointLight.getTransforms().add(e);
        pointLight.setTranslateX(VIEWPORT_SIZE / 4);
        pointLight.setTranslateY(VIEWPORT_SIZE / 4);
        root.getChildren().add(pointLight);
        root.getChildren().add(new AmbientLight(AMBIENT_COLOR));

    }

    private void tryLoadMeshViews(Dragboard db) {
        File filePath = db.getFiles().get(0);
        loadMeshViews(filePath);
    }

    private void tryLoadMeshViews(String url) {
        RunnableEx.run(() -> loadMeshViews(new File(new URL(url).getFile())));
    }

    public static MeshView loadMeshViews() {
        return new MeshView(ResourceFXUtils.importStlMesh(ORIGINAL_FILENAME));
    }

    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}