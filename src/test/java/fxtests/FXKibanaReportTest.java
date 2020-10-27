package fxtests;

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
import kibana.QueryObjects;
import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import paintexp.tool.RectBuilder;
import utils.DateFormatUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.RunnableEx;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXKibanaReportTest extends AbstractTestExecution {
    private WebBrowserApplication show;

    @Test
    public void testAutomatedSearch() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br");
        for (String string : asList) {
            filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
            for (QueryObjects queryObjects : queryList) {
                if (queryObjects.getLineChart() == null) {
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    List<Long> collect =
                            makeKibanaQuery.stream().mapToLong(m -> StringSigaUtils.toLong(m.get(numberCol))).boxed()
                                    .collect(Collectors.toList());
                    LongSummaryStatistics summaryStatistics = collect.stream().mapToLong(e -> e).summaryStatistics();
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .40;
                    WhoIsScanner whoIsScanner = new WhoIsScanner();
                    String collect2 =
                            makeKibanaQuery.stream().filter(m -> StringSigaUtils.toLong(m.get(numberCol)) > avg + range)
                                    .map(e -> {
                                        if (e.get(params[0]).matches(WhoIsScanner.IP_REGEX)) {
                                            Map<String, String> ipInformation = whoIsScanner.getIpInformation(e.get(params[0]));
                                            ipInformation.remove("last_analysis_stats");
                                            ipInformation.remove("malicious");
                                            e.putAll(ipInformation);
                                        }
                                        return e;
                                    })
                                    .map(e -> "\t" + e.values().stream().collect(Collectors.joining("\t")))
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

        String finalIP = "191.242.238.223";

        Map<String, Object> mapaSubstituicao =
                JsonExtractor.accessMap(JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/modeloRelatorio.json")));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("\\$ip", finalIP);
        params.put("\\$date", DateFormatUtils.currentDate());
        for (String key : mapaSubstituicao.keySet().stream().collect(Collectors.toList())) {
            mapaSubstituicao.compute(key, (k, v) -> {
                if (v instanceof List) {
                    return ((List<?>) v).stream().map(e -> remap(finalIP, params, e)).collect(Collectors.toList());
                }
                return replaceString(params, v);
            });
        }

        File file = ResourceFXUtils.getOutFile("docx/resultado" + finalIP + ".docx");
        WordService.getWord(mapaSubstituicao, "ModeloGeralReporte.docx", file);
    }

    private Image getImage(String finalIP, Map<String, Object> imageObj) {
        show = show == null ? show(WebBrowserApplication.class) : show;
        Property<Image> image = new SimpleObjectProperty<>();
        String kibanaURL = Objects.toString(imageObj.get("url"), "");
        String replaceAll = kibanaURL.replaceAll("191.96.73.211", finalIP);

        interactNoWait(() -> show.loadSite(replaceAll));

        measureTime("WordService.getWord", () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                interactNoWait(() -> {
                    boolean loading = show.isLoading();
                    atomicBoolean.set(loading);
                    return loading;
                });
                sleep(5000);
            }
            interactNoWait(RunnableEx.make(() -> {
                File saveHtmlImage = show.saveHtmlImage();
                String externalForm = ResourceFXUtils.convertToURL(saveHtmlImage).toExternalForm();
                Image value = new Image(externalForm);
                double width = value.getWidth() * 0.3;
                double height = value.getHeight() * 0.3;
                Rectangle a = new Rectangle(width * 2, height * 2);
                a.setLayoutX(width);
                a.setLayoutY(height);
                RectBuilder.copyImagePart(value, new WritableImage((int) a.getWidth(), (int) a.getHeight()), a);

                image.setValue(value);
            }));
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

    private static Object replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

}