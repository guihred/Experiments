package labyrinth;

import java.awt.Robot;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

final class MouseMovementHandler implements EventHandler<MouseEvent> {
	private double mouseOldX;
	private double mousePosX;
	private Scene sc;
	PerspectiveCamera camera;

	public MouseMovementHandler(Scene sc, CommomLabyrinth labyrinth) {
		this.sc = sc;
		camera = labyrinth.getCamera();
	}

	@Override
	public void handle(MouseEvent me) {
		mouseOldX = mousePosX;
		mousePosX = me.getX();
		double width = sc.getWidth();
		if ((int) mousePosX == (int) width - 1) {
			// Reached right edge of the screen
			Platform.runLater(() -> {
				try {
					Robot robot = new Robot();
					robot.mouseMove(1, (int) me.getY());
					mouseOldX = 0;
					mousePosX = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		if (mousePosX <= 5) {
			// Reached left edge of the screen
			Platform.runLater(() -> {
				try {
					Robot robot = new Robot();
					robot.mouseMove((int) width, (int) me.getY());
					mouseOldX = width;
					mousePosX = width;
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		double mouseDeltaX = mousePosX - mouseOldX;
		camera.setRotate(camera.getRotate() + mouseDeltaX * .5);
	}
}