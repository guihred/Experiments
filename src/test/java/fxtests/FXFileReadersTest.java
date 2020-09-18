package fxtests;

import com.google.common.collect.ImmutableMap;
import ethical.hacker.ImageCracker;
import ethical.hacker.PCapReader;
import extract.DocumentHelper;
import extract.ExcelService;
import extract.WordService;
import fxml.utils.XmlToXlsx;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import kibana.KibanaApi;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.util.WaitForAsyncUtils;
import org.w3c.dom.Document;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import utils.*;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXFileReadersTest extends AbstractTestExecution {
    @Test
    public void testCSVUtils() {
        measureTime("CSVUtils.splitFile",
                () -> CSVUtils.splitFile(ResourceFXUtils.getOutFile("WDIData.csv").getAbsolutePath(), 3));
        measureTime("CSVUtils.splitFile", () -> CSVUtils
                .splitFile(ResourceFXUtils.getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3));
    }

    @Test
    public void testDocumentHelper() {
        Document doc =
                measureTime("DocumentHelper.getDoc", () -> DocumentHelper.getDoc(ResourceFXUtils.toFile("About.html")));
        measureTime("DocumentHelper.getImgs", () -> DocumentHelper.getImgs("oioi.com", doc));
        measureTime("DocumentHelper.getLinks", () -> DocumentHelper.getLinks("oioi.com", doc));
    }

    @Test
    public void testExcelAndWordFile() {
        ObservableList<Medicamento> medicamentosSNGPCPDF = measureTime("LeitorArquivos.getMedicamentosSNGPCPDF",
                () -> LeitorArquivos.getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf")));
        Map<String, FunctionEx<Medicamento, Object>> fields = new LinkedHashMap<>();
        fields.put("Registro", Medicamento::getRegistro);
        fields.put("Codigo", Medicamento::getCodigo);
        fields.put("Lote", Medicamento::getLote);
        fields.put("Nome", Medicamento::getNome);
        fields.put("Quantidade", Medicamento::getQuantidade);
        measureTime("ExcelService.exportList", () -> ExcelService.getExcel(medicamentosSNGPCPDF, fields,
                ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));
        measureTime("WordService.getPowerPointImages",
                () -> WordService.getPowerPointImages(ResourceFXUtils.toFullPath("testPowerPoint.pptx")));
        measureTime("WordService.getWord", () -> {
            Map<String, Object> mapaSubstituicao = new HashMap<>();
            File file = ResourceFXUtils.getOutFile("resultado.docx");
            mapaSubstituicao.put("443", "444");
            WordService.getWord(mapaSubstituicao, "CONTROLE_DCDF_RDMs.docx", file);

        });
    }

    @Test
    public void testExcelService() {
        ObservableList<Medicamento> medicamentosSNGPCPDF =
                LeitorArquivos.getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> fields = new LinkedHashMap<>();
        fields.put("Registro", Medicamento::getRegistro);
        fields.put("Nome", Medicamento::getNome);
        fields.put("Lote", Medicamento::getLote);
        fields.put("Quantidade", Medicamento::getQuantidade);
        fields.put("Codigo", Medicamento::getCodigo);
        fields.put("Valido", Medicamento::isRegistroValido);
        measureTime("ExcelService.getExcel", () -> ExcelService.getExcel(medicamentosSNGPCPDF, fields,
                ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));

    }


    @Test
    public void testExcelService2() {
        ObservableList<Medicamento> medicamentos =
                LeitorArquivos.getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> fields = new LinkedHashMap<>();
        fields.put("Registro", Medicamento::getRegistro);
        fields.put("Nome", Medicamento::getNome);
        fields.put("Lote", Medicamento::getLote);
        fields.put("Quantidade", Medicamento::getQuantidade);
        fields.put("Codigo", Medicamento::getCodigo);
        int maxI = medicamentos.size() - 1;
        measureTime("ExcelService.getExcel",
                () -> ExcelService.getExcel(
                        (i, s) -> medicamentos.subList(Integer.min(i, maxI), Integer.min(i + s, maxI)), fields,
                        ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));

    }

    @Test
    public void testExcelService3() {
        String arquivo = "anvisa2208.xlsx";
        Map<Object, Object> map = new HashMap<>();
        map.put("AEBOL", "OUTRO");
        map.put("norfloxacino", Arrays.asList("OUTRO"));
        map.put("levofloxacino", Arrays.asList());
        map.put(2.0, 3.0);
        map.put(3.0, "3");
        map.put(4.0, new BigDecimal(3));
        measureTime("ExcelService.getExcel", () -> ExcelService.getExcel(arquivo, map,
                new FileOutputStream(ResourceFXUtils.getOutFile("result.xlsx"))));

    }

    @Test
    public void testExcelService4() {
        String arquivo = "anvisa2208.xlsx";
        Map<Object, Object> map = new HashMap<>();
        map.put("AEBOL", "OUTRO");
        map.put("norfloxacino", Arrays.asList("Outra Lista"));
        List<String> abas = Arrays.asList("Página2", "Cópia");
        map.put("ALGICOD", ImmutableMap.builder().put(abas.get(0), "TROCADO").build());
        map.put(2.0, 3.0);
        map.put(3.0, "3");
        map.put(4.0, new BigDecimal(3));
        measureTime("ExcelService.getExcel", () -> ExcelService.getExcel(arquivo, map, abas, 0,
                new FileOutputStream(ResourceFXUtils.getOutFile("result.xlsx"))));

    }

    @Test
    public void testImageCracker() {
        measureTime("ImageCracker.readScreenshots",
                () -> ImageCracker.crackImages(ResourceFXUtils.getOutFile("screenshots")));
        measureTime("ImageCracker.crackImage", () -> ImageCracker.crackImage(ResourceFXUtils.toFile("CAPTCHA.jpg")));
        measureTime("ImageCracker.crackImage", () -> ImageCracker.crackImage(ResourceFXUtils.toFile("CAPTCHA2.jpg")));
        measureTime("ImageCracker.createSelectedImage", () -> ImageCracker.crackImage(
                ImageCracker.createSelectedImage(new Image(ResourceFXUtils.toExternalForm("CAPTCHA.jpg")))));
    }

    @Test
    public void testLeitorArquivos() {

        File file = ResourceFXUtils.toFile("anvisa2208.xlsx");
        ObservableList<String> sheetsExcel =
                measureTime("LeitorArquivos.getSheetsExcel", () -> ExcelService.getSheetsExcel(file));

        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<List<String>> listExcel =
                measureTime("LeitorArquivos.getListExcel", () -> LeitorArquivos.getListExcel(file, sheetsExcel.get(0)));
        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<Medicamento> converterMedicamentos = measureTime("LeitorArquivos.converterMedicamentos",
                () -> LeitorArquivos.converterMedicamentos(listExcel, Arrays.asList(LeitorArquivos.REGISTRO,
                        LeitorArquivos.NOME, LeitorArquivos.LOTE, LeitorArquivos.QUANTIDADE)));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Size must be equal", 679, converterMedicamentos.size());
    }

    @Test
    public void testLeitorArquivosPDF() {
        measureTime("LeitorArquivos.getMedicamentosSNGPCPDF", () -> {
            File file = ResourceFXUtils.toFile("sngpc2808.pdf");
            ObservableList<Medicamento> medicamentos = LeitorArquivos.getMedicamentosSNGPCPDF(file);
            WaitForAsyncUtils.waitForFxEvents();
            Assert.assertEquals("Size must be equal", 656, medicamentos.size());
        });
        measureTime("LeitorArquivos.getMedicamentosAnvisa", () -> {
            File file2 = ResourceFXUtils.toFile("anvisa2208.xlsx");
            ObservableList<Medicamento> medicamentos = LeitorArquivos.getMedicamentosAnvisa(file2);
            WaitForAsyncUtils.waitForFxEvents();
            Assert.assertEquals("Size must be equal", 679, medicamentos.size());
        });
    }

    @Test
    public void testLoadKibanaApi() {
        measureTime("ExcelService.getExcel",()->ExcelService.getExcel(ResourceFXUtils.toFile("Lista de IP da Caixa.xlsx"), (List<Object> l) -> {
            if (l.isEmpty()) {
                return null;
            }
            long count = l.stream().filter(Objects::nonNull).count();
            if (count != 1) {
                return null;
            }
            String string = convert(l.get(0));
            if (string.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                Map<String, String> kibanaFullScan = KibanaApi.kibanaFullScan(string);
                List<String> collect = kibanaFullScan.values().stream().collect(Collectors.toList());
                collect.add(1, "Guilherme");
                return collect;
            }
            return null;
        }, ResourceFXUtils.getOutFile("apiResult2.xlsx"))
                );
    }

    @Test
    public void testPCapReader() {
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\CiscoCNNA\\one.pcapng");
        measureTime("PCapReader.readPCAPngFile", () ->PCapReader.readPCAPngFile(file));
    }

    @Test
    public void testRar() {
        measureTime("UnRar.extractRarFiles", () -> UnRar.extractRarFiles(UnRar.SRC_DIRECTORY));
        File userFolder = ResourceFXUtils.getOutFile().getParentFile();
        FileTreeWalker.getPathByExtension(userFolder, "rar").stream().map(FunctionEx.makeFunction(e -> {
            Path name = e.getName(e.getNameCount() - 1);
            File outFile = ResourceFXUtils.getOutFile(name.toString());
            ExtractUtils.copy(e, outFile);
            return outFile.toPath();
        })).forEach(ConsumerEx.makeConsumer(p -> UnRar.extractRarFiles(p.toFile())));

    }

    @Test
    public void testXmlToXlsx() {
        File file = new File("C:\\Users\\guigu\\Downloads\\export.xls");
        measureTime("XmlToXlsx.convertXML2XLS", () -> XmlToXlsx.convertXML2XLS(file));
    }

    @Test
    public void testZIP() {
        measureTime("UnZip.extractZippedFiles", () -> UnZip.extractZippedFiles(UnZip.ZIPPED_FILE_FOLDER));
    }

    private static String convert(Object o) {
        if (o instanceof Double) {
            long longValue = ((Double) o).longValue();
            return String.format(Locale.getDefault(), "%,d", longValue);
        }
        return Objects.toString(o, "");
    }

}
