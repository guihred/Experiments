package gaming.ex09;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Maze3DLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        GridPane root = new GridPane();
        camera.setNearClip(1. / 100);
        camera.setFarClip(1000.0);
        final Translate translate = new Translate(50, 0, 50);
        final Rotate rotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        camera.getTransforms().addAll(translate, rotate);

        Scene scene = new Scene(root, 500, 500, true, SceneAntialiasing.BALANCED);
		Maze3DModel.create(root);
        root.getTransforms().addAll(new Rotate(90, 0, 0, 0, Rotate.X_AXIS));

        handleKeyboard(scene, translate, rotate, camera);
        stage.setScene(scene);
        scene.setCamera(camera);
        stage.setWidth(500);
        stage.setHeight(500);
        stage.show();

    }

	private void handleKeyboard(Scene scene, Translate translate, Rotate rotate, PerspectiveCamera camera) {

        scene.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			private double mouseYold;

            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    double mouseYnew = event.getSceneY();
                    if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        double pitchRotate = rotate.getAngle() + (mouseYnew - mouseYold) / 1;
                        rotate.setAngle(pitchRotate);
                    }
                    mouseYold = mouseYnew;
                }
            }
        });

		scene.setOnKeyPressed((KeyEvent event) -> handleKeyPressed(scene, translate, rotate, camera, event));
    }

    private void handleKeyPressed(Scene scene, Translate translate, Rotate rotate, PerspectiveCamera camera,
			KeyEvent event) {
		int change = 1;
		KeyCode keycode = event.getCode();
		double changeX = 0;
		double changeZ = 0;
		if (keycode == KeyCode.W) {
            changeX = 5 * Math.sin(Math.toRadians(rotate.getAngle()));
            changeZ = 5 * Math.cos(Math.toRadians(rotate.getAngle()));
		}
		if (keycode == KeyCode.S) {
            changeX = -5 * Math.sin(Math.toRadians(rotate.getAngle()));
            changeZ = -5 * Math.cos(Math.toRadians(rotate.getAngle()));
		}
		if (keycode == KeyCode.UP) {
			translate.setY(translate.getY() + 5);
		}
		if (keycode == KeyCode.DOWN) {
			translate.setY(translate.getY() - 5);
		}
		translate.setZ(translate.getZ() + changeZ);

		if (scene.getRoot().getChildrenUnmodifiable().stream()
		        .filter(Parent.class::isInstance)
		        .map(Parent.class::cast)
		        .flatMap(p -> p.getChildrenUnmodifiable().stream())
		        .filter(Box.class::isInstance)
		        .anyMatch(s -> s.intersects(camera.getBoundsInParent()))) {
		    translate.setZ(translate.getZ() - changeZ);
		}
		translate.setX(translate.getX() + changeX);
		if (scene.getRoot().getChildrenUnmodifiable().stream()
		        .filter(Parent.class::isInstance)
		        .map(Parent.class::cast)
		        .flatMap(p -> p.getChildrenUnmodifiable().stream())
		        .filter(Box.class::isInstance)
		        .anyMatch(s -> s.intersects(camera.getBoundsInParent()))) {
		    translate.setX(translate.getX() - changeX);
		}

		if (keycode == KeyCode.A) {
		    rotate.setAngle(rotate.getAngle() - change);
		}
		if (keycode == KeyCode.D) {
		    rotate.setAngle(rotate.getAngle() + change);
		}
		if (keycode == KeyCode.SPACE) {
		    camera.setFieldOfView(camera.getFieldOfView() + change);
		}
		if (keycode == KeyCode.BACK_SPACE) {
		    camera.setFieldOfView(camera.getFieldOfView() - change);
		}
	}

    public static void main(String[] args) {
        launch(args);
    }
}
