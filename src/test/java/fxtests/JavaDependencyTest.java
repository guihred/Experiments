package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaDependencyTest {
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
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
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
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                Map<String, List<String>> tests = dependency.getPublicMethodsMap();
                tests.forEach((k, v) -> LOG.trace("{} ={}", dependency.getFullName(),
                    v.stream().collect(Collectors.joining("\n", "\n", ""))));

            }
        });
    }

    @Test
    public void testEJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            List<String> asList = Arrays.asList("EditSongHelper", "FilesComparator", "MusicOrganizer",
                "ZoomableScrollPane");

            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(asList, "fxtests");
            String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testFJavaCoverage() {

        measureTime("JavaFileDependency.javaCoverage", () -> {
            File csvFile = new File("target/site/jacoco/jacoco.csv");
            if (csvFile.exists()) {
                DataframeML b = DataframeBuilder.build(csvFile);
                b.filter("INSTRUCTION_COVERED", v -> ((Number) v).intValue() == 0);
                List<String> uncovered = b.list("CLASS");
                LOG.info("Uncovered classes ={}", uncovered);
                Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests");
                String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
                LOG.info("TestsToBeRun ={}", tests);
            }
        });
    }

    @Test
    public void testGTestUncovered() {
        
        measureTime("JavaFileDependency.testUncovered", () -> {
            File csvFile = new File("target/site/jacoco/jacoco.csv");
            if (csvFile.exists()) {
                DataframeML b = DataframeBuilder.build(csvFile);
                b.filter("INSTRUCTION_COVERED", v -> ((Number) v).intValue() == 0);
                List<String> uncovered = b.list("CLASS");
                List<Class<? extends Application>> classes = FXTesting.getClasses(Application.class);
                List<Class<? extends Application>> collect = classes.stream()
                    .filter(e -> uncovered.contains(e.getSimpleName())).collect(Collectors.toList());
                FXTesting.testApps(collect);
            }
        });
    }
}
