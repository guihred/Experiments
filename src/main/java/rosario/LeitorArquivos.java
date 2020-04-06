package rosario;

import static extract.ExcelService.getWorkbook;
import static extract.ExcelService.isExcel;
import static utils.StringSigaUtils.intValue;

import extract.PdfUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;
import utils.SupplierEx;

public final class LeitorArquivos {
    private static final Color RED_COLOR = new Color(1.0F, 0.2F, 0.2F);
    private static final String QTESTOQUECOMERCIAL = "qtestoquecomercial";
    private static final String CODPRODUTO = "codproduto";
    public static final String CODIGO = "Codigo";
    public static final String LOTE = "Lote";
    public static final String NOME = "Nome";
    public static final String QUANTIDADE = "Quantidade";
    public static final String REGISTRO = "Registro";
    private static final Logger LOGGER = HasLogging.log();

    private LeitorArquivos() {
    }

    public static ObservableList<Medicamento> converterMedicamentos(Iterable<List<String>> items2,
        List<String> columns) {

        ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
        for (List<String> item : items2) {
            Medicamento medicamento = new Medicamento();
            setFields(columns, item, medicamento);
            if (medicamento.getQuantidade() != null && StringUtils.isNotBlank(medicamento.getNome())) {
                medicamentos.add(medicamento);
            }
        }
        return medicamentos;
    }

    public static File exportarArquivo(ObservableList<Medicamento> medicamentosLoja,
        List<Medicamento> medicamentosSNGPC, ObservableList<Medicamento> medicamentosAnvisa) {

        XSSFWorkbook wb = new XSSFWorkbook();
        criarAbaLoja(medicamentosLoja, wb, "Estoque Loja");
        criarAba(medicamentosSNGPC, wb, "Estoque SNGPC");
        criarAba(medicamentosAnvisa, wb, "Estoque ANVISA");

        File file2 = new File("resultado.xlsx");
        RunnableEx.run(() -> {
            try (OutputStream file = new FileOutputStream(file2)) {
                wb.write(file);
            }
        });
        return file2;
    }

