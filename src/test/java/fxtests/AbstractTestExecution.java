package fxtests;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.apache.commons.lang.SystemUtils;
import org.testfx.api.FxRobotInterface;
import org.testfx.framework.junit.ApplicationTest;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public abstract class AbstractTestExecution extends ApplicationTest implements HasLogging {
	protected Stage currentStage;
	protected boolean isLinux = SystemUtils.IS_OS_LINUX;


	@Override
	public FxRobotInterface clickOn(Node node, MouseButton... buttons) {
		if (isLinux) {
			moveTo(node);

			return super.clickOn(buttons);
		}

		return super.clickOn(node, buttons);
	}

	@Override
	public FxRobotInterface clickOn(String node, MouseButton... buttons) {
		if (isLinux) {
			Node query = lookup(node).query();
			moveTo(query);
			return super.clickOn(buttons);
		}

		return super.clickOn(node, buttons);
	}

	@Override
	public FxRobotInterface doubleClickOn(Node node, MouseButton... buttons) {
		if (isLinux) {
			moveTo(node);
			return super.doubleClickOn(buttons);
		}
		return super.doubleClickOn(node, buttons);
	}

	@Override
	public FxRobotInterface moveTo(Node next) {
		if (isLinux) {
			if (next.getScene() != null && next.getScene().getWindow() != null) {
				next.getScene().getWindow().setX(0);
				next.getScene().getWindow().setY(0);
				double x2 = next.getScene().getX();
				Bounds local = next.getBoundsInParent();
				double x = -local.getWidth() / 2 - local.getMinX();
				Point2D offset = new Point2D(x + x2 / 2, 0);
				return super.moveTo(next, offset);
			}
		}
		return super.moveTo(next);
	}

	@Override
	public void start(Stage stage) throws Exception {
		ResourceFXUtils.initializeFX();
		currentStage = stage;
		currentStage.setX(0);
		currentStage.setY(0);
	}

	protected <T extends Application> void show(Class<T> c) {
		try {
			T newInstance = c.newInstance();
			interactNoWait(RunnableEx.makeRunnable(() -> newInstance.start(currentStage)));
		} catch (Exception e) {
			getLogger().error(String.format("ERRO IN %s", c), e);
		}
	}

}
