package rosario;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public final class LeitorArquivos {
    private static final String QTESTOQUECOMERCIAL = "qtestoquecomercial";
    private static final String CODPRODUTO = "codproduto";
    public static final String CODIGO = "Codigo";
    public static final String LOTE = "Lote";
    public static final String NOME = "Nome";
    public static final String QUANTIDADE = "Quantidade";
    public static final String REGISTRO = "Registro";
    private static final Logger LOGGER = HasLogging.log(LeitorArquivos.class);

    private LeitorArquivos() {
    }

    public static void main(String[] args) {
        File file = ResourceFXUtils.toFile("sngpc.pdf");
        try {
            ObservableList<Medicamento> medicamentosSNGPCPDF;
            medicamentosSNGPCPDF = getMedicamentosSNGPCPDF(file);
            medicamentosSNGPCPDF.forEach(s -> LOGGER.info("{}", s));
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    public static ObservableList<Medicamento> getMedicamentosSNGPCPDF(File file) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();

        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            pdfStripper.setStartPage(1);
            String parsedText = pdfStripper.getText(pdDoc);
            cosDoc.close();
            String[] linhas = parsedText.split("\r\n");
            Medicamento medicamento = new Medicamento();
            ObservableList<Medicamento> listaMedicamentos = FXCollections.observableArrayList();
            for (int i = 0; i < linhas.length; i++) {
                medicamento = tryReadSNGPCLine(linhas, medicamento, listaMedicamentos, i);

            }
            return listaMedicamentos;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw e;
        }

    }

    private static Medicamento tryReadSNGPCLine(String[] linhas, Medicamento m, ObservableList<Medicamento> arrayList,
            int i) {
        Medicamento medicamento = m;
        try {

            String s = linhas[i];
            String[] split = s.trim().split("\\s+");

            if ("Página:".equals(split[0]) || !StringUtil.isNumeric(split[split.length - 1])) {
                return medicamento;
            }
            if (split.length == 4 || split.length == 3) {
                medicamento.setLote(
                        !StringUtil.isNumeric(split[0]) ? split[0] : Integer.toString(Integer.valueOf(split[0])));
                medicamento.setRegistro(split[1].replaceAll("\\D+", ""));
                medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
                arrayList.add(medicamento);
                medicamento = medicamento.clonar();
            } else {
                medicamento.setCodigo(Integer.valueOf(split[0]));
                medicamento.setNome(
                        Stream.of(split).skip(1).limit((long) split.length - 2).collect(Collectors.joining(" ")));

                medicamento.setQuantidade(Integer.valueOf(split[split.length - 1]));
            }
        } catch (Exception e) {
            LOGGER.info("ERRO LINHA ={}", i);
            LOGGER.error("", e);
        }
        return medicamento;
    }

    public static ObservableList<Medicamento> getMedicamentosRosarioExcel(File selectedFile) {
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook xssfWorkbook = getWorkbook(selectedFile, fileInputStream)) {
            Sheet sheetAt = xssfWorkbook.getSheetAt(0);
            Iterator<Row> iterator = sheetAt.iterator();
            ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
            int i = 1;
            while (iterator.hasNext()) {
                i++;
                boolean tryReadAnvisaLine = tryReadRosarioLine(iterator, medicamentos, i);
                if (!tryReadAnvisaLine) {
                    break;
                }
            }
            return medicamentos;

        } catch (IOException e) {
            HasLogging.log().error("ERROR WHEN READING EXCEL", e);
            return FXCollections.observableArrayList();
        }

    }

    private static Workbook getWorkbook(File selectedFile, FileInputStream fileInputStream) throws IOException {
        return selectedFile.getName().endsWith(".xls") ? new HSSFWorkbook(fileInputStream)
                : new XSSFWorkbook(fileInputStream);
    }

    public static ObservableList<Medicamento> getMedicamentosRosario(File file) throws IOException {
        if (isExcel(file)) {
            return getMedicamentosRosarioExcel(file);
        }

        PDFTextStripper pdfStripper = new PDFTextStripper();
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            pdfStripper.setStartPage(1);
            String parsedText = pdfStripper.getText(pdDoc);
            String[] linhas = parsedText.split("\r\n");
            Map<String, IntUnaryOperator> hashMap = new HashMap<>();

            ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
            for (int i = 0; i < linhas.length; i++) {
                Medicamento medicamento = tryReadRosarioLine(hashMap, linhas, i);
                if (medicamento != null) {
                    medicamentos.add(medicamento);
                    if (StringUtil.isBlank(medicamento.getNome())) {
                        medicamento.setNome(medicamentos.get(medicamentos.size() - 2).getNome());
                    }
                }
            }
            return medicamentos;
        } catch (Exception e) {
            HasLogging.log().error("ERROR WHEN READING EXCEL", e);
            return FXCollections.observableArrayList();
        }

    }

    public static boolean isExcel(File file) {
        return file.getName().endsWith("xlsx") || file.getName().endsWith("xls");
    }

    private static Medicamento tryReadRosarioLine(Map<String, IntUnaryOperator> mapaCampos, String[] linhas, int i) {
        try {
            String s = linhas[i];
            String[] split = s.trim().split("\\s+");
            if (split.length > 2 && (s.toLowerCase().contains("descricao") || s.toLowerCase().contains(CODPRODUTO)
                    || s.toLowerCase().contains(QTESTOQUECOMERCIAL))) {
                if (split[1].equalsIgnoreCase(CODPRODUTO)) {
                    mapaCampos.put(CODPRODUTO, j -> j - 2);
                }
                if (split[0].equalsIgnoreCase(CODPRODUTO)) {
                    mapaCampos.put(CODPRODUTO, j -> 0);
                }
                mapaCampos.put(QTESTOQUECOMERCIAL, j -> j - 1);
            }
            if (!s.endsWith(",00")) {
                return null;
            }
            if (split.length >= 2) {
                Medicamento medicamento = new Medicamento();
                String s2 = split[mapaCampos.getOrDefault(CODPRODUTO, j -> 0).applyAsInt(split.length)];

                medicamento.setCodigo(Integer.valueOf(s2));
                medicamento.setNome(IntStream
                        .range(0, split.length).filter(e -> mapaCampos.values().stream()
                                .mapToInt(j -> j.applyAsInt(split.length)).noneMatch(j -> j == e))
                        .mapToObj(e -> split[e]).collect(Collectors.joining(" ")));
                medicamento.setQuantidade(Integer
                        .valueOf(split[mapaCampos.getOrDefault(QTESTOQUECOMERCIAL, j -> j - 1).applyAsInt(split.length)]
                                .replace(",00", "").replace(".", "")));
                return medicamento;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

    public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) throws IOException {
        if (isPDF(selectedFile)) {
            return getMedicamentosSNGPCPDF(selectedFile);
        }

        try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                Workbook xssfWorkbook = getWorkbook(selectedFile, fileInputStream)) {
            Sheet sheetAt = xssfWorkbook.getSheetAt(0);
            Iterator<Row> iterator = sheetAt.iterator();
            ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
            int i = 1;
            while (iterator.hasNext()) {
                i++;
                if (!tryReadAnvisaLine(iterator, medicamentos, i)) {
                    break;
                }
            }
            return medicamentos;

        } catch (Exception e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    public static boolean isPDF(File selectedFile) {
        return selectedFile.getName().endsWith(".pdf");
    }

    private static boolean tryReadAnvisaLine(Iterator<Row> iterator, ObservableList<Medicamento> medicamentos, int i) {
        try {

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
        } catch (Exception e) {
            LOGGER.info("ERRO LINHA={}", i);
            LOGGER.error("", e);
        }
        return true;
    }

    private static boolean tryReadRosarioLine(Iterator<Row> iterator, ObservableList<Medicamento> medicamentos, int i) {
        try {

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
            String stringCellValue = next.getCell(1).getStringCellValue();
            if ("Totais".equalsIgnoreCase(stringCellValue)) {
                return true;
            }
            medicamento.setNome(stringCellValue);
            medicamento.setQuantidade((int) next.getCell(2).getNumericCellValue());
            medicamentos.add(medicamento);
        } catch (Exception e) {
            LOGGER.info("ERRO LINHA={}", i);
            LOGGER.error("", e);
        }
        return true;
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

    private static String getLote(Row row) {
        Cell cell = row.getCell(row.getLastCellNum() - 2);
        if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
            return Integer.toString((int) cell.getNumericCellValue());
        } else if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        return "";
    }

    public static File exportarArquivo(ObservableList<Medicamento> medicamentosLoja,
            List<Medicamento> medicamentosSNGPC, ObservableList<Medicamento> medicamentosAnvisa) {

        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheetLoja = wb.createSheet("Estoque Loja");
        criarAbaLoja(medicamentosLoja, wb, sheetLoja);
        Sheet createSheet = wb.createSheet("Estoque SNGPC");
        criarAba(medicamentosSNGPC, wb, createSheet);
        Sheet sheet = wb.createSheet("Estoque ANVISA");
        criarAba(medicamentosAnvisa, wb, sheet);

        File file2 = new File("resultado.xlsx");
        try (OutputStream file = new FileOutputStream(file2)) {
            wb.write(file);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return file2;
    }

    private static void criarAba(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillBackgroundColor(new XSSFColor(new Color(1.0F, 0.2F, 0.2F)));
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

    private static void criarAbaLoja(List<Medicamento> medicamentos, XSSFWorkbook wb, Sheet createSheet) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillBackgroundColor(new XSSFColor(new Color(1.0F, 0.2F, 0.2F)));
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

    public static ObservableList<String> getSheetsExcel(File selectedFile) {
        ObservableList<String> list = FXCollections.observableArrayList();
        Platform.runLater(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    Workbook workbook = getWorkbook(selectedFile, fileInputStream)) {
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    list.add(workbook.getSheetAt(i).getSheetName());
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        });
        return list;
    }

    public static ObservableList<List<String>> getListExcel(File selectedFile, String sheetName) {
        ObservableList<List<String>> list = FXCollections.observableArrayList();
        Platform.runLater(() -> {
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
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        });
        return list;
    }

    public static ObservableList<Medicamento> converterMedicamentos(Iterable<List<String>> items2,
            List<String> columns) {


        ObservableList<Medicamento> medicamentos = FXCollections.observableArrayList();
        for (List<String> item : items2) {
            Medicamento medicamento = new Medicamento();
            setCampos(columns, item, medicamento);
            if (medicamento.getQuantidade() != null && StringUtils.isNotBlank(medicamento.getNome())) {
                medicamentos.add(medicamento);
            }

        }
        return medicamentos;
    }

    private static void setCampos(List<String> colunas, List<String> item, Medicamento medicamento) {
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

    private static Integer intValue(String s) {
        try {
            return Integer.valueOf(s.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            LOGGER.trace("", e);
            return null;
        }
    }


}