package utils;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.ex.FunctionEx;
import utils.ex.SupplierEx;

class ExcelHelper {

    private static final int DEFAULT_ROW_SIZE = 500;

    protected ExcelHelper() {
    }

    protected static Workbook getWorkbook(String selectedFile, InputStream fileInputStream) {

        return SupplierEx.remap(() -> selectedFile.endsWith(".xls") ? new HSSFWorkbook(fileInputStream)
                : new XSSFWorkbook(fileInputStream), "ERROR GETTING WORKBOOK " + selectedFile);
    }

    protected static <T> void makeBigExcel(BiFunction<Integer, Integer, List<T>> lista,
            Map<String, FunctionEx<T, Object>> fields, File file) throws IOException {
        try (FileOutputStream response = new FileOutputStream(file);
                SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(DEFAULT_ROW_SIZE)) {
            SXSSFSheet sheetAt = xssfWorkbook.createSheet();
            sheetAt.trackAllColumnsForAutoSizing();
            Row row2 = sheetAt.createRow(0);
            boolean addHeader = createHeader(row2, fields.keySet());
            Map<Class<?>, CellStyle> styleMap = styleMap(xssfWorkbook);
            final int step = 100;
            int i = 0;
            for (List<T> apply = lista.apply(0, step); !apply.isEmpty(); i += step, apply = lista.apply(i, step)) {
                for (int j = 0; j < apply.size(); j++) {
                    T entidade = apply.get(j);
                    Row row = sheetAt.createRow(i + (addHeader ? 1 : 0) + j);
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

    protected static <T> void makeExcelList(List<T> lista, Map<String, FunctionEx<T, Object>> mapa, File file)
            throws IOException {
        try (FileOutputStream response = new FileOutputStream(file); Workbook workbook = new XSSFWorkbook()) {
            Sheet sheetAt = workbook.createSheet();
            Row row2 = sheetAt.createRow(0);
            Set<String> keySet = mapa.keySet();
            boolean addHeader = createHeader(row2, keySet);
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

    protected static void makeExcelWithClonedSheets(String documento, Map<Object, Object> map, List<String> abas,
            int sheetClonada, OutputStream response) throws IOException {
        try (InputStream file = ResourceFXUtils.toStream(documento);
                Workbook workbookXLSX = getWorkbook(documento, file)) {
            List<String> abasPresentes = new ArrayList<>();
            for (int i = 0; i < workbookXLSX.getNumberOfSheets(); i++) {
                abasPresentes.add(workbookXLSX.getSheetName(i));
            }
            List<String> abasAdicionadas =
                    abas.stream().filter(s -> !abasPresentes.contains(s)).collect(Collectors.toList());
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

    protected static void makeExcelWithReplace(File arquivo, FunctionEx<List<Object>, Collection<?>> map,
            File outStream)
            throws IOException {
        // Get the workbook instance for XLS file
        try (InputStream response = new FileInputStream(arquivo);
                Workbook workbook = getWorkbook(arquivo.getName(), response)) {
            Map<Class<?>, CellStyle> formatMap = styleMap(workbook);
            for (Sheet sheetAt : workbook) {
                for (Row row2 : sheetAt) {
                    List<Object> arrayList = new ArrayList<>();
                    for (Cell cell : row2) {
                        Object o = getCellValue(cell);
                        arrayList.add(o);
                    }
                    Collection<?> apply = FunctionEx.apply(map, arrayList, arrayList);
                    int i = 0;
                    for (Object object : apply) {
                        setValorPorClasse(formatMap, row2, i++, object);
                    }
                }
            }
            try (FileOutputStream stream = new FileOutputStream(outStream)) {
                workbook.write(stream);
            }
        }
    }

    protected static void makeExcelWithSubstitutions(String arquivo, Map<Object, Object> map, OutputStream outStream)
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

    protected static <T> void makeMultiTabExcel(Map<String, List<T>> lista, Map<String, FunctionEx<T, Object>> mapa,
            File file) throws IOException {
        try (FileOutputStream response = new FileOutputStream(file); Workbook workbook = new XSSFWorkbook()) {
            Set<Entry<String, List<T>>> entrySet = lista.entrySet();
            for (Entry<String, List<T>> entry : entrySet) {

                Sheet sheetAt = workbook.createSheet(entry.getKey());
                Row row2 = sheetAt.createRow(0);
                Set<String> keySet = mapa.keySet();
                boolean addHeader = createHeader(row2, keySet);
                Map<Class<?>, CellStyle> formatMap = styleMap(workbook);

                for (int i = 0; i < entry.getValue().size(); i++) {
                    T entidade = entry.getValue().get(i);
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
            }

            workbook.write(response);

        }
    }

    private static void alterarValorCell(Map<Object, Object> map, Sheet sheet, Row row, Cell c) {
        if (c.getCellType() == CellType.NUMERIC) {
            alterNumeric(map, sheet, c);
        }
        if (c.getCellType() == CellType.STRING) {
            alterString(map, sheet, row, c);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void alterNumeric(Map<Object, Object> map, Sheet sheet, Cell cell) {
        double numericCellValue = cell.getNumericCellValue();
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
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void alterString(Map<Object, Object> map, Sheet sheet, Row row, Cell c) {
        Cell cell = c;
        String stringCellValue = cell.getStringCellValue();
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

    private static boolean createHeader(Row row2, Set<String> keySet) {
        boolean addHeader = !keySet.stream().allMatch(StringUtils::isNumeric);
        if (addHeader) {
            int j = 0;
            for (String titulo : keySet) {
                row2.createCell(j, CellType.STRING).setCellValue(titulo);
                j++;
            }
        }
        return addHeader;
    }

    private static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }


    private static void setValorPorClasse(Map<Class<?>, CellStyle> formatMap, Row row, int colIndex, Object content) {
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
        CellStyle defaultStyle = workbook.createCellStyle();
        defaultStyle.setAlignment(HorizontalAlignment.LEFT);
        defaultStyle.setVerticalAlignment(VerticalAlignment.TOP);
        defaultStyle.setWrapText(true);
        Map<Class<?>, CellStyle> formatMap = new HashMap<>();
        formatMap.put(Date.class, formatoData);
        formatMap.put(String.class, defaultStyle);
        return formatMap;
    }

}
