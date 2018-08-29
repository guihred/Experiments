package furigana.experiment;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JapaneseVerbConjugate {
    public static final Map<String, String[]> CONJUGATION_MAP = ImmutableMap.<String, String[]>builder()
            .put("[^\\n]+い", new String[] { "く", "くない", "くて", "かった", "くなかった", "ければ" })
            .put("[^\\n]+う", new String[] { "います", "わない", "わなかった", "って", "った", "える", "えば", "おう" })
            .put("[^\\n]+つ", new String[] { "ちます", "たない", "たなかった", "って", "った", "てる", "てば", "とう" })
            .put("[^\\n]+る", new String[] { "ります", "らない", "らなかった", "って", "った", "れる", "れば", "ろう" })
            .put("[^\\n]+く", new String[] { "きます", "かない", "かなかった", "いて", "いた", "ける", "けば", "こう" })
            .put("[^\\n]+ぐ", new String[] { "ぎます", "がない", "がなかった", "いで", "いだ", "げる", "げば", "ごう" })
            .put("[^\\n]+ぬ", new String[] { "にます", "なない", "ななかった", "んで", "んだ", "ねる", "ねば", "のう" })
            .put("[^\\n]+ぶ", new String[] { "びます", "ばない", "ばなかった", "んで", "んだ", "べる", "べば", "ぼう" })
            .put("[^\\n]+む", new String[] { "みます", "まない", "まなかった", "んで", "んだ", "める", "めば", "もう" })
            .put("[^\\n]+す", new String[] { "します", "さない", "さなかった", "して", "した", "せる", "せば", "そう" })
            .put(".+[いちりきぎにびみしじえてでれけげねべめせ.]+る", new String[] { "ます", "ない", "て", "た", "られる", "れば", "よう" }).build();

    public static boolean isVerb(String oi) {
        return oi.contains(".") && CONJUGATION_MAP.keySet().stream().anyMatch(oi::matches);
    }

    public static List<String> conjugateVerb(String verb) {
        return CONJUGATION_MAP.keySet().stream().filter(verb::matches)
                .flatMap(e -> Stream.of(CONJUGATION_MAP.get(e)).map(f -> verb.substring(0, verb.length() - 1) + f))
                .collect(Collectors.toList());
    }

}
