package furigana;

import static utils.FunctionEx.makeFunction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.*;

public class CrawlerFuriganaTask extends CrawlerTask {

    private static final String FURIGANA_READING = "hiraganaReading.txt";

    private static final Logger LOG = HasLogging.log();

    private static final int NUMBER_THREADS = 20;

    protected static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
            UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);
    private static final Pattern NUMBERS = Pattern.compile("^[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b"
            + "\u4e5d\u5341\u5341\u4e00\u5341\u4e8c\u4e8c\u5341\u4e94\u5341\u767e\u5343\u4e07\u5104\u5146]+");
    private static final int STEP = 10;

    private static final String URL_BASE = "http://jisho.org/search/";

    private ObservableMap<String, String> mapReading =
            FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());
    private List<Character> skipCharacters = Arrays.asList('、', 'を', '？', '}', '　', '』', '！', '…', '。', '）', '（');

    private List<Character> repeatCharacters = Arrays.asList('々');

    private BufferedWriter output;

    public CrawlerFuriganaTask() {
        readHiraganaFiles();
    }

    public String getReading(String currentWord, char currentLetter) {
        String key = skipCharacters.contains(currentLetter) ? currentWord : currentWord + currentLetter;
        String reading = getReading(currentWord, currentLetter, 0);
        return FunctionEx.mapIf(reading, s -> s, key);
    }

    public String getReading(String currentWord, char currentLetter, int recursive) {
        final String key = skipCharacters.contains(currentLetter) ? currentWord : currentWord + currentLetter;
        try {
            return mapReading.computeIfAbsent(key,
                    k -> SupplierEx.remap(() -> computeReading(currentWord, currentLetter), "ERRO " + currentWord));
        } catch (Exception e) {
            LOG.error("ERRO " + currentWord, e);
            if (recursive < 2) {
                return getReading(currentWord, currentLetter, recursive + 1);
            }
        }
        return currentWord;
    }

    @Override
    protected String task() {
        insertProxyConfig();
        List<String> lines = getLines();
        updateTitle("Example Task");
        updateMessage("Starting...");
        int total = lines.size();
        List<Thread> ths = new ArrayList<>();
        for (int j = 0; j < lines.size(); j += STEP) {
            if (isCancelled()) {
                return "Cancelled";
            }
            int k = j;
            Thread thread = new Thread(() -> {
                for (int i = 0; i < STEP && k + i < lines.size(); i++) {
                    if (isCancelled()) {
                        return;
                    }
                    String line = lines.get(k + i);
                    StringBuilder currentLine = placeFurigana(line);
                    lines.set(k + i, currentLine.toString());
                }
            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > NUMBER_THREADS) {
                count = ths.stream().filter(Thread::isAlive).count();
                long i = ths.size() - count;
                updateAll(i, total / STEP);
                if (isCancelled()) {
                    return "Cancelled";
                }
            }
        }
        while (ths.stream().anyMatch(Thread::isAlive)) {
            long count = ths.stream().filter(Thread::isAlive).count();
            long i = ths.size() - count;
            updateAll(i, total / STEP);
        }
        endTask(lines);
        updateAll(total / STEP, total / STEP);
        return "Completed at " + LocalTime.now();
    }

    private String computeReading(String key) {
        return mapReading.computeIfAbsent(key, s -> FunctionEx
                .apply(k -> computeReading(k.substring(0, k.length() - 1), k.charAt(k.length() - 1)), s));
    }

    private String computeReading(String currentWord, char currentLetter) throws IOException {
        Document parse = createDocument(currentWord, currentLetter);
        Elements kun = parse.select(".readings .japanese_gothic a");

        if (existsKunReading(currentWord, kun)) {
            if (kun.size() == 1) {
                return kun.text().split("\\.")[0];
            }
            List<String> kunReadings = kun.stream().map(Element::text).collect(Collectors.toList());
            List<String> kunWithOfurigana =
                    kunReadings.stream().map(e -> e.split("\\.")[0]).distinct().collect(Collectors.toList());
            if (kunWithOfurigana.size() == 1) {
                return kunWithOfurigana.get(0);
            }
            Optional<String> ofuriganaMatches = kunReadings.stream().filter(e -> e.contains(".") && !e.contains("-"))
                    .filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).findFirst();
            if (ofuriganaMatches.isPresent()) {
                return ofuriganaMatches.get().split("\\.")[0];
            }
            Optional<String> conjugatedReadings = conjugate(currentWord, currentLetter, kunReadings);
            if (conjugatedReadings.isPresent()) {
                return conjugatedReadings.get().split("\\.")[0];
            }
        }
        Optional<Element> firstRepresentation = parse.select(".concept_light-representation ").stream()
                .filter(element -> matchesCurrentWord(currentWord, currentLetter, element.select(".text").first()))
                .findFirst();
        if (firstRepresentation.isPresent()) {
            return firstRepresentation.get().select(".furigana").text();
        }
        if (currentWord.length() > 1) {
            return splitWord(currentWord, currentLetter);
        }

        return currentWord;
    }

    private Optional<String> conjugate(String currentWord, char currentLetter, List<String> kunReadings) {
        if (kunReadings.stream().noneMatch(JapaneseVerbConjugate::isVerb)) {
            return Optional.empty();
        }

        return kunReadings.stream().filter(e -> e.contains(".")).peek(e -> {
            String[] split = e.split("\\.");
            mapReading.putIfAbsent(currentWord + split[1].charAt(0), split[0]);
        }).filter(JapaneseVerbConjugate::isVerb).flatMap(e -> JapaneseVerbConjugate.conjugateVerb(e).stream())
                .peek(e -> {
                    String[] split = e.split("\\.");
                    mapReading.putIfAbsent(currentWord + split[1].charAt(0), split[0]);
                }).filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).findFirst();
    }

    private Document createDocument(String currentWord, char currentLetter) throws IOException {
        if (repeatCharacters.contains(currentLetter)) {
            return ExtractUtils.getDocument(
                    URL_BASE + StringSigaUtils.codificar(currentWord + currentWord.charAt(currentWord.length() - 1)));
        }
        return ExtractUtils.getDocument(URL_BASE + StringSigaUtils.codificar(currentWord));
    }

    private void log(String a, String b) {
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return;
        }
        LOG.info("{}={}", a, b);
        RunnableEx.run(() -> {
            if (output == null) {
                output = new BufferedWriter(
                        new FileWriterWithEncoding(ResourceFXUtils.getOutFile(FURIGANA_READING), "UTF-8", true));
            }
            synchronized (output) {
                output.append(a + "=" + b + "\n");
                output.flush();
            }
        });

    }

    private String onReading(String str) {
        return Objects.toString(mapReading.computeIfAbsent(str, makeFunction(CrawlerFuriganaTask::getOnReadings)), "");
    }

    private StringBuilder placeFurigana(String line) {
        String[] split = line.split("");
        StringBuilder currentWord = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        UnicodeBlock currentBlock = null;
        for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
            char currentLetter = split[i].charAt(0);
            UnicodeBlock of = UnicodeBlock.of(currentLetter);
            if (KANJI_BLOCK.contains(of)) {
                currentWord.append(currentLetter);
            }
            if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && currentWord.length() > 0) {
                String reading = getReading(currentWord.toString(), currentLetter);
                if (reading.equals(currentWord.toString())) {
                    currentLine.append(currentWord);
                } else {
                    currentLine.append(String.format("$\\stackrel{\\text{%s}}{\\text{%s}}$", reading, currentWord));
                }
                currentWord.delete(0, currentWord.length());
            }
            if (!KANJI_BLOCK.contains(of)) {
                currentLine.append(currentLetter);
            }
            currentBlock = of;
        }
        return currentLine;
    }

    private final void readHiraganaFiles() {
        RunnableEx.run(() -> {
            File outFile = ResourceFXUtils.getOutFile(FURIGANA_READING);
            if (outFile.exists()) {
                Files.lines(outFile.toPath(), StandardCharsets.UTF_8).forEach(ConsumerEx.makeConsumer(l -> {
                    String[] split = l.split("=");
                    mapReading.put(split[0], split[1]);
                }));
            }
            mapReading.addListener((MapChangeListener<String, String>) change -> {
                String key = change.getKey();
                String valueAdded = change.getValueAdded();
                log(key, valueAdded);
            });
        });
    }

    private String splitWord(String currentWord, char currentLetter) {
        Matcher matcher = NUMBERS.matcher(currentWord);
        if (matcher.find()) {
            String group = matcher.group();
            String sb = currentWord.substring(group.length());
            if (sb.isEmpty()) {
                return computeReading(group);
            }
            String onReadings = onReading(sb);
            if (sb.length() > 1 && (StringUtils.isBlank(onReadings) || !isNotKatakana(onReadings))) {
                onReadings = onReading(sb.substring(0, 1)) + computeReading(sb.substring(1) + currentLetter);
            }
            String computeReading = computeReading(group + sb.charAt(0));
            return computeReading + onReadings;
        }
        String firstPart = currentWord.substring(0, currentWord.length() - 1);
        String secondPart = currentWord.substring(currentWord.length() - 1, currentWord.length());
        return computeReading(firstPart + secondPart.charAt(0)) + computeReading(secondPart + currentLetter);
    }

    private static void endTask(List<String> lines) {
        RunnableEx.run(() -> Files.write(ResourceFXUtils.getOutFile("hp1Tex2Converted.tex").toPath(), lines,
                StandardCharsets.UTF_8));
    }

    private static boolean existsKunReading(String currentWord, Elements kun) {
        return currentWord.length() == 1 && !kun.isEmpty();
    }

    private static List<String> getLines() {
        List<String> lines = new ArrayList<>();
        RunnableEx.run(() -> {
            try (Stream<String> lines2 = Files.lines(ResourceFXUtils.toPath("hp1Tex2.tex"))) {
                lines.addAll(lines2.collect(Collectors.toList()));
            }
        });
        return lines;
    }

    private static String getOnReadings(String currentWord) throws IOException {
        String url = "http://jisho.org/search/" + URLEncoder.encode(currentWord, "UTF-8");
        Document parse = ExtractUtils.getDocument(url);
        Optional<Element> firstRepresentation = parse.select(".concept_light-representation ").stream()
                .filter(element -> element.select(".text").first().text().equals(currentWord)).findFirst();
        if (firstRepresentation.isPresent() && currentWord.length() > 1) {
            return firstRepresentation.get().select(".furigana").text();
        }
        Elements kun = parse.select(".readings .japanese_gothic a");
        List<String> kunReadings = kun.stream().map(Element::text).collect(Collectors.toList());
        return kunReadings.stream().sorted(Comparator.comparing(CrawlerFuriganaTask::isNotKatakana)).findFirst()
                .orElse(null);
    }

    private static boolean isNotKatakana(String e) {
        return e.codePoints().allMatch(a -> UnicodeBlock.KATAKANA != UnicodeBlock.of(a));
    }

    private static boolean matchesCurrentWord(String currentWord, char currentLetter, Element link) {
        return link.text().equals(currentWord) || link.text().equals(currentWord + currentLetter);
    }

}