package fxpro.ch07;

import java.util.ArrayList;
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
import utils.Axis;
import utils.CommonsFX;

public class Chart3dDemo extends Application {

    // size of graph
    private int size = 400;
    // variables for mouse interaction

    @Override
    public void start(Stage primaryStage) {
        Group cube = createCube(size);
        StackPane root = new StackPane();
        root.getChildren().add(cube);
        // perlin noise
        float[][] noiseArray = createNoise(size);
        // mesh
        TriangleMesh mesh = new TriangleMesh();
        // create points for x/z
        float amplification = 100; // amplification of noise
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                mesh.getPoints().addAll(x, noiseArray[x][z] * amplification, z);
            }
        }

        // texture
        int length = size;
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

        // faces
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

        // material
        Image diffuseMap = CommonsFX.createImage(size, noiseArray);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(diffuseMap);
        material.setSpecularColor(Color.WHITE);

        // mesh view
        MeshView meshView = new MeshView(mesh);
        meshView.setTranslateX(-0.5 * size);
        meshView.setTranslateZ(-0.5 * size);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);
        meshView.setDrawMode(DrawMode.FILL);
        meshView.setDepthTest(DepthTest.ENABLE);

        cube.getChildren().addAll(meshView);

        // testing / debugging stuff: show diffuse map on chart
        ImageView iv = new ImageView(diffuseMap);
        iv.setTranslateX(-0.5 * size);
        iv.setTranslateY(-0.10 * size);
        iv.setRotate(90);
        iv.setRotationAxis(new Point3D(1, 0, 0));
        cube.getChildren().add(iv);

        // scene
        Scene scene = new Scene(root, 1600, 900, true, SceneAntialiasing.BALANCED);
        scene.setCamera(new PerspectiveCamera());

        CommonsFX.setSpinnable(cube, scene);
        CommonsFX.makeZoomable(root);

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    /**
     * Create axis walls
     * 
     * @param size1
     * @return
     */
    private Group createCube(double size1) {

        Group cube = new Group();

        // size of the cube
        Color color = Color.DARKCYAN;

        List<Axis> cubeFaces = new ArrayList<>();
        Axis r;

        // back face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.5 * 1, 1.0));
        r.setTranslateX(-0.5 * size1);
        r.setTranslateY(-0.5 * size1);
        r.setTranslateZ(0.5 * size1);

        cubeFaces.add(r);

        // bottom face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.4 * 1, 1.0));
        r.setTranslateX(-0.5 * size1);
        r.setTranslateY(0);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);

        cubeFaces.add(r);

        // right face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.3 * 1, 1.0));
        r.setTranslateX(-1 * size1);
        r.setTranslateY(-0.5 * size1);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);

        // cubeFaces.add( r);

        // left face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.2 * 1, 1.0));
        r.setTranslateX(0);
        r.setTranslateY(-0.5 * size1);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);

        cubeFaces.add(r);

        // top face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.1 * 1, 1.0));
        r.setTranslateX(-0.5 * size1);
        r.setTranslateY(-1 * size1);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);

        // cubeFaces.add( r);

        // front face
        r = new Axis(size1);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.1 * 1, 1.0));
        r.setTranslateX(-0.5 * size1);
        r.setTranslateY(-0.5 * size1);
        r.setTranslateZ(-0.5 * size1);

        // cubeFaces.add( r);

        cube.getChildren().addAll(cubeFaces);

        return cube;
    }

    /**
     * Create an array of the given size with values of perlin noise
     * 
     * @param size1
     * @return
     */
    private static float[][] createNoise(int size1) {
        float[][] noiseArray = new float[size1][size1];

        for (int x = 0; x < size1; x++) {
            for (int y = 0; y < size1; y++) {

                double frequency = 10.0 / size1;

                double noise = ImprovedNoise.noise(x * frequency, y * frequency, 0);

                noiseArray[x][y] = (float) noise;
            }
        }

        return noiseArray;

    }


    public static void main(String[] args) {
        launch(args);
    }

}