package crypt;

import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.Assert;
import simplebuilder.HasLogging;

public final class FXTesting implements HasLogging {
    JFXPanel jfxPanel = new JFXPanel();

    Map<Class<?>, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<>());

    private FXTesting() {
    }

    @SafeVarargs
    public static void testApps(Class<? extends Application>... applicationClasses)
            throws Throwable {
        new FXTesting().testApplications(Arrays.asList(applicationClasses));
    }
    private void testApplications(List<Class<? extends Application>> applicationClasses)
            throws Throwable {
        List<Object> testedApps = Collections.synchronizedList(new ArrayList<>());
        long currentTimeMillis = System.currentTimeMillis();
        for (Class<? extends Application> class1 : applicationClasses) {
            Platform.runLater(() -> {
                try {
                    getLogger().info("TESTING " + class1.getSimpleName());
                    Application newInstance = class1.newInstance();
                    Stage primaryStage = new Stage();
                    newInstance.start(primaryStage);
                    primaryStage.close();
                    getLogger().info("ENDED " + class1.getSimpleName());
                    testedApps.add(newInstance);
                } catch (Throwable e) {
                    getLogger().error("", e);
                    setClass(class1, e);
                }
            });
        }
        int size = testedApps.size();
        while (testedApps.size() + exceptionMap.size() < applicationClasses.size()) {
            if (size != testedApps.size()) {
                getLogger().info("{}/{} done", testedApps.size() + exceptionMap.size(), applicationClasses.size());
                size = testedApps.size();
            }
            if (System.currentTimeMillis() - currentTimeMillis > 2 * 60 * 1000) {//2 minutes
                Assert.fail("Test is taking too long");
                break;
            }
        }
        if (!exceptionMap.isEmpty()) {
            String collect = exceptionMap.entrySet().stream().peek(e -> {
                getLogger().error("Class " + e.getKey().getSimpleName() + " threw an exception", e);
            }).map(e -> e.getKey().getSimpleName()).map(e -> String.format("Class %s threw an exception", e))
                    .collect(Collectors.joining("\n", "\n", "\n"));
            Assert.fail(collect);
        }
        Assert.assertTrue("TESTS SUCCESSFULL", true);
    }


    private synchronized void setClass(Class<? extends Application> class1, Throwable e) {
        exceptionMap.put(class1, e);
    }
}
