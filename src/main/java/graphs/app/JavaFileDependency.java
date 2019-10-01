package graphs.app;

import com.google.common.util.concurrent.Atomics;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    public String getFullName() {
        return getPackage() + "." + getName();
    }

    public List<String> getInvocationsMethods() {
        if (invocations == null && dependsOn != null) {
            invocations = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.map(JavaFileDependency::removeStrings).filter(t -> !t.matches(PUBLIC_METHOD_REGEX))
                        .map(t -> matches(t, INVOKE_METHOD_REGEX))
                        .flatMap(List<String>::stream)
                        .flatMap(d -> dependsOn.stream().flatMap(
                            e -> e.getPublicMethods().stream().filter(d::equals).map(g -> e.getFullName() + "." + g)))
                        .distinct().collect(Collectors.toList());
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

    public Map<String, List<String>> getPublicMethodsMap() {
        if (methodsMap == null) {
            methodsMap = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    Map<String, List<String>> methodMap = new HashMap<>();
                    AtomicReference<String> method = Atomics.newReference();
                    AtomicInteger stackSize = new AtomicInteger(0);
                    lines.map(JavaFileDependency::removeStrings)
                        .filter(StringUtils::isNotBlank)
                        .forEach(line -> appendToMap(methodMap, method, stackSize, line));
                    return methodMap;
                }
            });
        }
        return methodsMap;
    }

    public boolean search(String name1, List<JavaFileDependency> visited, List<JavaFileDependency> path) {
        visited.add(this);
        for (JavaFileDependency d : getDependents()) {
            if (d.getFullName().contains(name1)) {
                path.add(d);
            }
        }
        if (!path.isEmpty()) {
            path.add(this);
        }

        boolean anyMatch = getDependents().stream().filter(t -> !visited.contains(t))
            .anyMatch(d -> d.search(name1, visited, path));
        if (anyMatch) {
            path.add(this);
        }
        return anyMatch;
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
        List<JavaFileDependency> allFileDependencies = getAllFileDependencies();
        for (JavaFileDependency dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }
        Set<String> testClasses = new HashSet<>();
        for (JavaFileDependency dependecy : allFileDependencies) {
            if (asList.contains(dependecy.getName())) {
                List<JavaFileDependency> visited = new ArrayList<>();
                List<JavaFileDependency> path = new ArrayList<>();
                dependecy.search(name1, visited, path);
                HasLogging.log().info("{} {}", dependecy.getFullName(),
                    path.stream().map(JavaFileDependency::getFullName).collect(Collectors.toList()));
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

    private static String removeStrings(String s) {
        return s.replaceAll("\"[^\"]*\"", "\"\"");
    }

}