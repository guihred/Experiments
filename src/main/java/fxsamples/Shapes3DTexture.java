package fxsamples;
import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Point3D;
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
public class Shapes3DTexture extends Application {
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
	public Image createImage(double size) {
		int width = (int) size;
		int height = (int) size;
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256),
						rnd.nextInt(256));
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

		final double size = 400;
		group.getTransforms().addAll(rotateX, rotateY);
		Image diffuseMap = createImage(size);
		// show noise image

		ImageView iv = new ImageView(diffuseMap);
		iv.setTranslateX(-size / 2);
		iv.setTranslateY(-size / 5);
		iv.setRotate(90);
		iv.setRotationAxis(new Point3D(1, 0, 0));
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
		TriangleMesh pyramidMesh = new TriangleMesh();
		pyramidMesh.getTexCoords().addAll(1, 1, 1, 0, 0, 1, 0, 0);
		// Point 0 - Top
		pyramidMesh.getPoints().addAll(0, 0, 0,
				// Point 1 - Front
				0, h, -s / 2,
				// Point 2 - Left
				-s / 2, h, 0,
				// Point 3 - Back
				s / 2, h, 0,
				// Point 4 - Right
				0, h, s / 2
				);
		// Front left face
		pyramidMesh.getFaces().addAll(0, 0, 2, 0, 1, 0,
				// Front right face
				0, 0, 1, 0, 3, 0,
				// Back right face
				0, 0, 3, 0, 4, 0,
				// Back left face
				0, 0, 4, 0, 2, 0,
				// Bottom rear face
				4, 0, 1, 0, 2, 0,
				// Bottom front face
				4, 0, 3, 0, 1, 0
				);
		MeshView pyramid = new MeshView(pyramidMesh);
		pyramid.setDrawMode(DrawMode.FILL);
		pyramid.setTranslateY(-500 / 2.);
		// apply material
		pyramid.setMaterial(material);
		group.getChildren().add(pyramid);
		// scene
		StackPane root = new StackPane();
		root.getChildren().add(group);
		Scene scene = new Scene(root, 1000, 1000, true,
				SceneAntialiasing.BALANCED);
		scene.setCamera(new PerspectiveCamera());
		// interaction listeners
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
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}