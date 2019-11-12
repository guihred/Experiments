package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.StringSigaUtils.nonNull;

import graphs.app.JavaFileDependency;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
public class ToBeRunTest {
    private static final int LINES_MIN_COVERAGE = 30;
    private static final int BRANCH_MIN_COVERAGE = 20;
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
                tests.forEach((k, v) -> LOG.info("{} ={}", dependency.getFullName(),
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

    public static double getPercentage(double[] arr) {
        return arr[0] + arr[1] == 0 ? 100 : arr[1] / (arr[0] + arr[1]) * 100;
    }

    public static List<String> getUncovered() {
        return getUncovered(LINES_MIN_COVERAGE);
    }

    public static List<String> getUncovered(int min) {
        File csvFile = new File("target/site/jacoco/jacoco.csv");
        if (!csvFile.exists()) {
            return Collections.emptyList();
        }
        DataframeML b = DataframeBuilder.build(csvFile);
        DataframeUtils.crossFeature(b, "PERCENTAGE", ToBeRunTest::getPercentage, "LINE_MISSED", "LINE_COVERED");
        b.filter("PERCENTAGE", v -> ((Number) v).intValue() <= min);
        List<String> list = b.list("CLASS");
        List<String> nonNull = nonNull(list, Collections.emptyList());
        return nonNull.stream().map(s -> s.replaceAll("^(\\w+)\\..+", "$1")).collect(Collectors.toList());
    }

    public static List<Class<? extends Application>> getUncoveredApplications() {
        for (int i = LINES_MIN_COVERAGE; i < 50; i++) {
            List<Class<? extends Application>> uncoveredApplications = getUncoveredApplications(getUncovered(i));
            if (!uncoveredApplications.isEmpty()) {
                LOG.info("Min COVERAGE APPLICATIONS= {}", i);
                return uncoveredApplications;
            }
        }

        for (int i = BRANCH_MIN_COVERAGE; i < 50; i++) {
            List<Class<? extends Application>> uncoveredApplications = getUncoveredApplications(
                getUncoveredBranches(i));
            if (!uncoveredApplications.isEmpty()) {
                LOG.error("Min COVERAGE APPLICATIONS= {}", i);
                return uncoveredApplications;
            }
        }
        return getUncoveredApplications(getUncoveredBranches());
    }

    public static List<Class<? extends Application>> getUncoveredApplications(List<String> uncovered) {
        List<JavaFileDependency> allFileDependencies = JavaFileDependency.getAllFileDependencies();
        for (JavaFileDependency dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }
        List<JavaFileDependency> visited = new ArrayList<>();
        List<JavaFileDependency> path = new ArrayList<>();
        List<Class<? extends Application>> classes = FXTesting.getClasses(Application.class);
        List<String> collect2 = allFileDependencies.stream().filter(e -> uncovered.contains(e.getName()))
            .filter(d -> d.search(m -> ToBeRunTest.contains(classes, m), visited, path)).distinct()
            .map(JavaFileDependency::getName).collect(Collectors.toList());
        uncovered.addAll(collect2);
        uncovered.addAll(path.stream().map(JavaFileDependency::getName).collect(Collectors.toList()));
        return classes.stream().filter(e -> uncovered.contains(e.getSimpleName())).collect(Collectors.toList());
    }

    public static List<String> getUncoveredBranches() {
        return getUncoveredBranches(BRANCH_MIN_COVERAGE);
    }

    public static List<String> getUncoveredBranches(int min) {
        File csvFile = new File("target/site/jacoco/jacoco.csv");
        if (!csvFile.exists()) {
            return Collections.emptyList();
        }
        DataframeML b = DataframeBuilder.build(csvFile);
        DataframeUtils.crossFeature(b, "PERCENTAGE", ToBeRunTest::getPercentage, "BRANCH_MISSED", "BRANCH_COVERED");
        b.filter("PERCENTAGE", v -> ((Number) v).intValue() < min);
        return nonNull(b.list("CLASS"), Collections.emptyList());
    }

    public static List<String> getUncoveredTests() {
        return getUncoveredTests(new ArrayList<>());
    }

    public static List<String> getUncoveredTests(List<String> allPaths) {
        for (int i = LINES_MIN_COVERAGE; i < 50; i++) {
            List<String> uncoveredApplications = getUncoveredFxTest(getUncovered(i), allPaths);
            if (!uncoveredApplications.isEmpty()) {
                LOG.error("Min COVERAGE TESTS= {}", i);
                return uncoveredApplications;
            }
        }
        for (int i = BRANCH_MIN_COVERAGE; i < 50; i++) {
            List<String> uncoveredApplications = getUncoveredFxTest(getUncoveredBranches(i), allPaths);
            if (!uncoveredApplications.isEmpty()) {
                LOG.error("Min COVERAGE TESTS= {}", i);
                return uncoveredApplications;
            }
        }
        return Collections.emptyList();
    }

    private static boolean contains(List<Class<? extends Application>> classes, JavaFileDependency m) {
        return classes.stream().anyMatch(cl -> cl.getSimpleName().equals(m.getName()));
    }

    private static List<String> getUncoveredFxTest(List<String> uncovered, List<String> allPaths) {
        LOG.error("Uncovered Classes {}", uncovered);
        return JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests", allPaths).stream().distinct().sorted()
            .collect(Collectors.toList());
    }

}
