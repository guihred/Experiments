package fxtests;

import static fxtests.FXTesting.measureTime;
import static ml.data.CoverageUtils.getUncovered;
import static ml.data.CoverageUtils.getUncoveredApplications;
import static ml.data.CoverageUtils.getUncoveredBranches;
import static ml.data.CoverageUtils.getUncoveredTests;

import graphs.app.JavaFileDependency;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
public class ToBeRunTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testAGetJavaMethods() {
        measureTime("JavaFileDependency.getPublicMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                List<String> tests = dependency.getPublicMethods();
                LOG.trace("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testBGraphMethodMap() {
        measureTime("JavaFileDependency.getPublicMethodsFullName", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                dependency.setDependents(displayTestsToBeRun);
                Map<String, List<String>> tests = dependency.getPublicMethodsFullName();
                tests.forEach((k, v) -> LOG.trace("{} ={}", k, v.stream().collect(Collectors.joining("\n", "\n", ""))));
            }
        });
    }

    @Test
    public void testCInvocations() {
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                dependency.setDependents(displayTestsToBeRun);
                List<String> tests = dependency.getInvocationsMethods();
                LOG.trace("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testDMethodMap() {
        measureTime("JavaFileDependency.getPublicMethodsMap", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                Map<String, List<String>> tests = dependency.getPublicMethodsMap();
                tests.forEach((k, v) -> LOG.trace("{} ={}", dependency.getFullName(),
                    v.stream().collect(Collectors.joining("\n", "\n", ""))));

            }
        });
    }

    @Test
    public void testJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            List<String> asList = Arrays.asList();
            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(asList, "fxtests");
            String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testUncovered() {
        measureTime("JavaFileDependency.getUncovered", () -> {
            LOG.info("TestsToBeRun ={}", getUncovered());
        });
    }

    @Test
    public void testUncoveredApplications() {

        measureTime("JavaFileDependency.getUncoveredApplications", () -> {
            LOG.info("TestsToBeRun ={}", getUncoveredApplications());
        });
    }

    @Test
    public void testUncoveredBranches() {

        measureTime("JavaFileDependency.getUncoveredBranches", () -> {
            LOG.info("TestsToBeRun ={}", getUncoveredBranches());
        });
    }

    @Test
    public void testUncoveredTests() {

        measureTime("JavaFileDependency.getUncoveredTests", () -> {
            LOG.info("TestsToBeRun ={}", getUncoveredTests());
        });
    }


}
