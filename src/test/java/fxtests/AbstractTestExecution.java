package fxtests;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.lang.SystemUtils;
import org.assertj.core.api.exception.RuntimeIOException;
import org.testfx.framework.junit.ApplicationTest;
import utils.ConsumerEx;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public abstract class AbstractTestExecution extends ApplicationTest implements HasLogging {
	protected Stage currentStage;
	protected boolean isLinux = SystemUtils.IS_OS_LINUX;

	@Override
	public void start(Stage stage) throws Exception {
		ResourceFXUtils.initializeFX();
		currentStage = stage;
		currentStage.setX(0);
		currentStage.setY(0);
	}

	protected <T extends Application> T show(Class<T> c) {
		try {
			T newInstance = c.newInstance();
			interactNoWait(RunnableEx.makeRunnable(() -> newInstance.start(currentStage)));
            return newInstance;
		} catch (Exception e) {
            throw new RuntimeIOException(String.format("ERRO IN %s", c), e);
		}
	}
    protected <T extends Application> void show(T application) {
		try {
			interactNoWait(RunnableEx.makeRunnable(() -> application.start(currentStage)));
		} catch (Exception e) {
			getLogger().error(String.format("ERRO IN %s", application), e);
		}
	}

	protected void tryClickButtons() {
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
	}

}
