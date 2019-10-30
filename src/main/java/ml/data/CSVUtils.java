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
			exists = SupplierEx.get(() -> {
				try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
					String nextLine = scanner.nextLine();
					if (!csvHeader.equals(nextLine)) {
						return false;
					}
				}
				return true;
			});
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
					.map(Entry<String, Object>::getValue).map(Object::toString)
					.collect(Collectors.joining(",", "", "\n")));
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
				Files.delete(file.toPath());
			}
			boolean created = file.createNewFile();
			LOGGER.trace("file {} created {}", csvFile, created);
			return new BufferedWriter(new FileWriterWithEncoding(csvFile, StandardCharsets.UTF_8, true));
		}, "ERROR CREATING WRITER");
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
