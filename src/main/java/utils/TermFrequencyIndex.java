package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.exception.RuntimeIOException;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;

public final class TermFrequencyIndex {
    private static final Map<String, Map<File, Double>> MAP_TF_IDF = new HashMap<>();
    /**
     * 
     * Tf, in its basic form, is just the frequency that we look up in appropriate
     * table. In this case, it's one.
     * 
     * Idf is a bit more involved:
     * 
     * idf("this", D) = log( N/f(t,D))
     * 
     * The numerator of the fraction is the number of documents, which is two. The
     * number of documents in which "this" appears is also two, giving
     * 
     * idf("this", D) = log (2/2) = 0
     * 
     * So tf-idf is zero for this term, and with the basic definition this is true
     * of any term that occurs in all documents.
     * 
     * A slightly more interesting example arises from the word "example", which
     * occurs three times but in only one document. For this document, tf-idf of
     * "example" is:
     * 
     * D = conjunto total de documento
     * 
     * d_i = um documento i
     * 
     * tf("example", d_i) = 3 idf("example", D) = log (2/1) ~= 0.3010
     * tfidf("example", d_i) = tf("example", d_i) x idf("example", D) = 3 x 0.3010 =
     * 0.9030
     * 
     * Modos de calcular tf(t,d)=
     * 
     * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
     * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if f(t,
     * d) is zero; augmented frequency, to prevent a bias towards longer documents,
     * e.g. raw frequency divided by the maximum raw frequency of any term in the
     * document:
     * 
     * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
     * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
     */
    private static final Map<File, Map<String, Long>> MAPA_DOCUMENTO = new HashMap<>();

    /**
     * Modos de calcular tf(t,d)=
     * 
     * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
     * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if f(t,
     * d) is zero; augmented frequency, to prevent a bias towards longer documents,
     * e.g. raw frequency divided by the maximum raw frequency of any term in the
     * document:
     * 
     * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
     * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
     */

    private TermFrequencyIndex() {
    }

    public static void identifyKeyWordsInSourceFiles() {
        RunnableEx.run(() -> {
            File arquivo = new File("src");
            Map<File, Map<String, Long>> documentMap = getDocumentMap(arquivo);
            documentMap.forEach((c, v) -> v.forEach((p, fre) -> {
                double idf = getInverseDocumentFrequency(p);
                double termFrequency = getTermFrequency(fre);
                MAP_TF_IDF.computeIfAbsent(p, m -> new HashMap<>()).put(c, idf * termFrequency);
            }));
            // MAP_TF_IDF =
            List<Entry<String, Map<File, Double>>> entrySet = new ArrayList<>(MAP_TF_IDF.entrySet());

            entrySet.sort(TermFrequencyIndex::compare);
            // MAP
            File file = ResourceFXUtils.getOutFile("txt/resultado.txt");
            printWordFound(entrySet, file);
        });
    }

    private static int compare(Entry<String, Map<File, Double>> a, Entry<String, Map<File, Double>> b) {
        double da = 0D;
        for (Entry<File, Double> entry : a.getValue().entrySet()) {
            double value = entry.getValue().doubleValue();
            da = da < value ? value : da;
        }
        double db = 0D;
        for (Entry<File, Double> entry : b.getValue().entrySet()) {
            double value = entry.getValue().doubleValue();
            db = db < value ? value : db;
        }

        return Double.compare(db, da);
    }

    private static Map<File, Map<String, Long>> getDocumentMap(File f) {

        if (!f.isDirectory()) {
            if (f.getName().endsWith(".java")) {
                Map<String, Long> frequencyMap = TermFrequencyIndex.getFrequencyMap(f);
                MAPA_DOCUMENTO.put(f, frequencyMap);
            }
        } else {
            List<String> asList = Arrays.asList(f.list());
            ConsumerEx.foreach(asList, a -> getDocumentMap(new File(f, a)));
        }
        return MAPA_DOCUMENTO;
    }

    /**
     * Modos de calcular tf(t,d)=
     * 
     * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;
     * logarithmically scaled frequency: tf(t,d) = 1 + log f(t,d), or zero if f(t,
     * d) is zero; augmented frequency, to prevent a bias towards longer documents,
     * e.g. raw frequency divided by the maximum raw frequency of any term in the
     * document:
     * 
     * \mathrm{tf}(t,d) = 0.5 + \frac{0.5 \times \mathrm{f}(t,
     * d)}{\max\{\mathrm{f}(w, d):w \in d\}}
     */
    private static Map<String, Long> getFrequencyMap(File d) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(d.toPath())) {

            String readLine;
            Map<String, Long> frequencyMap = new ConcurrentHashMap<>();
            do {
                readLine = bufferedReader.readLine();
                if (readLine != null) {

                    String[] splitByCamelCase = StringSigaUtils.splitCamelCase(readLine);
                    List<String> asList = Arrays.asList(splitByCamelCase);
                    asList.parallelStream().filter(a -> !a.isEmpty()).reduce(frequencyMap, (mapa, a) -> {
                        Long long1 = mapa.computeIfAbsent(a.toLowerCase(), m -> 0L);
                        mapa.put(a.toLowerCase(), long1 + 1L);
                        return mapa;
                    }, (mapa1, mapa2) -> mapa1);
                }
            } while (readLine != null);

            return frequencyMap;
        } catch (IOException e) {
            throw new RuntimeIOException("ERROR READING FILE", e);
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

    private static double getTermFrequency(long fre) {
        return fre == 0 ? 0D : 1 + Math.log(fre);
    }

    private static void printWordFound(List<Entry<String, Map<File, Double>>> entrySet, File file) {
        RunnableEx.run(() -> {
            try (final PrintStream out = new PrintStream(file, StandardCharsets.UTF_8.displayName())) {
                entrySet.forEach(e -> {
                    if (!TermFrequency.getJavaKeywords().contains(e.getKey())) {
                        out.println(e.getKey() + "={");
                        e.getValue().forEach((f, d) -> out.println("   " + f.getName() + "=" + d));
                        out.println("}");
                    }
                });
            }
        });
    }
}
