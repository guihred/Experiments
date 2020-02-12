package ml.data;

import static utils.FunctionEx.makeFunction;
import static utils.ResourceFXUtils.getOutFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import utils.*;

public class CSVUtils {

    private static final Logger LOGGER = HasLogging.log();
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private char separators;
    private char customQuote;
    private boolean inQuotes;
    private boolean startCollectChar;
    private boolean doubleQuotesInColumn;

    public CSVUtils(char separators, char customQuote) {
        this.separators = separators;
        this.customQuote = customQuote;
    }

    private StringBuilder getCurrentVal(List<String> result, StringBuilder curVal, char[] chars, char ch) {
        if (!inQuotes) {
            if (ch != customQuote) {
                if (ch == separators) {
                    result.add(curVal.toString().replaceFirst("" + DEFAULT_QUOTE, ""));
                    startCollectChar = false;
                    return new StringBuilder();
                }
                if (ch != '\r') {
                    curVal.append(ch);
                }
                return curVal;
            }
            inQuotes = true;
            // Fixed : allow "" in empty quote enclosed
            if (isAppendQuote(customQuote, startCollectChar, chars)) {
                curVal.append('"');
            }
            return curVal;
        }
        startCollectChar = true;
        if (ch != customQuote) {
            if (ch == '\"') {
                if (!doubleQuotesInColumn) {
                    curVal.append(ch);
                    doubleQuotesInColumn = true;
                }
                return curVal;
            }
            curVal.append(ch);
            return curVal;
        }
        inQuotes = false;
        doubleQuotesInColumn = false;
        return curVal;
    }

    private StringBuilder getField(String cvsLine, List<String> result) {
        StringBuilder curVal = new StringBuilder();
        char[] chars = cvsLine.toCharArray();
        for (int i = 0; i < chars.length && chars[i] != '\n'; i++) {
            curVal = getCurrentVal(result, curVal, chars, chars[i]);
        }
        return curVal;
    }

    public static void appendLine(File file, Map<String, Object> rowMap) {
        boolean exists = file.exists();
        String csvHeader = rowMap.keySet().stream().collect(Collectors.joining(",", "", ""));
        if (exists) {
            exists = SupplierEx.get(() -> doesHeaderMatch(file, csvHeader));
            if (!exists) {
                RunnableEx.run(() -> Files.deleteIfExists(file.toPath()));
            }
        }
        try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true)) {
            if (!exists) {
                fw.append(csvHeader + "\n");
            }
            List<String> cols = rowMap.keySet().stream().collect(Collectors.toList());
            fw.append(rowMap.entrySet().stream().sorted(Comparator.comparing(t -> cols.indexOf(t.getKey())))
                .map(Entry<String, Object>::getValue).map(Object::toString).collect(Collectors.joining(",", "", "\n")));
        } catch (Exception e1) {
            LOGGER.error("{}", e1);
        }
    }

    public static String[] getDataframeCSVs() {
        File file = getOutFile();
        String[] list = file.list((dir, name) -> name.matches("WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv"));
        if (list.length == 0) {
            File outFile = getOutFile("WDIData.csv");
            if (!outFile.exists()) {
                UnZip.extractZippedFiles(new File(UnZip.ZIPPED_FILE_FOLDER));
            }
            CSVUtils.splitFile(outFile.getAbsolutePath(), 3);
            CSVUtils.splitFile(getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3);
            return file.list((dir, name) -> name.matches("WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv"));
        }
        return list;
    }

    public static void main(String[] args) {
        splitFile(ResourceFXUtils.getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3);
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separator, char quote) {
        List<String> result = new ArrayList<>();
        // if empty, return!
        if (cvsLine == null || cvsLine.isEmpty()) {
            return result;
        }
        char customQuote = quote == ' ' ? DEFAULT_QUOTE : quote;
        char separators = separator == ' ' ? DEFAULT_SEPARATOR : separator;
        StringBuilder curVal = new CSVUtils(separators, customQuote).getField(cvsLine, result);
        result.add(curVal.toString());
        return result;
    }
    public static void splitFile(String csvFile, int columnIndex) {
        Map<String, Writer> writersBytype = new HashMap<>();
        File source = Paths.get(csvFile).toFile();
        RunnableEx.run(() -> {
            try (Scanner scanner = new Scanner(source, StandardCharsets.UTF_8.displayName())) {
                String firstLine = null;
                while (scanner.hasNext()) {
                    String nextLine = scanner.nextLine();
                    List<String> parseLine = CSVUtils.parseLine(nextLine);
                    if (parseLine.size() > columnIndex) {
                        if (firstLine == null) {
                            firstLine = nextLine;
                        }
                        final String fline = firstLine;
                        String string = parseLine.get(columnIndex);
                        writersBytype.computeIfAbsent(string, makeFunction(s -> newWrite(source, fline, s)))
                            .append(nextLine + "\n");
                    }
                }
            }
        });
    }

    private static Writer createWriter(String csvFile) {
        return SupplierEx.remap(() -> {
            File file = new File(csvFile);
            if (file.exists()) {
                Files.deleteIfExists(file.toPath());
            }
            boolean created = file.createNewFile();
            LOGGER.trace("file {} created {}", csvFile, created);
            return new BufferedWriter(new FileWriterWithEncoding(csvFile, StandardCharsets.UTF_8, true));
        }, "ERROR CREATING WRITER");
    }

    private static Boolean doesHeaderMatch(File file, String csvHeader) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
            String nextLine = scanner.nextLine();
            if (!csvHeader.equals(nextLine)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAppendQuote(char customQuote, boolean startCollectChar, char[] chars) {
        return chars[0] != '"' && customQuote == '\"' || startCollectChar;
    }

    private static Writer newWrite(File source, String firstLine, String string) throws IOException {
        String child = source.getName().replaceAll("\\..+", "") + string + ".csv";
        File file = ResourceFXUtils.getOutFile(child);
        Writer output = createWriter(file.getAbsolutePath());
        output.append(firstLine + "\n");
        return output;
    }

}
