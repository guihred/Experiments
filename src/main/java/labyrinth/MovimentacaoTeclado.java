package labyrinth;

import java.util.EnumSet;

import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class MovimentacaoTeclado implements EventHandler<KeyEvent> {
	/**
	 * 
	 */
	private final CommomLabyrinth labyrinth3dWallTexture;
	private PerspectiveCamera camera;
	private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 5.0;
	private final EnumSet<KeyCode> enumSet = EnumSet.noneOf(KeyCode.class);
	public MovimentacaoTeclado(CommomLabyrinth labyrinth3dWallTexture) {
		this.labyrinth3dWallTexture = labyrinth3dWallTexture;
		camera = labyrinth3dWallTexture.getCamera();
	}

	public void keyReleased(KeyEvent event) {
		KeyCode keycode = event.getCode();
		enumSet.remove(keycode);
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
		enumSet.add(keycode);
		// Step 2c: Add Zoom controls
		if (enumSet.contains(KeyCode.W)) {
			double sin = Math.sin(Math.toRadians(camera.getRotate()))
					* change;
			double cos = Math.cos(Math.toRadians(camera.getRotate()))
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
		if (enumSet.contains(KeyCode.S)) {
			double sin = Math.sin(Math.toRadians(camera.getRotate()))
					* change;
			double cos = Math.cos(Math.toRadians(camera.getRotate()))
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
		if (enumSet.contains(KeyCode.A)) {

			double sin = Math.sin(Math.toRadians(camera.getRotate() + 90)) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate() + 90)) * change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateX(camera.getTranslateX() + sin);
			}
			camera.setTranslateZ(camera.getTranslateZ() - cos);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateZ(camera.getTranslateZ() + cos);
			}
		}
		if (enumSet.contains(KeyCode.DOWN)) {
			camera.setTranslateY(camera.getTranslateY() + change);
		}
		if (enumSet.contains(KeyCode.D)) {
			double sin = Math.sin(Math.toRadians(camera.getRotate() - 90)) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate() - 90)) * change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateX(camera.getTranslateX() + sin);
			}
			camera.setTranslateZ(camera.getTranslateZ() - cos);
			if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
				camera.setTranslateZ(camera.getTranslateZ() + cos);
			}
		}
		if (enumSet.contains(KeyCode.UP)) {
			camera.setTranslateY(camera.getTranslateY() - change);
		}

		labyrinth3dWallTexture.endKeyboard();
	}
}