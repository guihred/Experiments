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

public class Shapes3DApp extends Application {

	private double mousePosX, mousePosY;
	private double mouseOldX, mouseOldY;
	private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
	private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

	@Override
	public void start(Stage primaryStage) {
		// cube
		Group group = new Group();
		// size of the cube
		double size = 400;
		group.getTransforms().addAll(rotateX, rotateY);

		Image diffuseMap = createImage(size);
		// show noise image
		ImageView iv = new ImageView(diffuseMap);
		iv.setTranslateX(-0.5 * size);
		iv.setTranslateY(-0.20 * size);
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
		float h = 150; // Height
		float s = 150; // Side
		float hs = s / 2;

		// coordinates of the mapped image
		float x0 = 0.0f;
		float y0 = 0.0f;
		float x1 = 1.0f;
		float y1 = 1.0f;

		TriangleMesh pyramidMesh = new TriangleMesh();
		pyramidMesh.getPoints().addAll( //
				0.0f, 0.0f, 0.0f, // A 0 Top of Pyramid
				hs, h, -hs, // B 1
				hs, h, hs, // C 2
				-hs, h, hs, // D 3
				-hs, h, -hs // E 4
				);
		pyramidMesh.getTexCoords().addAll( //
				x0, y0, // 0
				x0, y1, // 1
				x1, y0, // 2
				x1, y1 // 3
				);
		pyramidMesh.getFaces().addAll(// index of point, index of texture, index
										// of point, index of texture, index of
										// point, index of texture
				0, 0, 1, 1, 2, 3, // ABC (counter clockwise)
				0, 0, 2, 1, 3, 3, // ACD (counter clockwise)
				0, 0, 3, 1, 4, 3, // ADE (counter clockwise)
				0, 0, 4, 1, 1, 3, // AEB (counter clockwise)
				4, 0, 3, 1, 2, 3, // EDC (Bottom first triangle clock wise)
				2, 0, 1, 1, 4, 3 // CBE (Bottom second triangle clock wise)
				);

		MeshView pyramid = new MeshView();
		pyramid.setMesh(pyramidMesh);
		pyramid.setDrawMode(DrawMode.FILL);
		pyramid.setTranslateY(-250);
		// apply material
		// TODO: why is the diffuse map not displayed?
		pyramid.setMaterial(material);
		group.getChildren().add(pyramid);
		// scene
		StackPane root = new StackPane();
		root.getChildren().add(group);
		Scene scene = new Scene(root, 1600, 900, true,
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

	/**
	 * Create image with random noise
	 */
	public Image createImage(double size) {

		Random rnd = new Random();

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

	public static void main(String[] args) {
		Application.launch(args);
	}
}