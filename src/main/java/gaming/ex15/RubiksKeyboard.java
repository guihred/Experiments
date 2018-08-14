package gaming.ex15;

import java.util.EnumSet;

import org.slf4j.Logger;

import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import simplebuilder.HasLogging;

public class RubiksKeyboard implements EventHandler<KeyEvent> , HasLogging{
	/**
	 * 
	 */
	private PerspectiveCamera camera;
	private static final double CAMERA_MODIFIER = 50.0;
	private static final double CAMERA_QUANTITY = 5.0;
	private final EnumSet<KeyCode> enumSet = EnumSet.noneOf(KeyCode.class);
	private RubiksCubeLauncher rubiksCubeLauncher;

	public RubiksKeyboard(PerspectiveCamera camera, RubiksCubeLauncher rubiksCubeLauncher) {
		this.camera = camera;
		this.rubiksCubeLauncher = rubiksCubeLauncher;
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
			double sin = Math.sin(Math.toRadians(camera.getRotate())) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate())) * change;

			camera.setTranslateX(camera.getTranslateX() + sin);
			camera.setTranslateZ(camera.getTranslateZ() + cos);
		}
		if (enumSet.contains(KeyCode.S)) {
			double sin = Math.sin(Math.toRadians(camera.getRotate())) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate())) * change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			camera.setTranslateZ(camera.getTranslateZ() - cos);
		}
		// Step 2d: Add Strafe controls
		if (enumSet.contains(KeyCode.A)) {

			double sin = Math.sin(Math.toRadians(camera.getRotate() + 90)) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate() + 90)) * change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			camera.setTranslateZ(camera.getTranslateZ() - cos);
		}
		if (enumSet.contains(KeyCode.DOWN)) {
			camera.setTranslateY(camera.getTranslateY() + change);
		}
		if (enumSet.contains(KeyCode.D)) {
			double sin = Math.sin(Math.toRadians(camera.getRotate() - 90)) * change;
			double cos = Math.cos(Math.toRadians(camera.getRotate() - 90)) * change;

			camera.setTranslateX(camera.getTranslateX() - sin);
			camera.setTranslateZ(camera.getTranslateZ() - cos);
		}
		if (enumSet.contains(KeyCode.UP)) {
			camera.setTranslateY(camera.getTranslateY() - change);
		}
		if (enumSet.contains(KeyCode.R)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.RIGHT, !event.isShiftDown());
		}
		if (enumSet.contains(KeyCode.L)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.LEFT, !event.isShiftDown());
		}
		if (enumSet.contains(KeyCode.U)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.UP, !event.isShiftDown());
		}
		if (enumSet.contains(KeyCode.D)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.DOWN, !event.isShiftDown());
		}
		if (enumSet.contains(KeyCode.B)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.BACK, !event.isShiftDown());
		}
		if (enumSet.contains(KeyCode.F)) {
			rubiksCubeLauncher.rotateCube(RubiksCubeFaces.FRONT, !event.isShiftDown());
		}

	}

	public static void main(String[] args) {
        Logger log = HasLogging.log();
		for (int i = 0; i < 9; i++) {
			int j = rotateClockWise(i);
			int k = rotateAntiClockWise(j);
            log.info("{}={}={}", i, j, k);
		}
	}

	private static int rotateAntiClockWise(int j) {
		return j % 3 * 3 + 2 - j / 3;
	}

	private static int rotateClockWise(int i) {
		return 6 - i % 3 * 3 + i / 3;
	}
}
