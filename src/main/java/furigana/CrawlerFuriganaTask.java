package furigana;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Character.UnicodeBlock;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class CrawlerFuriganaTask extends CrawlerTask {

    private static final int NUMBER_THREADS = 5;

    protected static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
            UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);

    private Map<String, String> mapReading = Collections.synchronizedMap(new HashMap<>());

    public void addFuriganaReading() {
        CrawlerTask.insertProxyConfig();
        try (Stream<String> lines = Files.lines(ResourceFXUtils.toPath("hp1Tex2.tex"))) {
            lines.forEach(line -> {
                String[] split = line.split("");
                StringBuilder currentWord = new StringBuilder();
                UnicodeBlock currentBlock = null;
                for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
                    char currentLetter = split[i].charAt(0);
                    UnicodeBlock of = UnicodeBlock.of(currentLetter);
                    if (KANJI_BLOCK.contains(of)) {
                        currentWord.append(currentLetter);
                    }
                    if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && currentWord.length() != 0) {
                        String w = currentWord.toString();
                        getLogger().trace("{}={}", w, getReading(w, currentLetter));
                        currentWord.delete(0, currentWord.length());
                    }
                    currentBlock = of;
                }
            });
        } catch (Exception e) {
            HasLogging.log().error("", e);
        }
    }

    public String getReading(String currentWord, char currentLetter) {
        return getReading(currentWord, currentLetter, true);
    }

    public String getReading(String currentWord, char currentLetter, boolean recursive) {
        String key = currentWord + currentLetter;
        if (mapReading.containsKey(key)) {
            return mapReading.get(key);
        }
        try {
			Document parse = getDocument("http://jisho.org/search/" + URLEncoder.encode(currentWord, "UTF-8"));

            Elements kun = parse.select(".readings .japanese_gothic a");
            if (currentWord.length() == 1 && !kun.isEmpty()) {
                if (kun.size() == 1) {
                    String string = kun.text().split("\\.")[0];
                    mapReading.put(key, string);
                    return string;
                }
                List<String> kunReadings = kun.stream().map(Element::text).collect(Collectors.toList());
                List<String> kunWithOfurigana = kunReadings.stream().map(e -> e.split("\\.")[0]).distinct()
                        .collect(Collectors.toList());
                if (kunWithOfurigana.size() == 1) {
                    String string = kunWithOfurigana.get(0);
                    mapReading.put(key, string);
                    return string;
                }
                List<String> ofuriganaMatches = kunReadings.stream().filter(e -> e.contains(".") && !e.contains("-"))
                        .filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).collect(Collectors.toList());
                if (!ofuriganaMatches.isEmpty()) {
                    String string = ofuriganaMatches.get(0).split("\\.")[0];
                    mapReading.put(key, string);
                    return string;
                }
                if (kunReadings.stream().anyMatch(JapaneseVerbConjugate::isVerb)) {
                    Optional<String> conjugatedReadings = kunReadings.stream().filter(e -> e.contains("."))
                            .peek(e -> {
                                String[] split = e.split("\\.");
                                mapReading.put(currentWord + split[1].charAt(0), split[0]);
                            }).filter(JapaneseVerbConjugate::isVerb)
                            .flatMap(e -> JapaneseVerbConjugate.conjugateVerb(e).stream()).peek(e -> {
                                String[] split = e.split("\\.");
                                mapReading.put(currentWord + split[1].charAt(0), split[0]);
                            }).filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).findFirst();
                    if (conjugatedReadings.isPresent()) {
                        String finalReading = conjugatedReadings.get().split("\\.")[0];
                        mapReading.put(key, finalReading);
                        return finalReading;
                    }
                    
                }

            }


			Elements select = parse.select(".concept_light-representation ");
			for (Element element : select) {
				Element link = element.select(".text").first();
                if (matchesCurrentWord(currentWord, currentLetter, link)) {
					String text = element.select(".furigana").text();
					mapReading.put(key, text);
					return text;
                }
            }
			Elements twoWord = parse.select(".japanese_word__furigana ");
			if (!twoWord.isEmpty()) {
				String text = twoWord.text();
				if (text.equals(currentWord)) {
					mapReading.put(key, text);
					return text;
				}
			}

        } catch (Exception e) {
            getLogger().error("ERRO " + currentWord, e);
            if (recursive) {
                return getReading(currentWord, currentLetter, false);
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
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (int j = 0; j < lines.size(); j++) {
            if (isCancelled()) {
                return "Cancelled";
            }
            int k = j;
            Thread thread = new Thread(() -> {
                String line = lines.get(k);
                StringBuilder currentLine = placeFurigana(line);
                getLogger().info("{}", currentLine);
                lines.set(k, currentLine.toString());

            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > NUMBER_THREADS) {
                count = ths.stream().filter(Thread::isAlive).count();
                long i = ths.size() - count;
                updateAll(i, total);
            }

            updateProgress(j, lines.size());
        }

        while (ths.stream().anyMatch(Thread::isAlive)) {
            long count = ths.stream().filter(Thread::isAlive).count();
            long i = ths.size() - count;
            updateAll(i, total);
        }
        try (PrintStream printStream = new PrintStream(
                new File(ResourceFXUtils.toFile("out"), "hp1Tex2Converted.tex"),
                StandardCharsets.UTF_8.displayName())) {
            for (String s : lines) {
                printStream.println(s);
            }
        } catch (Exception e) {
            getLogger().error("ERROR ", e);
        }

        updateAll(total, total);


        return "Completed at " + LocalTime.now();
    }


    private List<String> getLines() {
        List<String> lines = new ArrayList<>();
        try (Stream<String> lines2 = Files.lines(ResourceFXUtils.toPath("hp1Tex2.tex"))) {
            lines.addAll(lines2.collect(Collectors.toList()));
        } catch (IOException e) {
            getLogger().error("ERROR ", e);
        }
        return lines;
    }

    private static boolean matchesCurrentWord(String currentWord, char currentLetter, Element link) {
        return link.text().equals(currentWord) || link.text().equals(currentWord + currentLetter);
    }

    private StringBuilder placeFurigana(String line) {
        String[] split = line.split("");
        String currentWord = "";
        StringBuilder currentLine = new StringBuilder();
        UnicodeBlock currentBlock = null;
        for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
            char currentLetter = split[i].charAt(0);
            UnicodeBlock of = UnicodeBlock.of(currentLetter);
            if (KANJI_BLOCK.contains(of)) {
                currentWord += currentLetter;
            }
            if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && !currentWord.isEmpty()) {
                String reading = getReading(currentWord, currentLetter);
                if (currentWord.equals(reading)) {
                    currentLine.append(currentWord);
                } else {
                    currentLine.append(String.format("$\\stackrel{\\text{%s}}{\\text{%s}}$", reading, currentWord));
                }
                currentWord = "";
            }
            if (!KANJI_BLOCK.contains(of)) {
                currentLine.append(currentLetter);
            }
            currentBlock = of;
        }
        return currentLine;
    }

    public static void main(String[] args) {
        new CrawlerFuriganaTask().addFuriganaReading();
    }



}