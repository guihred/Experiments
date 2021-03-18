package utils;

import static utils.ResourceFXUtils.getOutFile;
import static utils.ex.FunctionEx.makeFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class CSVUtils {

    private static final Logger LOGGER = HasLogging.log();
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static final char ESCAPE_CHARACTER = '\\';
    private final char separators;
    private final char customQuote;
    private boolean inQuotes;
    private boolean startCollectChar;
    private boolean doubleQuotesInColumn;

    private CSVUtils(char separators, char customQuote) {
        this.separators = separators;
        this.customQuote = customQuote;
    }

    public List<String> getFields(String cvsLine) {
        List<String> result = new ArrayList<>();
        // if empty, return!
        if (cvsLine == null || cvsLine.isEmpty()) {
            return result;
        }
        StringBuilder curVal = getField(cvsLine, result);
        result.add(curVal.toString());
        return result;
    }

    public boolean isInQuotes() {
        return inQuotes;
    }

    private StringBuilder getCurrentVal(List<String> result, StringBuilder curVal, char[] chars, int i) {
        char previousCh = getPreviousChar(chars, i);
        char ch = chars[i];
        if (!inQuotes) {
            if (isQuote(previousCh, ch)) {
                inQuotes = true;
                // Fixed : allow "" in empty quote enclosed
                if (chars[0] != customQuote || startCollectChar) {
                    curVal.append(customQuote);
                }
                return curVal;
            }
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
        startCollectChar = true;
        if (isQuote(previousCh, ch)) {
            inQuotes = false;
            doubleQuotesInColumn = false;
            return curVal;
        }
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

    private StringBuilder getField(String cvsLine, List<String> result) {
        StringBuilder curVal = new StringBuilder();
        char[] chars = cvsLine.toCharArray();
        for (int i = 0; i < chars.length && chars[i] != '\n'; i++) {
            curVal = getCurrentVal(result, curVal, chars, i);
        }
        return curVal;
    }

    private boolean isQuote(char previousCh, char ch) {
        return ch == customQuote && previousCh != ESCAPE_CHARACTER;
    }

    public static void appendLine(File file, Map<String, Object> rowMap) {
        boolean exists = file.exists();
        String csvHeader = rowMap.keySet().stream().collect(Collectors.joining(",", "", ""));
        if (exists) {
            String header = SupplierEx.get(() -> getHeader(file));
            if (!csvHeader.equals(header)) {
                RunnableEx.run(() -> Files.deleteIfExists(file.toPath()));
                exists = false;
            }
        }
        try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true)) {
            if (!exists) {
                fw.append(csvHeader + "\n");
            }
            List<String> cols = rowMap.keySet().stream().collect(Collectors.toList());
            fw.append(rowMap.entrySet().stream().sorted(Comparator.comparing(t -> cols.indexOf(t.getKey())))
                    .map(Entry<String, Object>::getValue).map(Object::toString)
                    .collect(Collectors.joining(",", "", "\n")));
        } catch (Exception e1) {
            LOGGER.error("{}", e1);
        }
    }

    public static void appendLines(File file, List<Map<String, Object>> rowMap) {
        boolean exists = file.exists();
        List<String> keySet = rowMap.get(0).keySet().stream().collect(Collectors.toList());
        String csvHeader = join(keySet.stream());
        if (exists) {
            String csvHeader2 = SupplierEx.get(() -> getHeader(file));
            if (!csvHeader.equals(csvHeader2)) {
                RunnableEx.run(() -> Files.deleteIfExists(file.toPath()));
                exists = false;
            }
        }
        try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true)) {
            if (!exists) {
                fw.append(csvHeader + "\n");
            }
            for (Map<String, Object> map : rowMap) {
                keySet.forEach(s -> map.putIfAbsent(s, ""));
                fw.append(map.entrySet().stream().sorted(Comparator.comparing(t -> keySet.indexOf(t.getKey())))
                        .map(Entry<String, Object>::getValue).map(Object::toString)
                        .collect(Collectors.joining("\",\"", "\"", "\"\n")));
            }
        } catch (Exception e1) {
            LOGGER.error("{}", e1);
        }
    }

    public static CSVUtils defaultCSVUtils() {
        return new CSVUtils(DEFAULT_SEPARATOR, DEFAULT_QUOTE);

    }

    public static void fixEmptyLine(List<String> header, List<String> line2, int size) {
        if (header.size() != line2.size()) {
            LOGGER.error("ERROR FIELDS COUNT line {}", size + 1);
            createNullRow(header, line2);
        }
    }

    public static long fixMultipleLines(Scanner scanner, CSVUtils defaultCSVUtils, List<String> line2) {
        long co = 0;
        while (defaultCSVUtils.isInQuotes() && scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            co += nextLine.getBytes(StandardCharsets.UTF_8).length;
            List<String> fields = defaultCSVUtils.getFields(nextLine);
            int last = line2.size() - 1;
            if (fields.isEmpty()) {
                continue;
            }
            line2.set(last, line2.get(last) + "\n" + fields.remove(0));
            for (String string : fields) {
                line2.add(string.replaceAll("^\"", ""));
            }
        }
        return co;
    }

    public static String[] getDataframeCSVs() {
        File file = getOutFile();
        String regex = "WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv";
        List<Path> list = FileTreeWalker.getFirstFileMatch(file, path -> path.getFileName().toString().matches(regex));
        if (!list.isEmpty()) {
            return list.stream().map(e -> file.toPath().relativize(e)).map(Path::toString)
                    .map(s -> s.replaceAll("\\\\", "/")).toArray(String[]::new);
        }
        File outFile = getOutFile("WDIData.csv");
        if (!outFile.exists()) {
            UnZip.extractZippedFiles(new File(UnZip.ZIPPED_FILE_FOLDER));
        }
        CSVUtils.splitFile(outFile.getAbsolutePath(), 3);
        CSVUtils.splitFile(getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3);
        return FileTreeWalker.getFirstFileMatch(file, name -> name.getFileName().toString().matches(regex)).stream()
                .map(e -> file.toPath().relativize(e)).map(Path::toString).map(s -> s.replaceAll("\\\\", "/"))
                .toArray(String[]::new);
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
        StringBuilder curVal = new CSVUtils(separator, quote).getField(cvsLine, result);
        result.add(curVal.toString());
        return result;
    }

    public static <T> void saveToFile(TableView<T> table, File f) throws IOException {
        List<Integer> selectedItems = table.getSelectionModel().getSelectedIndices();
        if (selectedItems.isEmpty()) {
            selectedItems = IntStream.range(0, table.getItems().size()).boxed().collect(Collectors.toList());
        }

        List<TableColumn<T, ?>> columns =
                table.getColumns().stream().filter(c -> !"Nº".equals(c.getText())).collect(Collectors.toList());
        String fields = join(columns.stream().map(TableColumn::getText));
        String lines = selectedItems.stream()
                .map(l -> join(columns.stream().map(e -> Objects.toString(e.getCellData(l), ""))
                        .map(s -> s.replaceAll("\"", "\\\""))))
                .collect(Collectors.joining("\n"));
        Files.write(f.toPath(), Arrays.asList(fields, lines), StandardCharsets.UTF_8);
    }

    public static void splitFile(File source, int columnIndex) {
        Map<String, Writer> writersBytype = new HashMap<>();
        RunnableEx.run(() -> {
            try (Scanner scanner = new Scanner(source, StandardCharsets.UTF_8.displayName())) {
                String firstLine = scanner.nextLine();
                while (scanner.hasNext()) {
                    String nextLine = scanner.nextLine();
                    List<String> parseLine = CSVUtils.parseLine(nextLine);
                    if (parseLine.size() > columnIndex) {
                        String string = parseLine.get(columnIndex);
                        writersBytype.computeIfAbsent(string, makeFunction(s -> newWrite(source, firstLine, s)))
                                .append(nextLine + "\n");
                    }
                }
                ConsumerEx.foreach(writersBytype.values(), Writer::close);
            }
            LOGGER.info("FILE {} WAS SPLIT", source);
        });
    }

    public static void splitFile(String csvFile, int columnIndex) {
        File source = Paths.get(csvFile).toFile();
        splitFile(source, columnIndex);

    }

    private static void createNullRow(Collection<String> header, Collection<String> line2) {
        if (line2.size() < header.size()) {
            long maxSize2 = header.size() - (long) line2.size();
            line2.addAll(Stream.generate(() -> "").limit(maxSize2).collect(Collectors.toList()));
        }
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

    private static String getHeader(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
            return scanner.nextLine();
        }
    }

    private static char getPreviousChar(char[] chars, int i) {
        return i > 0 ? chars[i - 1] : ' ';
    }

    private static String join(Stream<String> stream) {
        return stream.collect(Collectors.joining("\",\"", "\"", "\""));
    }

    private static Writer newWrite(File source, String firstLine, String string) throws IOException {
        String child = source.getName().replaceAll("\\.\\w+$", "") + string.replaceAll("[:/\\?]+", "") + ".csv";
        File file = ResourceFXUtils.getOutFile("csv/" + child);
        Writer output = createWriter(file.getAbsolutePath());
        output.append(firstLine + "\n");
        return output;
    }

}
