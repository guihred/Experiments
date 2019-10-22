package fxtests;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.apache.commons.lang.SystemUtils;
import org.assertj.core.api.exception.RuntimeIOException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.testfx.framework.junit.ApplicationTest;
import utils.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractTestExecution extends ApplicationTest implements HasLogging {
    protected Stage currentStage;
    protected boolean isLinux = SystemUtils.IS_OS_LINUX;
    private final Logger logger = HasLogging.super.getLogger();

    protected Random random = new Random();
	private Map<String, Object> initialStage;

    @Override
    public Logger getLogger() {
        return logger;
    }
    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage;
        initialStage = ClassReflectionUtils.getFieldMap(stage, Stage.class);
        currentStage.setX(0);
        currentStage.setY(0);
    }

    @Override
    public void stop() throws Exception {
        currentStage.close();
    }

    protected void moveRandom(int bound) {
        moveBy(randomNumber(bound), randomNumber(bound));
    }

    protected int randomNumber(int bound) {
        return random.nextInt(bound) - bound / 2;
    }

	protected <T extends Application> T show(Class<T> c) {
        try {
			resetStage();
            logger.info("SHOWING {}", c.getSimpleName());
            T newInstance = c.newInstance();
            interactNoWait(RunnableEx.make(() -> newInstance.start(currentStage)));
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeIOException(String.format("ERRO IN %s", c), e);
        }
    }

	protected <T extends Application> void show(T application) {
        interactNoWait(RunnableEx.make(() -> {
        	resetStage();
            logger.info("SHOWING {}", application.getClass().getSimpleName());
            application.start(currentStage);
        }, e -> logger.error(String.format("ERRO IN %s", application), e)));
    }

    protected void tryClickButtons() {
        lookup(".button").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
    }

    private void resetStage() {
        initialStage.forEach((f, v) -> ClassReflectionUtils.invoke(currentStage, f, v));
	}

    @SuppressWarnings("deprecation")
	protected static KeyCode[] typeText(String txt) {
		KeyCode[] values = KeyCode.values();
		return txt.chars().mapToObj(e -> Objects.toString((char) e).toUpperCase())
				.flatMap(s -> Stream.of(values).filter(v -> v.impl_getChar().equals(s))).toArray(KeyCode[]::new);
	}

}
