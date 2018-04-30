package furigana.experiment;

import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import simplebuilder.HasLogging;

public class FuriganaCrawler implements HasLogging {



	public static void main(String[] args) throws IOException {
		new FuriganaCrawler().migrateCities();
	}

	public static final List<UnicodeBlock> KANJI_BLOCK = Arrays.asList(UnicodeBlock.CJK_COMPATIBILITY,
			UnicodeBlock.CJK_COMPATIBILITY_FORMS, UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
			UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
			UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
			UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);


	public void migrateCities() throws IOException {
		Files.lines(Paths.get("C:\\Users\\guigu\\Documents\\Estudo\\hp1Tex2.tex")).forEach(line -> {
			String[] split = line.split("");
			String current="";
			UnicodeBlock currentBlock=null;
			for (int i = 0; i < split.length && !split[i].isEmpty(); i++) {
				char charAt = split[i].charAt(0);
				UnicodeBlock of = UnicodeBlock.of(charAt);
				if(KANJI_BLOCK.contains(of)) {
					current+=charAt;
				}
				if (KANJI_BLOCK.contains(currentBlock) && !KANJI_BLOCK.contains(of) && !current.isEmpty()) {
					System.out.println(current);
					current = "";
				}
				currentBlock = of;
			}
			
			
			
			
		});
		System.out.println();
		// List<String> asList = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es",
		// "go", "ma", "mg", "ms", "mt",
		// "pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp",
		// "to");
		// String alphabet = "abcdefghijklmnopqrstuvwxyz";z
		// for (String estado : asList) {
		// for (String letter : alphabet.split("")) {
		// Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br/" +
		// estado + "/" + letter + "/");
		// try {
		// Document parse = connect
		// .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101
		// Firefox/52.0")
		// .get();
		//
		// Elements select = parse.select(".lista-estados .custom li");
		// for (Element element : select) {
		// Element link = element.select("a").first();
		// link.attr("href");
		// element.select("span").first().text().replaceAll("\\D", "");
		// }
		// } catch (Exception e) {
		// getLogger().error("ERRO cidade " + estado + " " + letter, e);
		// }
		// }
		// }
		// HibernateUtil.shutdown();
	}

    private Integer convertNumerico(String eleitores) {
        String replaceAll = eleitores.replaceAll("[^0-9]", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
	}

}
