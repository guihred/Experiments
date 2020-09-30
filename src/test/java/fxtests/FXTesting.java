package fxtests;

import static utils.ex.RunnableEx.run;

import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class FXTesting implements HasLogging {
    private static final String TIME_TOOK_FORMAT = "{} took {}";

    private static final String TIME_FORMAT = "HHH:mm:ss.SSS";

    private Map<Class<?>, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<>());

    protected void testApplications(List<Class<? extends Application>> applicationClasses) {

        CommonsFX.initializeFX();
        List<Class<? extends Application>> testedApps = Collections.synchronizedList(new ArrayList<>());
        long currentTimeMillis = System.currentTimeMillis();
        for (Class<? extends Application> class1 : applicationClasses) {
            Platform.runLater(RunnableEx.make(() -> measureTime("TESTING " + class1.getSimpleName(), () -> {
                Application newInstance = class1.newInstance();
                Stage primaryStage = new Stage();
                newInstance.start(primaryStage);
                primaryStage.close();
                testedApps.add(class1);
            }), e -> {
                getLogger().error("", e);
                setClass(class1, e);
            }));
        }
        int size = testedApps.size();
        while (testedApps.size() + exceptionMap.size() < applicationClasses.size()) {
            if (size != testedApps.size()) {
                getLogger().info("{}/{} done", testedApps.size() + exceptionMap.size(), applicationClasses.size());
                size = testedApps.size();
            }
            if (System.currentTimeMillis() - currentTimeMillis > 5 * 60 * 1000) {// 2 minutes
                List<Class<? extends Application>> notExecutedApps =
                        applicationClasses.stream().collect(Collectors.toList());
                notExecutedApps.removeAll(testedApps);
                String notExecuted =
                        notExecutedApps.stream().map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
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
    }

    private synchronized void setClass(Class<? extends Application> class1, Throwable e) {
        exceptionMap.put(class1, e);
    }

    public static void measureTime(Logger log, String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error("Exception in " + name, e);
            Assert.fail("Exception in " + name);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, TIME_FORMAT);
        log.info(TIME_TOOK_FORMAT, name, formatDuration);
    }

    public static <T> T measureTime(Logger log, String name, SupplierEx<T> runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        T t = null;
        try {
            t = runnable.get();
        } catch (Exception e) {
            log.error("Exception in " + name, e);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, TIME_FORMAT);
        log.info(TIME_TOOK_FORMAT, String.format("%s=>%s", name, t), formatDuration);
        return t;
    }

    public static void measureTime(String name, RunnableEx runnable) {
        measureTime(HasLogging.log(1), name, runnable);
    }

    public static <T> T measureTime(String name, SupplierEx<T> runnable) {
        return measureTime(HasLogging.log(1), name, runnable);
    }

    public static void measureTimeExpectException(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Exception e) {
            long currentTimeMillis2 = System.currentTimeMillis();
            long arg2 = currentTimeMillis2 - currentTimeMillis;
            String formatDuration = DurationFormatUtils.formatDuration(arg2, TIME_FORMAT);
            Logger log = HasLogging.log(1);
            log.info(TIME_TOOK_FORMAT, name, formatDuration);
            log.trace("Exception in " + name, e);
        }
    }

    public static void runInTime(String name, RunnableEx runnable, final long maxTime) {
        run(() -> {
            Thread thread = new Thread(RunnableEx.make(() -> measureTime(name, runnable)));
            thread.start();
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < maxTime) {
                if (!thread.isAlive()) {
                    return;
                }
            }
            thread.interrupt();
        });
    }
}
