package furigana.experiment;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.Character.UnicodeBlock;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import election.experiment.CrawlerTask;
import simplebuilder.HasLogging;

public class CrawlerFuriganaTask extends CrawlerTask {

    private static final int NUMBER_THREADS = 5;

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
            int k = j;
            Thread thread = new Thread(() -> {
                String line = lines.get(k);
                StringBuilder currentLine = placeFurigana(line);
                System.out.println(currentLine);
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
        try (PrintStream printStream = new PrintStream("hp1Tex2Converted.tex");) {
            for (String s : lines) {
                printStream.println(s);
            }
        } catch (Exception e) {
            getLogger().error("ERROR ", e);
        }

        updateAll(total, total);


        return "Completed at " + LocalTime.now();
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

    private List<String> getLines() {
        List<String> lines = new ArrayList<>();
        try (Stream<String> lines2 = Files.lines(Paths.get("hp1Tex2.tex"));) {
            lines.addAll(lines2.collect(Collectors.toList()));
        } catch (IOException e) {
            getLogger().error("ERROR ", e);
        }
        return lines;
    }

    public static void main(String[] args) {
        new CrawlerFuriganaTask().migrateCities();
    }

    protected static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
            UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);

    public void migrateCities() {
        // insertProxyConfig()
        try (Stream<String> lines = Files.lines(Paths.get("hp1Tex2.tex"));) {
            lines.forEach(line -> {
                String[] split = line.split("");
                String currentWord = "";
                UnicodeBlock currentBlock = null;
                for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
                    char currentLetter = split[i].charAt(0);
                    UnicodeBlock of = UnicodeBlock.of(currentLetter);
                    if (KANJI_BLOCK.contains(of)) {
                        currentWord += currentLetter;
                    }
                    if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && !currentWord.isEmpty()) {
                        System.out.println(currentWord + "=" + getReading(currentWord, currentLetter));

                        currentWord = "";
                    }
                    currentBlock = of;
                }

            });
            System.out.println();
        } catch (Exception e) {
            HasLogging.log().error("", e);
        }
    }


    Map<String, String> mapReading = new HashMap<>();

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
                List<String> collect2 = kun.stream().map(Element::text).collect(Collectors.toList());
                List<String> collect = collect2.stream().map(e -> e.split("\\.")[0]).distinct()
                        .collect(Collectors.toList());
                if (collect.size() == 1) {
                    String string = collect.get(0);
                    mapReading.put(key, string);
                    return string;
                }
                List<String> oi = collect2.stream().filter(e -> e.contains(".") && !e.contains("-"))
                        .filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).collect(Collectors.toList());
                if (!oi.isEmpty()) {
                    String string = oi.get(0).split("\\.")[0];
                    mapReading.put(key, string);
                    return string;
                }
                if (collect2.stream().anyMatch(JapaneseVerbConjugate::isVerb)) {
                    Optional<String> findFirst = collect2.stream().filter(e -> e.contains("."))
                            .peek(e -> {
                                String[] split = e.split("\\.");
                                mapReading.put(currentWord + split[1].charAt(0), split[0]);
                            }).filter(JapaneseVerbConjugate::isVerb)
                            .flatMap(e -> JapaneseVerbConjugate.conjugateVerb(e).stream()).peek(e -> {
                                String[] split = e.split("\\.");
                                mapReading.put(currentWord + split[1].charAt(0), split[0]);
                            }).filter(e -> e.split("\\.")[1].charAt(0) == currentLetter).findFirst();
                    if(findFirst.isPresent()) {
                        String string = findFirst.get().split("\\.")[0];
                        mapReading.put(key, string);
                        return string;
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

    private boolean matchesCurrentWord(String currentWord, char currentLetter, Element link) {
        return link.text().equals(currentWord) || link.text().equals(currentWord + currentLetter);
    }



}