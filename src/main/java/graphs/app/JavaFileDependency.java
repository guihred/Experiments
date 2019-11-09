package graphs.app;

import com.google.common.util.concurrent.Atomics;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import utils.HasLogging;
import utils.SupplierEx;

public class JavaFileDependency {
    private static final String IMPORT_REGEX = "import ([\\w\\.]+)\\.[\\w\\*]+;"
        + "|import static ([\\w\\.]+)\\.\\w+\\.\\w+;";
    private static final String CLASS_REGEX = "\\W+([A-Z]\\w+)\\W";
    private static final String PUBLIC_METHOD_REGEX = "\\W+public .+ (\\w+)\\(.+";
    private static final String INVOKE_METHOD_REGEX = "\\W+(\\w+)\\(";
    private static final String PACKAGE_REGEX = "package ([\\w\\.]+);";
    private Path javaPath;
    private List<String> dependencies;
    private List<JavaFileDependency> dependsOn;
    private List<String> publicMethods;
    private List<String> classes;
    private List<JavaFileDependency> dependents;
    private String name;
    private String packageName;
    private List<String> invocations;
    private Map<String, List<String>> methodsMap;

    public JavaFileDependency(Path javaPath) {
        this.javaPath = javaPath;
    }

    public List<String> getClasses() {
        if (classes == null) {
            classes = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.map(JavaFileDependency::removeStrings).map(JavaFileDependency::linesMatches)
                        .flatMap(List<String>::stream).filter(e -> !getName().equals(e)).collect(Collectors.toList());
                }
            });
        }
        return classes;
    }

    public List<String> getDependencies() {
        if (dependencies == null) {
            dependencies = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.filter(e -> e.matches(IMPORT_REGEX)).map(e -> e.replaceAll(IMPORT_REGEX, "$1$2"))
                        .distinct().collect(Collectors.toList());
                }
            });
        }
        return dependencies;
    }

    public List<JavaFileDependency> getDependents() {
        return dependents;
    }

    public List<JavaFileDependency> getDependsOn() {
        return dependsOn;
    }

    public String getFullName() {
        return getPackage() + "." + getName();
    }

    public List<String> getInvocations(Stream<String> lines, Collection<JavaFileDependency> dependsOn2) {
        return lines.map(JavaFileDependency::removeStrings).filter(t -> !t.matches(PUBLIC_METHOD_REGEX))
            .map(t -> matches(t, INVOKE_METHOD_REGEX)).flatMap(List<String>::stream)
            .flatMap(invoke -> Stream.concat(dependsOn2.stream(), Stream.of(this)).flatMap(e -> e.getPublicMethods()
                .stream().filter(invoke::equals).map(publicMethod -> e.getName() + "." + publicMethod)))
            .collect(Collectors.toList());
    }

    public List<String> getInvocationsMethods() {
        if (invocations == null && dependsOn != null) {
            invocations = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return getInvocations(lines, dependsOn);
                }
            });
        }
        return invocations;
    }

    public String getName() {
        if (name == null) {
            name = javaPath.toFile().getName().replaceAll(".java", "");
        }
        return name;
    }

    public String getPackage() {
        if (packageName == null) {
            packageName = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.filter(e -> e.matches(PACKAGE_REGEX)).map(e -> e.replaceAll(PACKAGE_REGEX, "$1"))
                        .findFirst().orElse("");
                }
            }, "");
        }
        return packageName;
    }

    public List<String> getPublicMethods() {
        if (publicMethods == null) {
            publicMethods = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.map(JavaFileDependency::removeStrings).map(t -> matches(t, PUBLIC_METHOD_REGEX))
                        .flatMap(List<String>::stream).collect(Collectors.toList());
                }
            });
        }
        return publicMethods;
    }

    public Map<String, List<String>> getPublicMethodsFullName() {
        return getPublicMethodsMap().entrySet().stream().collect(
            Collectors.toMap(e -> getName() + "." + e.getKey(), e -> getInvocations(e.getValue().stream(), dependsOn)));
    }

    public Map<String, List<String>> getPublicMethodsMap() {
        if (methodsMap == null) {
            methodsMap = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    Map<String, List<String>> methodMap = new HashMap<>();
                    AtomicReference<String> method = Atomics.newReference();
                    AtomicInteger stackSize = new AtomicInteger(0);
                    lines.map(JavaFileDependency::removeStrings).filter(StringUtils::isNotBlank)
                        .forEach(line -> appendToMap(methodMap, method, stackSize, line));
                    return methodMap;
                }
            });
        }
        return methodsMap;
    }

    public boolean search(Predicate<JavaFileDependency> test, List<JavaFileDependency> visited,
        List<JavaFileDependency> path) {
        for (JavaFileDependency d : getDependents()) {
            if (matchesAndNotIn(test, path, d)) {
                path.add(d);
            }
        }
        if (matchesAndNotIn(test, path, this)) {
            path.add(this);
        }

        visited.add(this);
        boolean anyMatch = false;
        for (JavaFileDependency t : getDependents()) {
            if (!visited.contains(t) && t.search(test, visited, path)) {
                anyMatch = true;
            }
        }
        if (anyMatch && !path.contains(this)) {
            path.add(this);
        }
        return test.test(this) || anyMatch;
    }

    public boolean search(String name1, List<JavaFileDependency> visited, List<JavaFileDependency> path) {
        return search(d -> d.getFullName().contains(name1), visited, path);
    }

    public void setDependents(Collection<JavaFileDependency> dependents) {
        this.dependents = dependents.stream().filter(d -> d.getClasses().contains(getName()))
            .collect(Collectors.toList());
        dependsOn = dependents.stream().filter(d -> getClasses().contains(d.getName())).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getPackage() + "." + getName() + " " + getClasses();
    }

    public static Set<String> displayTestsToBeRun(Collection<String> asList, String name1) {
        return displayTestsToBeRun(asList, name1, new ArrayList<>());
    }

    public static Set<String> displayTestsToBeRun(Collection<String> dependecyList, String name1,
        List<String> allPaths) {
        List<JavaFileDependency> allFileDependencies = getAllFileDependencies();
        for (JavaFileDependency dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }
        Set<String> testClasses = new LinkedHashSet<>();
        for (JavaFileDependency dependecy : allFileDependencies) {
            if (dependecyList == null || dependecyList.contains(dependecy.getName())) {
                List<JavaFileDependency> visited = new ArrayList<>();
                List<JavaFileDependency> path = new ArrayList<>();
                dependecy.search(name1, visited, path);
                if (!path.isEmpty()) {
                    List<String> filesFullPath = path.stream().map(JavaFileDependency::getFullName)
                        .collect(Collectors.toList());
                    allPaths.addAll(filesFullPath);
                    HasLogging.log().info("{} {}", dependecy.getFullName(), filesFullPath);
                }
                testClasses.addAll(path.stream().filter(e -> e.getFullName().contains(name1))
                    .map(JavaFileDependency::getName).collect(Collectors.toList()));
            }
        }
        return testClasses;
    }

    public static List<JavaFileDependency> getAllFileDependencies() {
        return SupplierEx.get(() -> {
            File file = new File("src");
            try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
                return walk.filter(e -> e.toFile().getName().endsWith(".java")).map(JavaFileDependency::new)
                    .collect(Collectors.toList());
            }
        }, new ArrayList<>());

    }

    public static List<JavaFileDependency> getJavaFileDependencies(String packName) {
        return JavaFileDependency.getAllFileDependencies().stream()
            .filter(e -> StringUtils.isBlank(packName) || e.getPackage().equals(packName)).collect(Collectors.toList());
    }

    private static void appendToMap(Map<String, List<String>> methodMap, AtomicReference<String> method,
        AtomicInteger stackSize, String line) {
        if (line.matches(PUBLIC_METHOD_REGEX) && stackSize.get() == 1) {
            method.set(line.replaceAll(PUBLIC_METHOD_REGEX, "$1"));
            methodMap.put(method.get(), new ArrayList<>());
        }
        if (method.get() != null) {
            methodMap.get(method.get()).add(line);
        }
        if (line.contains("{")) {
            stackSize.getAndIncrement();
        }
        if (line.contains("}")) {
            stackSize.getAndDecrement();
        }
        if (stackSize.get() == 1) {
            method.set(null);
        }
    }

    private static List<String> linesMatches(String line) {
        return matches(line, CLASS_REGEX);
    }

    private static List<String> matches(String line, String classRegex) {
        Matcher matcher = Pattern.compile(classRegex).matcher(line);
        List<String> linkedList = new LinkedList<>();
        while (matcher.find()) {
            linkedList.add(matcher.group(1));
        }
        return linkedList;
    }

    private static boolean matchesAndNotIn(Predicate<JavaFileDependency> test, List<JavaFileDependency> path,
        JavaFileDependency o) {
        return test.test(o) && !path.contains(o);
    }

    private static String removeStrings(String s) {
        return s.replaceAll("\"[^\"]*\"", "\"\"");
    }

}