package ml.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ResourceFXUtils;

public class CSVUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSVUtils.class);
	private static final char DEFAULT_SEPARATOR = ',';
	private static final char DEFAULT_QUOTE = '"';

	public static void main(String[] args) {
		splitFile("WDIData.csv", 3);
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

		try (Scanner scanner = new Scanner(new File(csvFile), StandardCharsets.UTF_8.displayName())) {
			String firstLine = scanner.nextLine();
			while (scanner.hasNext()) {
				String nextLine = scanner.nextLine();
				List<String> parseLine = CSVUtils.parseLine(nextLine);
				String string = parseLine.get(columnIndex);
				if (!writersBytype.containsKey(string)) {
					File file = new File(ResourceFXUtils.getOutFile(),
							csvFile.replaceAll("\\..+", "") + string + ".csv");
					Writer output = createWriter(file.getAbsolutePath());
					output.append(firstLine + "\n");
					writersBytype.put(string, output);
				}
				writersBytype.get(string).append(nextLine + "\n");
			}
		}catch(Exception e) {
			LOGGER.error("ERROR ", e);
		}
	}

	private static Writer createWriter(String csvFile) {
		try {
			File file = new File(csvFile);
			if (file.exists()) {
				Files.delete(file.toPath());
			}
			boolean created = file.createNewFile();
			LOGGER.info("file created {}", created);
			return new BufferedWriter(new FileWriterWithEncoding(csvFile, StandardCharsets.UTF_8, true));
		} catch (Exception e) {
			throw new RuntimeIOException("ERROR CREATING WRITER", e);
		}
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
			} else if (ch == '\n') {
				break;
			} else if (ch != '\r') {
				curVal.append(ch);
			}
		}
		return curVal;
	}


}
