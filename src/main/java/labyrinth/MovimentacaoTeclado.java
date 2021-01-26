package labyrinth;

import java.util.EnumSet;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class MovimentacaoTeclado implements EventHandler<KeyEvent> {
    private static final int STRAIGHT_ANGLE = 90;
    private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 5.0;
	private final CommomLabyrinth labyrinth3dWallTexture;
	private PerspectiveCamera camera;
	private final EnumSet<KeyCode> keysPressed = EnumSet.noneOf(KeyCode.class);
	public MovimentacaoTeclado(CommomLabyrinth labyrinth3dWallTexture) {
		this.labyrinth3dWallTexture = labyrinth3dWallTexture;
		camera = labyrinth3dWallTexture.getCamera();
	}

	@Override
	public void handle(KeyEvent event) {
		double change = CAMERA_QUANTITY;
		if (event.isShiftDown()) {
			change = CAMERA_MODIFIER;
		}
		KeyCode keycode = event.getCode();
		keysPressed.add(keycode);
		if (keysPressed.contains(KeyCode.W)) {
            moveForward(change);
		}
		if (keysPressed.contains(KeyCode.S)) {
            moveBackward(change);
		}
		// Step 2d: Add Strafe controls
		if (keysPressed.contains(KeyCode.A)) {

            moveLeft(change);
		}
		if (keysPressed.contains(KeyCode.DOWN)) {
			camera.setTranslateY(camera.getTranslateY() + change);
		}
		if (keysPressed.contains(KeyCode.D)) {
            moveRight(change);
		}
		if (keysPressed.contains(KeyCode.UP)) {
			camera.setTranslateY(camera.getTranslateY() - change);
		}

		labyrinth3dWallTexture.endKeyboard();
	}

    public void keyReleased(KeyEvent event) {
        KeyCode keycode = event.getCode();
        keysPressed.remove(keycode);
    }

    private void moveBackward(double change) {
        double sin = Math.sin(Math.toRadians(camera.getRotate())) * change;
        double cos = Math.cos(Math.toRadians(camera.getRotate())) * change;

        camera.setTranslateX(camera.getTranslateX() - sin);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
            camera.setTranslateX(camera.getTranslateX() + sin);
        }
        camera.setTranslateZ(camera.getTranslateZ() - cos);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
            camera.setTranslateZ(camera.getTranslateZ() + cos);
        }
    }

    private void moveForward(double change) {
        double sin = Math.sin(Math.toRadians(camera.getRotate())) * change;
        double cos = Math.cos(Math.toRadians(camera.getRotate())) * change;

        camera.setTranslateX(camera.getTranslateX() + sin);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
            camera.setTranslateX(camera.getTranslateX() - sin);
        }
        camera.setTranslateZ(camera.getTranslateZ() + cos);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
            camera.setTranslateZ(camera.getTranslateZ() - cos);
        }
    }

    private void moveLeft(double change) {
        double sin = Math.sin(Math.toRadians(camera.getRotate() + STRAIGHT_ANGLE)) * change;
        double cos = Math.cos(Math.toRadians(camera.getRotate() + STRAIGHT_ANGLE)) * change;

        camera.setTranslateX(camera.getTranslateX() - sin);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
        	camera.setTranslateX(camera.getTranslateX() + sin);
        }
        camera.setTranslateZ(camera.getTranslateZ() - cos);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
        	camera.setTranslateZ(camera.getTranslateZ() + cos);
        }
    }

	private void moveRight(double change) {
        double sin = Math.sin(Math.toRadians(camera.getRotate() - STRAIGHT_ANGLE)) * change;
        double cos = Math.cos(Math.toRadians(camera.getRotate() - STRAIGHT_ANGLE)) * change;

        camera.setTranslateX(camera.getTranslateX() - sin);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
        	camera.setTranslateX(camera.getTranslateX() + sin);
        }
        camera.setTranslateZ(camera.getTranslateZ() - cos);
        if (labyrinth3dWallTexture.checkColision(camera.getBoundsInParent())) {
        	camera.setTranslateZ(camera.getTranslateZ() + cos);
        }
    }
}