package ml.data;

import static utils.PredicateEx.makeTest;
import static utils.StringSigaUtils.nonNull;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import graphs.app.JavaFileDependency;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.SupplierEx;

public final class CoverageUtils {
    private static final int MAX_LINE_COVERAGE = 60;

    private static final int MAX_BRANCH_COVERAGE = 80;

    private static final String PERCENTAGE = "PERCENTAGE";

    private static final int LINES_MIN_COVERAGE = 30;

    private static final int BRANCH_MIN_COVERAGE = 20;

    private static final Logger LOG = HasLogging.log();

    private CoverageUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getClasses(Class<T> cl) {
        List<Class<? extends T>> appClass = new ArrayList<>();
        List<String> excludePackages = Arrays.asList("javafx.", "org.", "com.");
        return SupplierEx.get(() -> ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses()
            .stream().filter(e -> excludePackages.stream().noneMatch(p -> e.getName().contains(p)))
            .filter(makeTest(e -> cl.isAssignableFrom(e.load()))).map(ClassInfo::load)
            .filter(cla -> !Modifier.isAbstract(cla.getModifiers())).map(e -> (Class<? extends T>) e)
            .collect(Collectors.toCollection(() -> appClass)), appClass);
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
        DataframeUtils.crossFeature(b, PERCENTAGE, CoverageUtils::getPercentage, "LINE_MISSED", "LINE_COVERED");
        b.filter(PERCENTAGE, v -> ((Number) v).intValue() <= min);
        List<String> list = b.list("CLASS");
        List<String> nonNull = nonNull(list, Collections.emptyList());
        return nonNull.stream().map(s -> s.replaceAll("^(\\w+)\\..+", "$1")).collect(Collectors.toList());
    }

    public static List<Class<? extends Application>> getUncoveredApplications() {
        return getByCoverage(CoverageUtils::getUncoveredApplications);
    }

    public static List<Class<? extends Application>> getUncoveredApplications(List<String> uncovered) {
        List<String> path = new ArrayList<>();
        List<JavaFileDependency> allFileDependencies = JavaFileDependency.getAllFileDependencies();
        for (JavaFileDependency dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }
        List<Class<? extends Application>> classes = CoverageUtils.getClasses(Application.class);
        Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, m -> contains(classes, m),
            path);
        if (!displayTestsToBeRun.isEmpty()) {
            LOG.error("APPS FOUND= {}", displayTestsToBeRun);
        }
        return displayTestsToBeRun.stream().distinct()
            .flatMap(e -> classes.stream().filter(cl -> cl.getSimpleName().equals(e))).collect(Collectors.toList());
    }

    public static List<Class<? extends Application>> getUncoveredApplications2(Collection<String> uncovered) {
        List<JavaFileDependency> allFileDependencies = JavaFileDependency.getAllFileDependencies();
        for (JavaFileDependency dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }
        List<JavaFileDependency> visited = new ArrayList<>();
        List<JavaFileDependency> path = new ArrayList<>();
        List<Class<? extends Application>> classes = CoverageUtils.getClasses(Application.class);
        List<String> collect2 = allFileDependencies.stream().filter(e -> uncovered.contains(e.getName()))
            .filter(d -> d.search(m -> contains(classes, m), visited, path)).distinct().map(JavaFileDependency::getName)
            .collect(Collectors.toList());
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
        DataframeUtils.crossFeature(b, PERCENTAGE, CoverageUtils::getPercentage, "COMPLEXITY_MISSED",
            "COMPLEXITY_COVERED");
        b.filter(PERCENTAGE, v -> ((Number) v).intValue() < min);
        return nonNull(b.list("CLASS"), Collections.emptyList());
    }

    public static List<String> getUncoveredMethods(Collection<JavaFileDependency> javaFileDependencies,
        List<String> allPaths, String className) {
        return javaFileDependencies.parallelStream().filter(j -> j.getName().equals(className))
            .map(JavaFileDependency::getPublicMethodsMap).flatMap(m -> m.entrySet().stream())
            .filter(e -> containsPath(allPaths, e)).map(Entry<String, List<String>>::getKey)
            .collect(Collectors.toList());
    }

    public static List<String> getUncoveredTests() {
        return getUncoveredTests(new ArrayList<>());
    }

    public static List<String> getUncoveredTests(List<String> allPaths) {
        return getByCoverage(s -> getUncoveredFxTest(s, allPaths));
    }

    private static boolean contains(List<Class<? extends Application>> classes, JavaFileDependency m) {
        return classes.stream().anyMatch(cl -> cl.getSimpleName().equals(m.getName()));
    }

    private static boolean containsPath(List<String> allPaths, Entry<String, List<String>> e) {
        return e.getValue().stream().anyMatch(l -> allPaths.stream().anyMatch(l::contains));
    }

    private static <T> List<T> getByCoverage(Function<List<String>, List<T>> func) {
        for (int i = LINES_MIN_COVERAGE; i < MAX_LINE_COVERAGE; i++) {
            List<String> uncovered = getUncovered(i);
            List<T> uncoveredApplications = func.apply(uncovered);
            if (!uncoveredApplications.isEmpty()) {
                LOG.info("LINE COVERAGE = {}% APPS = {}", i, uncovered);
                return uncoveredApplications;
            }
        }

        for (int i = BRANCH_MIN_COVERAGE; i < MAX_BRANCH_COVERAGE; i++) {
            List<String> uncoveredBranches = getUncoveredBranches(i);
            List<T> uncoveredApplications = func.apply(uncoveredBranches);
            if (!uncoveredApplications.isEmpty()) {
                LOG.error("BRANCH COVERAGE = {}% APPS = {}", i, uncoveredBranches);
                return uncoveredApplications;
            }
        }
        return Collections.emptyList();
    }

    private static List<String> getUncoveredFxTest(List<String> uncovered, List<String> allPaths) {
        return JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests", allPaths).stream().distinct().sorted()
            .collect(Collectors.toList());
    }
}
