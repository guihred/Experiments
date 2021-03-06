package fxsamples;

import java.util.Random;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Shapes3DApp extends Application {
    private static final int MAX_BYTE = 256;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
    private final Random rnd = new Random();

    /**
     * Create image with random noise
     */
    public Image createImage(int size) {
        WritableImage wr = new WritableImage(size, size);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int r = rnd.nextInt(MAX_BYTE);
                int g = rnd.nextInt(256);
                int b = rnd.nextInt(256);
                Color color = Color.rgb(r, g, b);
                pw.setColor(x, y, color);
            }
        }
        return wr;
    }

    @Override
    public void start(Stage primaryStage) {
        // cube
        Group group = new Group();
        // size of the cube
        group.getTransforms().addAll(rotateX, rotateY);
        final int imageSize = 500;
        Image diffuseMap = createImage(imageSize);
        ImageView iv = new ImageView(diffuseMap);
        iv.setRotationAxis(Rotate.X_AXIS);
        final int straightAngle = 90;
        iv.setRotate(straightAngle);
        iv.setTranslateX(-diffuseMap.getWidth() / 2);
        iv.setTranslateY(-diffuseMap.getHeight() / 5);
        group.getChildren().add(iv);
        // create material out of the noise image
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(diffuseMap);
        // create box with noise diffuse map
        Box box = new Box(100, 100, 100);
        box.setMaterial(material);
        group.getChildren().add(box);
        // create pyramid with diffuse map
        // Height
        final float h = 150;
        // Side
        final float s = 150;
        float hs = s / 2;
        // coordinates of the mapped image
        float x0 = 0.0F;
        float y0 = 0.0F;
        float x1 = 1.0F;
        float y1 = 1.0F;
        TriangleMesh pyramidMesh = new TriangleMesh();
        pyramidMesh.getPoints().addAll(
            // A 0 Top of Pyramid
            0.0F, 0.0F, 0.0F,
            // B 1
            hs, h, -hs,
            // C 2
            hs, h, hs,
            // D 3
            -hs, h, hs,
            // E 4
            -hs, h, -hs);
        pyramidMesh.getTexCoords().addAll(
            // 0
            x0, y0,
            // 1
            x0, y1,
            // 2
            x1, y0,
            // 3
            x1, y1);
        // index of point, index of texture, index
        pyramidMesh.getFaces().addAll(
            // of point, index of texture, index of
            // point, index of texture
            // ABC (counter clockwise)
            0, 0, 1, 1, 2, 3,
            // ACD (counter clockwise)
            0, 0, 2, 1, 3, 3,
            // ADE (counter clockwise)
            0, 0, 3, 1, 4, 3,
            // AEB (counter clockwise)
            0, 0, 4, 1, 1, 3,
            // EDC (Bottom first triangle clock wise)
            4, 0, 3, 1, 2, 3,
            // CBE (Bottom second triangle clock wise)
            2, 0, 1, 1, 4, 3);
        MeshView pyramid = new MeshView();
        pyramid.setMesh(pyramidMesh);
        pyramid.setDrawMode(DrawMode.FILL);
        pyramid.setTranslateY(-imageSize / 2);
        // apply material
        pyramid.setMaterial(material);
        group.getChildren().add(pyramid);
        // scene
        StackPane root = new StackPane();
        root.getChildren().add(group);
        Scene scene = new Scene(root, imageSize * 2, imageSize * 2, true, SceneAntialiasing.BALANCED);
        scene.setCamera(new PerspectiveCamera());
        // interaction listeners
        scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });
        scene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}