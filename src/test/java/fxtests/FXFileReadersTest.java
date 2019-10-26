package fxtests;

import static ethical.hacker.ImageCracker.crackImage;
import static ethical.hacker.ImageCracker.createSelectedImage;
import static fxtests.FXTesting.measureTime;
import static japstudy.JapanRefactoring.TXT_FILE;
import static japstudy.JapanRefactoring.refactorJapaneseFile;
import static japstudy.JapanRefactoring.renameFile;
import static java.nio.file.Files.deleteIfExists;
import static utils.ResourceFXUtils.getOutFile;
import static utils.ResourceFXUtils.toExternalForm;
import static utils.ResourceFXUtils.toFile;

import com.google.common.collect.ImmutableMap;
import contest.db.Contest;
import ethical.hacker.PortServices;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.PointCh4;
import ex.j9.ch4.PrimaryColor;
import ex.j9.ch4.RectangleCh4;
import extract.ExcelService;
import extract.WordService;
import gaming.ex01.SnakeSquare;
import gaming.ex16.MadEdge;
import gaming.ex16.MadEdgeDistance;
import graphs.EdgeElement;
import graphs.Vertex;
import graphs.entities.EdgeDistancePack;
import graphs.entities.Linha;
import graphs.entities.Ponto;
import japstudy.JapaneseLesson;
import japstudy.LessonPK;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ml.Word2VecExample;
import ml.data.CSVUtils;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import utils.FunctionEx;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXFileReadersTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        stage.show();
    }

    @Test
    public void testCSVUtils() {
        measureTime("CSVUtils.splitFile", () -> CSVUtils.splitFile(getOutFile("WDIData.csv").getAbsolutePath(), 3));
        measureTime("CSVUtils.splitFile",
            () -> CSVUtils.splitFile(getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3));
    }

    @Test
    public void testExcelAndWordFile() throws Exception {
        ObservableList<Medicamento> medicamentosSNGPCPDF = measureTime("LeitorArquivos.getMedicamentosSNGPCPDF",
            () -> LeitorArquivos.getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf")));
        Map<String, FunctionEx<Medicamento, Object>> campos = new LinkedHashMap<>();
        campos.put("Registro", Medicamento::getRegistro);
        campos.put("Codigo", Medicamento::getCodigo);
        campos.put("Lote", Medicamento::getLote);
        campos.put("Nome", Medicamento::getNome);
        campos.put("Quantidade", Medicamento::getQuantidade);
        measureTime("ExcelService.exportList",
            () -> ExcelService.getExcel(medicamentosSNGPCPDF, campos, ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));
        measureTime("WordService.getPowerPointImages",
            () -> WordService.getPowerPointImages(ResourceFXUtils.toFullPath("testPowerPoint.pptx")));
        measureTime("WordService.getWord", () -> {
            Map<String, Object> mapaSubstituicao = new HashMap<>();
            File file = ResourceFXUtils.getOutFile("resultado.docx");
            mapaSubstituicao.put("443", "444");
            WordService.getWord(mapaSubstituicao, "CONTROLE_DCDF_RDMs.docx", file);

        });
        measureTime("JapanRefactoring.refactorJapaneseFile",
            () -> refactorJapaneseFile(TXT_FILE, renameFile(TXT_FILE)));
    }

    @Test
    public void testExcelService() {
        ObservableList<Medicamento> medicamentosSNGPCPDF = LeitorArquivos
            .getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> campos = new LinkedHashMap<>();
        campos.put("Registro", Medicamento::getRegistro);
        campos.put("Nome", Medicamento::getNome);
        campos.put("Lote", Medicamento::getLote);
        campos.put("Quantidade", Medicamento::getQuantidade);
        campos.put("Codigo", Medicamento::getCodigo);
        campos.put("Valido", Medicamento::isRegistroValido);
        measureTime("ExcelService.getExcel",
            () -> ExcelService.getExcel(medicamentosSNGPCPDF, campos, ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));

    }

    @Test
    public void testExcelService2() {
        ObservableList<Medicamento> medicamentos = LeitorArquivos
            .getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> campos = new LinkedHashMap<>();
        campos.put("Registro", Medicamento::getRegistro);
        campos.put("Nome", Medicamento::getNome);
        campos.put("Lote", Medicamento::getLote);
        campos.put("Quantidade", Medicamento::getQuantidade);
        campos.put("Codigo", Medicamento::getCodigo);
        int maxI = medicamentos.size() - 1;
        measureTime("ExcelService.getExcel",
            () -> ExcelService.getExcel((i, s) -> medicamentos.subList(Integer.min(i, maxI), Integer.min(i + s, maxI)),
                campos, ResourceFXUtils.getOutFile("sngpcMeds.xlsx")));

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
        measureTime("ExcelService.getExcel", () -> {
            OutputStream outStream = new FileOutputStream(getOutFile("result.xlsx"));
            ExcelService.getExcel(arquivo, map, outStream);
        });

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
        measureTime("ExcelService.getExcel", () -> {
            OutputStream outStream = new FileOutputStream(getOutFile("result.xlsx"));
            ExcelService.getExcel(arquivo, map, abas, 0, outStream);
        });

    }

    @Test
    public void testImageCracker() {
        measureTime("ImageCracker.crackImage", () -> crackImage(toFile("CAPTCHA.jpg")));
        measureTime("ImageCracker.crackImage", () -> crackImage(toFile("CAPTCHA2.jpg")));
        measureTime("ImageCracker.createSelectedImage",
            () -> crackImage(createSelectedImage(new Image(toExternalForm("CAPTCHA.jpg")))));
    }

    @Test
    public void testLeitorArquivos() {

        File file = ResourceFXUtils.toFile("anvisa2208.xlsx");
        ObservableList<String> sheetsExcel = measureTime("LeitorArquivos.getSheetsExcel",
            () -> LeitorArquivos.getSheetsExcel(file));

        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<List<String>> listExcel = measureTime("LeitorArquivos.getListExcel",
            () -> LeitorArquivos.getListExcel(file, sheetsExcel.get(0)));
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
    public void testPoints() {
        measureTime("Test.equals", () -> {
            Set<Object> equalsTest = new LinkedHashSet<>(getList());
            Set<Object> equalsTest2 = new LinkedHashSet<>(getList());
            equalsTest.forEach(e -> equalsTest2.stream().anyMatch(a -> Objects.equals(a, e)));
        });
    }

    @Test
    public void testPortServices() {
        measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
        measureTime("PortServices.getServiceByPort", () -> PortServices.getServiceByPort(80));
    }

    @Test
    public void testWord2Vec() throws IOException {
        File file = Word2VecExample.getPathToSave();
        deleteIfExists(file.toPath());
        measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
    }

    private List<Object> getList() {
        Vertex v = new Vertex(5);
        return Arrays.asList(new PointCh4(2, 4), new LabeledPoint("Oi", 3, 5),
            PrimaryColor.RED, new EdgeDistancePack(new Linha(new Ponto(2, 4, null), new Ponto(2, 4, null)), 5),
            new RectangleCh4(new PointCh4(2, 4), 3, 5), new EdgeElement(v, null, 2), new EdgeElement(v, v, 5),
            new Contest(), new LessonPK(),
            new MadEdge(null, null), new MadEdgeDistance(null, 2F), new SnakeSquare(), new JapaneseLesson());
    }

}
