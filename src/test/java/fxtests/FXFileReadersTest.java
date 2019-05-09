package fxtests;

import static fxtests.FXTesting.measureTime;

import contest.db.Contest;
import ethical.hacker.PortServices;
import ex.j9.ch4.LabeledPoint;
import ex.j9.ch4.Point;
import ex.j9.ch4.PrimaryColor;
import ex.j9.ch4.Rectangle;
import extract.ExcelService;
import graphs.Edge;
import graphs.app.JavaFileDependecy;
import graphs.app.PackageTopology;
import graphs.entities.EdgeDistancePack;
import graphs.entities.Linha;
import graphs.entities.Ponto;
import japstudy.LessonPK;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import ml.Word2VecExample;
import ml.data.CSVUtils;
import org.assertj.core.util.Files;
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

public class FXFileReadersTest extends ApplicationTest {
    private static final Logger LOG = HasLogging.log();

    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        stage.show();
    }

    @Test
    public void testCSVUtils() {
        measureTime("CSVUtils.splitFile",
            () -> CSVUtils.splitFile(ResourceFXUtils.getOutFile("WDIData.csv").getAbsolutePath(), 3));
    }

    @Test
    public void testExcelService() throws IOException {
        ObservableList<Medicamento> medicamentosSNGPCPDF = LeitorArquivos
            .getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> campos = new LinkedHashMap<>();
        campos.put("Registro", Medicamento::getRegistro);
        campos.put("Nome", Medicamento::getNome);
        campos.put("Lote", Medicamento::getLote);
        campos.put("Quantidade", Medicamento::getQuantidade);
        campos.put("Codigo", Medicamento::getCodigo);
        ExcelService.getExcel(medicamentosSNGPCPDF, campos, new File(new File("out"), "sngpcMeds.xlsx"));

    }

    @Test
    public void testExcelService2() throws IOException {
        ObservableList<Medicamento> medicamentos = LeitorArquivos
            .getMedicamentosSNGPCPDF(ResourceFXUtils.toFile("sngpc2808.pdf"));
        Map<String, FunctionEx<Medicamento, Object>> campos = new LinkedHashMap<>();
        campos.put("Registro", Medicamento::getRegistro);
        campos.put("Nome", Medicamento::getNome);
        campos.put("Lote", Medicamento::getLote);
        campos.put("Quantidade", Medicamento::getQuantidade);
        campos.put("Codigo", Medicamento::getCodigo);
        int maxI = medicamentos.size() - 1;
        ExcelService.getExcel((i, s) -> medicamentos.subList(Integer.min(i, maxI), Integer.min(i + s, maxI)), campos,
            new File(new File("out"), "sngpcMeds.xlsx"));

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
    public void testLeitorArquivosPDF() throws IOException {
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
                new Rectangle(new Point(2, 4), 3, 5), new Edge(), new Contest(), new LessonPK());
            equalsTest.forEach(e -> equalsTest.contains(e));
        });
    }

    @Test
    public void testPortServices() {
        measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
    }

    @Test
    public void testWord2Vec() {
        File file = Word2VecExample.getPathToSave();
        if (file.exists()) {
            Files.delete(file);
        }
        measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
    }

}
