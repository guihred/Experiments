
package ethical.hacker;

import static utils.ex.PredicateEx.makeTest;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import ml.data.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class CoverageUtils {
    private static final String CLASS = "CLASS";

    private static final String MISSED = "_MISSED";

    private static final String COVERED = "_COVERED";

    private static final int MAX_LINE_COVERAGE = 60;

    private static final int MAX_BRANCH_COVERAGE = 50;

    private static final String PERCENTAGE = "PERCENTAGE";

    private static final int LINES_MIN_COVERAGE = 10;

    private static final int BRANCH_MIN_COVERAGE = 20;

    private static final Logger LOG = HasLogging.log();

    private CoverageUtils() {
    }

    public static List<Entry<Object, Double>> buildDataframe() {
        File csvFile = getCoverageFile();
        DataframeML b = buildDataframe("LINE_MISSED", "LINE_COVERED", PERCENTAGE, csvFile);

        List<Entry<Object, Double>> createNumberSeries = DataframeUtils.createSeries(b, CLASS, PERCENTAGE);
        createNumberSeries.sort(Comparator.comparing(Entry<Object, Double>::getValue));
        return createNumberSeries;
    }

    public static <T> List<Class<? extends T>> getClasses(Class<T> cl) {
        List<String> excludePackages = Arrays.asList("javafx.", "org.", "com.");
        return getClasses(cl, excludePackages);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getClasses(Class<T> cl, List<String> excludePackages) {
        List<Class<? extends T>> appClass = new ArrayList<>();
        return SupplierEx.get(() -> ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses()
                .stream().filter(e -> excludePackages.stream().noneMatch(p -> e.getName().contains(p)))
                .filter(makeTest(e -> cl.isAssignableFrom(e.load()))).map(ClassInfo::load)
                .filter(cla -> !Modifier.isAbstract(cla.getModifiers())).map(e -> (Class<? extends T>) e)
                .collect(Collectors.toCollection(() -> appClass)), appClass);
    }

    public static File getCoverageFile() {
        return FileTreeWalker.getPathByExtension(new File("target/site/"), ".csv").stream().map(Path::toFile)
                .filter(e -> ResourceFXUtils.computeAttributes(e).size() > 0L)
                .max(Comparator.comparing(e -> ResourceFXUtils.computeAttributes(e).size())).orElse(null);
    }

    public static double getPercentage(double[] arr) {
        return arr[0] + arr[1] == 0 ? 100 : arr[1] / (arr[0] + arr[1]) * 100;
    }

    public static List<String> getUncovered() {
        return getUncovered(LINES_MIN_COVERAGE);
    }

    public static List<String> getUncovered(int min) {
        return getUncoveredAttribute(min, "LINE_MISSED", "LINE_COVERED", PERCENTAGE);
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
        List<String> displayTestsToBeRun =
                JavaFileDependency.displayTestsToBeRun(uncovered, m -> contains(classes, m), path);

        Map<String, Long> count =
                displayTestsToBeRun.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        List<Class<? extends Application>> applications =
                displayTestsToBeRun.stream().distinct().sorted(Comparator.comparingLong(count::get).reversed())
                        .flatMap(e -> classes.stream().filter(cl -> Objects.equals(cl.getSimpleName(), e)))
                        .collect(Collectors.toList());
        if (!applications.isEmpty()) {
            String appsFound = applications.stream().map(Class::getName).collect(Collectors.joining(", ", "[", "]"));
            LOG.error("{} APPS FOUND= {}", applications.size(), appsFound);
        }
        return applications;
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
                .filter(d -> d.search(m -> contains(classes, m), visited, path)).distinct()
                .map(JavaFileDependency::getName).collect(Collectors.toList());
        uncovered.addAll(collect2);
        uncovered.addAll(path.stream().map(JavaFileDependency::getName).collect(Collectors.toList()));
        return classes.stream().filter(e -> uncovered.contains(e.getSimpleName())).collect(Collectors.toList());
    }

    public static List<String> getUncoveredBranches() {
        return getUncoveredBranches(BRANCH_MIN_COVERAGE);
    }

    public static List<String> getUncoveredBranches(int min) {
        return getUncoveredAttribute(min, "BRANCH_MISSED", "BRANCH_COVERED", PERCENTAGE);
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

    public static void showSummary() {
        File csvFile = getCoverageFile();

        DataframeML b = DataframeBuilder.build(csvFile);
        Set<String> cols = new HashSet<>(b.cols());
        List<String> coveredAttr = cols.stream().filter(e -> e.endsWith(COVERED)).map(e -> e.replaceFirst(COVERED, ""))
                .collect(Collectors.toList());
        for (String colName : coveredAttr) {
            DataframeUtils.crossFeature(b, PERCENTAGE + "_" + colName, CoverageUtils::getPercentage, colName + MISSED,
                    colName + COVERED);
        }
        Map<String, DataframeStatisticAccumulator> makeStats = DataframeUtils.makeStats(b);
        for (String colName : coveredAttr) {
            makeStats.remove(colName + MISSED);
            makeStats.remove(colName + COVERED);
        }
        makeStats.remove(CLASS);
        DataframeUtils.displayStats(makeStats);
    }

    private static DataframeML buildDataframe(String string, String string2, String percentage2, File csvFile) {
        DataframeML b = DataframeBuilder.build(csvFile);
        DataframeUtils.crossFeature(b, percentage2, CoverageUtils::getPercentage, string, string2);
        return b;
    }

    private static boolean contains(List<Class<? extends Application>> classes, JavaFileDependency m) {
        return classes.stream().anyMatch(cl -> Objects.equals(cl.getSimpleName(), m.getName()));
    }

    private static boolean containsPath(List<String> allPaths, Entry<String, List<String>> e) {
        return e.getValue().stream()
                .anyMatch(l -> allPaths.stream().anyMatch(t -> StringUtils.containsIgnoreCase(l, t)));
    }

    private static <T> List<T> getByCoverage(Function<List<String>, List<T>> func) {

        for (int i = LINES_MIN_COVERAGE; i < MAX_LINE_COVERAGE / 2; i += 5) {
            List<String> uncovered = getUncovered(i);
            List<T> uncoveredApplications = func.apply(uncovered);
            if (!uncoveredApplications.isEmpty()) {
                LOG.info("LINE COVERAGE = {}% APPS = {}", i, uncovered);
                return uncoveredApplications;
            }
        }
        if (ExtractUtils.isPortOpen(SonarApi.SONAR_API_ISSUES)) {
            List<Map<String, Object>> issuesList = new ArrayList<>();
            SonarApi.onUpdate(issuesList);
            List<String> uncovered = issuesList.stream()
                    .filter(e -> "common-java:InsufficientLineCoverage".equals(e.get("rule")))
                    .map(e -> e.getOrDefault("component", "")).map(t -> Objects.toString(t, ""))
                    .filter(StringUtils::isNotBlank)
                    .map(s -> s.replaceAll(".+/(\\w+)\\.java$", "$1")).collect(Collectors.toList());
            List<T> uncoveredApplications = func.apply(uncovered);
            if (!uncoveredApplications.isEmpty()) {
                LOG.info("LINE COVERAGE = {}% APPS = {}", uncovered, uncoveredApplications);
                return uncoveredApplications;
            }
        }

        for (int i = BRANCH_MIN_COVERAGE; i <= MAX_BRANCH_COVERAGE; i += 5) {
            List<String> uncoveredBranches = getUncoveredBranches(i);
            List<T> uncoveredApplications = func.apply(uncoveredBranches);
            if (!uncoveredApplications.isEmpty()) {
                LOG.error("BRANCH COVERAGE = {}% APPS = {}", i, uncoveredBranches);
                return uncoveredApplications;
            }
        }
        return Collections.emptyList();
    }

    private static List<String> getUncoveredAttribute(int min, String string, String string2, String percentage2) {
        File csvFile = getCoverageFile();
        if (csvFile == null || !csvFile.exists()) {
            return Collections.emptyList();
        }
        DataframeML b = buildDataframe(string, string2, percentage2, csvFile);
        b.filter(percentage2, v -> ((Number) v).intValue() <= min);
        List<String> list = b.list(CLASS);
        List<String> nonNull = SupplierEx.nonNull(list, Collections.emptyList());
        return nonNull.stream().map(s -> s.replaceAll("^(\\w+)\\..+", "$1")).collect(Collectors.toList());
    }

    private static List<String> getUncoveredFxTest(List<String> uncovered, List<String> allPaths) {
        List<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests", allPaths);

        Map<String, Long> count =
                displayTestsToBeRun.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        return displayTestsToBeRun.stream().distinct().sorted().sorted(Comparator.comparingLong(count::get).reversed())
                .collect(Collectors.toList());
    }
}
