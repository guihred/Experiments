package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.CoverageUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import utils.*;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaDependencyTest {
    private static final Logger LOG = HasLogging.log();
    private static final int NUMBER_TESTS = 10;

    @Test
    public void testGTestUncovered() {

        List<String> failedTests = new ArrayList<>();

        measureTime("JavaFileDependency.testUncovered", () -> {
            List<String> paths = new ArrayList<>();
            List<JavaFileDependency> javaFileDependencies = JavaFileDependency.getJavaFileDependencies("fxtests");
            List<String> tests = CoverageUtils.getUncoveredTests(paths);
            List<String> allPaths = paths.stream().map(e -> e.replaceAll(".+\\.(\\w+)$", "$1"))
                .collect(Collectors.toList());
            LOG.info(" All Paths {}", allPaths);
            int min = Math.min(tests.size() / 2, NUMBER_TESTS / 2);
            List<String> subList = tests.subList(0, min);
            List<String> subList2 = tests.subList(min, tests.size());
            List<String> uncoveredTests = new ArrayList<>(subList);
            Collections.shuffle(subList2);
            uncoveredTests.addAll(subList2);

            for (int i = 0; i < Math.min(uncoveredTests.size(), NUMBER_TESTS); i++) {
                String className = uncoveredTests.get(i);
                if (className.equals("JavaDependencyTest")) {
                    continue;
                }
                LOG.info("RUN TESTS {}", uncoveredTests.subList(i, uncoveredTests.size()));
                LOG.info("RUNNING TEST {} {}/{}", className, i + 1, uncoveredTests.size());
                Class<?> forName = Class.forName("fxtests." + className);
                if (Modifier.isAbstract(forName.getModifiers())) {
                    continue;
                }
                if (forName.getConstructors()[0].getParameterCount() != 0) {

                    Collection<?> orElseThrow = ClassReflectionUtils.getAllMethodsRecursive(forName).stream()
                        .filter(e -> e.getAnnotationsByType(Parameterized.Parameters.class).length > 0)
                        .map(FunctionEx.makeFunction(e -> e.invoke(null))).findFirst().map(e -> (Collection<?>) e)
                        .orElseGet(() -> Collections.emptyList());
                    for (Object object : orElseThrow) {
                        RunnableEx.run(() -> {
                            Object ob = forName.getConstructors()[0].newInstance(object);
                            runTest(forName, ob, failedTests);
                        });
                    }
                    continue;
                }
                Object ob = forName.newInstance();
                List<String> methods = CoverageUtils.getUncoveredMethods(javaFileDependencies, allPaths, className);

                LOG.info(" To Be Run Methods {}", methods);
                runTest(forName, ob, failedTests, methods);
                LOG.info(" TESTS RUN {} {}/{}", "fxtests." + className, i + 1, uncoveredTests.size());

            }
        });
        if (!failedTests.isEmpty()) {
            String errorsFound = failedTests.stream().collect(Collectors.joining("\n\t", "ERRORS IN {\n\t", "\n}"));
            LOG.error(errorsFound);
            Assert.fail(errorsFound);
        }
    }

    @Test
    public void testHTestUncoveredApps() {
        measureTime("JavaFileDependency.testUncoveredApps", () -> {
            HibernateUtil.setShutdownEnabled(false);
            List<Class<? extends Application>> uncoveredApplications = CoverageUtils.getUncoveredApplications();
            AbstractTestExecution
                .testApps(uncoveredApplications.subList(0, Math.min(uncoveredApplications.size(), NUMBER_TESTS)));
            HibernateUtil.setShutdownEnabled(true);
        });
    }

    private boolean isNotSame(Throwable e, Class<? extends Throwable> expected) {
        boolean notSame = expected != e.getCause().getClass();
        if (notSame) {
            LOG.error("expected={} , Thrown = {}", expected, e.getCause().getClass());
        }
        return notSame;
    }

    private void runTest(Class<?> testClass, Object test, List<String> failedTests) {
        FXTesting.measureTime(testClass.getSimpleName(), () -> {
            List<Method> declaredMethods = ClassReflectionUtils.getAllMethodsRecursive(testClass);
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Before.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                .sorted(Comparator.comparing(Method::getName))
                .forEach(ConsumerEx.make(e -> e.invoke(test), (Method o, Throwable e) -> {
                    Class<? extends Throwable> expected = o.getAnnotationsByType(Test.class)[0].expected();
                    if (expected == null || isNotSame(e, expected)) {
                        failedTests.add(o + "");
                        LOG.error("ERROR invoking " + o, e);
                    }
                }));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(After.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
        });

    }

    private void runTest(Class<?> testClass, Object test, List<String> failedTests, List<String> methods) {
        FXTesting.measureTime(testClass.getSimpleName(), () -> {
            RunnableEx.run(() -> HibernateUtil.setShutdownEnabled(false));
            List<Method> declaredMethods = ClassReflectionUtils.getAllMethodsRecursive(testClass);
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Before.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                .sorted(Comparator.comparing(Method::getName))
                .filter(e -> methods.isEmpty() || methods.contains(e.getName()))
                .forEach(ConsumerEx.make(e -> e.invoke(test), (Method o, Throwable e) -> {
                    Class<? extends Throwable> expected = o.getAnnotationsByType(Test.class)[0].expected();
                    if (expected == null || isNotSame(e, expected)) {
                        failedTests.add(o + "");
                        LOG.error("ERROR invoking " + o, e);
                    }
                }));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(After.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
            HibernateUtil.setShutdownEnabled(true);
        });

    }

}
