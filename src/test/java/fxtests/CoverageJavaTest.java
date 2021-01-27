package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.CoverageUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.JavaFileDependency;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.HibernateUtil;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CoverageJavaTest {
    private static final Logger LOG = HasLogging.log();
    private static final int NUMBER_TESTS = 4;

    @Test
    public void testTestUncoveredApps() {
        measureTime("JavaFileDependency.testUncoveredApps", () -> {
            HibernateUtil.setShutdownEnabled(false);
            List<Class<? extends Application>> uncoveredApplications = CoverageUtils.getUncoveredApplications();
            int b = uncoveredApplications.size();
            AbstractTestExecution.testApps(uncoveredApplications.subList(0, Math.min(uncoveredApplications.size(), b)));
            HibernateUtil.setShutdownEnabled(true);
        });
    }

    @Test
    public void testTestUncoveredTests() {
        List<String> failedTests = new ArrayList<>();
        measureTime("JavaFileDependency.testUncovered", () -> {
            List<String> paths = new ArrayList<>();
            List<String> tests = CoverageUtils.getUncoveredTests(paths);
            List<String> allPaths =
                    paths.stream().map(e -> e.replaceAll(".+\\.(\\w+)$", "$1")).collect(Collectors.toList());
            LOG.info(" All Paths {}", allPaths);
            if (allPaths.contains("StringSigaUtils") || allPaths.contains("ExtractUtils")) {
                tests.remove("IndependentTest");
                tests.add(0, "IndependentTest");
            }

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
                List<JavaFileDependency> javaFileDependencies = JavaFileDependency.getJavaFileDependencies("fxtests");
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

    private <T extends Annotation> void invoke(List<Method> declaredMethods, Object test, Class<T> annotationClass) {

        declaredMethods.stream().filter(e -> e.getAnnotationsByType(annotationClass).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
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
            invoke(declaredMethods, test, Before.class);
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                    .sorted(Comparator.comparing(Method::getName))
                    .forEach(ConsumerEx.make(e -> e.invoke(test), (o, e) -> {
                        Class<? extends Throwable> expected = o.getAnnotationsByType(Test.class)[0].expected();
                        if (expected == null || isNotSame(e, expected)) {
                            failedTests.add(o + "");
                            LOG.error("ERROR invoking " + o, e);
                        }
                    }));
            invoke(declaredMethods, test, After.class);
        });

    }

    private void runTest(Class<?> testClass, Object test, List<String> failedTests, List<String> methods) {
        FXTesting.measureTime(testClass.getSimpleName(), () -> {
            List<Method> declaredMethods = ClassReflectionUtils.getAllMethodsRecursive(testClass);

            invoke(declaredMethods, test, Before.class);
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                    .sorted(Comparator.comparing(Method::getName))
                    .filter(e -> methods.isEmpty() || methods.contains(e.getName()))
                    .forEach(ConsumerEx.make(e -> e.invoke(test), (o, e) -> {
                        Class<? extends Throwable> expected = o.getAnnotationsByType(Test.class)[0].expected();
                        if (expected == null || isNotSame(e, expected)) {
                            failedTests.add(o + "");
                            LOG.error("ERROR invoking " + o, e);
                        }
                    }));

            invoke(declaredMethods, test, After.class);
        });

    }

}
