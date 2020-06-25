package ml.data;

import extract.ExcelService;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class ExcelDataReader extends DataframeUtils {
    private static final Logger LOG = HasLogging.log();

    public static void main(String[] args) {
        Path firstPathByExtension =
                ResourceFXUtils.getFirstPathByExtension(ResourceFXUtils.getOutFile().getParentFile(), ".xls");
        readExcel(firstPathByExtension.toFile());
    }

    public static DataframeML readExcel(File excelFile) {
        LOG.info("READING {}", excelFile);
        DataframeML dataframeML2 = new DataframeML();
        RunnableEx.remap(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(excelFile);
                    Workbook workbook = ExcelService.getWorkbook(excelFile, fileInputStream);) {

                Set<String> arrayList = new LinkedHashSet<>();

                for (Sheet sheet : workbook) {
                    List<String> arrayList1 = new ArrayList<>();
                    List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

                    List<Integer> headerRows = mergedRegions.stream().filter(e -> e.getFirstRow() != e.getLastRow())
                            .flatMapToInt(m -> IntStream.range(m.getFirstRow(), m.getLastRow() + 1)).boxed().distinct()
                            .collect(Collectors.toList());
                    int rowi = 0;
                    for (Row next : sheet) {
                        Map<String, Object> linkedHashMap = new LinkedHashMap<>();
                        int cellj = 0;
                        for (Cell cell : next) {
                            int rowindex = rowi;
                            if (mergedRegions.stream().noneMatch(e -> e.containsRow(rowindex))) {
                                Object value = getValue(cell);
                                if (value != null) {
                                    int cellIndex = cellj;
                                    String stringValue = getHeader(sheet, headerRows, cellIndex);
                                    linkedHashMap.put(stringValue, value);
                                }
                            }
                            cellj++;
                        }
                        rowi++;
                    }
                    if (arrayList1.isEmpty()) {
                        break;
                    }

                    arrayList.addAll(arrayList1);
                }

                arrayList.forEach(e -> {

                    LOG.error("HEADERS {}", e);
                });

            }
        }, "ERROR IN FILE " + excelFile);
        return dataframeML2;
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
        if (cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        return null;
    }

    private static Object getValue(Cell cell) {
        if (cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return null;
    }

    private static void readRows(DataframeML dataframe, Scanner scanner, List<String> header) {
        while (scanner.hasNext()) {
            dataframe.size++;
            List<String> line2 = CSVUtils.parseLine(scanner.nextLine());
            if (header.size() != line2.size()) {
                LOG.error("ERROR FIELDS COUNT");
                createNullRow(header, line2);
            }

            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = getFromList(i, line2);
                Object tryNumber = tryNumber(dataframe, key, field);
                if (dataframe.filters.containsKey(key) && !dataframe.filters.get(key).test(tryNumber)) {
                    removeRow(dataframe, header, i);
                    break;
                }
                categorizeIfCategorizable(dataframe, key, tryNumber);
                tryNumber = mapIfMappable(dataframe, key, tryNumber);

                dataframe.list(key).add(tryNumber);
            }
            if (dataframe.size > dataframe.maxSize) {
                break;
            }
        }
    }

}
