package extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.Workbook;
import utils.CommonsFX;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public final class ExcelService extends ExcelHelper {

    private ExcelService() {
    }

    public static <T> void getExcel(BiFunction<Integer, Integer, List<T>> lista,
            Map<String, FunctionEx<T, Object>> fields, File file) {
        RunnableEx.run(() -> makeBigExcel(lista, fields, file));
    }

    public static void getExcel(File arquivo, FunctionEx<List<Object>, Collection<?>> map, File outStream) {
        RunnableEx.run(() -> makeExcelWithReplace(arquivo, map, outStream));
    }

    public static void getExcel(List<Map<String, String>> items2, File outFile) {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        List<String> columns =
                items2.stream().flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList());
        for (String tableColumn : columns) {
            String text = tableColumn;
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        ExcelService.getExcel(items2, mapa, outFile);
    }

    public static <T> void getExcel(List<T> lista, Map<String, FunctionEx<T, Object>> mapa, File file) {
        RunnableEx.run(() -> makeExcelList(lista, mapa, file));
    }

    public static <T> void getExcel(Map<String, List<T>> lista, Map<String, FunctionEx<T, Object>> mapa, File file) {
        RunnableEx.run(() -> makeMultiTabExcel(lista, mapa, file));
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
        CommonsFX.runInPlatform(() -> {
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

    public static Workbook getWorkbook(File selectedFile, InputStream fileInputStream) {
        return getWorkbook(selectedFile.getName(), fileInputStream);
    }

    public static boolean isExcel(File file) {
        return file.getName().endsWith("xlsx") || file.getName().endsWith("xls");
    }


}
