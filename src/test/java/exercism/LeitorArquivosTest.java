package exercism;

import extract.ExcelService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import log.analyze.FunctionEx;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import rosario.LeitorArquivos;
import rosario.Medicamento;
import simplebuilder.ResourceFXUtils;

public class LeitorArquivosTest {
    public LeitorArquivosTest() {
        ResourceFXUtils.initializeFX();
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
        Assert.assertEquals("Size must be equal", converterMedicamentos.size(), 679);
    }

    @Test
    public void testLeitorArquivosPDF() throws IOException {
        File file = ResourceFXUtils.toFile("sngpc2808.pdf");
        ObservableList<Medicamento> medicamentos = LeitorArquivos.getMedicamentosSNGPCPDF(file);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Size must be equal", medicamentos.size(), 656);
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
}
