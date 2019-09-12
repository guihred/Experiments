package utils;

import static utils.SupplierEx.get;

import java.lang.Character.UnicodeBlock;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.text.MaskFormatter;
import org.apache.commons.lang3.StringUtils;

public class StringSigaUtils extends StringUtils {

    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CEP = 8;
    private static final int TAMANHO_CNPJ = 14;
    private static final int TAMANHO_MATRICULA = 7;
    private static final int TAMANHO_PAP = 9;

    private static final List<Class<?>> FORMAT_HIERARCHY = Arrays.asList(String.class, Integer.class, Long.class,
        Double.class);

    public static String changeCase(String simpleName) {
        if (Character.isLowerCase(simpleName.charAt(0))) {
            return simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        }
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    public static String codificar(String nome) {
        return get(() -> URLEncoder.encode(nome, "UTF-8"), nome);
    }

    public static String corrigirProblemaEncoding(String nomeEncoding) {
        if (nomeEncoding == null) {
            return null;
        }
        byte[] bytes = nomeEncoding.getBytes(StandardCharsets.ISO_8859_1);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String decodificar(String nome) {
        return get(() -> URLDecoder.decode(nome, "UTF-8"), nome);
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
        return "\t%" + length + ".1f";
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
        if (isNotBlank(texto)) {
            return Integer.parseInt(getApenasNumeros(texto));
        }
        return null;
    }

    public static String getCEPFormatado(Long cep) {
        return getCEPFormatado(Objects.toString(cep, ""));
    }

    public static String getCEPFormatado(String cep) {
        if (StringUtils.isNotBlank(cep)) {
            String formatado = StringUtils.leftPad(cep, TAMANHO_CEP, "0");
            return formatar("#####-###", formatado);
        }
        return cep;
    }

    public static String getCnpjFormatado(Long cnpj) {
        if (cnpj != null) {
            return getCnpjFormatado(cnpj.toString());
        }
        return null;
    }

    public static String getCnpjFormatado(String cnpj) {
        if (StringUtils.isNotBlank(cnpj)) {
            String formatado = StringUtils.leftPad(cnpj, TAMANHO_CNPJ, "0");
            return formatar("##.###.###/####-##", formatado);
        }
        return cnpj;
    }

    public static Long getCpfDesformatado(String cpf) {
        if (StringUtils.isNotBlank(cpf)) {
            String valor = retirarMascara(cpf);
            return Long.valueOf(valor);
        }
        return null;
    }

    public static String getCpfFormatado(Long cpf) {
        if (cpf != null) {
            return getCpfFormatado(cpf.toString());
        }
        return null;
    }

    public static String getCpfFormatado(String cpf) {
        if (StringUtils.isNotBlank(cpf)) {
            String formatado = StringUtils.leftPad(cpf, TAMANHO_CPF, "0");
            return formatar("###.###.###-##", formatado);
        }
        return cpf;
    }

    public static String getMatriculaFormatado(String matricula) {
        if (StringUtils.isNotBlank(matricula)) {
            String formatado = StringUtils.leftPad(getApenasNumeros(matricula), TAMANHO_MATRICULA, "0");
            return formatar("#.###.###", formatado);
        }
        return matricula;
    }


    public static String getPAPFormatado(String pap) {
        if (StringUtils.isNotBlank(pap)) {
            String formatado = StringUtils.leftPad(pap, TAMANHO_PAP, "0");
            return formatar("#####/####", formatado);
        }
        return pap;
    }

    public static String intFormating(int length) {
        return "\t%" + length + "d";
    }

    public static Integer intValue(String v) {
        try {
            return Integer.valueOf(v.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            HasLogging.log(1).trace("NUMBER NOT PARSED", e);
            HasLogging.log(1).error("NUMBER NOT PARSED {} {}", v, HasLogging.getCurrentLine(1));

            return null;
        }
    }

    public static String justified(List<String> map, int maxLetters, int i) {
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

    public static String removeMathematicalOperators(String s) {
        if (s.codePoints().mapToObj(UnicodeBlock::of).distinct().collect(Collectors.toList())
            .contains(UnicodeBlock.MATHEMATICAL_OPERATORS)) {
            return s.replaceAll("[\u2200-\u22FF]", "?");
        }
        return s;
    }

    public static String removeNotPrintable(String s) {
        String fixEncoding = fixEncoding(s.replaceAll("\t", " "), StandardCharsets.UTF_8, Charset.forName("CESU-8"));
        if (fixEncoding == null) {
            return null;
        }
        return fixEncoding.replaceAll("[\u0000-\u0010]", "?");
    }

    public static String removerDiacritico(String string) {
        return Normalizer.normalize(string, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static String retirarMascara(String valor) {
        if (StringUtils.isNotBlank(valor)) {
            return valor.replaceAll("[./-]", "");
        }
        return valor;
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

        return get(() -> toInteger(Objects.toString(numero, "")), 0);
    }

    public static Integer toInteger(String numero) {
        String replaceAll = numero.replaceAll("\\D", "");
        return Integer.valueOf(replaceAll);
    }

    public static <T extends Comparable<?>> Object tryNumber(Map<String, Class<? extends Comparable<?>>> formatMap,
        Class<T> class1, Class<?> currentFormat, String number, String header, Function<String, T> func) {
        if (FORMAT_HIERARCHY.indexOf(currentFormat) <= FORMAT_HIERARCHY.indexOf(class1)) {
            T valueOf = func.apply(number);
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
        for (int j = 0; j < diff; j++) {
            if (!matcher.find()) {
                matcher.appendTail(sb);
                matcher = compile.matcher(sb.toString().trim());
                sb.delete(0, sb.length());
                j--;
            } else {
                String group = matcher.group(0);
                matcher.appendReplacement(sb, group + " ");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String formatar(String pattern, String valor) {
        return get(() -> {
            MaskFormatter mask = new MaskFormatter(pattern);
            mask.setValueContainsLiteralCharacters(false);
            return mask.valueToString(valor);
        }, valor);
    }

    private static boolean paragraphEnd(String str) {
        return str.endsWith(".") || str.endsWith(".”") || str.matches("Texto \\d+");
    }

    private static boolean validUTF8(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB && (input[2] & 0xFF) == 0xBF) {
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
