package fxtests;

import static utils.PredicateEx.makeTest;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.testfx.framework.junit.ApplicationTest;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;
import utils.SupplierEx;

public final class FXTesting implements HasLogging {
    private static final String TIME_TOOK_FORMAT = "{} took {}";

    private static final String TIME_FORMAT = "HHH:mm:ss.SSS";

    private Map<Class<?>, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<>());

    private synchronized void setClass(Class<? extends Application> class1, Throwable e) {
        exceptionMap.put(class1, e);
    }

    private void testApplications(List<Class<? extends Application>> applicationClasses) {

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
                } catch (Exception e) {
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
            if (System.currentTimeMillis() - currentTimeMillis > 5 * 60 * 1000) {// 2 minutes
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
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getClasses(Class<T> cl) {
        List<Class<? extends T>> appClass = new ArrayList<>();
        List<String> excludePackages = Arrays.asList("javafx.", "org.", "com.");
        try {
            ClassPath.from(FXTesting.class.getClassLoader()).getTopLevelClasses().stream()
                .filter(e -> excludePackages.stream().noneMatch(p -> e.getName().contains(p)))
                .filter(makeTest(e -> cl.isAssignableFrom(e.load()))).map(ClassInfo::load)
                .filter(cla -> !Modifier.isAbstract(cla.getModifiers())).map(e -> (Class<? extends T>) e)
                .forEach(appClass::add);
        } catch (Exception e) {
            HasLogging.log().error("", e);
        }
        return appClass;
    }

    public static void measureTime(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        Logger log = HasLogging.log(1);
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("Exception in " + name, e);
            Assert.fail("Exception in " + name);
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, TIME_FORMAT);
        log.info(TIME_TOOK_FORMAT, name, formatDuration);
    }

    public static <T> T measureTime(String name, SupplierEx<T> runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        Logger log = HasLogging.log(1);
        T t = null;
        try {
            t = runnable.get();
        } catch (Exception e) {
            log.error("Exception in " + name, e);
        }
		log.info("{}=>{}", name, t);
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, TIME_FORMAT);
        log.info(TIME_TOOK_FORMAT, name, formatDuration);
        return t;
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

    @SafeVarargs
    public static void testApps(Class<? extends Application>... applicationClasses) {
        new FXTesting().testApplications(Arrays.asList(applicationClasses));
    }

    public static void testApps(List<Class<? extends Application>> applicationClasses) {
        new FXTesting().testApplications(applicationClasses);
    }

    public static void verifyAndRun(ApplicationTest app, Stage currentStage, Runnable consumer,
        List<Class<? extends Application>> applicationClasses) {
        Logger log = HasLogging.log(1);
        for (Class<? extends Application> class1 : applicationClasses) {
            log.info(" RUN {}", class1.getSimpleName());
            app.interactNoWait(RunnableEx.make(() -> class1.newInstance().start(currentStage)));
            consumer.run();
        }
    }
}
