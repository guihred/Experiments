package crypt;

import exercise.java8.RunnableEx;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import log.analyze.SupplierEx;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public final class FXTesting implements HasLogging {
    private static final Logger LOGGER = HasLogging.log(FXTesting.class);
    protected JFXPanel jfxPanel = new JFXPanel();

    private Map<Class<?>, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<>());

    private FXTesting() {
    }

    @SafeVarargs
    public static void testApps(Class<? extends Application>... applicationClasses)
            throws Exception {
        new FXTesting().testApplications(Arrays.asList(applicationClasses));
    }
    private void testApplications(List<Class<? extends Application>> applicationClasses)
            throws Exception {

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
                List<Class<? extends Application>> notExecutedApps = applicationClasses.stream()
                        .collect(Collectors.toList());
                notExecutedApps.removeAll(testedApps);
                String notExecuted = notExecutedApps.stream().map(e -> e.getSimpleName())
                        .collect(Collectors.joining(",", "(", ")"));
                Assert.fail("Test is taking too long, not executed " + notExecuted);
                break;
            }
        }
        if (!exceptionMap.isEmpty()) {
            String classesExceptions = exceptionMap.entrySet().stream()
                    .peek(e -> getLogger().error("Class " + e.getKey().getSimpleName() + " threw an exception", e))
                    .map(e -> e.getKey().getSimpleName()).map(e -> String.format("Class %s threw an exception", e))
                    .collect(Collectors.joining("\n", "\n", "\n"));
            Assert.fail(classesExceptions);
        }
        Assert.assertTrue("TESTS SUCCESSFULL", true);
    }

    public static void measureTime(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Throwable e) {
            LOGGER.error("Exception in " + name, e);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
        LOGGER.info("{} took {}", name, formatDuration);
    }

    public static void measureTimeExpectException(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Throwable e) {
            long currentTimeMillis2 = System.currentTimeMillis();
            long arg2 = currentTimeMillis2 - currentTimeMillis;
            String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
            LOGGER.info("{} took {}", name, formatDuration);
            LOGGER.trace("Exception in " + name, e);
        }
    }

    public static <T> T measureTime(String name, SupplierEx<T> runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        T t = null;
        try {
            t = runnable.get();
        } catch (Exception e) {
            LOGGER.error("Exception thrown", e);
            Assert.fail("Exception in " + name);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
        LOGGER.info("{} took {}", name, formatDuration);
        return t;
    }

    private synchronized void setClass(Class<? extends Application> class1, Throwable e) {
        exceptionMap.put(class1, e);
    }
}
