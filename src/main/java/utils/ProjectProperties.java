package utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class ProjectProperties {

    private static final Map<String, String> PROPERTIES = loadProperties();

    private static final Logger LOG = HasLogging.log();

    public static String getField() {
        return SupplierEx.get(() -> {
            String stackMatch = HasLogging.getStackMatch(s -> !s.contains("ProjectProperties"));
            String[] stackParts = stackMatch.split("[:\\.]+");
            String line = stackParts[stackParts.length - 1];
            String fileName = stackParts[stackParts.length - 2];
            Path javaPath = FileTreeWalker.getFirstPathByExtension(new File("src"), fileName + ".java");
            try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8)) {
                String orElse = lines.skip(StringSigaUtils.toLong(line) - 1L).filter(s -> s.contains("=")).findFirst()
                        .map(s -> Stream.of(s.split("[\\s=]+")).filter(StringUtils::isNotBlank)
                                .filter(m -> Character.isUpperCase(m.charAt(0)))
                                .filter(t -> !TermFrequency.getJavaKeywords().contains(t))
                                .filter(t -> !"String".equals(t)).findFirst().orElse(s))
                        .orElse(null);
                String key = fileName + "." + orElse;
                String string = PROPERTIES.get(key);
                if (string != null) {
                    return string;
                }
                LOG.error("FIELD {} DOES NOT EXISTS", key);
                return string;
            }
        });
    }

    private static Map<String, String> loadProperties() {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        RunnableEx.run(() -> {
            File file = ResourceFXUtils.toFile("project.properties");
            Files.lines(file.toPath()).forEach(s -> {
                String regex = "(.+?)=(.+)";
                String property = s.replaceAll(regex, "$1").trim();
                String value = s.replaceAll(regex, "$2");
                if (StringUtils.isBlank(property) || StringUtils.isBlank(value)) {
                    LOG.error("ERROR IN LINE {}", s);
                    return;
                }
                linkedHashMap.put(property, value);
            });
        });

        return linkedHashMap;
    }
}
