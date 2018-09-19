package crypt;

import exercise.java8.RunnableEx;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import log.analyze.FunctionEx;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public abstract class AbstractTestExecution extends ApplicationTest {

    private Stage currentStage;
    private List<Class<? extends Application>> applications;
    @SafeVarargs
    public AbstractTestExecution(Class<? extends Application>... applications) {
        this.applications = Arrays.asList(applications);
    }

    @Override
    public void start(Stage stage) throws Exception {
        currentStage = stage;
    }

    @Test
    public void testLabyrinth2() throws Exception {
        List<? extends Application> apps = applications.stream()
                .map(FunctionEx.makeFunction(Class<? extends Application>::newInstance)).collect(Collectors.toList());
        for (int i = 0; i < applications.size(); i++) {

            int j = i;
            Platform.runLater(RunnableEx.makeRunnable(() -> apps.get(j).start(currentStage)));
            WaitForAsyncUtils.waitForFxEvents();
            execute();
        }
    }

    public abstract void execute();
}
