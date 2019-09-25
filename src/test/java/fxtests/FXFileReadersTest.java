package fxtests;

import static ethical.hacker.ImageCracker.crackImage;
import static ethical.hacker.ImageCracker.createSelectedImage;
import static fxtests.FXTesting.measureTime;
import static java.nio.file.Files.deleteIfExists;
import static utils.ResourceFXUtils.getOutFile;
import static utils.ResourceFXUtils.toExternalForm;
import static utils.ResourceFXUtils.toFile;

import com.google.common.collect.ImmutableMap;
import contest.db.Contest;
import ethical.hacker.PortServices;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.Point;
import ex.j9.ch4.PrimaryColor;
import ex.j9.ch4.Rectangle;
import extract.ExcelService;
import gaming.ex16.MadEdge;
import graphs.EdgeElement;
import graphs.app.JavaFileDependecy;
import graphs.app.PackageTopology;
import graphs.entities.EdgeDistancePack;
import graphs.entities.Linha;
import graphs.entities.Ponto;
import japstudy.LessonPK;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ml.Word2VecExample;
import ml.data.CSVUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXFileReadersTest extends ApplicationTest {
    private static final Logger LOG = HasLogging.log();

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
    }

    @Test
    public void testPackageTopology() {
        measureTime("PackageTopology.main", () -> {
            List<JavaFileDependecy> javaFiles = PackageTopology.getJavaFileDependencies(null);
            Map<String, List<JavaFileDependecy>> filesByPackage = javaFiles.stream()
                .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));
            filesByPackage.forEach((pack, files) -> {
                LOG.trace(pack);
                Map<String, Map<String, Long>> packageDependencyMap = PackageTopology.createFileDependencyMap(files);
                PackageTopology.printDependencyMap(packageDependencyMap);
            });
        });
    }

    @Test
    public void testPoints() {
        measureTime("Test.equals", () -> {
            List<Object> equalsTest = Arrays.asList(new Point(2, 4), new LabeledPoint("Oi", 3, 5), PrimaryColor.RED,
                new EdgeDistancePack(new Linha(new Ponto(2, 4, null), new Ponto(2, 4, null)), 5),
                new Rectangle(new Point(2, 4), 3, 5), new EdgeElement(), new Contest(), new LessonPK(),
                new MadEdge(null, null));
            equalsTest.forEach(e -> equalsTest.contains(e));
        });
    }

    @Test
    public void testPortServices() {
        measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
    }

    @Test
    public void testWord2Vec() throws IOException {
        File file = Word2VecExample.getPathToSave();
        deleteIfExists(file.toPath());
        measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
    }

}
