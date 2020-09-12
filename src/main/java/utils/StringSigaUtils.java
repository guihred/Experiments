package utils;

import static utils.ex.SupplierEx.getIgnore;

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
    public static final Map<Class<? extends Comparable<?>>, Function<String, Comparable<?>>> FORMAT_HIERARCHY_MAP =
            formatHierarchy();

    public static String changeCase(String simpleName) {
        if (Character.isLowerCase(simpleName.charAt(0))) {
            return simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        }
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    public static String codificar(String nome) {
        return getIgnore(() -> URLEncoder.encode(Objects.toString(nome, ""), "UTF-8"), nome);
    }

    public static Integer convertNumerico(final String eleitores) {
        String replaceAll = eleitores.replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
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
        if (!(mean instanceof Double)) {
            String format = "\t%" + length + "s";
            return String.format(format, mean);
        }
        return String.format(floatFormating(length), mean);
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

    public static String getFileSize(String sizeInBytes) {
        return SupplierEx.get(() -> getFileSize(Double.valueOf(sizeInBytes).longValue()));
    }

    public static List<String> getLinks(String content) {
        List<String> links = new ArrayList<>();
        Pattern p = Pattern.compile("(?i)href=\"http://(.*?)\"");
        Matcher m = p.matcher(content);
        while (m.find()) {
            links.add(m.group(1));
        }
        return links;
    }

    public static String intFormating(int length) {
        return "\t%" + length + "d";
    }

    public static Integer intValue(String v) {
        try {
            return Integer.valueOf(v.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            HasLogging.log(1).trace("NUMBER NOT PARSED", e);
            HasLogging.log(1).error("NUMBER NOT PARSED \"{}\" {}", v, HasLogging.getCurrentLine(1));
            return null;
        }
    }

    public static String putNumbers(List<String> map) {
        int orElse = map.stream().mapToInt(String::length).max().orElse(0);
        return IntStream.range(0, map.size()).mapToObj(i -> numberLines(map, orElse, i))
                .collect(Collectors.joining("\n"));
    }

    public static String removeMathematicalOperators(String s) {
        if (s.codePoints().mapToObj(UnicodeBlock::of).anyMatch(b -> b == UnicodeBlock.MATHEMATICAL_OPERATORS)) {
            return s.replaceAll("[\u2200-\u22FF]", "?");
        }
        return s;
    }

    public static String removeNotPrintable(String s) {
        String fixEncoding = fixEncoding(s.replaceAll("\t", " "), StandardCharsets.UTF_8, Charset.forName("CESU-8"));
        if (fixEncoding == null) {
            return null;
        }
        return fixEncoding.replaceAll("[\u0000-\u0010]", "?").replaceAll("\u00A0", " ");
    }

    public static String removerDiacritico(String string) {
        return Normalizer.normalize(Objects.toString(string, ""), Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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
        return readLine.split(REGEX_CAMEL_CASE);
    }

    public static String splitMargeCamelCase(String readLine) {
        return Stream.of(readLine.split(StringSigaUtils.REGEX_CAMEL_CASE)).collect(Collectors.joining(" "));
    }

    public static String substituirNaoNumeros(String numero) {
        if (StringUtils.isBlank(numero)) {
            return null;
        }
        return numero.replaceAll("\\D", "");
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

    public static Object tryAsNumber(Map<String, Class<? extends Comparable<?>>> formatMap2, String header,
            Class<?> currentFormat, String number) {
        Set<Entry<Class<? extends Comparable<?>>, Function<String, Comparable<?>>>> entrySet =
                FORMAT_HIERARCHY_MAP.entrySet();
        for (Entry<Class<? extends Comparable<?>>, Function<String, Comparable<?>>> entry : entrySet) {
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
            Function<String, Comparable<?>> func) {
        if (FORMAT_HIERARCHY.indexOf(currentFormat) <= FORMAT_HIERARCHY.indexOf(class1)) {
            Comparable<?> valueOf = func.apply(number);
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

    private static Map<Class<? extends Comparable<?>>, Function<String, Comparable<?>>> formatHierarchy() {
        Map<Class<? extends Comparable<?>>, Function<String, Comparable<?>>> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put(Integer.class, Integer::valueOf);
        linkedHashMap.put(Long.class, Long::valueOf);
        linkedHashMap.put(Double.class, Double::valueOf);
        return linkedHashMap;
    }

    private static boolean hasBom(byte[] input) {
        return input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB && (input[2] & 0xFF) == 0xBF;
    }

    private static String justified(List<String> map, int maxLetters, int i) {
        String str = map.get(i);
        int diff = maxLetters - str.length();
        if (i == 0 || paragraphEnd(map.get(i - 1))) {
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

    private static boolean validUTF8(byte[] input) {
        int i = 0;
        // Check for BOM
        if (hasBom(input)) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

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
