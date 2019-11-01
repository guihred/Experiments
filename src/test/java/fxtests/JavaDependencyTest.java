package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.ConsumerEx;
import utils.FunctionEx;
import utils.HasLogging;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaDependencyTest {
    static final Logger LOG = HasLogging.log();

    @Test
    public void testFJavaCoverage() {
        measureTime("JavaFileDependency.javaCoverage", () -> {
            List<String> uncovered = ToBeRunTest.getUncovered();
            LOG.info("Uncovered classes ={}", uncovered);
            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests");
            String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

	@Test
    public void testGTestUncovered() {

        List<String> failedTests = new ArrayList<>();
        measureTime("JavaFileDependency.testUncovered", () -> {
            List<String> uncoveredTests = ToBeRunTest.getUncoveredTests();

            for (int i = 0; i < uncoveredTests.size(); i++) {
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
                        Object ob = forName.getConstructors()[0].newInstance(object);
                        runTest(forName, ob, failedTests);
                    }
                    continue;
                }
                Object ob = forName.newInstance();
                runTest(forName, ob, failedTests);
                LOG.info(" TESTS RUN {} {}/{}", "fxtests." + className, i + 1, uncoveredTests.size());

            }
        });
        if (!failedTests.isEmpty()) {
            String collect = failedTests.stream().collect(Collectors.joining("\n\t", "ERRORS IN {\n\t", "\n}"));
            LOG.error(collect);
            Assert.fail(collect);
        }
    }

    @Test
	public void testHTestUncoveredApps() {

        measureTime("JavaFileDependency.testUncoveredApps", () -> FXTesting.testApps(ToBeRunTest.getUncoveredApplications()));
    }
    private void runTest(Class<?> testClass, Object test, List<String> failedTests) {
        FXTesting.measureTime(testClass.getSimpleName(), () -> {
            List<Method> declaredMethods = ClassReflectionUtils.getAllMethodsRecursive(testClass);
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Before.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                .sorted(Comparator.comparing(Method::getName)).forEach(ConsumerEx.make(e -> e.invoke(test), (o, e) -> {
                    failedTests.add(o + "");
                    LOG.error("ERROR invoking " + o, e);
                }));
            declaredMethods.stream().filter(e -> e.getAnnotationsByType(After.class).length > 0)
                .forEach(e -> ClassReflectionUtils.invoke(test, e));
        });

    }


}
