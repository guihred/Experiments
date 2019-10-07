package ml.data;

import static utils.FunctionEx.makeFunction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;
import utils.SupplierEx;

public class CSVUtils {

    private static final Logger LOGGER = HasLogging.log();
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    public static void appendLine(File file, Map<String, Object> rowMap) {
    	boolean exists = file.exists();
        String collect = rowMap.keySet().stream().collect(Collectors.joining(",", "", ""));
    	if (exists) {
    		try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
    			String nextLine = scanner.nextLine();
    			if (!collect.equals(nextLine)) {
    				exists = false;
    			}
    		} catch (Exception e) {
                LOGGER.error("{}", e);
    		}
    		if (!exists) {
                RunnableEx.run(() -> Files.deleteIfExists(file.toPath()));
    		}
    	}
    
    	try (FileWriterWithEncoding fw = new FileWriterWithEncoding(file, StandardCharsets.UTF_8, true)) {
    		if (!exists) {
    			fw.append(collect + "\n");
    		}
            List<String> cols = rowMap.keySet().stream().collect(Collectors.toList());
    
            fw.append(rowMap.entrySet().stream().sorted(Comparator.comparing(t -> cols.indexOf(t.getKey())))
                .map(Entry<String, Object>::getValue).map(Object::toString).collect(Collectors.joining(",", "", "\n")));
    	} catch (Exception e1) {
            LOGGER.error("{}", e1);
    	}
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
        char customQuote = quote;
        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }
        char separators = separator;
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }
        StringBuilder curVal = getField(cvsLine, result, separators, customQuote);
        result.add(curVal.toString());
        return result;
    }

    public static void splitFile(String csvFile, int columnIndex) {
        Map<String, Writer> writersBytype = new HashMap<>();
        File source = Paths.get(csvFile).toFile();
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
        } catch (Exception e) {
            LOGGER.error("ERROR ", e);
        }
    }

    private static Writer createWriter(String csvFile) {
        return SupplierEx.remap(() -> {
            File file = new File(csvFile);
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            boolean created = file.createNewFile();
            LOGGER.trace("file {} created {}", csvFile, created);
            return new BufferedWriter(new FileWriterWithEncoding(csvFile, StandardCharsets.UTF_8, true));
        }, "ERROR CREATING WRITER");
    }

    private static StringBuilder getField(String cvsLine, List<String> result, char separators, char customQuote) {
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;
        StringBuilder curVal = new StringBuilder();
        char[] chars = cvsLine.toCharArray();
        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else if (ch == '\"') {
                    if (!doubleQuotesInColumn) {
                        curVal.append(ch);
                        doubleQuotesInColumn = true;
                    }
                } else {
                    curVal.append(ch);
                }

            } else if (ch == customQuote) {
                inQuotes = true;
                // Fixed : allow "" in empty quote enclosed
                if (chars[0] != '"' && customQuote == '\"' || startCollectChar) {
                    curVal.append('"');
                }
            } else if (ch == separators) {
                result.add(curVal.toString().replaceFirst("" + DEFAULT_QUOTE, ""));
                curVal = new StringBuilder();
                startCollectChar = false;
            } else if (ch == '\n') {
                break;
            } else if (ch != '\r') {
                curVal.append(ch);
            }
        }
        return curVal;
    }

    private static Writer newWrite(File source, String firstLine, String string) throws IOException {
        String child = source.getName().replaceAll("\\..+", "") + string + ".csv";
        File file = ResourceFXUtils.getOutFile(child);
        Writer output = createWriter(file.getAbsolutePath());
        output.append(firstLine + "\n");
        return output;
    }

}
