package ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVUtils.class);
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

	public static List<String> parseLine(String cvsLine, char separator, char quote) {
        List<String> result = new ArrayList<>();
		char separators = separator;
		char customQuote = quote;
        // if empty, return!
        if (cvsLine == null || cvsLine.isEmpty()) {
            return result;
        }
        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }
        StringBuilder curVal = getField(cvsLine, result, separators, customQuote);
        result.add(curVal.toString());
        return result;
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
                if (chars[0] != '"' && customQuote == '\"') {
                    curVal.append('"');
                }
                // double quotes in column will hit this!
                if (startCollectChar) {
                    curVal.append('"');
                }
            } else if (ch == separators) {
                result.add(curVal.toString().replaceFirst("" + DEFAULT_QUOTE, ""));
                curVal = new StringBuilder();
                startCollectChar = false;
            } else if (ch == '\r') {
                continue;
            } else if (ch == '\n') {
                break;
            } else {
                curVal.append(ch);
            }
        }
        return curVal;
    }

    public static void splitFile(String csvFile, int columnIndex) {
        Map<String, Writer> hashMap = new HashMap<>();
        File file = new File("out");
        if (!file.exists()) {
            file.mkdir();
        }

	    try (Scanner scanner = new Scanner(new File(csvFile));) {
            String firstLine = scanner.nextLine();
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                List<String> parseLine = CSVUtils.parseLine(nextLine);
                String string = parseLine.get(columnIndex);
                if (!hashMap.containsKey(string)) {
                    Writer output = createWriter("out/" + csvFile.replaceAll("\\..+", "") + string + ".csv");
                    output.append(firstLine + "\n");
                    hashMap.put(string, output);
                }
                hashMap.get(string).append(nextLine + "\n");
            }
	    }catch(Exception e) {
            LOGGER.error("ERROR ", e);
	    }
    }

    private static Writer createWriter(String csvFile) throws IOException {
        try {
            File file = new File(csvFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            return new BufferedWriter(new FileWriter(csvFile, true));
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        splitFile("WDIData.csv", 3);
    }
	

}
