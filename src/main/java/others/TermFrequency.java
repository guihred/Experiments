package others;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.HasLogging;

public final class TermFrequency {
    private static final Logger LOGGER = HasLogging.log();
    private static final String REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|(\\W+)";

    private static final Map<File, Map<String, Long>> mapaDocumentos = new HashMap<>();

    private TermFrequency() {
    }

    public static void displayTermFrequency() {

        File file = new File("src");
        if (file.exists()) {
            try {
                Map<File, Map<String, Long>> mapa = getMapaDocumentos(file, ".java");
                mapa.forEach((k, v) -> v.entrySet().stream()
                    .sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
                    .forEach(p -> LOGGER.info("{},{}={}", k.getName(), p.getKey(), p.getValue())));
            } catch (Exception e) {
                LOGGER.debug("", e);
            }

        }

    }

    public static Map<String, Long> getFrequencyMap(File f, String suffix) {
        Map<String, Long> map = new ConcurrentHashMap<>();
        if (!f.getName().endsWith(suffix)) {
            return map;
        }

        try (BufferedReader buff = Files.newBufferedReader(f.toPath())) {

            String readLine;
            do {
                readLine = buff.readLine();
                if (readLine != null) {
                    Stream.of(readLine.split(REGEX)).parallel()
                        .map(e -> e.toLowerCase())
                        .filter(e -> !StringUtils.isNumeric(e))
                        .filter(t -> !TermFrequencyIndex.JAVA_KEYWORDS.contains(t))
                        .reduce(map, TermFrequency::reduceToMap, (m1, m2) -> m1);
                }
            } while (readLine != null);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return map;
    }

    public static double getInvertDocumentFrequency(String t) {
        Double idf = 1D;
        for (Entry<File, Map<String, Long>> entry : mapaDocumentos.entrySet()) {
            if (entry.getValue().containsKey(t)) {
                idf += 1;
            }
        }
        return Math.log(mapaDocumentos.size() / idf);
    }

    public static Map<File, Map<String, Long>> getMapaDocumentos(File file, String suffix) {
        if (!file.isDirectory()) {
            Map<String, Long> termFrequencyMap = getFrequencyMap(file, suffix);
            mapaDocumentos.put(file, termFrequencyMap);
        } else {
            String[] list = file.list();
            if (list != null) {
                for (String f : list) {
                    try {
                        TermFrequency.getMapaDocumentos(new File(file, f), suffix);
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            }
        }

        return mapaDocumentos;
    }

    public static double getTermFrequency(String t, File d) {
        long freq = mapaDocumentos.get(d).getOrDefault(t, 0L);
        if (freq == 0) {
            return 0;
        }
        return 1 + Math.log(freq);
    }

    private static Map<String, Long> reduceToMap(Map<String, Long> mapa, String str) {
        if (str.isEmpty()) {
            return mapa;
        }
        String str2 = str.toLowerCase();
        if (!mapa.containsKey(str2)) {
            mapa.put(str2, 1L);
        }
        mapa.put(str2, mapa.get(str2) + 1);
        return mapa;
    }

}
