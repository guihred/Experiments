package fxtests;

import static utils.StringSigaUtils.toDouble;

import ethical.hacker.WebBrowserApplication;
import ethical.hacker.WhoIsScanner;
import extract.PhantomJSUtils;
import extract.WordService;
import fxml.utils.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private List<String> excludeOwners = Arrays.asList("CAIXA ECONOMICA FEDERAL",
            "SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO", "BANCO DO BRASIL S.A.", "Itau Unibanco S.A.");

    @Test
    public void testAutomatedSearch() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br");
        for (QueryObjects queryObjects : queryList) {
            if (queryObjects.getLineChart() == null) {
                for (String string : asList) {
                    filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    List<Double> collect =
                            makeKibanaQuery.stream().filter(m -> !getFirst(params, m).matches("10\\..+|::1"))
                                    .map(m -> getNumber(numberCol, m)).collect(Collectors.toList());
                    DoubleSummaryStatistics summaryStatistics =
                            collect.stream().mapToDouble(e -> e).summaryStatistics();
                    if (summaryStatistics.getCount() <= 1 || summaryStatistics.getSum() == 0) {
                        continue;
                    }
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .45;
                    WhoIsScanner whoIsScanner = new WhoIsScanner();
                    String collect2 =
                            makeKibanaQuery.parallelStream().filter(m -> !getFirst(params, m).matches("10\\..+|::1"))
                                    .filter(m -> getNumber(numberCol, m) > avg + range).map(e -> {
                                        String field = getFirst(params, e);
                                        if (field.matches(WhoIsScanner.IP_REGEX)) {

                                            Map<String, String> ipInformation = whoIsScanner.getIpInformation(field);
                                            ipInformation.remove("last_analysis_stats");
                                            ipInformation.remove("malicious");
                                            e.putAll(ipInformation);
                                        }
                                        return e;
                                    }).filter(m -> !excludeOwners.contains(m.getOrDefault("as_owner", "")))
                                    .filter(m -> {
                                        String field = getFirst(params, m);
                                        return isNotBlocked(days, field);
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
    public void testAutomatedSearchNetwork() {
        ConsultasInvestigator show2 = show(ConsultasInvestigator.class);
        List<QueryObjects> queryList = show2.getQueryList();
        Map<String, String> filter1 = new HashMap<>();
        Integer days = 1;
        List<String> asList = Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br",
                "tarefas.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br");
        for (QueryObjects queryObjects : queryList) {
            for (String string : asList) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, string);
                if (queryObjects.getLineChart() == null
                        && QueryObjects.CLIENT_IP_QUERY.equals(queryObjects.getQuery())) {
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> makeKibanaQuery = queryObjects.makeKibanaQuery(filter1, days);
                    String query = queryObjects.getQuery();
                    WhoIsScanner whoIsScanner = new WhoIsScanner();
                    List<Map<String, String>> whoIsInfo = makeKibanaQuery.parallelStream()
                            .filter(m -> !getFirst(params, m).matches("10\\..+|::1")).map(e -> {
                                Map<String, String> ipInformation = whoIsScanner.getIpInformation(getFirst(params, e));
                                ipInformation.remove("last_analysis_stats");
                                ipInformation.remove("malicious");
                                e.putAll(ipInformation);
                                return e;
                            }).collect(Collectors.toList());
                    Map<String,
                            Double> collect = whoIsInfo.stream()
                                    .collect(Collectors.groupingBy(
                                            m -> m.getOrDefault("as_owner", "") + "\t" + m.getOrDefault("network", ""),
                                            Collectors.summingDouble(m -> getNumber(numberCol, m))));
                    DoubleSummaryStatistics summaryStatistics =
                            collect.values().stream().mapToDouble(e -> e).summaryStatistics();
                    double avg = summaryStatistics.getAverage();
                    double max = summaryStatistics.getMax();
                    double min = summaryStatistics.getMin();
                    double range = (max - min) * .45;
                    String collect3 = collect.entrySet().stream().filter(m -> m.getValue() > avg + range)
                            .filter(m -> excludeOwners.stream().noneMatch(ow -> m.getKey().startsWith(ow)))
                            .map(s -> "\t" + s).collect(Collectors.joining("\n"));
                    if (StringUtils.isNotBlank(collect3)) {
                        getLogger().info("\n\tTOP NETWORKS\n\t{}\n\t{}\n{}", string, query, collect3);
                    }
                }
            }
        }
    }

    @Test
    public void testWordReport() throws IOException {
        String finalIP = "189.68.23.187";
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
    public void testWordReportAuxilio() throws IOException {
        String finalIP = "177.58.255.90";
        Map<String, Object> mapaSubstituicao = JsonExtractor
                .accessMap(JsonExtractor.toObject(ResourceFXUtils.toFile("kibana/modeloRelatorioAuxilio.json")));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("\\$ip", finalIP);
        params.put("\\$date", DateFormatUtils.currentDate());
        addParameters(finalIP, mapaSubstituicao, params);

        File file = ResourceFXUtils.getOutFile("docx/Reporte_Eventos_auxilioemergencial_" + finalIP + ".docx");
        getLogger().info("APPLYING MAP {}", mapaSubstituicao);
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

        List<Object> collect = makeKibanaSearch.values().stream().map(this::textToImage).collect(Collectors.toList());
        mergeImage(mapaSubstituicao, collect);
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
                    return ((List<?>) v).stream().map(e -> remap(finalIP, params, e)).filter(Objects::nonNull)
                            .collect(Collectors.toList());
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
            interactNoWait(RunnableEx.make(() -> image.setValue(saveImage(imageObj, outFile)),
                    e -> getLogger().error("ERROR LOADING {},{}", finalIP, imageObj)));
        });
        return image.getValue();
    }

    @SuppressWarnings({ "unchecked", "static-method" })
    private void mergeImage(Map<String, Object> mapaSubstituicao, List<Object> collect) {
        mapaSubstituicao.merge("gerid", collect, (o, n) -> {
            ((List<Object>) o).addAll((List<?>) n);
            return o;
        });
    }

    @SuppressWarnings("unchecked")
    private Object remap(String finalIP, Map<String, String> params, Object e) {
        if (e instanceof Map) {
            return getImage(finalIP, (Map<String, Object>) e);
        }
        if (e instanceof Image) {
            return e;
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

    private Image textToImage(String s) {
        String collect2 = Stream.of(s.split("\n")).filter(StringUtils::isNotBlank)
                .map(str -> "<p>" + str.replaceAll("(\\d+\\.\\d+\\.\\d+\\.\\d+)", "<font>$1</font>") + "</p>")
                .collect(Collectors.joining("\n"));
        String format =
                String.format("<!DOCTYPE html>\n<html>\n<head>\n<style>\nfont {background-color: yellow;}</style>\n"
                        + "</head><body>%s</body>\n</html>", collect2);
        return PhantomJSUtils.saveHtmlImage(format);
    }

    private static String getFirst(String[] params, Map<String, String> m) {
        String orDefault = m.getOrDefault(params[0], m.values().iterator().next());
        return orDefault;
    }

    private static Double getNumber(String numberCol, Map<String, String> m) {
        return toDouble(m.getOrDefault(numberCol, m.get(numberCol + 0)));
    }

    private static boolean isNotBlocked(Integer days, String ip) {
        if (ip.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> blocked = KibanaApi
                    .makeKibanaSearch("policiesQuery.json", ip, days, "key");
            if (blocked.values().stream().anyMatch(s -> s.contains("block"))) {
                return false;
            }
        }
        return true;
    }

    private static Object replaceString(Map<String, String> params, Object v) {
        String string = v.toString();
        for (Entry<String, String> entry : params.entrySet()) {
            string = string.replaceAll(entry.getKey(), entry.getValue());
        }
        return string;
    }

}