package fxsamples;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import utils.HasLogging;

public class TriangleMeshes extends Application implements HasLogging {
    private static final double SCENE_WIDTH = 600;
    private static final double SCENE_HEIGHT = 600;
    private double anchorAngleX;
    private double anchorAngleY;
    private double scenex;
    private double sceney;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    @Override
    public void start(Stage primaryStage) {
        // Step 1: Build your Scene and Camera
        Group sceneRoot = new Group();
        Scene scene = new Scene(sceneRoot, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setFill(Color.BLACK);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1. / 10);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-1000);
        scene.setCamera(camera);
        final int hypotenuse = 200;
        Group pyramid1 = buildPyramid(100, hypotenuse, Color.GOLDENROD, true, false);
        pyramid1.setTranslateX(-100);
        // Step 3b: Create and transform a Pyramid using DrawMode FILL
        Group pyramid2 = buildPyramid(100, hypotenuse, Color.GOLDENROD, true, true);
        // Since the pyramid is a group it can be translated and rotated like a
        // primitive
        pyramid2.setTranslateX(-100);
        pyramid2.setTranslateY(-100);
        pyramid2.setRotationAxis(Rotate.Z_AXIS);
        pyramid2.setRotate(180);
        // Step 3c: Add some more pyramids of a different color
        Group pyramid3 = buildPyramid(100, hypotenuse, Color.LAWNGREEN, true, true);
        pyramid3.setTranslateX(100);
        Group pyramid4 = buildPyramid(100, hypotenuse, Color.LAWNGREEN, true, false);
        pyramid4.setTranslateX(100);
        pyramid4.setTranslateY(-100);
        pyramid4.setRotationAxis(Rotate.Z_AXIS);
        pyramid4.setRotate(180);
        Group pyramidGroup = new Group(pyramid1, pyramid2, pyramid3, pyramid4);
        sceneRoot.getChildren().addAll(pyramidGroup);
        // Step 4a: Add a Mouse Handler for Rotations
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        pyramidGroup.getTransforms().addAll(xRotate, yRotate);
        // Use Binding so your rotation doesn't have to be recreated
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);
        // Start Tracking mouse movements only when a button is pressed
        scene.setOnMousePressed(event -> {
            scenex = event.getSceneX();
            sceney = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });
        // Angle calculation will only change when the button has been pressed
        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (scenex - event.getSceneY()));
            angleY.set(anchorAngleY + sceney - event.getSceneX());
        });
        // Step 4b: Add a Point light to show specular highlights
        PointLight light = new PointLight(Color.WHITE);
        sceneRoot.getChildren().add(light);
        light.setTranslateZ(-SCENE_WIDTH / 2);
        light.setTranslateY(-SCENE_HEIGHT / 2);
        primaryStage.setTitle("Triangle Meshes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Step 2a: Create a general Pyramid TriangleMesh building method with
    // height and hypotenuse
    private Group buildPyramid(float height, float hypotenuse, Color color, boolean ambient, boolean fill) {
        final TriangleMesh mesh = new TriangleMesh();
        // End Step 2a
        // Step 2b: Add 5 points, later we will build our faces from these
        // Point 0: Top of Pyramid
        mesh.getPoints().addAll(0, 0, 0,
            // Point 1: closest base point to
            0, height, -hypotenuse / 2,
            // camera
            // Point 2: leftmost base point to
            -hypotenuse / 2, height, 0,
            // camera
            // Point 3: farthest base point to
            hypotenuse / 2, height, 0,
            // camera
            // Point 4: rightmost base point to
            0, height, hypotenuse / 2
        // camera
        // End Step 2b
        );
        // Step 2c:
        // for now we'll just make an empty texCoordinate group
        mesh.getTexCoords().addAll(0, 0);
        // End Step 2c
        // Step 2d: Add the faces "winding" the points generally counter clock
        // wise
        // use dummy texCoords
        mesh.getFaces().addAll(
            // Vertical Faces "wind" counter clockwise
            0, 0, 2, 0, 1, 0,
            // Vertical Faces "wind" counter clockwise
            0, 0, 1, 0, 3, 0,
            // Vertical Faces "wind" counter clockwise
            0, 0, 3, 0, 4, 0,
            // Vertical Faces "wind" counter clockwise
            0, 0, 4, 0, 2, 0,
            // Base Triangle 1 "wind" clockwise because
            4, 0, 1, 0, 2, 0,
            // camera has rotated
            // Base Triangle 2 "wind" clockwise because
            4, 0, 3, 0, 1, 0
        // camera has rotated
        // End Step 2d
        );
        // Step 2e: Create a viewable MeshView to be added to the
        // scene
        // To add a TriangleMesh to a 3D scene you need a MeshView
        // container object
        MeshView meshView = new MeshView(mesh);
        // The MeshView allows you to control how the TriangleMesh is rendered
        // show lines only by default
        meshView.setDrawMode(DrawMode.LINE);
        // Removing culling to show back
        meshView.setCullFace(CullFace.BACK);
        // lines
        // End Step 2e
        // Step 2f: Add it to a group, this will be useful later
        Group pyramidGroup = new Group();
        pyramidGroup.getChildren().add(meshView);
        // End Step 2f
        // Step 2g: Customizing your Pyramid
        if (null != color) {
            PhongMaterial material = new PhongMaterial(color);
            meshView.setMaterial(material);
        }
        if (ambient) {
            AmbientLight light = new AmbientLight(Color.WHITE);
            light.getScope().add(meshView);
            pyramidGroup.getChildren().add(light);
        }
        if (fill) {
            meshView.setDrawMode(DrawMode.FILL);
        }
        // End Step 2g
        return pyramidGroup;
    }

    // End Step 1
    public static void main(String[] args) {
        launch(args);
    }
}