package extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ExcelService {

	private static final Logger LOG = HasLogging.log();

    public static <T> void getExcel(BiFunction<Integer, Integer, List<T>> lista,
            Map<String, FunctionEx<T, Object>> fields,
            File file) {
        try (FileOutputStream response = new FileOutputStream(file);
                SXSSFWorkbook xssfWorkbook = new SXSSFWorkbook(500)) {
            SXSSFSheet sheetAt = xssfWorkbook.createSheet();
            sheetAt.trackAllColumnsForAutoSizing();
			Row row2 = sheetAt.createRow(0);
            List<String> keySet = new ArrayList<>(fields.keySet());
			for (int i = 0; i < keySet.size(); i++) {
				row2.createCell(i, CellType.STRING).setCellValue(keySet.get(i));
			}
			CreationHelper createHelper = xssfWorkbook.getCreationHelper();
			CellStyle formatoDate = xssfWorkbook.createCellStyle();
			formatoDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			CellStyle formatoBigDecimal = xssfWorkbook.createCellStyle();
			formatoBigDecimal.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

			List<T> apply;
			for (int i = 0; !(apply = lista.apply(i, 100)).isEmpty(); i += 100) {
				for (int j = 0; j < apply.size(); j++) {
					T entidade = apply.get(j);
					Row row = sheetAt.createRow(i + 1 + j);
					int k = 0;
                    for (FunctionEx<T, Object> campoFunction : fields.values()) {
                        Object campo = FunctionEx.makeFunction(campoFunction).apply(entidade);
						setValorPorClasse(formatoDate, formatoBigDecimal, row, k, campo);
						k++;
					}
				}
			}

            for (int k = 0; k < fields.size(); k++) {
                sheetAt.autoSizeColumn(k);
            }
            xssfWorkbook.write(response);
		} catch (IOException e) {
            LOG.error("ERRO ", e);
		}
	}

    public static <T> void getExcel(List<T> lista, Map<String, FunctionEx<T, Object>> mapa, File file) {

        try (FileOutputStream response = new FileOutputStream(file); XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheetAt = workbook.createSheet();
			XSSFRow row2 = sheetAt.createRow(0);
			int j = 0;
			Set<String> keySet = mapa.keySet();
			for (String titulo : keySet) {
				row2.createCell(j, CellType.STRING).setCellValue(titulo);
				j++;
			}
			XSSFCreationHelper createHelper = workbook.getCreationHelper();
			CellStyle formatoData = workbook.createCellStyle();
			formatoData.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			CellStyle formatoBigDecimal = workbook.createCellStyle();
			formatoBigDecimal.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

			for (int i = 0; i < lista.size(); i++) {
				T entidade = lista.get(i);
				XSSFRow row = sheetAt.createRow(i + 1);
				int k = 0;
                for (FunctionEx<T, Object> campoFunction : mapa.values()) {
                    Object campo = FunctionEx.makeFunction(campoFunction).apply(entidade);
					setValorPorClasse(formatoData, formatoBigDecimal, row, k, campo);
					k++;
				}

			}
			for (int k = 0; k < mapa.size(); k++) {
				sheetAt.autoSizeColumn(k);
			}

            workbook.write(response);

		} catch (IOException e) {
            LOG.error("ERRO ", e);
		}
	}

    public static void getExcel(String documento, Map<Object, Object> map, List<String> abas, int sheetClonada,
			OutputStream response) {
        try (InputStream file = ResourceFXUtils.toStream(documento);
                XSSFWorkbook workbookXLSX = new XSSFWorkbook(file)) {

			List<String> abasPresentes = new ArrayList<>();
			for (int i = 0; i < workbookXLSX.getNumberOfSheets(); i++) {
				abasPresentes.add(workbookXLSX.getSheetName(i));
			}
			List<String> abasAdicionadas = abas.stream().filter(s -> !abasPresentes.contains(s)).collect(Collectors.toList());

			for (String aba : abasAdicionadas) {
				XSSFSheet cloneSheet = workbookXLSX.cloneSheet(sheetClonada);
				String sheetName = cloneSheet.getSheetName();
				workbookXLSX.setSheetName(workbookXLSX.getSheetIndex(sheetName), aba);
			}

			for (int i = 0; i < workbookXLSX.getNumberOfSheets(); i++) {
				XSSFSheet sheet = workbookXLSX.getSheetAt(i);
				String sheetName = sheet.getSheetName();
				if (!abas.contains(sheetName)) {
					workbookXLSX.removeSheetAt(i);
					i--;
					continue;
				}
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					for (Cell cell : row) {
						alterarValorCell(map, sheet, row, cell);
					}
				}
			}
			XSSFFormulaEvaluator.evaluateAllFormulaCells(workbookXLSX);
			workbookXLSX.write(response);

		} catch (IOException e) {
            LOG.error("ERRO DE ENTRADA DE DADOS", e);
		}
	}

    public static void getExcel(String arquivo, Map<Object, Object> map, OutputStream outStream) {
        try (InputStream file = ResourceFXUtils.toStream(arquivo);
		// Get the workbook instance for XLS file
                XSSFWorkbook workbookXLSX = new XSSFWorkbook(file)) {
			// Get first sheet from the workbook
			XSSFSheet sheet = workbookXLSX.getSheetAt(0);
			// Get iterator to all the rows in current sheet

			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				for (Cell cell : row) {
					alterarValorCell(map, sheet, row, cell);
				}
			}

			XSSFFormulaEvaluator.evaluateAllFormulaCells(workbookXLSX);
			workbookXLSX.write(outStream);

		} catch (IOException e) {
            LOG.error("ERRO DE ENTRADA DE DADOS", e);
		}

	}


    public static void printDebug(Object value) {
        LOG.trace("{}", value);
	}

    public static void setValorPorClasse(CellStyle dateFormat, CellStyle formatoBigDecimal, Row row, int k,
			Object campo) {
		if (campo instanceof Date) {
			Cell createCell = row.createCell(k, CellType.NUMERIC);
			createCell.setCellValue((Date) campo);
			createCell.setCellStyle(dateFormat);
		} else if (campo instanceof BigDecimal) {
			Cell createCell = row.createCell(k, CellType.NUMERIC);
			createCell.setCellValue(((BigDecimal) campo).doubleValue());
			createCell.setCellStyle(formatoBigDecimal);
		} else if (campo instanceof Number) {
			Cell createCell = row.createCell(k, CellType.NUMERIC);
			createCell.setCellValue(((Number) campo).doubleValue());
		} else if (campo instanceof String) {
			Cell createCell = row.createCell(k, CellType.STRING);
            createCell.setCellValue(Objects.toString(campo, ""));
		} else if (campo instanceof Boolean) {
			Cell createCell = row.createCell(k, CellType.STRING);
			Boolean campo2 = (Boolean) campo;
			createCell.setCellValue(campo2 ? "Sim" : "Não");
		} else {
			row.createCell(k, CellType.BLANK);
		}
	}

    private static void alterarValorCell(Map<Object, Object> map, XSSFSheet sheet, Row row, Cell c) {
		Cell cell = c;
		if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            alterNumeric(map, sheet, cell);
		}
		if (cell.getCellTypeEnum() == CellType.STRING) {
            alterString(map, sheet, row, cell);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void alterNumeric(Map<Object, Object> map, XSSFSheet sheet, Cell cell) {
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
	private static void alterString(Map<Object, Object> map, XSSFSheet sheet, Row row, Cell c) {
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

}
