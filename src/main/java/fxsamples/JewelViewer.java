package fxsamples;
import java.io.File;
import java.net.MalformedURLException;
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
import org.slf4j.LoggerFactory;
import simplebuilder.ResourceFXUtils;

public class JewelViewer extends Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(JewelViewer.class);
	private static final Color JEWEL_COLOR = Color.BURLYWOOD;
	private static final Color LIGHT_COLOR = Color.rgb(125, 125, 125);
    public static final String ORIGINAL_FILENAME = ResourceFXUtils.toFullPath("original.stl");

	private static final double MODEL_SCALE_FACTOR = 4;

	private static final double MODEL_X_OFFSET = 0; // standard
	private static final double MODEL_Y_OFFSET = 0; // standard

    private static final double VIEWPORT_SIZE = 800;

	private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 10.0;

	private PerspectiveCamera camera;
	private PointLight pointLight;
	private Group root;

	private PerspectiveCamera addCamera(Scene scene) {
		camera = new PerspectiveCamera();
        LOGGER.info("Near Clip: {}", camera.getNearClip());
        LOGGER.info("Far Clip:  {}", camera.getFarClip());
        LOGGER.info("FOV:       {}", camera.getFieldOfView());

		scene.setCamera(camera);
		return camera;
	}

	private Group buildScene() {
		MeshView meshViews = loadMeshViews();
		meshViews.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
		meshViews.setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
		meshViews.setTranslateZ(VIEWPORT_SIZE / 2);
		meshViews.setScaleX(MODEL_SCALE_FACTOR);
		meshViews.setScaleY(MODEL_SCALE_FACTOR);
		meshViews.setScaleZ(MODEL_SCALE_FACTOR);
		PhongMaterial sample = new PhongMaterial(JEWEL_COLOR);
		sample.setSpecularColor(LIGHT_COLOR);
		sample.setSpecularPower(16);
		meshViews.setMaterial(sample);
		meshViews.getTransforms().setAll(new Rotate(0, Rotate.Z_AXIS),
					new Rotate(-90, Rotate.X_AXIS));
		pointLight = new PointLight(LIGHT_COLOR);
		pointLight.setTranslateY(VIEWPORT_SIZE / 2);
		pointLight.setTranslateZ(VIEWPORT_SIZE / 2);
        pointLight.setTranslateX(VIEWPORT_SIZE * 3 / 4);
		Color ambientColor = Color.rgb(80, 80, 80, 0);
		AmbientLight ambient = new AmbientLight(ambientColor);
		root = new Group(meshViews);
		root.getChildren().add(pointLight);
		root.getChildren().add(ambient);

		return root;
	}
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
		camera.setRotationAxis(Rotate.Y_AXIS);
		scene.setOnKeyPressed(this::handleKeyPressed);
		// End Step 2b-d
		initFileDragNDrop(scene);
		primaryStage.setTitle("Jewel Viewer");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleKeyPressed(KeyEvent event) {
		// Add shift modifier to simulate "Running Speed"
		double change = event.isShiftDown() ? CAMERA_MODIFIER : CAMERA_QUANTITY;
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
		if (keycode == KeyCode.D) {
			camera.setRotate(camera.getRotate() + 1);
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
		MeshView meshViews = new MeshView(mesh);
		meshViews.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
		meshViews.setTranslateY(VIEWPORT_SIZE / 2 + MODEL_Y_OFFSET);
		meshViews.setTranslateZ(VIEWPORT_SIZE / 2);
		meshViews.setScaleX(MODEL_SCALE_FACTOR);
		meshViews.setScaleY(MODEL_SCALE_FACTOR);
		meshViews.setScaleZ(MODEL_SCALE_FACTOR);

		PhongMaterial sample = new PhongMaterial(JEWEL_COLOR);
		sample.setSpecularColor(LIGHT_COLOR);
		sample.setSpecularPower(16);
		meshViews.setMaterial(sample);

		meshViews.getTransforms().setAll(new Rotate(0, Rotate.Z_AXIS), new Rotate(-90, Rotate.X_AXIS));

		pointLight = new PointLight(LIGHT_COLOR);
		pointLight.setTranslateX(VIEWPORT_SIZE * 3 / 4);
		pointLight.setTranslateY(VIEWPORT_SIZE / 2);
		pointLight.setTranslateZ(VIEWPORT_SIZE / 2);

		Color ambientColor = Color.rgb(80, 80, 80, 0);
		AmbientLight ambient = new AmbientLight(ambientColor);
		root.getChildren().clear();
		root.getChildren().add(pointLight);
		root.getChildren().add(ambient);

	}

	private void tryLoadMeshViews(String url) {
		try {
			loadMeshViews(new File(new URL(url).getFile()));
		} catch (MalformedURLException e) {
			LOGGER.error("", e);
		}
	}

	private void tryLoadMeshViews(Dragboard db) {
		File filePath = db.getFiles().get(0);
		loadMeshViews(filePath);
	}

	static MeshView loadMeshViews() {
        Mesh mesh = ResourceFXUtils.importStlMesh(ORIGINAL_FILENAME);
		return new MeshView(mesh);
	}

	public static void main(String[] args) {
		System.setProperty("prism.dirtyopts", "false");
		launch(args);
	}
}