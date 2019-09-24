
package ml.graph;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.slf4j.Logger;
import utils.Axis;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class Chart3dGraph extends Application {

    private static final Logger LOG = HasLogging.log();

    // size of graph
    private static final int SIZE = 400;

    // variables for mouse interaction
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        // create axis walls
        Group cube = createCube(SIZE);
        // initial cube rotation
        cube.getTransforms().addAll(rotateX, rotateY);
        // add objects to scene
        StackPane root = new StackPane();
        root.getChildren().add(cube);
        // perlin noise
        float[][] noiseArray = createPlane(SIZE);
        // mesh
        TriangleMesh mesh = new TriangleMesh();
        // create points for x/z
        float amplification = 100; // amplification of noise
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                mesh.getPoints().addAll(x, noiseArray[x][z] * amplification, z);
            }
        }
        // texture
        int length = createTexture(mesh, SIZE);

        // faces
        createFaces(mesh, length);

        // material
        Image diffuseMap = ResourceFXUtils.createImage(SIZE, noiseArray);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(diffuseMap);
        material.setSpecularColor(Color.WHITE);

        // mesh view
        MeshView meshView = new MeshView(mesh);
        meshView.setTranslateZ(-SIZE / 2D);
        meshView.setTranslateX(-SIZE / 2D);
        meshView.setMaterial(material);
        meshView.setDrawMode(DrawMode.FILL);
        meshView.setCullFace(CullFace.NONE);
        meshView.setDepthTest(DepthTest.ENABLE);
        cube.getChildren().addAll(meshView);
        // testing / debugging stuff: show diffuse map on chart
        ImageView iv = new ImageView(diffuseMap);
        iv.setRotate(90);
        iv.setTranslateY(-SIZE / 10D);
        iv.setTranslateX(-SIZE / 2D);
        iv.setRotationAxis(new Point3D(1, 0, 0));
        cube.getChildren().add(iv);
        // scene
        final int height = 900;
        Scene scene = new Scene(root, SIZE * 4, height, true, SceneAntialiasing.BALANCED);
        scene.setCamera(new PerspectiveCamera());
        scene.setOnMousePressed(me -> {
            mouseOldY = me.getSceneY();
            mouseOldX = me.getSceneX();
        });
        scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });
        RotateUtils.makeZoomable(root);
        RotateUtils.setMovable(root);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Create an array of the given size with values of perlin noise
     * 
     * @param size1
     * @return
     * @return
     */

    /* T(°C) = (T(°F) - minF) × (size)/(maxF-minF) */

    private static double convert(double f, double size1, double maxF, double minF) {
        return (f - minF) * size1 / (maxF - minF);
    }

    /**
     * Create axis walls
     * 
     * @param length
     * @return
     */
    private static Group createCube(double length) {
        Group cube = new Group();
        // size of the cube
        Color color = Color.DARKCYAN;
        List<Axis> cubeFaces = new ArrayList<>();
        Axis r;
        // back face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 5 / 10D, 1.0));
        r.setTranslateX(-length / 2);
        r.setTranslateY(-length / 2);
        r.setTranslateZ(length / 2);
        cubeFaces.add(r);
        // bottom face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 6 / 10D, 1.0));
        r.setTranslateX(-length / 2);
        r.setTranslateY(0);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);
        cubeFaces.add(r);
        // left face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 8 / 10D, 1.0));
        r.setTranslateX(0);
        r.setTranslateY(-length / 2);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);
        cubeFaces.add(r);
        // top face
        // front face
        cube.getChildren().addAll(cubeFaces);
        return cube;
    }

    private static void createFaces(TriangleMesh mesh, int length) {
        for (int x = 0; x < length - 1; x++) {
            for (int z = 0; z < length - 1; z++) {
                int tl = x * length + z; // top-left
                int bl = x * length + z + 1; // bottom-left
                int tr = (x + 1) * length + z; // top-right
                int br = (x + 1) * length + z + 1; // bottom-right
                int offset = (x * (length - 1) + z) * 8 / 2; // div 2 because we have u AND v in the list
                // working
                mesh.getFaces().addAll(bl, offset + 1, tl, offset + 0, tr, offset + 2);
                mesh.getFaces().addAll(tr, offset + 2, br, offset + 3, bl, offset + 1);
            }
        }
    }

    private static float[][] createPlane(int size1) {
		DataframeML dataframeML = DataframeBuilder.build("california_housing_train.csv");
        DoubleSummaryStatistics lat = dataframeML.summary("latitude");
        DoubleSummaryStatistics lon = dataframeML.summary("longitude");
        DoubleSummaryStatistics pop = dataframeML.summary("population");
        int total = dataframeML.getSize();
		List<Double> xLatitude = DataframeUtils.crossFeature(dataframeML, "x",
            d -> convert(d[0], size1 - 1., lat.getMax(), lat.getMin()), "latitude");
		List<Double> yLongitude = DataframeUtils.crossFeature(dataframeML, "y",
            d -> convert(d[0], size1 - 1., lon.getMax(), lon.getMin()), "longitude");
		List<Double> z = DataframeUtils.crossFeature(dataframeML, "z", d -> -convert(d[0], 5, pop.getMax(), 0),
				"population");

        float[][] noiseArray = new float[size1][size1];
        for (int i = 0; i < total; i++) {
            int x = xLatitude.get(i).intValue();
            int y = yLongitude.get(i).intValue();
            if (x > size1 || y > size1) {
                LOG.info("ERRRRROOOOOOOOOO");
                return noiseArray;
            }
            noiseArray[x][y] += z.get(i);

        }
        return noiseArray;

    }

    private static int createTexture(TriangleMesh mesh, int size1) {
        int length = size1;
        float total = length;
        for (float x = 0; x < length - 1; x++) {
            for (float y = 0; y < length - 1; y++) {
                float x0 = x / total;
                float y0 = y / total;
                float x1 = (x + 1) / total;
                float y1 = (y + 1) / total;
                mesh.getTexCoords().addAll( //
                    x0, y0, // 0, top-left
                    x0, y1, // 1, bottom-left
                    x1, y1, // 2, top-right
                    x1, y1 // 3, bottom-right
                );

            }
        }
        return length;
    }

}