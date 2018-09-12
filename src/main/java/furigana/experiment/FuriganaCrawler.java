package furigana.experiment;

import java.lang.Character.UnicodeBlock;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public class FuriganaCrawler implements HasLogging {



    private static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
			UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
			UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
			UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
			UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);

    public void migrateCities() {
        try (Stream<String> lines = Files.lines(ResourceFXUtils.toPath("hp1Tex2.tex"))) {
            lines.forEach(line -> {
            	String[] split = line.split("");
            	String currentWord = "";
            	UnicodeBlock currentBlock=null;
            	for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
            		char currentLetter = split[i].charAt(0);
            		UnicodeBlock of = UnicodeBlock.of(currentLetter);
            		if(KANJI_BLOCK.contains(of)) {
            			currentWord += currentLetter;
            		}
            		if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && !currentWord.isEmpty()) {
                        getLogger().info("{}={}", currentWord, getReading(currentWord));
            			currentWord = "";
            		}
            		currentBlock = of;
            	}
            });
        } catch (Exception e) {
            getLogger().error("", e);
        }
	}


    @SuppressWarnings("deprecation")
    private String getReading(String currentWord) {
		Connection connect = Jsoup.connect("http://jisho.org/search/" + URLEncoder.encode(currentWord));

		try {
			Document parse = connect

					.get();

            Elements select = parse.select(".concept_light-representation .text:contains(\"" + currentWord + "\")");
            if (!select.isEmpty()) {
				for (Element element : select) {
                    Element link = element.parent();

                    if (element.text().equals(currentWord)) {
                        return link.select(".furigana").text();
					}

				}
			}
		} catch (Exception e) {
			getLogger().error("ERRO " + currentWord, e);
		}
		return currentWord;
	}

    public static void main(String[] args) {
		new FuriganaCrawler().migrateCities();
	}


}
