package fxsamples;
import java.io.File;

import javafx.application.Application;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

public class JewelViewer extends Application {


	private static final double MODEL_SCALE_FACTOR = 0.5;
	private static final double MODEL_X_OFFSET = 0; // standard
	private static final double MODEL_Y_OFFSET = 0; // standard

	private static final int VIEWPORT_SIZE = 800;

	private static final Color lightColor = Color.rgb(125, 125, 125);
	private static final Color jewelColor = Color.BURLYWOOD;

	private Group root;
	private PointLight pointLight;

	private static final String MESH_FILENAME = "C:\\Users\\Guilherme\\workspace\\OiJava3D\\Minotaur.stl";
	static MeshView loadMeshViews() {
		File file = new File(MESH_FILENAME);
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(file);
		Mesh mesh = importer.getImport();

		return new MeshView(mesh);
	}

	private Group buildScene() {
		MeshView meshViews = loadMeshViews();
		meshViews.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
		meshViews.setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
		meshViews.setTranslateZ(VIEWPORT_SIZE / 2);
		meshViews.setScaleX(MODEL_SCALE_FACTOR);
		meshViews.setScaleY(MODEL_SCALE_FACTOR);
		meshViews.setScaleZ(MODEL_SCALE_FACTOR);

		PhongMaterial sample = new PhongMaterial(jewelColor);
		sample.setSpecularColor(lightColor);
		sample.setSpecularPower(16);
		meshViews.setMaterial(sample);

		meshViews.getTransforms().setAll(new Rotate(0, Rotate.Z_AXIS),
					new Rotate(-90, Rotate.X_AXIS));

		pointLight = new PointLight(lightColor);
		pointLight.setTranslateX(VIEWPORT_SIZE * 3 / 4);
		pointLight.setTranslateY(VIEWPORT_SIZE / 2);
		pointLight.setTranslateZ(VIEWPORT_SIZE / 2);

		Color ambientColor = Color.rgb(80, 80, 80, 0);
		AmbientLight ambient = new AmbientLight(ambientColor);

		root = new Group(meshViews);
		root.getChildren().add(pointLight);
		root.getChildren().add(ambient);

		return root;
	}

	private PerspectiveCamera camera;

	private PerspectiveCamera addCamera(Scene scene) {
		camera = new PerspectiveCamera();
		System.out.println("Near Clip: " + camera.getNearClip());
		System.out.println("Far Clip:  " + camera.getFarClip());
		System.out.println("FOV:       " + camera.getFieldOfView());

		scene.setCamera(camera);
		return camera;
	}

	private final double cameraQuantity = 10.0;
	private final double cameraModifier = 50.0;

	@Override
	public void start(Stage primaryStage) {
		Group group = buildScene();
		group.setScaleX(2);
		group.setScaleY(2);
		group.setScaleZ(2);
		group.setTranslateX(50);
		group.setTranslateY(50);

		Scene scene = new Scene(group, VIEWPORT_SIZE, VIEWPORT_SIZE, true);
		scene.setFill(Color.rgb(10, 10, 40));
		addCamera(scene);
		scene.setOnMouseClicked(event -> {
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
		});
		// End Step 2a
		// Step 2b: Add a Movement Keyboard Handler
		scene.setOnKeyPressed(event -> {
			double change = cameraQuantity;
			// Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = cameraModifier;
			}
			// What key did the user press?
			KeyCode keycode = event.getCode();
			// Step 2c: Add Zoom controls
			if (keycode == KeyCode.W) {
				camera.setTranslateZ(camera.getTranslateZ() + change);
			}
			if (keycode == KeyCode.S) {
				camera.setTranslateZ(camera.getTranslateZ() - change);
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.A) {
				camera.setRotate(camera.getRotate() - 1);
			}
			camera.setRotationAxis(Rotate.Y_AXIS);
			if (keycode == KeyCode.D) {
				camera.setRotate(camera.getRotate() + 1);
			}
		});
		// End Step 2b-d

		primaryStage.setTitle("Jewel Viewer");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		System.setProperty("prism.dirtyopts", "false");
		launch(args);
	}
}