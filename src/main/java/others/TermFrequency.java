package others;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TermFrequency {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermFrequency.class);
	private static final String REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|(\\W+)";

	private static final Map<File, Map<String, Long>> mapaDocumentos = new HashMap<>();

	private TermFrequency() {
	}

    public static void displayTermFrequency() {

        File file = new File("src");
		if (file.exists()) {
			try {
                Map<File, Map<String, Long>> mapa = getMapaDocumentos(file, ".java");
                mapa.forEach((File k, Map<String, Long> v) -> v
                        .forEach((String p, Long f) -> LOGGER.error("{},{},{}", k.getName(), p, f)));
            } catch (Exception e) {
                LOGGER.debug("", e);
			}

		}

	}

	public static Map<String, Long> getFrequencyMap(File f) {
		Map<String, Long> map = new ConcurrentHashMap<>();
        try (BufferedReader buff = Files.newBufferedReader(f.toPath())) {

			String readLine;
			do {
				readLine = buff.readLine();
				if (readLine != null) {
					String[] split = readLine.split(REGEX);
					List<String> asList = Arrays.asList(split);
					asList.stream().parallel().reduce(map, (mapa, str) -> {
						if (str.isEmpty()) {
							return mapa;
						}
						String str2 = str.toLowerCase();
						if (!mapa.containsKey(str2)) {
							mapa.put(str2, 1L);
						}
						mapa.put(str2, mapa.get(str2) + 1);
						return mapa;
					}, (m1, m2) -> m1);
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

    public static Map<File, Map<String, Long>> getMapaDocumentos(File file,String suffix) {
		if (!file.isDirectory()) {
			Map<String, Long> termFrequencyMap = getFrequencyMap(file);
			mapaDocumentos.put(file, termFrequencyMap);
		} else {
			String[] list = file.list((a,b)->b.endsWith(suffix));
			if (list != null) {
				Arrays.asList(list).forEach(f -> {
					try {
                        TermFrequency.getMapaDocumentos(new File(file, f), suffix);
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				});
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

}