    public static ObservableList<List<String>> getListExcel(File selectedFile, String sheetName) {
        ObservableList<List<String>> list = FXCollections.observableArrayList();
        Platform.runLater(RunnableEx.make(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook workbook = getWorkbook(selectedFile, fileInputStream)) {
                Sheet sheetAt = sheetName == null ? workbook.getSheetAt(0) : workbook.getSheet(sheetName);
                for (Row row : sheetAt) {
                    List<String> fields = new ArrayList<>();
                    for (Cell cell : row) {
                        fields.add(getString(cell));
                    }
                    list.add(fields);
                }
            }
        }));
        return list;
    }

    public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
        if (isPDF(selectedFile)) {
            return getMedicamentosSNGPCPDF(selectedFile);
        }
        return SupplierEx.remap(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook xssfWorkbook = getWorkbook(selectedFile, fileInputStream)) {
                Sheet sheetAt = xssfWorkbook.getSheetAt(0);
                Iterator<Row> iterator = sheetAt.iterator();
                ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
                while (iterator.hasNext()) {
                    if (!SupplierEx.get(() -> tryReadAnvisaLine(iterator, medicamentos), true)) {
                        break;
                    }
                }
                return medicamentos;
            }
        }, "ERROR READING ANVISA FILE " + selectedFile);

    }

    public static ObservableList<Medicamento> getMedicamentosRosario(File file) {
        if (isExcel(file)) {
            return getMedicamentosRosarioExcel(file);
        }

        return SupplierEx.remap(() -> {
            String[] linhas = PdfUtils.getAllLines(file);
            Map<String, IntUnaryOperator> intMapper = new HashMap<>();
            ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
            for (int i = 0; i < linhas.length; i++) {
                int j = i;
                Medicamento medicamento = SupplierEx.get(() -> tryReadRosarioLine(intMapper, linhas, j));
                if (medicamento != null) {
                    medicamentos.add(medicamento);
                    if (StringUtil.isBlank(medicamento.getNome())) {
                        medicamento.setNome(medicamentos.get(medicamentos.size() - 2).getNome());
                    }
                }
            }
            return medicamentos;
        }, "ERROR READING FILE");

    }

    public static ObservableList<Medicamento> getMedicamentosSNGPCPDF(File file) {
        return SupplierEx.remap(() -> {
            String[] linhas = PdfUtils.getAllLines(file);
            Medicamento medicamento = new Medicamento();
            ObservableList<Medicamento> listaMedicamentos = FXCollections.observableArrayList();
            for (int i = 0; i < linhas.length; i++) {
                medicamento = tryReadSNGPCLine(linhas, medicamento, listaMedicamentos, i);
            }
            return listaMedicamentos;
        }, "ERROR READING FILE");
    }

    public static ObservableList<String> getSheetsExcel(File selectedFile) {
        ObservableList<String> list = FXCollections.observableArrayList();
        Platform.runLater(RunnableEx.make(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook workbook = getWorkbook(selectedFile, fileInputStream)) {
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    list.add(workbook.getSheetAt(i).getSheetName());
                }
            }
        }));
        return list;
    }

    public static void main(String[] args) {
        File file = ResourceFXUtils.toFile("sngpc.pdf");
        RunnableEx.run(() -> getMedicamentosSNGPCPDF(file).forEach(s -> LOGGER.info("{}", s)));
    }

    private static void criarAba(List<Medicamento> medicamentos, XSSFWorkbook wb, String sheetName) {
        Sheet createSheet = wb.createSheet(sheetName);

        XSSFCellStyle style = wb.createCellStyle();
        style.setFillBackgroundColor(new XSSFColor(RED_COLOR));
        style.setFillPattern(FillPatternType.FINE_DOTS);
        int j = 0;
        for (int i = 0; i < medicamentos.size(); i++) {
            Medicamento medicamento = medicamentos.get(i);
            if (medicamento.isRegistroValido() && medicamento.isLoteValido() && medicamento.isQuantidadeValido()) {
                continue;
            }
            Row createRow = createSheet.createRow(j++);

            Cell createCell = createRow.createCell(0);

            createCell.setCellValue(medicamento.getRegistro());
            if (!medicamento.isRegistroValido()) {

                createCell.setCellStyle(style);
            }

            createRow.createCell(1).setCellValue(medicamento.getNome());
            Cell createCell2 = createRow.createCell(2);
            createCell2.setCellValue(medicamento.getLote());
            if (!medicamento.isLoteValido()) {
                createCell2.setCellStyle(style);
            }
            Cell createCell3 = createRow.createCell(3);
            createCell3.setCellValue(medicamento.getQuantidade());
            if (!medicamento.isQuantidadeValido()) {
                createCell3.setCellStyle(style);
            }
        }

        createSheet.autoSizeColumn(0);
        createSheet.autoSizeColumn(1);
        createSheet.autoSizeColumn(2);
        createSheet.autoSizeColumn(3);
        createSheet.autoSizeColumn(4);
    }

    private static void criarAbaLoja(List<Medicamento> medicamentos, XSSFWorkbook wb, String sheetname) {

        Sheet createSheet = wb.createSheet(sheetname);
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillBackgroundColor(new XSSFColor(RED_COLOR));
        style.setFillPattern(FillPatternType.FINE_DOTS);
        int j = 0;
        for (int i = 0; i < medicamentos.size(); i++) {
            Medicamento medicamento = medicamentos.get(i);
            if (medicamento.isCodigoValido() && medicamento.isQuantidadeValido()) {
                continue;
            }
            Row createRow = createSheet.createRow(j++);

            Cell createCell = createRow.createCell(0);

            createCell.setCellValue(medicamento.getCodigo());
            if (!medicamento.isCodigoValido()) {
                createCell.setCellStyle(style);
            }

            createRow.createCell(1).setCellValue(medicamento.getNome());
            Cell createCell3 = createRow.createCell(2);
            createCell3.setCellValue(medicamento.getQuantidade());
            if (!medicamento.isQuantidadeValido()) {
                createCell3.setCellStyle(style);
            }
        }

        createSheet.autoSizeColumn(0);
        createSheet.autoSizeColumn(1);
        createSheet.autoSizeColumn(2);
    }

    private static String getLote(Row row) {
        Cell cell = row.getCell(row.getLastCellNum() - 2);
        if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
            return Integer.toString((int) cell.getNumericCellValue());
        } else if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        return "";
    }

    private static ObservableList<Medicamento> getMedicamentosRosarioExcel(File selectedFile) {
        return SupplierEx.get(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook xssfWorkbook = getWorkbook(selectedFile, fileInputStream)) {
                Sheet sheetAt = xssfWorkbook.getSheetAt(0);
                Iterator<Row> iterator = sheetAt.iterator();
                ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
                while (iterator.hasNext()) {
                    if (!SupplierEx.get(() -> tryReadRosarioLine(iterator, medicamentos), true)) {
                        break;
                    }
                }
                return medicamentos;
            }
        }, FXCollections.observableArrayList());

    }

    private static String getRegistro(Cell cell0) {
        if (cell0.getCellTypeEnum() == CellType.NUMERIC) {
            return Integer.toString((int) cell0.getNumericCellValue());
        }
        return cell0.getStringCellValue().replaceAll("\\D+", "");
    }

    private static String getString(Cell cell0) {
        if (cell0.getCellTypeEnum() == CellType.NUMERIC) {
            double numericCellValue = cell0.getNumericCellValue();
            return Long.toString((long) numericCellValue);
        }
        return cell0.getStringCellValue();
    }

    private static boolean isPDF(File selectedFile) {
        return selectedFile.getName().endsWith(".pdf");
    }

    private static void setFields(List<String> colunas, List<String> item, Medicamento medicamento) {
        for (int i = 0; i < colunas.size(); i++) {
            String selectedItem = colunas.get(i);
            if (!item.isEmpty()) {
                switch (selectedItem) {
                    case REGISTRO:
                        medicamento.setRegistro(Objects.toString(medicamento.getRegistro(), "")
                            + item.get(i % item.size()).replaceAll("\\D+", ""));
                        break;
                    case NOME:
                        medicamento.setNome(Objects.toString(medicamento.getNome(), "") + item.get(i % item.size()));
                        break;
                    case LOTE:
                        String string = item.get(i % item.size());
                        medicamento.setLote(Objects.toString(medicamento.getLote(), "") + string);
                        break;
                    case QUANTIDADE:
                        Integer qnt = intValue(item.get(i % item.size()));
                        if (qnt != null) {
                            medicamento.setQuantidade(qnt);
                        }
                        break;
                    case CODIGO:
                        Integer codigo = intValue(item.get(i % item.size()));
                        if (codigo != null) {
                            medicamento.setCodigo(codigo);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static boolean tryReadAnvisaLine(Iterator<Row> iterator, ObservableList<Medicamento> medicamentos) {
        Row next = iterator.next();
        Cell cell0 = next.getCell(0);
        if (cell0 == null) {
            return false;
        }
        Medicamento medicamento = new Medicamento();
        medicamento.setRegistro(getRegistro(cell0));
        medicamento.setNome(next.getCell(1).getStringCellValue());
        medicamento.setLote(getLote(next));
        medicamento.setQuantidade((int) next.getCell(next.getLastCellNum() - 1).getNumericCellValue());
        medicamentos.add(medicamento);
        return true;
    }

    private static boolean tryReadRosarioLine(Iterator<Row> iterator, ObservableList<Medicamento> medicamentos) {
        Row next = iterator.next();
        Cell cell0 = next.getCell(0);
        if (cell0 == null) {
            return false;
        }
        String registro = getRegistro(cell0);
        if (registro.isEmpty()) {
            return true;
        }
        Medicamento medicamento = new Medicamento();
        medicamento.setCodigo(Integer.valueOf(registro));
        String nome = next.getCell(1).getStringCellValue();
        if ("Totais".equalsIgnoreCase(nome)) {
            return true;
        }
        medicamento.setNome(nome);
        medicamento.setQuantidade((int) next.getCell(2).getNumericCellValue());
        medicamentos.add(medicamento);
        return true;
    }

    private static Medicamento tryReadRosarioLine(Map<String, IntUnaryOperator> mapaFields, String[] linhas, int i) {
        String s = linhas[i];
        String[] split = s.trim().split("\\s+");
        if (split.length > 2 && (s.toLowerCase().contains("descricao") || s.toLowerCase().contains(CODPRODUTO)
            || s.toLowerCase().contains(QTESTOQUECOMERCIAL))) {
            if (split[1].equalsIgnoreCase(CODPRODUTO)) {
                mapaFields.put(CODPRODUTO, j -> j - 2);
            }
            if (split[0].equalsIgnoreCase(CODPRODUTO)) {
                mapaFields.put(CODPRODUTO, j -> 0);
            }
            mapaFields.put(QTESTOQUECOMERCIAL, j -> j - 1);
        }
        if (!s.endsWith(",00")) {
            return null;
        }
        if (split.length >= 2) {
            Medicamento medicamento = new Medicamento();
            String s2 = split[mapaFields.getOrDefault(CODPRODUTO, j -> 0).applyAsInt(split.length)];

            medicamento.setCodigo(Integer.valueOf(s2));
            medicamento.setNome(IntStream.range(0, split.length)
                .filter(
                    e -> mapaFields.values().stream().mapToInt(j -> j.applyAsInt(split.length)).noneMatch(j -> j == e))
                .mapToObj(e -> split[e]).collect(Collectors.joining(" ")));
            medicamento.setQuantidade(
                Integer.valueOf(split[mapaFields.getOrDefault(QTESTOQUECOMERCIAL, j -> j - 1).applyAsInt(split.length)]
                    .replace(",00", "").replace(".", "")));
            return medicamento;
        }
        return null;
    }

    private static Medicamento tryReadSNGPCLine(String[] linhas, Medicamento m, ObservableList<Medicamento> arrayList,
        int i) {
        Medicamento medicamento = m;
        try {
            String[] split = linhas[i].trim().split("\\s+");
            if ("Página:".equals(split[0]) || !StringUtil.isNumeric(split[split.length - 1])) {
                return medicamento;
            }
            if (split.length == 4 || split.length == 3) {
                medicamento
                    .setLote(!StringUtil.isNumeric(split[0]) ? split[0] : Integer.toString(Integer.valueOf(split[0])));
                medicamento.setRegistro(split[1].replaceAll("\\D+", ""));
                medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
                arrayList.add(medicamento);
                medicamento = medicamento.clonar();
            } else {
                medicamento.setCodigo(Integer.valueOf(split[0]));
                medicamento
                    .setNome(Stream.of(split).skip(1).limit((long) split.length - 2).collect(Collectors.joining(" ")));

                medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
            }
        } catch (Exception e) {
            LOGGER.info("ERRO LINHA ={}", i);
            LOGGER.error("", e);
        }
        return medicamento;
    }

}