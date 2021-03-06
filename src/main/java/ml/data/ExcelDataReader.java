package ml.data;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import utils.ExcelService;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.PredicateEx;
import utils.ex.RunnableEx;

public class ExcelDataReader extends DataframeUtils {
    private static final String SHEET_COLUMN = "Sheet";
    private static final Logger LOG = HasLogging.log();

    public static void extractHTML(DataframeML frame) {
        RunnableEx.run(() -> {
            String extractBodyFromHTML = Jsoup.parse(frame.file, "UTF-8").body().text();
            List<String> asList = Arrays.asList(extractBodyFromHTML.split("(?<=\") (?=\")"));
            frame.file = ResourceFXUtils.getOutFile("csv/" + frame.file.getName());
            Files.write(frame.file.toPath(), asList, StandardOpenOption.CREATE);
            frame.stats = null;
            try (Scanner scanner2 = new Scanner(frame.file, "UTF-8")) {
                DataframeUtils.addHeaders(frame, scanner2);
            }
        });
    }

    public static DataframeML readExcel(DataframeML dataframeML2, File excelFile) {
        LOG.info("READING {}", excelFile);
        RunnableEx.remap(() -> {
            dataframeML2.file = excelFile;
            dataframeML2.size = 0;
            Set<String> keySet = new LinkedHashSet<>();
            try (FileInputStream fileInputStream = new FileInputStream(excelFile);
                    Workbook workbook = ExcelService.getWorkbook(excelFile, fileInputStream)) {
                List<Map<String, Object>> finalList = new ArrayList<>();
                for (Sheet sheet : workbook) {
                    readSheet(keySet, finalList, sheet);
                }
                readRows(dataframeML2, finalList, keySet.stream().collect(Collectors.toList()));

            }
        }, "ERROR IN FILE " + excelFile);
        return dataframeML2;
    }

    public static DataframeML readExcel(File excelFile) {
        return readExcel(new DataframeML(), excelFile);
    }

    private static Map<String, Object> addHeaders(Map<String, Object> data, Sheet sheet,
            List<CellRangeAddress> mergedRegions, List<Integer> collect, int cellIndex) {
        if (sheet.getWorkbook().getNumberOfSheets() > 1) {
            data.put(SHEET_COLUMN, sheet.getSheetName());
        }
        List<CellRangeAddress> headers = mergedRegions.stream().filter(e -> collect.stream().anyMatch(e::containsRow))
                .filter(e -> e.containsColumn(cellIndex)).collect(Collectors.toList());
        for (int i = 0; i < headers.size(); i++) {
            CellRangeAddress e = headers.get(i);
            String stringValue = getStringValue(sheet.getRow(e.getFirstRow()).getCell(e.getFirstColumn()));
            data.put("indicador" + i, stringValue);
        }
        return data;
    }

    private static void addToFinalList(Set<String> keySet, List<Map<String, Object>> finalList, Sheet sheet,
            List<CellRangeAddress> mergedRegions, List<Integer> headerRows, Row next) {
        Map<String, Object> data = new LinkedHashMap<>();
        finalList.add(data);
        int cellj = 0;
        for (Cell cell : next) {
            Object value = getValue(cell);
            if (value != null) {
                String thisHeader = getHeader(sheet, headerRows, cellj);
                if (data.containsKey(thisHeader)) {
                    data = newCopy(data, thisHeader);
                    finalList.add(data);
                }
                addHeaders(data, sheet, mergedRegions, headerRows, cellj);
                data.put(thisHeader, value);
            }
            cellj++;
        }
        if (!data.isEmpty()) {
            keySet.addAll(data.keySet());
        }
        if (data.isEmpty()) {
            finalList.remove(data);
        }
        if (data.size() != keySet.size()) {
            for (String key : keySet) {
                data.putIfAbsent(key, null);
            }
        }
    }

    private static void fixMapSize(List<String> header, Map<String, Object> map) {
        if (header.size() != map.size()) {
            createNullRow(header, map);
        }
    }

    private static String getHeader(Sheet sheet, List<Integer> collect, int cellIndex) {
        for (int i = collect.size() - 1; i >= 0; i--) {
            Integer integer = collect.get(i);
            String stringValue = getStringValue(sheet.getRow(integer).getCell(cellIndex));
            if (stringValue != null) {
                return stringValue;
            }
        }
        return null;
    }

    private static String getStringValue(Cell cell) {
        return Objects.toString(getValue(cell), "");
    }

    private static Object getValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return null;
    }

    private static Map<String, Object> newCopy(Map<String, Object> data0, String stringValue) {
        Map<String, Object> data = new LinkedHashMap<>(data0);
        boolean found = false;
        for (String string : data0.keySet()) {
            if (Objects.equals(stringValue, string) || found) {
                data.remove(string);
                found = true;
            }
        }
        return data;
    }

    private static void readRows(DataframeML dataframe, List<Map<String, Object>> scanner, List<String> header) {
        for (String column : header) {
            dataframe.getDataframe().put(column, new ArrayList<>());
            dataframe.putFormat(column, String.class);
        }
        for (Map<String, Object> map : scanner) {
            if (skipFirstLine(dataframe, map)) {
                continue;
            }

            dataframe.size++;
            fixMapSize(header, map);
            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = StringSigaUtils.toStringSpecial(map.get(key));
                Object tryNumber = tryNumber(dataframe, key, field);
                if (dataframe.filters.containsKey(key) && !PredicateEx.test(dataframe.filters.get(key), tryNumber)) {
                    removeRow(dataframe, header, i);
                    break;
                }
                categorizeIfCategorizable(dataframe, key, tryNumber);
                tryNumber = mapIfMappable(dataframe, key, tryNumber);
                dataframe.list(key).add(tryNumber);
            }
            if (dataframe.size > dataframe.maxSize) {
                return;
            }
        }
    }

    private static void readSheet(Set<String> headers, List<Map<String, Object>> finalList, Sheet sheet) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        List<Integer> headerRows = mergedRegions.stream().filter(e -> e.getFirstRow() != e.getLastRow())
                .flatMapToInt(m -> IntStream.range(m.getFirstRow(), m.getLastRow() + 1)).boxed().distinct()
                .collect(Collectors.toList());
        if (headerRows.isEmpty()) {
            headerRows.add(0);
        }
        int rowi = 0;
        for (Row row : sheet) {
            int rowindex = rowi;
            if (mergedRegions.stream().noneMatch(e -> e.containsRow(rowindex))) {
                addToFinalList(headers, finalList, sheet, mergedRegions, headerRows, row);
            }
            rowi++;
        }
    }

    private static boolean skipFirstLine(DataframeML dataframe, Map<String, Object> map) {
        return dataframe.size == 0 && map.entrySet().stream().filter(e -> !SHEET_COLUMN.equals(e.getKey()))
                .allMatch(e -> Objects.equals(e.getKey(), e.getValue())
                        || Objects.equals(Objects.toString(e.getKey(), ""), Objects.toString(e.getValue(), "")));
    }
}
