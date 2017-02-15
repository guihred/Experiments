package labyrinth;

import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class MovimentacaoTeclado implements EventHandler<KeyEvent> {
	/**
	 * 
	 */
	private final CommomLabyrinth labyrinth3dWallTexture;
	private PerspectiveCamera camera;
	private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 5.0;

	public MovimentacaoTeclado(CommomLabyrinth labyrinth3dWallTexture) {
		this.labyrinth3dWallTexture = labyrinth3dWallTexture;
		camera = labyrinth3dWallTexture.getCamera();
	}

	@Override
	public void handle(KeyEvent event) {
		double change = CAMERA_QUANTITY;
		// Add shift modifier to simulate "Running Speed"
		if (event.isShiftDown()) {
			change = CAMERA_MODIFIER;
		}
		// What key did the user press?
		KeyCode keycode = event.getCode();
		// Step 2c: Add Zoom controls
		if (keycode == KeyCode.W) {
			double sin = Math.sin(camera.getRotate() * Math.PI / 180)
					* change;
			double cos = Math.cos(camera.getRotate() * Math.PI / 180)
					* change;

			camera.setTranslateX(camera.getTranslateX() + sin);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateX(camera.getTranslateX() - sin);
			}
			camera.setTranslateZ(camera.getTranslateZ() + cos);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateZ(camera.getTranslateZ() - cos);
			}
		}
		if (keycode == KeyCode.S) {
			double sin = Math.sin(camera.getRotate() * Math.PI / 180)
					* change;
			double cos = Math.cos(camera.getRotate() * Math.PI / 180)
					* change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateX(camera.getTranslateX() + sin);
			}
			camera.setTranslateZ(camera.getTranslateZ() - cos);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateZ(camera.getTranslateZ() + cos);
			}
		}
		// Step 2d: Add Strafe controls
		if (keycode == KeyCode.A) {
			camera.setRotationAxis(Rotate.Y_AXIS);
			camera.setRotate(camera.getRotate() - change);
		}
		if (keycode == KeyCode.DOWN) {
			camera.setTranslateY(camera.getTranslateY() + change);
		}
		if (keycode == KeyCode.D) {
			camera.setRotationAxis(Rotate.Y_AXIS);
			camera.setRotate(camera.getRotate() + change);
		}
		if (keycode == KeyCode.UP) {
			camera.setTranslateY(camera.getTranslateY() - change);
		}

		labyrinth3dWallTexture.endKeyboard();
	}
}