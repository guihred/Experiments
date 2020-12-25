package utils;

import static utils.ex.SupplierEx.getIgnore;

import com.google.common.collect.ImmutableMap;
import java.lang.Character.UnicodeBlock;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.text.MaskFormatter;
import org.apache.commons.lang3.StringUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class StringSigaUtils extends StringUtils {
    private static final int BYTES_IN_A_KILOBYTE = 1024;
    private static final String[] SIZES = { "B", "KB", "MB", "GB", "TB" };
    private static final int TAMANHO_CEP = 8;

    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CNPJ = 14;
    private static final List<Class<?>> FORMAT_HIERARCHY =
            Arrays.asList(String.class, Integer.class, Long.class, Double.class);
    public static final String REGEX_CAMEL_CASE = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|(\\W+)";
    public static final ImmutableMap<Class<? extends Comparable<?>>, Function<String, ?>> FORMAT_HIERARCHY_MAP =
            ImmutableMap.copyOf(formatHierarchy());

    public static Map<String, String> asMap(String line) {
        return asMap(line, ":");
    }
    public static Map<String, String> asMap(String line, String separator) {
        return Stream.of(lines(line)).filter(s -> s.contains(separator))
                .collect(Collectors.toMap(s -> s.split(separator)[0],
                        s -> Stream.of(s.split(separator)).skip(1).collect(Collectors.joining(separator)),
                        (u, v) -> u + "\n" + v));
    }

    public static String changeCase(String str) {
        if (isBlank(str)) {
            return "";
        }
        if (Character.isLowerCase(str.charAt(0))) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String codificar(String nome) {
        return getIgnore(() -> URLEncoder.encode(Objects.toString(nome, ""), "UTF-8"), nome);
    }

    public static Integer convertNumerico(final String nome) {
        String replaceAll = Objects.toString(nome, "").replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
    }

    public static String decode64(String line) {
        byte[] bytes = Base64.getDecoder().decode(line);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String decodificar(String nome) {
        return getIgnore(() -> URLDecoder.decode(Objects.toString(nome, ""), "UTF-8"), nome);
    }

    public static String fixEncoding(String latin1) {
        if (latin1 == null) {
            return null;
        }

        byte[] bytes = latin1.getBytes(StandardCharsets.ISO_8859_1);
        if (!validUTF8(bytes)) {
            return latin1;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String fixEncoding(String latin1, Charset initial, Charset finalCharset) {
        if (latin1 == null) {
            return null;
        }
        byte[] bytes = latin1.getBytes(initial);
        return new String(bytes, finalCharset);
    }

    public static String floatFormating(int length) {
        return "\t%" + Math.max(length, 1) + ".1f";
    }

    public static String format(int length, Object mean) {
        int max = Math.max(length, 1);

        if (!(mean instanceof Double)) {
            String format = "\t%" + max + "s";
            return String.format(format, mean);
        }
        return String.format(floatFormating(max), mean);
    }

    public static String formating(String s) {
        if (StringUtils.isBlank(s)) {
            return "%s\t";
        }
        return "%" + s.length() + "s\t";
    }

    public static String getApenasNumeros(String texto) {
        if (isNotBlank(texto)) {
            return texto.replaceAll("\\D+", "");
        }
        return null;
    }

    public static Integer getApenasNumerosInt(String texto) {
        return SupplierEx.getIgnore(() -> Integer.parseInt(getApenasNumeros(texto)));
    }

    public static String getCEPFormatado(String cep) {
        if (StringUtils.isNotBlank(cep)) {
            String formatado = StringUtils.leftPad(cep, TAMANHO_CEP, "0");
            return formatar("#####-###", formatado);
        }
        return cep;
    }

    public static String getCnpjFormatado(String cnpj) {
        if (StringUtils.isNotBlank(cnpj)) {
            String formatado = StringUtils.leftPad(cnpj, TAMANHO_CNPJ, "0");
            return formatar("##.###.###/####-##", formatado);
        }
        return cnpj;
    }

    public static Long getCpfDesformatado(String cpf) {
        return SupplierEx.getIgnore(() -> Long.valueOf(retirarMascara(cpf)));
    }

    public static String getCpfFormatado(String cpf) {
        if (StringUtils.isNotBlank(cpf)) {
            String formatado = StringUtils.leftPad(cpf, TAMANHO_CPF, "0");
            return formatar("###.###.###-##", formatado);
        }
        return cpf;
    }

    public static String getFileSize(long sizeInBytes) {
        if (sizeInBytes == 0) {
            return "0";
        }
        int a0 = (int) Math.floor(Math.log10(sizeInBytes) / Math.log10(BYTES_IN_A_KILOBYTE));
        return String.format(Locale.ENGLISH, "%.2f %s", sizeInBytes / Math.pow(BYTES_IN_A_KILOBYTE, a0), SIZES[a0]);
    }

    public static String getFileSize(Number sizeInBytes) {
        return SupplierEx.getIgnore(() -> getFileSize(sizeInBytes.longValue()), sizeInBytes+"");
    }

    public static String getFileSize(String sizeInBytes) {
        return SupplierEx.getIgnore(() -> getFileSize(Double.valueOf(sizeInBytes).longValue()), sizeInBytes);
    }

    public static List<String> getLinks(String content) {
        List<String> links = new ArrayList<>();
        Pattern p = Pattern.compile("(?i)href=\"http://(.*?)\"");
        Matcher m = p.matcher(Objects.toString(content, ""));
        while (m.find()) {
            links.add(m.group(1));
        }
        return links;
    }

    public static String getMatches(String content, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(Objects.toString(content, ""));
        StringBuilder matches = new StringBuilder();
        while (m.find()) {
            matches.append(m.group(1));
        }
        return matches.toString();
    }

    public static String intFormating(int length) {
        return "\t%" + length + "d";
    }

    public static Integer intValue(String v) {
        try {
            return Integer.valueOf(Objects.toString(v, "").replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            HasLogging.log(1).trace("NUMBER NOT PARSED", e);
            HasLogging.log(1).error("NUMBER NOT PARSED \"{}\" {}", v, HasLogging.getCurrentLine(1));
            return null;
        }
    }

    public static String[] lines(String nome) {
        return split(Objects.toString(nome, ""), "[\n\r]+");
    }


    public static String putNumbers(List<String> map) {
        int orElse = map.stream().map(v -> Objects.toString(v, "")).mapToInt(String::length).max().orElse(0);
        return IntStream.range(0, map.size()).mapToObj(i -> numberLines(map, orElse, i))
                .collect(Collectors.joining("\n"));
    }

    public static String removeMathematicalOperators(String v) {
        String s = Objects.toString(v, "");
        if (s.codePoints().mapToObj(UnicodeBlock::of).anyMatch(b -> b == UnicodeBlock.MATHEMATICAL_OPERATORS)) {
            return s.replaceAll("[\u2200-\u22FF]", "?");
        }
        return s;
    }

    public static String removeNotPrintable(String s) {
        String fixEncoding = fixEncoding(Objects.toString(s, "").replaceAll("\t", " "), StandardCharsets.UTF_8,
                Charset.forName("CESU-8"));
        if (fixEncoding == null) {
            return null;
        }
        return fixEncoding.replaceAll("[\u0000-\u0010]", "?").replaceAll("\u00A0", " ");
    }

    public static String removerDiacritico(String string) {
        return Normalizer.normalize(Objects.toString(string, ""), Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static String replaceAll(String nome, String regex) {
        return SupplierEx.getIgnore(
                () -> nome.replaceAll(regex, IntStream.rangeClosed(1, Pattern.compile(regex).matcher(nome).groupCount())
                        .mapToObj(i -> "$" + i).collect(Collectors.joining())),
                nome);
    }

    public static String replaceToLowerCase(String str) {
        Pattern compile = Pattern.compile("</?[\\w\\-]+");
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            String group = matcher.group(0);
            matcher.appendReplacement(sb, group.toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String retirarMascara(String valor) {
        if (StringUtils.isNotBlank(valor)) {
            return valor.replaceAll("[./-]", "");
        }
        return valor;
    }

    public static String simNao(Boolean a) {
        return a ? "Sim" : "Não";
    }

    public static String[] splitCamelCase(String readLine) {
        return Objects.toString(readLine, "").split(REGEX_CAMEL_CASE);
    }

    public static String splitMergeCamelCase(String readLine) {
        return Stream.of(Objects.toString(readLine, "").split(StringSigaUtils.REGEX_CAMEL_CASE))
                .collect(Collectors.joining(" "));
    }

    public static long strToFileSize(String sizeInBytes) {
        if (isBlank(sizeInBytes) || sizeInBytes.matches("0+") || !sizeInBytes.matches("(?i)[\\d\\.]+(B|KB|MB|GB|TB)")) {
            return 0;
        }
        String[] tokens = sizeInBytes.split("(?<=[\\d\\.]+)(?=[A-Z]+)");
        Double integer = Double.valueOf(tokens[0]);
        int indexOf = Arrays.asList(SIZES).indexOf(tokens[1].toUpperCase());
        double pow = Math.pow(BYTES_IN_A_KILOBYTE, indexOf);
        return (long) (pow * integer);
    }

    public static String substituirNaoNumeros(String numero) {
        if (StringUtils.isBlank(numero)) {
            return null;
        }
        return numero.replaceAll("\\D", "");
    }

    public static Double toDouble(Object numero) {
        if (numero instanceof Number) {
            return ((Number) numero).doubleValue();
        }
        return getIgnore(() -> Double.valueOf(Objects.toString(numero, "").replaceAll("[^0-9\\.]", "")), 0.);
    }

    public static Integer toInteger(Object numero) {
        if (numero instanceof Number) {
            return ((Number) numero).intValue();
        }
        return getIgnore(() -> toInteger(Objects.toString(numero, "")), 0);
    }

    public static Integer toInteger(String numero) {
        return getIgnore(() -> Integer.valueOf(Objects.toString(numero, "").replaceAll("\\D", "")), 0);
    }

    public static Long toLong(Object numero) {
        if (numero instanceof Number) {
            return ((Number) numero).longValue();
        }
        return getIgnore(() -> Long.valueOf(Objects.toString(numero, "").replaceAll("\\D", "")), 0L);
    }

    public static String toStringSpecial(Object n) {
        if (n instanceof Number) {
            return ((Number) n).doubleValue() % 1 == 0 ? String.format("%.0f", ((Number) n).doubleValue())
                    : Objects.toString(n);
        }
        return Objects.toString(n, "");
    }

    public static Object tryAsNumber(Map<String, Class<? extends Comparable<?>>> formatMap2, String header,
            Class<?> currentFormat, String number) {
        Set<Entry<Class<? extends Comparable<?>>, Function<String, ?>>> entrySet = FORMAT_HIERARCHY_MAP.entrySet();
        for (Entry<Class<? extends Comparable<?>>, Function<String, ?>> entry : entrySet) {
            try {
                return tryNumber(formatMap2, entry.getKey(), currentFormat, number, header, entry.getValue());
            } catch (Exception e) {
                HasLogging.log(1).trace("FORMAT ERROR ", e);
            }
        }
        return null;
    }

    public static Object tryNumber(Map<String, Class<? extends Comparable<?>>> formatMap,
            Class<? extends Comparable<?>> class1, Class<?> currentFormat, String number, String header,
            Function<String, ?> func) {
        if (FORMAT_HIERARCHY.indexOf(currentFormat) <= FORMAT_HIERARCHY.indexOf(class1)) {
            Object valueOf = func.apply(number);
            if (currentFormat != class1) {
                formatMap.put(header, class1);
            }
            return valueOf;
        }
        throw new NumberFormatException("Not");

    }

    private static String addSpaces(String str, int diff) {

        Pattern compile = Pattern.compile(" +");
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < Math.abs(diff); j++) {
            if (!matcher.find()) {
                matcher.appendTail(sb);
                String trim = sb.toString().trim();
                matcher = compile.matcher(trim);
                sb.delete(0, sb.length());
                j--;
                if (sb.toString().isEmpty()) {
                    break;
                }
            } else {
                String group = matcher.group(0);
                matcher.appendReplacement(sb, group + " ");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String formatar(String pattern, String valor) {
        return getIgnore(() -> {
            MaskFormatter mask = new MaskFormatter(pattern);
            mask.setValueContainsLiteralCharacters(false);
            return mask.valueToString(valor);
        }, valor);
    }

    private static Map<Class<? extends Comparable<?>>, Function<String, ?>> formatHierarchy() {
        Map<Class<? extends Comparable<?>>, Function<String, ?>> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put(Integer.class, Integer::valueOf);
        linkedHashMap.put(int.class, Integer::parseInt);
        linkedHashMap.put(Long.class, Long::valueOf);
        linkedHashMap.put(Double.class, Double::valueOf);
        return linkedHashMap;
    }

    private static int hasBom(byte[] input) {
        return input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB && (input[2] & 0xFF) == 0xBF

                ? 3
                : 0;
    }

    private static String justified(List<String> map, int maxLetters, int i) {
        String str = Objects.toString(map.get(i), "");
        int diff = maxLetters - str.length();
        if (i == 0 || paragraphEnd(Objects.toString(map.get(i - 1), ""))) {
            return leftPad(addSpaces(str, Math.max(diff - 8, 0)), maxLetters, "");
        }
        if (diff >= maxLetters / 2 || paragraphEnd(str)) {
            return rightPad(str, maxLetters, "");
        }
        String sb = addSpaces(str, diff);
        return rightPad(sb, maxLetters, "");
    }

    private static String numberLines(List<String> map, int maxLetters, int lineNumber) {
        String object = justified(map, maxLetters, lineNumber);
        return String.format("(%02d)    %s", lineNumber + 1, object);
    }

    private static boolean paragraphEnd(String str) {
        return str.endsWith(".") || str.endsWith(".”") || str.matches("Texto \\d+");
    }

    private static int utf8LeadingByte(int octet) {
        if ((octet & 0xE0) == 0xC0) {
            return 1;
        } else if ((octet & 0xF0) == 0xE0) {
            return 2;
        } else if ((octet & 0xF8) == 0xF0) {
            return 3;
        } else {
            // Java only supports BMP so 3 is max
            return 0;
        }
    }

    private static boolean validUTF8(byte[] input) {
        int i = hasBom(input);

        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }
            int leadingByte = utf8LeadingByte(octet);
            // Check for UTF-8 leading byte
            if (leadingByte == 0) {
                return false;
            }

            int end = i + leadingByte;

            while (i < end) {
                i++;
                if (i >= input.length) {
                    break;
                }
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }
}
