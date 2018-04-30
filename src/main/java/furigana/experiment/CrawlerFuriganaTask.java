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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import election.experiment.CrawlerTask;

public class CrawlerFuriganaTask extends CrawlerTask {

    // private CidadeDAO cidadeDAO = new CidadeDAO();

    private static final int NUMBER_THREADS = 5;

    @Override
    protected String task() {
        insertProxyConfig();
        List<String> lines = new ArrayList<>();
        try {
            lines.addAll(Files.lines(Paths.get("hp1Tex2.tex")).collect(Collectors.toList()));
        } catch (IOException e) {
            getLogger().error("ERROR ", e);
        }
        updateTitle("Example Task");
        updateMessage("Starting...");
        int total = lines.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (int j = 0; j < lines.size(); j++) {
            int k = j;
            Thread thread = new Thread(() -> {
                String line = lines.get(k);
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

    public static void main(String[] args) throws IOException {
        new CrawlerFuriganaTask().migrateCities();
    }

    protected static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
            UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
            UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);

    public void migrateCities() throws IOException {
        insertProxyConfig();
        Files.lines(Paths.get("hp1Tex2.tex")).forEach(line -> {
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
    }

    String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
    Map<String, String> mapReading = new HashMap<>();

    public String getReading(String currentWord, char currentLetter) {
        return getReading(currentWord, currentLetter, true);
    }

    public String getReading(String currentWord, char currentLetter, boolean recursive) {
        String key = currentWord + currentLetter;
        if (mapReading.containsKey(key)) {
            return mapReading.get(key);
        }


        Connection connect = Jsoup.connect("http://jisho.org/search/" + URLEncoder.encode(currentWord));
        connect.header("Proxy-Authorization", "Basic " + encoded);

        try {
            Document
                parse = connect
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
                    .get();

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


            if (currentWord.length() >= 1) {
                Elements select = parse.select(".concept_light-representation ");
                for (Element element : select) {
                    Element link = element.select(".text").first();
                    if (link.text().equals(currentWord)) {
                        String text = element.select(".furigana").text();
                        mapReading.put(key, text);
                        return text;
                    }
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


}