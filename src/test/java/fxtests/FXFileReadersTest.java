package fxtests;

import com.google.common.collect.ImmutableMap;
import extract.PPTService;
import extract.WordService;
import extract.web.DocumentHelper;
import fxml.utils.XmlToXlsx;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import javafx.scene.image.Image;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;
import pdfreader.BalabolkaApi;
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
    public void testPPTService() throws MalformedURLException {
        measureTime("PPTService.getPowerPointImages", () -> PPTService
                .getPowerPointImages(ResourceFXUtils.toFile("models/Radar_Eventos_v01122020REL1.pptx")));
        Map<String, Object> replacementMap = new HashMap<>();
        replacementMap.put("Dia: 01/12/2020", "DIA: 01/02/2021");

        Path image = FileTreeWalker.getFirstPathByExtension(ResourceFXUtils.getOutFile("print"), ".png");
        Path csv = FileTreeWalker.getFirstPathByExtension(ResourceFXUtils.getOutFile("csv"), "destination01022021.csv");

        replacementMap.put("Acessos volumétricos por destino - Firewall", new ArrayList<>(
                Arrays.asList(new Image(image.toFile().toURI().toURL().toExternalForm()), csv.toFile())));
        measureTime("PPTService.getPowerPoint", () -> PPTService.getPowerPoint(replacementMap,
                "Radar_Eventos_v01122020REL1.pptx", ResourceFXUtils.getOutFile("pptx/Teste.pptx")));
    }

    @Test
    public void testRar() {
        measureTime("UnRar.extractRarFiles", () -> {
            File userFolder = ResourceFXUtils.getOutFile().getParentFile();
            FileTreeWalker.getPathByExtension(userFolder, "rar").stream().map(FunctionEx.makeFunction(e -> {
                Path name = e.getName(e.getNameCount() - 1);
                File outFile = ResourceFXUtils.getOutFile(name.toString());
                ExtractUtils.copy(e, outFile);
                return outFile.toPath();
            })).forEach(ConsumerEx.makeConsumer(p -> UnRar.extractRarFiles(p.toFile())));
        });

    }


    @Test
    public void testWordFile() {
        measureTime("WordService.getWord", () -> {
            Map<String, Object> mapaSubstituicao = new HashMap<>();
            File file = ResourceFXUtils.getOutFile("resultado.docx");
            WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
        });
    }

    @Test
    public void testXmlToXlsx() {
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Referencias\\Aplicativos.xls");
        measureTime("XmlToXlsx.convertXML2XLS", () -> XmlToXlsx.convertXML2XLS(file));
    }

    @Test
    public void testZIP() {
        measureTime("UnZip.extractZippedFiles", () -> UnZip.extractZippedFiles(UnZip.ZIPPED_FILE_FOLDER));
    }

    @Test
    public void textBalabolkaApi() {
        measureTime("BalabolkaApi.speak", () -> BalabolkaApi.speak("It Worked"));
    }


}
