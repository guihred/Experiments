package fxtests;

import static language.FXTesting.measureTime;

import ex.j8.Chapter4;
import extract.ExcelService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import language.FXTesting;
import ml.Word2VecExample;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class LeitorArquivosTest {
    private static final Logger LOG = HasLogging.log(LeitorArquivosTest.class);

    @BeforeClass
    public static void init() {
        new JFXPanel().toString();
        Platform.setImplicitExit(false);
    }

    @Test
    public void testLeitorArquivos() {
        File file = ResourceFXUtils.toFile("anvisa2208.xlsx");
        ObservableList<String> sheetsExcel = LeitorArquivos.getSheetsExcel(file);
        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<List<String>> listExcel = LeitorArquivos.getListExcel(file,
                sheetsExcel.get(0));
        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<Medicamento> converterMedicamentos = LeitorArquivos.converterMedicamentos(listExcel,
                Arrays.asList(LeitorArquivos.REGISTRO, LeitorArquivos.NOME,
                LeitorArquivos.LOTE, LeitorArquivos.QUANTIDADE));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Size must be equal", 679, converterMedicamentos.size());
    }

    @Test
    public void testLeitorArquivosPDF() throws IOException {
        File file = ResourceFXUtils.toFile("sngpc2808.pdf");
        ObservableList<Medicamento> medicamentos = LeitorArquivos.getMedicamentosSNGPCPDF(file);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Size must be equal", 656, medicamentos.size());
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
    public void testWord2Vec() {
        File file = new File(Word2VecExample.PATH_TO_SAVE_MODEL_TXT);
        if (file.exists()) {
            boolean delete = file.delete();
            LOG.info("File deleted {}", delete);
        }
        measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
    }

    @Test
    public void testChapter4() {
        measureTime("Chapter4.testApps", () -> FXTesting.testApps(Chapter4.Ex1.class, Chapter4.Ex4.class,
                Chapter4.Ex5.class, Chapter4.Ex6.class, Chapter4.Ex7.class, Chapter4.Ex9.class, Chapter4.Ex10.class));
    }
}
