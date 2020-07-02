package extract;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class ExcelService {

    private static final int DEFAULT_ROW_SIZE = 500;
    private static final Logger LOG = HasLogging.log();

    private ExcelService() {
    }

    public static <T> void getExcel(BiFunction<Integer, Integer, List<T>> lista,
        Map<String, FunctionEx<T, Object>> fields, File file) {
        RunnableEx.run(() -> makeBigExcel(lista, fields, file));
    }

    public static <T> void getExcel(List<T> lista, Map<String, FunctionEx<T, Object>> mapa, File file) {
        RunnableEx.run(() -> makeExcelList(lista, mapa, file));
    }

    public static void getExcel(String documento, Map<Object, Object> map, List<String> abas, int sheetClonada,
        OutputStream response) {
        RunnableEx.run(() -> makeExcelWithClonedSheets(documento, map, abas, sheetClonada, response));
    }

    public static void getExcel(String arquivo, Map<Object, Object> map, OutputStream outStream) {
        RunnableEx.run(() -> makeExcelWithSubstitutions(arquivo, map, outStream));

    }

    public static ObservableList<String> getSheetsExcel(File selectedFile) {
        ObservableList<String> list = FXCollections.observableArrayList();
        RunnableEx.runInPlatform(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook workbook = getWorkbook(selectedFile, fileInputStream)) {
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    list.add(workbook.getSheetAt(i).getSheetName());
                }
            }
        });
        return list;
    }

    public static Workbook getWorkbook(File selectedFile, InputStream fileInputStream) throws IOException {
        return getWorkbook(selectedFile.getName(), fileInputStream);
    }

    public static boolean isExcel(File file) {
        return file.getName().endsWith("xlsx") || file.getName().endsWith("xls");
    }

    private static void alterarValorCell(Map<Object, Object> map, Sheet sheet, Row row, Cell c) {
        if (c.getCellTypeEnum() == CellType.NUMERIC) {
            alterNumeric(map, sheet, c);
        }
        if (c.getCellTypeEnum() == CellType.STRING) {
            alterString(map, sheet, row, c);
        }
    }


    @SuppressWarnings("rawtypes")
    private static void alterNumeric(Map<Object, Object> map, Sheet sheet, Cell cell) {
        double numericCellValue = cell.getNumericCellValue();
        printDebug(numericCellValue);
        if (map.containsKey(numericCellValue)) {
            Object object = map.get(numericCellValue);
            if (object instanceof Map) {
                object = ((Map) object).get(sheet.getSheetName());
            }
            if (object instanceof Number) {
                cell.setCellValue(((Number) object).doubleValue());
            }
            if (object instanceof String) {
                cell.setCellValue((String) object);
                cell.setCellType(CellType.STRING);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void alterString(Map<Object, Object> map, Sheet sheet, Row row, Cell c) {
        Cell cell = c;
        String stringCellValue = cell.getStringCellValue();
        printDebug(stringCellValue);
        if (map.containsKey(cell.getStringCellValue())) {
            Object cellValue = map.get(stringCellValue);
            if (cellValue instanceof Map) {
                cellValue = ((Map) cellValue).get(sheet.getSheetName());
            }
            if (cellValue instanceof String) {
                cell.setCellValue((String) cellValue);
            }
            if (cellValue instanceof List) {
                int rowNum = row.getRowNum();
                int columnIndex = cell.getColumnIndex();
                List listValue = (List) cellValue;
                if (listValue.isEmpty()) {
                    cell.setCellValue("");
                }
                for (Object object : listValue) {
                    cell.setCellValue(Objects.toString(object, ""));
                    ++rowNum;
                    Row next = sheet.getRow(rowNum);
                    cell = next.getCell(columnIndex);
                }
                map.remove(stringCellValue);
            }

        }
    }

    private static Workbook getWorkbook(String selectedFile, InputStream fileInputStream) throws IOException {
        return selectedFile.endsWith(".xls") ? new HSSFWorkbook(fileInputStream) : new XSSFWorkbook(fileInputStream);
    }

    private static <T> void makeBigExcel(BiFunction<Integer, Integer, List<T>> lista,
        Map<String, FunctionEx<T, Object>> fields, File file) throws IOException {
        try (FileOutputStream response = new FileOutputStream(file);
            SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(DEFAULT_ROW_SIZE)) {
            SXSSFSheet sheetAt = xssfWorkbook.createSheet();
            sheetAt.trackAllColumnsForAutoSizing();
            Row row2 = sheetAt.createRow(0);
            List<String> keySet = new ArrayList<>(fields.keySet());
            for (int i = 0; i < keySet.size(); i++) {
                row2.createCell(i, CellType.STRING).setCellValue(keySet.get(i));
            }
            Map<Class<?>, CellStyle> styleMap = styleMap(xssfWorkbook);
            final int step = 100;
            int i = 0;
            for (List<T> apply = lista.apply(0, step); !apply.isEmpty(); i += step, apply = lista.apply(i, step)) {
                for (int j = 0; j < apply.size(); j++) {
                    T entidade = apply.get(j);
                    Row row = sheetAt.createRow(i + 1 + j);
                    int k = 0;
                    for (FunctionEx<T, Object> campoFunction : fields.values()) {
                        Object campo = FunctionEx.makeFunction(campoFunction).apply(entidade);
                        setValorPorClasse(styleMap, row, k, campo);
                        k++;
                    }
                }
            }

            for (int k = 0; k < fields.size(); k++) {
                sheetAt.autoSizeColumn(k);
            }
            xssfWorkbook.write(response);
        }
    }

    private static <T> void makeExcelList(List<T> lista, Map<String, FunctionEx<T, Object>> mapa, File file)
        throws IOException {
        try (FileOutputStream response = new FileOutputStream(file); Workbook workbook = new XSSFWorkbook()) {
            Sheet sheetAt = workbook.createSheet();
            Row row2 = sheetAt.createRow(0);
            Set<String> keySet = mapa.keySet();
            boolean addHeader = !keySet.stream().allMatch(StringUtils::isNumeric);
            if (addHeader) {
                int j = 0;
                for (String titulo : keySet) {
                    row2.createCell(j, CellType.STRING).setCellValue(titulo);
                    j++;
                }
            }
            Map<Class<?>, CellStyle> formatMap = styleMap(workbook);

            for (int i = 0; i < lista.size(); i++) {
                T entidade = lista.get(i);
                Row row = sheetAt.createRow(i + (addHeader ? 1 : 0));
                int k = 0;
                for (FunctionEx<T, Object> campoFunction : mapa.values()) {
                    Object campo = FunctionEx.makeFunction(campoFunction).apply(entidade);
                    setValorPorClasse(formatMap, row, k, campo);
                    k++;
                }

            }
            for (int k = 0; k < mapa.size(); k++) {
                sheetAt.autoSizeColumn(k);
            }

            workbook.write(response);

        }
    }

    private static void makeExcelWithClonedSheets(String documento, Map<Object, Object> map, List<String> abas,
        int sheetClonada, OutputStream response) throws IOException {
        try (InputStream file = ResourceFXUtils.toStream(documento);
            Workbook workbookXLSX = getWorkbook(documento, file)) {
            List<String> abasPresentes = new ArrayList<>();
            for (int i = 0; i < workbookXLSX.getNumberOfSheets(); i++) {
                abasPresentes.add(workbookXLSX.getSheetName(i));
            }
            List<String> abasAdicionadas = abas.stream().filter(s -> !abasPresentes.contains(s))
                .collect(Collectors.toList());
            for (String aba : abasAdicionadas) {
                Sheet cloneSheet = workbookXLSX.cloneSheet(sheetClonada);
                String sheetName = cloneSheet.getSheetName();
                workbookXLSX.setSheetName(workbookXLSX.getSheetIndex(sheetName), aba);
            }
            int j = 0;
            for (int i = 0; i - j < workbookXLSX.getNumberOfSheets(); i++) {
                Sheet sheet = workbookXLSX.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                if (!abas.contains(sheetName)) {
                    workbookXLSX.removeSheetAt(i - j);
                    j++;
                }
            }

            for (Sheet sheet : workbookXLSX) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        alterarValorCell(map, sheet, row, cell);
                    }
                }
            }
            BaseFormulaEvaluator.evaluateAllFormulaCells(workbookXLSX);
            workbookXLSX.write(response);
        }
    }

    private static void makeExcelWithSubstitutions(String arquivo, Map<Object, Object> map, OutputStream outStream)
        throws IOException {
        try (InputStream file = ResourceFXUtils.toStream(arquivo);
            // Get the workbook instance for XLS file
            Workbook workbookXLSX = getWorkbook(arquivo, file)) {
            // Get first sheet from the workbook
            Sheet sheet = workbookXLSX.getSheetAt(0);
            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                for (Cell cell : row) {
                    alterarValorCell(map, sheet, row, cell);
                }
            }
            BaseFormulaEvaluator.evaluateAllFormulaCells(workbookXLSX);
            workbookXLSX.write(outStream);
        }
    }

    private static void printDebug(Object value) {
        LOG.trace("{}", value);
    }

    private static void setValorPorClasse(Map<Class<?>, CellStyle> formatMap, Row row, int colIndex, Object content) {
        if (content instanceof Date) {
            Cell createCell = row.createCell(colIndex, CellType.NUMERIC);
            createCell.setCellValue((Date) content);
            createCell.setCellStyle(formatMap.get(Date.class));
            return;
        }
        if (content instanceof BigDecimal) {
            Cell createCell = row.createCell(colIndex, CellType.NUMERIC);
            createCell.setCellValue(((BigDecimal) content).doubleValue());
            createCell.setCellStyle(formatMap.get(BigDecimal.class));
            return;
        }
        if (content instanceof Number) {
            Cell createCell = row.createCell(colIndex, CellType.NUMERIC);
            createCell.setCellValue(((Number) content).doubleValue());
            return;
        }
        if (content instanceof String) {
            Cell createCell = row.createCell(colIndex, CellType.STRING);
            String string = Objects.toString(content, "");
            createCell.setCellValue(string.replaceAll("\n", "\r\n").replaceAll("\t", ""));
            createCell.setCellStyle(formatMap.get(String.class));
            row.setHeightInPoints(Math.max(row.getHeightInPoints(),
                row.getSheet().getDefaultRowHeightInPoints() * string.replaceAll("[^\n]", "").length()));
            return;
        }
        if (content instanceof Boolean) {
            Cell createCell = row.createCell(colIndex, CellType.STRING);
            Boolean campo2 = (Boolean) content;
            createCell.setCellValue(campo2 ? "Sim" : "Não");
            return;
        }
        row.createCell(colIndex, CellType.BLANK);
    }

    private static Map<Class<?>, CellStyle> styleMap(Workbook workbook) {
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle formatoData = workbook.createCellStyle();
        formatoData.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
        CellStyle formatoBigDecimal = workbook.createCellStyle();
        formatoBigDecimal.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        CellStyle defaultStyle = workbook.createCellStyle();
        defaultStyle.setAlignment(HorizontalAlignment.LEFT);
        defaultStyle.setVerticalAlignment(VerticalAlignment.TOP);
        defaultStyle.setWrapText(true);
        Map<Class<?>, CellStyle> formatMap = new HashMap<>();
        formatMap.put(Date.class, formatoData);
        formatMap.put(BigDecimal.class, formatoBigDecimal);
        formatMap.put(String.class, defaultStyle);
        return formatMap;
    }

}
