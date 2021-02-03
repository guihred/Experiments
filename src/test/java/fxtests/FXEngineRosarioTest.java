package fxtests;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import rosario.RosarioCommons;
import rosario.RosarioComparadorArquivos;
import utils.ExcelService;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;

public class FXEngineRosarioTest extends AbstractTestExecution {

    private File value = ResourceFXUtils.toFile("sngpc2808.pdf");

    @Test
    public void testExcelFile() {
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
    public void verify() {
        show(new RosarioComparadorArquivos());
        RosarioCommons.setOpenAtExport(false);
        RosarioCommons.choseFile("Carregar Arquivo SNGPC").setInitialDirectory(value.getParentFile());
        RosarioCommons.choseFile("Carregar Arquivo Anvisa").setInitialDirectory(value.getParentFile());
        getLogger().info("FXEngineRosarioTest STARTED");
        getLogger().info("VERIFYING FXEngineRosarioTest ");
        clickOn("#SNGPC");
        type(typeText(value.getName()));
        type(KeyCode.ENTER);
        clickOn("#anvisa");
        type(typeText("anvisa2208.xlsx"));
        type(KeyCode.ENTER);
        clickOn("Importar Arquivo");
        clickOn(".text-field");
        String text = "asdsd";
        type(typeText(text));
        eraseText(text.length());
        clickOn("Exportar Excel");
        getLogger().info("VERIFIED FXEngineRosarioTest ");
    }

}
