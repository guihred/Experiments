package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.ConsumerEx;
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
    public void testEJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            List<String> asList = Arrays.asList("ConcentricLayout", "JavaFileDependency", "ConvergeLayout",
                "CircleLayout", "CatanDragContext", "DataframeML", "PhotoViewerHelper", "CatanModel", "EdgeCatan",
                "Deal", "CatanLogger", "CatanAppMain", "CatanCard", "Terrain", "CommonsFX", "CatanHelper",
                "MouseGestures", "City", "PackageTopology", "CSVUtils", "Chapter4", "MethodsTopology", "ListHelper",
                "Combination", "ContestApplicationController", "PlayerColor", "EditSongController", "SolitaireModel",
                "CircleTopology", "GraphMain", "PhotoViewer", "SettlePoint", "DevelopmentType", "UserChart");

            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(asList, "fxtests");
            String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testFJavaCoverage() {

        measureTime("JavaFileDependency.javaCoverage", () -> {
            List<String> uncovered = getUncovered();
            LOG.info("Uncovered classes ={}", uncovered);
            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests");
            String tests = displayTestsToBeRun.stream().sorted().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testGTestUncovered() {

        measureTime("JavaFileDependency.testUncovered", () -> {
            List<String> uncovered = getUncovered();

            List<String> collect = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests").stream().distinct()
                .sorted().collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                String className = collect.get(i);
                if (className.equals("JavaDependencyTest")) {
                    continue;
                }
                LOG.info("RUNNING TEST {} {}/{}", className, i + 1, collect.size());
                Class<?> forName = Class.forName("fxtests." + className);
                Object ob = forName.newInstance();
                List<Method> declaredMethods = ClassReflectionUtils.getAllMethodsRecursive(forName);
                declaredMethods.stream().filter(e -> e.getAnnotationsByType(Before.class).length > 0)
                    .forEach(e -> ClassReflectionUtils.invoke(ob, e));
                declaredMethods.stream().filter(e -> e.getAnnotationsByType(Test.class).length > 0)
                    .forEach(ConsumerEx.makeConsumer(e -> e.invoke(ob)));
                declaredMethods.stream().filter(e -> e.getAnnotationsByType(After.class).length > 0)
                    .forEach(e -> ClassReflectionUtils.invoke(ob, e));
                LOG.info(" TESTS RUN {} {}/{}", "fxtests." + className, i + 1, collect.size());

            }
        });
    }

    @Test
    public void testHTestUncovered() {

        measureTime("JavaFileDependency.testUncovered", () -> {
            List<String> uncovered = getUncovered();
            List<Class<? extends Application>> classes = FXTesting.getClasses(Application.class);
            List<Class<? extends Application>> collect = classes.stream()
                .filter(e -> uncovered.contains(e.getSimpleName())).collect(Collectors.toList());
            FXTesting.testApps(collect);
        });
    }

    private List<String> getUncovered() {
        File csvFile = new File("target/site/jacoco/jacoco.csv");
        if (!csvFile.exists()) {
            return Collections.emptyList();
        }
        DataframeML b = DataframeBuilder.build(csvFile);
        DataframeUtils.crossFeature(b, "PERCENTAGE", arr -> arr[1] / (arr[0] + arr[1]) * 100, "LINE_MISSED",
            "LINE_COVERED");
        b.filter("PERCENTAGE", v -> ((Number) v).intValue() <= 35);
        List<String> uncovered = b.list("CLASS");
        return uncovered;
    }
}
