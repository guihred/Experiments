package utils.fx;

import java.awt.Robot;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ex.HasLogging;

public final class MouseInScreenHandler implements EventHandler<MouseEvent> {
    private static final Logger LOG = HasLogging.log();
	private double mouseOldX;
	private double mousePosX;
    private PerspectiveCamera camera;
	private Scene sc;

	public MouseInScreenHandler(Scene sc, PerspectiveCamera camera) {
		this.sc = sc;
		this.camera = camera;
	}

	@Override
	public void handle(MouseEvent me) {
		mouseOldX = mousePosX;
		mousePosX = me.getX();
		double width = sc.getWidth();
		if ((int) mousePosX == (int) width - 1) {
			// Reached right edge of the screen
            CommonsFX.runInPlatform(() -> moveMouseLeft(me));
		}
		if (mousePosX <= 5) {
			// Reached left edge of the screen
            CommonsFX.runInPlatform(() -> moveMouseRight(me, width));
		}

		double mouseDeltaX = mousePosX - mouseOldX;
        camera.setRotate(camera.getRotate() + mouseDeltaX / 2);
	}

    private void moveMouseLeft(MouseEvent me) {
        try {
        	Robot robot = new Robot();
        	robot.mouseMove(1, (int) me.getY());
            mousePosX = 0;
        	mouseOldX = 0;
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void moveMouseRight(MouseEvent me, double width) {
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) width, (int) me.getY());
            mouseOldX = width;
            mousePosX = width;
        } catch (Exception e) {
            LOG.error("", e);
        }
    }
}