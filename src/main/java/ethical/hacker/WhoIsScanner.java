package ethical.hacker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.RunnableEx;
import utils.SupplierEx;

public class WhoIsScanner {
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
        String scanIP = "http://isc.sans.edu/api/ip/" + ip;
        Map<String, String> map = new LinkedHashMap<>();
        ExtractUtils.getDocument(scanIP, cookies).getElementsByTag("ip").forEach(
                e -> e.children().forEach(m -> map.put(m.tagName(), StringEscapeUtils.unescapeHtml4(m.text()))));
        LOG.info("{}", map);

        return map;
    }

    public static void main(String[] args) {
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        File csvFile = new File(
                "C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Downs\\[Acesso Web] Top Origens x URL acessadas.csv");
        DataframeML dataframe = DataframeBuilder.builder(csvFile)
                .build();
        String ipColumn = dataframe.cols().stream().filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst()
                .orElse("IP de Origem");
        dataframe.filter(ipColumn, s -> !s.toString().matches("^10\\..+"));
        DataframeUtils.crossFeatureObject(dataframe, "Rede",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("network"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("network")),
                ipColumn);
        String target = "Count";
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, "Rede", target);
        createSeries.forEach(s -> LOG.info("{}", s));
        DataframeUtils.crossFeatureObject(dataframe, "Owner",
                e -> SupplierEx.getFirst(() -> VirusTotalApi.getIpTotalInfo(e[0].toString()).get("as_owner"),
                        () -> whoIsScanner.whoIsScan(e[0].toString()).get("asname")),
                ipColumn);
        List<Entry<Object, Double>> series = DataframeUtils.createSeries(dataframe, "Owner", target);
        series.forEach(s -> LOG.info("{}", s));
        DataframeUtils.sort(dataframe, target);
        dataframe.removeCol("filters");
        String string2 = DataframeUtils.toString(dataframe, 30);
        LOG.info(string2);

    }
}