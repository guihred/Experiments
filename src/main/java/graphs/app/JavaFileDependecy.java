package graphs.app;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.SupplierEx;

public class JavaFileDependecy {
    private static final String IMPORT_REGEX = "import ([\\w\\.]+)\\.[\\w\\*]+;"
        + "|import static ([\\w\\.]+)\\.\\w+\\.\\w+;";
    private static final String CLASS_REGEX = "\\W+([A-Z]\\w+)\\W";
    private static final String PACKAGE_REGEX = "package ([\\w\\.]+);";
    private Path javaPath;
    private List<String> dependencies;
    private List<String> classes;
    private List<JavaFileDependecy> dependents;
    private String name;
    private String packageName;

    public JavaFileDependecy(Path javaPath) {
        this.javaPath = javaPath;
    }

    public List<String> getClasses() {
        if (classes == null) {
            classes = SupplierEx.get(() -> {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                    return lines.map(JavaFileDependecy::linesMatches).flatMap(List<String>::stream)
                        .filter(e -> !getName().equals(e)).collect(Collectors.toList());
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

    public List<JavaFileDependecy> getDependents() {
        return dependents;
    }

    public String getFullName() {
        return getPackage()+"."+getName();
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

    public boolean search(String name1, List<JavaFileDependecy> visited, List<JavaFileDependecy> path) {
        visited.add(this);
        for(JavaFileDependecy d:getDependents()){
            if (d.getFullName().contains(name1)) {
                path.add(d);
            }
        }
        if (!path.isEmpty()) {
            path.add(this);
            return true;
        }

        boolean anyMatch = getDependents().stream().filter(t -> !visited.contains(t))
            .anyMatch(d -> d.search(name1, visited, path));
        if (anyMatch) {
            path.add(this);
        }
        return anyMatch;
    }

    public void setDependents(List<JavaFileDependecy> dependents) {
        this.dependents = dependents.stream()
            .filter(d->d.getClasses().contains(getName()))
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getPackage() + "." + getName() + " " + getClasses();
    }

    public static List<JavaFileDependecy> getAllFileDependencies() {
        return SupplierEx.get(() -> {
            File file = new File("src");
            try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
                return walk.filter(e -> e.toFile().getName().endsWith(".java")).map(JavaFileDependecy::new)
                    .collect(Collectors.toList());
            }
        }, new ArrayList<>());

    }

    public static void main(String[] args) {
        List<JavaFileDependecy> allFileDependencies = getAllFileDependencies();
        for (JavaFileDependecy dependecy : allFileDependencies) {
            dependecy.setDependents(allFileDependencies);
        }

        List<String> asList = Arrays.asList("IadesHelper", "Cidade", "CrawlerCandidates2018Task", "WordService",
            "EWSTest", "StatsLogAccess", "LeitorArquivos");
        for (JavaFileDependecy dependecy : allFileDependencies) {
            if(asList.contains(dependecy.getName())) {
                List<JavaFileDependecy> visited= new ArrayList<>();
                List<JavaFileDependecy> path = new ArrayList<>();
                if (dependecy.search("fxtests", visited, path)) {
                    System.out.println(dependecy.getFullName() + " "
                        + path.stream().map(e -> e.getFullName()).collect(Collectors.toList()));
                }
            }
        }
//        System.out.println(allFileDependencies.stream().map(Objects::toString).collect(Collectors.joining("\n")));
    }

    private static List<String> linesMatches(String line) {
        Matcher matcher = Pattern.compile(CLASS_REGEX).matcher(line);
        List<String> linkedList = new LinkedList<>();
        while (matcher.find()) {
            linkedList.add(matcher.group(1));
        }
        return linkedList;
    }

}