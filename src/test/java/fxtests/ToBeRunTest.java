package fxtests;

import static fxtests.FXTesting.measureTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ml.data.CoverageUtils;
import ml.data.JavaFileDependency;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ToBeRunTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testFJavaCoverage() {
        measureTime("JavaFileDependency.javaCoverage", () -> {
            List<String> uncovered = CoverageUtils.getUncovered();
            LOG.info("Uncovered classes ={}", uncovered);
            List<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests");
            String tests = displayTestsToBeRun.stream().distinct().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testGetJavaMethods() {
        measureTime("JavaFileDependency.getPublicMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                List<String> tests = dependency.getPublicMethods();
                LOG.trace("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testGraphMethodMap() {
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
    public void testInvocations() {
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
    public void testJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun",
            () -> JavaFileDependency.displayTestsToBeRun(Arrays.asList(), "fxtests").stream().distinct().sorted()
                .collect(Collectors.joining(",*", "*", "")));
    }

    @Test
    public void testMethodMap() {
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
    public void testUncovered() {
        measureTime("JavaFileDependency.getUncovered", () -> CoverageUtils.getUncovered());
    }

    @Test
    public void testUncoveredBranches() {
        measureTime("JavaFileDependency.getUncoveredBranches", () -> CoverageUtils.getUncoveredBranches());
    }

    @Test
    public void testUncoveredTests() {

        measureTime("JavaFileDependency.getUncoveredTests", () -> CoverageUtils.getUncoveredTests());
    }

    @Test
    public void testUncoveredZApplications() {
        measureTime("JavaFileDependency.getUncoveredApplications", () -> CoverageUtils.getUncoveredApplications());
    }

    @Test
    public void testUncoveredZApplications2() {
        measureTime("JavaFileDependency.getUncoveredApplications2",
            () -> CoverageUtils.getUncoveredApplications2(CoverageUtils.getUncoveredBranches()));
    }

    @Test
    public void verifySummary() {
        CoverageUtils.showSummary();
    }

}
