package crypt;

import exercise.java8.RunnableEx;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import log.analyze.SupplierEx;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.testfx.framework.junit.ApplicationTest;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public final class FXTesting implements HasLogging {
    private static final Logger LOGGER = HasLogging.log(FXTesting.class);

    private Map<Class<?>, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<>());

    private FXTesting() {
        ResourceFXUtils.initializeFX();
    }
    @SafeVarargs
    public static void verifyAndRun(ApplicationTest app, Stage currentStage, Runnable consumer,
            Class<? extends Application>... applicationClasses) throws Exception {
        for (int i = 0; i < applicationClasses.length; i++) {
            Class<? extends Application> class1 = applicationClasses[i];
            app.interactNoWait(RunnableEx.makeRunnable(() -> class1.newInstance().start(currentStage)));
            consumer.run();
        }
    }

    @SafeVarargs
    public static void testApps(Class<? extends Application>... applicationClasses)
            throws Exception {
        new FXTesting().testApplications(Arrays.asList(applicationClasses));
    }
    private void testApplications(List<Class<? extends Application>> applicationClasses)
            throws Exception {

        ResourceFXUtils.initializeFX();
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
                String notExecuted = notExecutedApps.stream().map(Class::getSimpleName)
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
        T t = SupplierEx.makeSupplier(runnable).get();
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
