package graphs.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.HasLogging;

public class JavaFileDependecy implements HasLogging {
    private static final String IMPORT_REGEX = "import ([\\w\\.]+)\\.[\\w\\*]+;"
        + "|import static ([\\w\\.]+)\\.\\w+\\.\\w+;";
    private static final String CLASS_REGEX = "\\W+([A-Z]\\w+)\\W";
    private static final String PACKAGE_REGEX = "package ([\\w\\.]+);";
    private Path javaPath;
    private List<String> dependencies;
    private List<String> classes;
    private String name;
    private String packageName;

    public JavaFileDependecy(Path javaPath) {
        this.javaPath = javaPath;
    }

    public List<String> getClasses() {
        if (classes == null) {
            try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                classes = lines
                        .map(JavaFileDependecy::linesMatches)
                        .flatMap(List<String>::stream)
                        .filter(e -> !getName().equals(e))
                        .collect(Collectors.toList());

            } catch (IOException e) {
                getLogger().error("", e);
            }
        }
        return classes;
    }
    public List<String> getDependencies() {
        if (dependencies == null) {
            try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                dependencies = lines.filter(e -> e.matches(IMPORT_REGEX))
                        .map(e -> e.replaceAll(IMPORT_REGEX, "$1$2"))
                        .distinct()
                        .collect(Collectors.toList());
            } catch (IOException e) {
                getLogger().error("", e);
            }
        }
        return dependencies;
    }

    public String getName() {
        if (name == null) {
            name = javaPath.toFile().getName().replaceAll(".java", "");
        }
        return name;
    }

    public String getPackage() {
        if (packageName == null) {
            try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                packageName = lines.filter(e -> e.matches(PACKAGE_REGEX))
                        .map(e -> e.replaceAll(PACKAGE_REGEX, "$1")).findFirst().orElse("");
            } catch (IOException e) {
                getLogger().error("", e);
            }
        }
        return packageName;
    }

    @Override
    public String toString() {
        return getPackage() + "." + getName() + " " + getClasses();
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