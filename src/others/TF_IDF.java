package others;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TF_IDF {
	public static class ValueComparator implements Comparator<Entry<String, Map<File, Double>>> {

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		@Override
		public int compare(Entry<String, Map<File, Double>> a, Entry<String, Map<File, Double>> b) {
			Double da = 0D;
			for (Entry<File, Double> entry : a.getValue().entrySet()) {
				da = da < entry.getValue() ? entry.getValue() : da;
			}
			Double db = 0D;
			for (Entry<File, Double> entry : b.getValue().entrySet()) {
				db = db < entry.getValue() ? entry.getValue() : db;
			}

			return db.compareTo(da);
		}
	}

	public static final Logger logger = LoggerFactory.getLogger(TF_IDF.class);

	private static final Map<String, Map<File, Double>> MAP_TF_IDF = new HashMap<>();

	/*
	 * Modos de calcular tf(t,d)=
	 * 
	 * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
	 * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if
	 * f(t, d) is zero; augmented frequency, to prevent a bias towards longer
	 * documents, e.g. raw frequency divided by the maximum raw frequency of any
	 * term in the document:
	 * 
	 * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
	 * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
	 */

	/*
	 * 
	 * Tf, in its basic form, is just the frequency that we look up in
	 * appropriate table. In this case, it's one.
	 * 
	 * Idf is a bit more involved:
	 * 
	 * idf("this", D) = log( N/f(t,D))
	 * 
	 * The numerator of the fraction is the number of documents, which is two.
	 * The number of documents in which "this" appears is also two, giving
	 * 
	 * idf("this", D) = log (2/2) = 0
	 * 
	 * So tf�idf is zero for this term, and with the basic definition this is
	 * true of any term that occurs in all documents.
	 * 
	 * A slightly more interesting example arises from the word "example", which
	 * occurs three times but in only one document. For this document, tf�idf of
	 * "example" is:
	 * 
	 * D = conjunto total de documento
	 * 
	 * d_i = um documento i
	 * 
	 * tf("example", d_i) = 3 idf("example", D) = log (2/1) ~= 0.3010
	 * tfidf("example", d_i) = tf("example", d_i) x idf("example", D) = 3 x
	 * 0.3010 = 0.9030
	 * 
	 * Modos de calcular tf(t,d)=
	 * 
	 * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
	 * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if
	 * f(t, d) is zero; augmented frequency, to prevent a bias towards longer
	 * documents, e.g. raw frequency divided by the maximum raw frequency of any
	 * term in the document:
	 * 
	 * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
	 * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
	 */
	private static final Map<File, Map<String, Long>> MAPA_DOCUMENTO = new HashMap<>();

	public static final String REGEX_CAMEL_CASE = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|\\W+";

	private TF_IDF() {
	}

	public static Map<File, Map<String, Long>> getDocumentMap(File f) throws IOException {

		if (!f.isDirectory()) {
			if (f.getName().endsWith(".java")) {
				Map<String, Long> frequencyMap = TF_IDF.getFrequencyMap(f);
				MAPA_DOCUMENTO.put(f, frequencyMap);
			}
		} else {
			List<String> asList = Arrays.asList(f.list());
			asList.forEach(a -> {
				try {
					getDocumentMap(new File(f, a));
				} catch (Exception e) {
					logger.error("", e);
				}
			});
		}
		return MAPA_DOCUMENTO;
	}

	/*
	 * Modos de calcular tf(t,d)=
	 * 
	 * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
	 * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if
	 * f(t, d) is zero; augmented frequency, to prevent a bias towards longer
	 * documents, e.g. raw frequency divided by the maximum raw frequency of any
	 * term in the document:
	 * 
	 * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
	 * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
	 */
	public static Map<String, Long> getFrequencyMap(File d) throws IOException {
		try (BufferedReader bufferedReader = Files.newBufferedReader(d.toPath());) {

			String readLine;
			Map<String, Long> collect = new ConcurrentHashMap<>();
			do {
				readLine = bufferedReader.readLine();
				if (readLine != null) {

					String[] split = readLine.split(REGEX_CAMEL_CASE);
					List<String> asList = Arrays.asList(split);
					asList.parallelStream().filter(a -> !a.isEmpty()).reduce(collect, (mapa, a) -> {
						if (mapa.containsKey(a.toLowerCase())) {
							mapa.put(a.toLowerCase(), mapa.get(a.toLowerCase()) + 1L);
						} else {
							mapa.put(a.toLowerCase(), 1L);
						}
						return mapa;
					}, (mapa1, mapa2) -> mapa1);
				}
			} while (readLine != null);
			bufferedReader.close();

			return collect;
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		}
	}

	private static double getInverseDocumentFrequency(String p) {
		Set<Entry<File, Map<String, Long>>> entrySet = MAPA_DOCUMENTO.entrySet();
		double idf = 1D;
		for (Entry<File, Map<String, Long>> entry : entrySet) {
			if (entry.getValue().containsKey(p)) {
				idf += 1;
			}
		}

		return Math.log(MAPA_DOCUMENTO.size() / idf);
	}

	private static double getTermFrequency(Long fre) {
		return fre == 0 ? 0D : 1 + Math.log(fre);
	}

	public static void main(String[] args) throws IOException {
		try {

			File arquivo = new File("src");
			Map<File, Map<String, Long>> documentMap = getDocumentMap(arquivo);
			documentMap.forEach((c, v) -> v.forEach((p, fre) -> {
				double idf = getInverseDocumentFrequency(p);
				if (!TF_IDF.MAP_TF_IDF.containsKey(p)) {
					MAP_TF_IDF.put(p, new HashMap<File, Double>());
				}
				Double termFrequency = getTermFrequency(fre);
				MAP_TF_IDF.get(p).put(c, idf * termFrequency);
			}));
			// MAP_TF_IDF =
			List<Entry<String, Map<File, Double>>> entrySet = new ArrayList<>(MAP_TF_IDF.entrySet());

			entrySet.sort(new ValueComparator());
			// MAP
			File file = new File("resultado.txt");

			System.out.println(file.getAbsolutePath());
			printWordFound(entrySet, file);
		} catch (Exception e2) {
			logger.error("", e2);
		}
	}

	private static void printWordFound(List<Entry<String, Map<File, Double>>> entrySet, File file) throws Exception {
		try (final PrintStream out = new PrintStream(file, StandardCharsets.UTF_8.displayName());) {
			List<String> javaKeywords = Arrays.asList("abstract", "continue", "for", "new", "switch", "assert",
					"default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
					"double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws",
					"case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try",
					"char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp",
					"volatile", "const", "float", "native", "super", "while");

			entrySet.forEach(e -> {
				if (!javaKeywords.contains(e.getKey())) {
					out.println(e.getKey() + "={");
					e.getValue().forEach((f, d) -> out.println("   " + f.getName() + "=" + d));
					out.println("}");
				}
			});
		} catch (Exception e2) {
			throw e2;
		}
	}
}
