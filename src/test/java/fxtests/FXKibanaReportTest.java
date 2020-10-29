package fxtests;

import static utils.StringSigaUtils.toDouble;
import static utils.StringSigaUtils.toLong;

import ethical.hacker.WebBrowserApplication;
import ethical.hacker.WhoIsScanner;
import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import kibana.ConsultasInvestigator;
import kibana.KibanaApi;
import kibana.QueryObjects;
import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import paintexp.tool.RectBuilder;
import utils.DateFormatUtils;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {
    private static final int WAIT_LOAD_TIME = 6000;
    private WebBrowserApplication show;

    @Test
    public void testAutomatedSearch() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br");
        for (QueryObjects queryObjects : queryList) {
            for (String string : asList) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
                if (queryObjects.getLineChart() == null) {
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    List<Long> collect =
                            makeKibanaQuery.stream().filter(m -> !m.getOrDefault(params[0], "").matches("10\\..+|::1"))
                                    .mapToLong(m -> toLong(m.get(numberCol))).boxed().collect(Collectors.toList());
                    LongSummaryStatistics summaryStatistics = collect.stream().mapToLong(e -> e).summaryStatistics();
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .40;
                    WhoIsScanner whoIsScanner = new WhoIsScanner();
                    String collect2 =
                            makeKibanaQuery.stream().filter(m -> !m.getOrDefault(params[0], "").matches("10\\..+|::1"))
                                    .filter(m -> toLong(m.get(numberCol)) > avg + range).map(e -> {
                                        if (e.get(params[0]).matches(WhoIsScanner.IP_REGEX)) {
                                            Map<String, String> ipInformation =
                                                    whoIsScanner.getIpInformation(e.get(params[0]));
                                            ipInformation.remove("last_analysis_stats");
                                            ipInformation.remove("malicious");
                                            e.putAll(ipInformation);
                                        }
                                        return e;
                                    }).map(e -> "\t" + e.values().stream().collect(Collectors.joining("\t")))
                                    .collect(Collectors.joining("\n"));
                    if (StringUtils.isNotBlank(collect2)) {
                        getLogger().info("\n\t{}\n\t{}\n{}", string, query, collect2);
                    }
                }
            }
        }
    }

    @Test
    public void testWordReport() throws IOException {
        String finalIP = "185.185.147.138";
        Map<String, Object> mapaSubstituicao =
                JsonExtractor.accessMap(JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/modeloRelatorio.json")));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("\\$ip", finalIP);
        params.put("\\$date", DateFormatUtils.currentDate());
        addParameters(finalIP, mapaSubstituicao, params);

        File file = ResourceFXUtils.getOutFile("docx/Reporte_Eventos_MeuINSS_" + finalIP + ".docx");
        getLogger().info("APPLYING MAP{}", mapaSubstituicao);
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
    }

    @Test
    public void testWordReportConsultas() throws IOException {
        String finalIP = "191.17.40.15";
        Map<String, Object> mapaSubstituicao = JsonExtractor
                .accessMap(JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/modeloRelatorioConsultas.json")));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("\\$ip", finalIP);
        params.put("\\$date", DateFormatUtils.currentDate());
        Map<String, String> makeKibanaSearch = KibanaApi.getGeridCredencial(finalIP);
        params.put("\\$creds", makeKibanaSearch.keySet().stream().collect(Collectors.joining("\n")));

        params.put("\\$gerid", makeKibanaSearch.values().stream().collect(Collectors.joining("\n")));
        addParameters(finalIP, mapaSubstituicao, params);

        File file = ResourceFXUtils.getOutFile("docx/Reporte_Eventos_consultas_" + finalIP + ".docx");
        getLogger().info("APPLYING MAP {}", mapaSubstituicao);
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporteConsultas.docx", file);
    }

    @Test
    public void testWordReportGeridCredenciais() {
        String finalIP = "187.46.91.147";
        measureTime("KibanaApi.getGeridCredencial", () -> KibanaApi.getGeridCredencial(finalIP));
    }

    private void addParameters(String finalIP, Map<String, Object> mapaSubstituicao, Map<String, String> params) {
        for (String key : mapaSubstituicao.keySet().stream().collect(Collectors.toList())) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    return ((List<?>) v).stream().map(e -> remap(finalIP, params, e)).collect(Collectors.toList());
                }
                return replaceString(params, v);
            });
        }
    }

    private Image getImage(String finalIP, Map<String, Object> imageObj) {
        File outFile = ResourceFXUtils.getOutFile("print/" + imageObj.getOrDefault("name", "erro") + finalIP + ".png");
        if (outFile.exists()) {
            return new Image(ResourceFXUtils.convertToURL(outFile).toExternalForm());
        }
        show = SupplierEx.orElse(show, () -> show(WebBrowserApplication.class));
        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");
        String replaceAll = kibanaURL.replaceAll("191.96.73.211", finalIP);
        interactNoWait(() -> show.loadSite(replaceAll));
        measureTime("Load Site " + imageObj.get("name"), () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                sleep(WAIT_LOAD_TIME);
                interactNoWait(() -> {
                    boolean loading = show.isLoading();
                    atomicBoolean.set(loading);
                    return loading;
                });
                sleep(WAIT_LOAD_TIME);
            }
            interactNoWait(RunnableEx.make(() -> image.setValue(saveImage(imageObj, outFile))));
        });
        return image.getValue();
    }

    @SuppressWarnings("unchecked")
    private Object remap(String finalIP, Map<String, String> params, Object e) {
        if (e instanceof Map) {
            return getImage(finalIP, (Map<String, Object>) e);
        }
        return replaceString(params, e);
    }

    private WritableImage saveImage(Map<String, Object> info, File outFile) throws IOException {
        File saveHtmlImage = show.saveHtmlImage();
        String externalForm = ResourceFXUtils.convertToURL(saveHtmlImage).toExternalForm();
        Image value = new Image(externalForm);
        double width = value.getWidth();
        double height = value.getHeight();
        double width2 = width * toDouble(info.get("width"));
        double height2 = height * toDouble(info.get("height"));
        Rectangle a = new Rectangle(width2, height2);
        a.setLayoutX(width * toDouble(info.get("x")));
        a.setLayoutY(height * toDouble(info.get("y")));
        WritableImage destImage = new WritableImage((int) a.getWidth(), (int) a.getHeight());
        RectBuilder.copyImagePart(value, destImage, a);
        ImageFXUtils.saveImage(destImage, outFile);
        return destImage;
    }

    private static Object replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

}