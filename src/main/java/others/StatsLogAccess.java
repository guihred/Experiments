package others;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class StatsLogAccess {
    private static final String EXCLUDE_PACKAGES =
            "\tat (sun|java|javax|org|com|javafx|io.pkts|utils\\.ex"
                    + "|fxtests\\.(FXTesting|JavaDependencyTest|AbstractTestExecution))\\..+";
    private static final Logger LOG = HasLogging.log();

    private StatsLogAccess() {
    }

    public static List<Entry<String, Long>> displayErrors() {
        return SupplierEx.get(() -> {
            File file = new File("target/errors.log");
            try (BufferedReader scanner = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
                Map<String, Long> collect2 = scanner.lines().filter(l -> !l.matches(EXCLUDE_PACKAGES))
                        .filter(l -> l.matches("\tat .+?\\(.+\\)")).map(l -> l.replaceAll("\tat (.+?\\(.+\\))", "$1"))
                        .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
                return collect2.entrySet().stream()
                        .sorted(Comparator.comparing((Entry<String, Long> e) -> -e.getValue())
                                .thenComparing(Entry<String, Long>::getKey))
                        .collect(Collectors.toList());
            }
        });
    }

    public static void main(String[] args) {
        List<Entry<String, Long>> displayErrors = displayErrors();
        displayErrors.forEach(l -> LOG.info("{}", l));
    }

}
