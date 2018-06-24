
package ml;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Chart3dGraph extends Application {

    // size of graph
    private int size = 400;

    // variables for mouse interaction
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        // create axis walls
        Group cube = createCube(size);
        // initial cube rotation
        cube.getTransforms().addAll(rotateX, rotateY);
        // add objects to scene
        StackPane root = new StackPane();
        root.getChildren().add(cube);
        // perlin noise
        float[][] noiseArray = createPlane(size);
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
        int length = createTexture(mesh);

        // faces
        createFaces(mesh, length);

        // material
        Image diffuseMap = createImage(size, noiseArray);

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

        scene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });
        makeZoomable(root);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createFaces(TriangleMesh mesh, int length) {
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

    private int createTexture(TriangleMesh mesh) {
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
        return length;
    }

    /**
	 * Create texture for uv mapping
	 * 
	 * @param size1
	 * @param noise
	 * @return
	 */
	public Image createImage(double size1, float[][] noise) {
		int width = (int) size1;
		int height = (int) size1;

        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float value = noise[x][y];
                double gray = normalizeValue(value, -.5, .5, 0., 1.);
                gray = clamp(gray, 0, 1);
                Color color = Color.RED.interpolate(Color.YELLOW, gray);
                pw.setColor(x, y, color);
            }
        }

        return wr;

    }

    /**
     * Axis wall
     */
    public static class Axis extends Pane {

        Rectangle wall;

        public Axis(double size) {

            // wall
            // first the wall, then the lines => overlapping of lines over walls
            // works
            wall = new Rectangle(size, size);
            getChildren().add(wall);

            // grid
            double zTranslate = 0;
            double lineWidth = 1.0;
            Color gridColor = Color.WHITE;

            for (int y = 0; y <= size; y += size / 10) {

                Line line = new Line(0, 0, size, 0);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateY(y);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

            for (int x = 0; x <= size; x += size / 10) {

                Line line = new Line(0, 0, 0, size);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateX(x);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

        }

        public void setFill(Paint paint) {
            wall.setFill(paint);
        }

    }

    public void makeZoomable(StackPane control) {

        final double MAX_SCALE = 20.0;
        final double MIN_SCALE = 0.1;

        control.addEventFilter(ScrollEvent.ANY, event -> {

            double delta = 1.2;

            double scale = control.getScaleX();

            if (event.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }

            scale = clamp(scale, MIN_SCALE, MAX_SCALE);

            control.setScaleX(scale);
            control.setScaleY(scale);

            event.consume();

        });

    }

    /**
     * Create axis walls
     * 
     * @param length
     * @return
     */
    private Group createCube(int length) {
        Group cube = new Group();
        // size of the cube
        Color color = Color.DARKCYAN;
        List<Axis> cubeFaces = new ArrayList<>();
        Axis r;
        // back face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.5 * 1, 1.0));
        r.setTranslateX(-0.5 * length);
        r.setTranslateY(-0.5 * length);
        r.setTranslateZ(0.5 * length);
        cubeFaces.add(r);
        // bottom face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.4 * 1, 1.0));
        r.setTranslateX(-0.5 * length);
        r.setTranslateY(0);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);
        cubeFaces.add(r);
        // right face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.3 * 1, 1.0));
        r.setTranslateX(-1 * length);
        r.setTranslateY(-0.5 * length);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);
        // cubeFaces.add( r);
        // left face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.2 * 1, 1.0));
        r.setTranslateX(0);
        r.setTranslateY(-0.5 * length);
        r.setRotationAxis(Rotate.Y_AXIS);
        r.setRotate(90);
        cubeFaces.add(r);
        // top face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.1 * 1, 1.0));
        r.setTranslateX(-0.5 * length);
        r.setTranslateY(-1 * length);
        r.setRotationAxis(Rotate.X_AXIS);
        r.setRotate(90);
        // cubeFaces.add( r);
        // front face
        r = new Axis(length);
        r.setFill(color.deriveColor(0.0, 1.0, 1 - 0.1 * 1, 1.0));
        r.setTranslateX(-0.5 * length);
        r.setTranslateY(-0.5 * length);
        r.setTranslateZ(-0.5 * length);
        // cubeFaces.add( r);
        cube.getChildren().addAll(cubeFaces);
        return cube;
    }

    /**
	 * Create an array of the given size with values of perlin noise
	 * 
	 * @param size1
	 * @return
	 * @return
	 */

    /* T(°C) = (T(°F) - minF) × (size)/(maxF-minF) */

	private double convert(double f, double size1, double maxF, double minF) {
		return (f - minF) * size1 / (maxF - minF);
    }

	private float[][] createPlane(int size1) {
        DataframeML dataframeML = new DataframeML("california_housing_train.csv");
        DoubleSummaryStatistics lat = dataframeML.summary("latitude");
        DoubleSummaryStatistics lon = dataframeML.summary("longitude");
        DoubleSummaryStatistics pop = dataframeML.summary("population");
        int total = dataframeML.getSize();
		List<Double> xLatitude = dataframeML.crossFeature("x",
				d -> convert(d[0], size1 - 1, lat.getMax(), lat.getMin()),
                "latitude");
        List<Double> yLongitude = dataframeML.crossFeature("y",
				d -> convert(d[0], size1 - 1, lon.getMax(), lon.getMin()),
                "longitude");
        List<Double> z = dataframeML.crossFeature("z", d -> -convert(d[0], 5, pop.getMax(), 0), "population");

		float[][] noiseArray = new float[size1][size1];
        for (int i = 0; i < total; i++) {
            int x = xLatitude.get(i).intValue();
            int y = yLongitude.get(i).intValue();
			if (x > size1 || y > size1) {
                System.out.println("ERRRRROOOOOOOOOO");
                return noiseArray;
            }
            noiseArray[x][y] += z.get(i);
            // noiseArray[x.intValue()][y.intValue()] =
            // Float.min(noiseArray[x.intValue()][y.intValue()], -size);

        }
        return noiseArray;

    }
    public static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    public static double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }


    public static void main(String[] args) {
        launch(args);
    }

}