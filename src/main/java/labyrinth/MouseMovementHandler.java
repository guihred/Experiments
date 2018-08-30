package labyrinth;

import java.awt.Robot;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MouseMovementHandler implements EventHandler<MouseEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MouseMovementHandler.class);
	private double mouseOldX;
	private double mousePosX;
	private Scene sc;
	private PerspectiveCamera camera;

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
            Platform.runLater(() -> moveMouseLeft(me));
		}
		if (mousePosX <= 5) {
			// Reached left edge of the screen
            Platform.runLater(() -> moveMouseRight(me, width));
		}

		double mouseDeltaX = mousePosX - mouseOldX;
		camera.setRotate(camera.getRotate() + mouseDeltaX * .5);
	}

    private void moveMouseRight(MouseEvent me, double width) {
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) width, (int) me.getY());
            mouseOldX = width;
            mousePosX = width;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private void moveMouseLeft(MouseEvent me) {
        try {
        	new Robot().mouseMove(1, (int) me.getY());
        	mouseOldX = 0;
        	mousePosX = 0;
        } catch (Exception e) {
        	LOGGER.error("", e);
        }
    }
}