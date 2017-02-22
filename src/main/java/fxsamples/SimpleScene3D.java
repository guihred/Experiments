package fxsamples;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
public class SimpleScene3D extends Application {
	private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 10.0;
	private static final double SCENE_WIDTH = 600;
	private static final double SCENE_HEIGHT = 600;
	private static final double CAMERA_Y_LIMIT = 15;
	private static final double ROTATE_MODIFIER = 25;
	private PerspectiveCamera camera;
	private double mouseXold;
	private double mouseYold;
	private EventHandler<? super MouseEvent> mouseHandler = event -> {
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
	};

	private void handleKeyPressed(KeyEvent event) {
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
			camera.setTranslateX(camera.getTranslateX() - change);
		}
		if (keycode == KeyCode.D) {
			camera.setTranslateX(camera.getTranslateX() + change);
		}
	}
	@Override
	public void start(Stage primaryStage) {
		// Step 1a: Build your Scene and Camera

		Group sceneRoot = new Group();
		Scene scene = new Scene(sceneRoot, SCENE_WIDTH, SCENE_HEIGHT);
		scene.setFill(Color.BLACK);
		camera = new PerspectiveCamera(true);
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setTranslateZ(-1000);
		scene.setCamera(camera);
		final Cylinder cylinder = new Cylinder(50, 100);
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);
		cylinder.setMaterial(blueMaterial);
		cylinder.setDrawMode(DrawMode.FILL);
		// Step 1c: Translate and Rotate primitive into position

		cylinder.setRotationAxis(Rotate.X_AXIS);
		cylinder.setRotate(45);
		cylinder.setTranslateZ(-200);
		// End Step 1c

		// Step 1d: Add and Transform more primitives

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);
		final Box cube = new Box(50, 50, 50);
		cube.setMaterial(greenMaterial);
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);
		final Sphere sphere = new Sphere(50);
		sphere.setMaterial(redMaterial);
		cube.setRotationAxis(Rotate.Y_AXIS);
		cube.setRotate(45);
		cube.setTranslateX(-150);
		cube.setTranslateY(-150);
		cube.setTranslateZ(150);
		sphere.setTranslateX(150);
		sphere.setTranslateY(150);
		sphere.setTranslateZ(-150);
		sceneRoot.getChildren().addAll(cylinder, cube, sphere);
		// End Step 1d

		// Step 1e: All Together Now: Grouped Primitives

		Group primitiveGroup = new Group(cylinder, cube, sphere);
		primitiveGroup.setRotationAxis(Rotate.Z_AXIS);
		// Rotate the Group as a whole
		primitiveGroup.setRotate(180);
		sceneRoot.getChildren().addAll(primitiveGroup);
		// End Step 1e

		// Step 2a: Primitive Picking for Primitives

		scene.setOnMouseClicked(mouseHandler);
		// End Step 2a

		// Step 2b: Add a Movement Keyboard Handler
		scene.setOnKeyPressed(this::handleKeyPressed);
		// End Step 2b-d

		// Step 3: Add a Camera Control Mouse Event handler

		Rotate xRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
		Rotate yRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
		camera.getTransforms().addAll(xRotate, yRotate);
		scene.addEventHandler(
				MouseEvent.ANY,
				event -> {
					if (event.getEventType() == MouseEvent.MOUSE_PRESSED
							|| event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
						double mouseXnew = event.getSceneX();
						double mouseYnew = event.getSceneY();
						if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
							double pitchRotate = xRotate.getAngle() + (mouseYnew - mouseYold) / ROTATE_MODIFIER;
							pitchRotate = pitchRotate > CAMERA_Y_LIMIT ? CAMERA_Y_LIMIT : pitchRotate;
							pitchRotate = pitchRotate < -CAMERA_Y_LIMIT ? -CAMERA_Y_LIMIT : pitchRotate;
							xRotate.setAngle(pitchRotate);
							double yawRotate = yRotate.getAngle() - (mouseXnew - mouseXold) / ROTATE_MODIFIER;
							yRotate.setAngle(yawRotate);
						}
						mouseXold = mouseXnew;
						mouseYold = mouseYnew;
					}
				});
		// End Step 3

		primaryStage.setTitle("SimpleScene3D");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}