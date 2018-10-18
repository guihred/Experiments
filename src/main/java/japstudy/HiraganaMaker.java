package japstudy;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class HiraganaMaker {

    private static final Map<String, String> HIRAGANA_MAP = ImmutableMap.<String, String>builder().put("a", "あ")
            .put("i", "い").put("u", "う").put("e", "え").put("o", "お").put("ka", "か").put("ki", "き").put("ku", "く")
            .put("ke", "け").put("ko", "こ").put("kya", "きゃ").put("kyu", "きゅ").put("kkyu", "っきゅ").put("kyo", "きょ")
            .put("kkyo", "っきょ")
            .put("kka", "っか")
            .put("kki", "っき").put("kku", "っく").put("kke", "っけ").put("kko", "っこ").put("sa", "さ").put("shi", "し")
            .put("su", "す").put("se", "せ").put("so", "そ").put("sha", "しゃ").put("ssha", "っしゃ").put("shu", "しゅ")
            .put("sshu", "っしゅ")
            .put("sho", "しょ")
            .put("ssho", "っしょ")
            .put("ssa", "っさ").put("sshi", "っし").put("ssu", "っす").put("sse", "っせ").put("sso", "っそ").put("ta", "た")
            .put("chi", "ち").put("ti", "ち").put("tsu", "つ").put("te", "て").put("to", "と").put("cha", "ちゃ")
            .put("tcha", "っちゃ")
            .put("ccha", "っちゃ")
            .put("chu", "ちゅ")
            .put("tchu", "っちゅ")
            .put("che", "チェ")
            .put("cho", "ちょ").put("tta", "った").put("tchi", "っち").put("tcho", "っちょ").put("tti", "っち").put("cchi", "っち")
            .put("ttsu", "っつ")
            .put("tte", "って")
            .put("tto", "っと")
            .put("na", "な").put("ni", "に").put("nu", "ぬ").put("ne", "ね").put("no", "の").put("nya", "にゃ")
            .put("nyu", "にゅ").put("nyo", "にょ").put("ha", "は").put("hi", "ひ").put("fu", "ふ").put("he", "へ")
            .put("ho", "ほ").put("hya", "ひゃ").put("hyu", "ひゅ").put("hyo", "ひょ").put("ma", "ま").put("mi", "み")
            .put("mu", "む").put("me", "め").put("mo", "も").put("mya", "みゃ").put("myu", "みゅ").put("myo", "みょ")
            .put("ya", "や").put("yu", "ゆ").put("yo", "よ").put("ra", "ら").put("ri", "り").put("ru", "る").put("re", "れ")
            .put("ro", "ろ").put("la", "ら").put("li", "り").put("lu", "る").put("le", "れ").put("lo", "ろ").put("rya", "りゃ")
            .put("ryu", "りゅ").put("ryo", "りょ").put("wa", "わ").put("wi", "ゐ")
            .put("wu", "ゔ").put("we", "ゑ").put("wo", "を").put("wya", "ゐゃ").put("wyu", "ゐゅ").put("wyo", "ゐょ")
            .put("n", "ん").put("m", "ん").put("ga", "が").put("gi", "ぎ").put("gu", "ぐ").put("ggu", "っぐ").put("ge", "げ")
            .put("go", "ご")
            .put("gya", "ぎゃ").put("gyu", "ぎゅ").put("gyo", "ぎょ").put("za", "ざ").put("ji", "じ").put("zi", "じ")
            .put("zu", "ず")
            .put("ze", "ぜ")
            .put("zo", "ぞ").put("ja", "じゃ").put("ju", "じゅ").put("jo", "じょ").put("da", "だ").put("dji", "ぢ")
            .put("di", "ぢ")
            .put("dzu", "づ").put("de", "で").put("do", "ど").put("ddo", "っど").put("dya", "ぢゃ").put("dyu", "ぢゅ")
            .put("dyo", "ぢょ")
            .put("ba", "ば").put("bi", "び").put("bu", "ぶ").put("be", "べ").put("bo", "ぼ").put("bya", "びゃ")
            .put("byu", "びゅ").put("byo", "びょ").put("bba", "っば").put("bbi", "っび").put("bbu", "っぶ").put("bbe", "っべ")
            .put("bbo", "っぼ").put("pa", "ぱ").put("pi", "ぴ").put("pu", "ぷ").put("pe", "ぺ").put("po", "ぽ")
            .put("pya", "ぴゃ").put("ppya", "っぴゃ").put("ppyo", "っぴょ").put("pyu", "ぴゅ").put("pyo", "ぴょ").put("ppa", "っぱ")
            .put("ppi", "っぴ")
            .put("ppu", "っぷ")
            .put("fi", "フィ")
            .put("fa", "ファ")
            .put("fo", "フォ")
            .put("…", "...")
            .put(":", ":")
            .put("h", "っ")
            .put("ppe", "っぺ").put("ppo", "っぽ").put(" ", "").put(".", "。").put(",", "、").put("?", "？").put("!", "！")
            .put("-", "")
            .put("''", "")
            .put("‘", "").put("’", "")
            .build();
    public static final String LESSON_REGEX = "INSERT INTO JAPANESE_LESSON\\(english,japanese,romaji,exercise,lesson\\) VALUES\\('([^\n]+)','([^\n]+)','([^\n]+)',(\\d+),(\\d+)\\);";
	public static final String TXT_FILE = ResourceFXUtils.toFullPath("create_database.sql");
    private static final Logger LOG = HasLogging.log();
    private static final List<String> SPECIAL_LETTERS = Arrays.asList("n", "m", "h");

    public static String convertHiragana(String romaji) {
        if (romaji == null || romaji.isEmpty()) {
            return null;
        }
        StringBuilder c = new StringBuilder();
        StringBuilder result = new StringBuilder();
        String[] s = romaji.split("");
        for (int i = 0; i < s.length; i++) {
            c.append(s[i].toLowerCase());
            if (SPECIAL_LETTERS.contains(c.toString()) && i < s.length - 1 && HIRAGANA_MAP.containsKey(c + s[i + 1])) {
                continue;
            }

            if (HIRAGANA_MAP.containsKey(c.toString())) {
                result.append(HIRAGANA_MAP.get(c.toString()));
                c.delete(0, c.length());
            }
        }
        if (c.length() != 0) {
            LOG.trace("NOT JAPANESE----{}", romaji);
        }

        return result.toString();
    }

    public static void displayInHiragana() {
        try (Stream<String> lines = Files.lines(new File(TXT_FILE).toPath(), StandardCharsets.UTF_8)) {
            lines.forEach(t -> {
                if (!t.matches(LESSON_REGEX)) {
                    return;
                }
                String[] split = t.replaceAll(LESSON_REGEX, "$1@$2@$3@$4@$5").split("@");

                String replaceAll = split[2].replaceAll("\\([^\n]*\\)", "");
                String convertHiragana = convertHiragana(replaceAll);
                LOG.trace("{}={}={}", replaceAll, convertHiragana, split[1]);

            });
        } catch (Exception e) {
			LOG.error("ERROR", e);
        }
    }

    public static void main(String[] args) {
        displayInHiragana();

    }

}
