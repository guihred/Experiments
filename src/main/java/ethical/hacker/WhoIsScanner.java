package ethical.hacker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.DataframeUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import utils.*;

public class WhoIsScanner {
    public static final String IP_REGEX = "^\\d+\\.\\d+\\.\\d+\\.\\d+$";

    private static final Logger LOG = HasLogging.log();

    private final Map<String, String> cookies;

    public WhoIsScanner() {
        cookies = new HashMap<>();
    }

    public ObservableList<Map<String, String>> scanIps(String ip) {
        ObservableList<Map<String, String>> observableArrayList = FXCollections.observableArrayList();
        String[] split = ip.split("[\\s,;]+");
        RunnableEx.runNewThread(() -> {
            for (String string : split) {
                RunnableEx.run(() -> observableArrayList.add(whoIsScan(string)));
            }
        });
        return observableArrayList;
    }

    public Map<String, String> whoIsScan(String ip) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Document document = reloadIfExists(ip);
        document.getElementsByTag("ip").forEach(
                e -> e.children().forEach(m -> map.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        LOG.info("{}", map);
        return map;
    }

    private Document reloadIfExists(String ip) throws IOException {
        String scanIP = "http://isc.sans.edu/api/ip/" + ip;
        File outFile = ResourceFXUtils.getOutFile("xml/" + ip + ".xml");
        if (outFile.exists()) {
            return Jsoup.parse(outFile, StandardCharsets.UTF_8.name());
        }
        Document document = ExtractUtils.getDocument(scanIP, cookies);
        Files.write(outFile.toPath(), Arrays.asList(document.outerHtml()), StandardCharsets.UTF_8);
        return document;
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        builder.filter(ipColumn, s -> !s.toString().matches("^10\\..+") && s.toString().matches(IP_REGEX));
        DataframeML dataframe = builder.build();
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        DataframeUtils.crossFeatureObject(dataframe, "Rede",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("network"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("network")),
                ipColumn);
        DataframeUtils.crossFeatureObject(dataframe, "Owner",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("as_owner"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("asname")),
                ipColumn);
        DataframeUtils.crossFeatureObject(dataframe, "Country",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("country"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("ascountry")),
                ipColumn);
        return dataframe;
    }

    public static void main(String[] args) {
        File csvFile = new File(
                "C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Downs\\[Acesso Web] Top Origens x URL Únicas acessadas.csv");
        DataframeML dataframe = fillIPInformation(csvFile);
        reorderAndLog(dataframe, getLastNumberField(dataframe));
        DataframeUtils.save(dataframe, ResourceFXUtils.getOutFile("csv/" + csvFile.getName()));

    }

    private static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = getIPColumn(builder);
        return fillIPInformation(builder, ipColumn);
    }

    private static String getIPColumn(DataframeBuilder builder) {
        return builder.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst().orElse(null);
    }

    private static String getLastNumberField(DataframeML dataframe) {
        List<String> numberCols =
                dataframe.getFormatMap().entrySet().stream().filter(e -> Number.class.isAssignableFrom(e.getValue()))
                        .map(Entry<String, Class<? extends Comparable<?>>>::getKey).collect(Collectors.toList());
        return numberCols.get(numberCols.size() - 1);
    }

    private static void reorderAndLog(DataframeML dataframe, String numberField) {
        DataframeUtils.sort(dataframe, numberField);
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, "Rede", numberField);
        createSeries.forEach(s2 -> LOG.info("{}", s2));
        List<Entry<Object, Double>> series = DataframeUtils.createSeries(dataframe, "Owner", numberField);
        series.forEach(s1 -> LOG.info("{}", s1));
        dataframe.removeCol("filters");
        String string2 = DataframeUtils.toString(dataframe, 30);
        LOG.info(string2);
    }
}