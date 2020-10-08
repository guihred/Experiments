package kibana;

import fxml.utils.JsonExtractor;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class TimelionApi extends KibanaApi {
    private static final Logger LOG = HasLogging.log();
    public static final String TIMELINE_USERS = ".es(index=inss-*-prod*,q=\\\"dtpsistema:portalatendimento\\\","
            + "split=mdc.uid.keyword:12).label('$1','.*>.*:(.*)>.*')";
    public static final String TIMELINE_IPS =
            ".es(index=*apache-prod*,q=\\\"dtptype:nginx OR dtptype:apache OR dtptype:varnish\\\","
                    + "split=clientip.keyword:12).label('$1','.*>.*:(.*)>.*')";

    private TimelionApi() {
    }

    public static Object maketimelionSearch(File file, String timelionQuery, Map<String, String> search, String time) {
        return SupplierEx.get(() -> {
            String values = search.values().stream().collect(Collectors.joining());
            String replaceAll = timelionQuery.replaceAll(".+split=(.+?):.+", "$1");

            File outFile = newJsonFile(replaceAll + Stream.of(values, time).collect(Collectors.joining()));
            if (!outFile.exists() || oneDayModified(outFile)) {
                String keywords = convertSearchKeywords(search);
                String content = getContent(file, timelionQuery, keywords, time);
                getFromURLJson("https://n321p000124.fast.prevnet/api/timelion/run", content, outFile);
            }
            return JsonExtractor.toObject(outFile);
        });
    }

    public static ObservableList<XYChart.Series<Number, Number>> timelionFullScan(String user, String time) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(user)) {
            map.put("mdc.uid.keyword", user);
        }
        return timelionScan(FXCollections.observableArrayList(), TIMELINE_USERS, map, time);
    }

    public static ObservableList<Series<Number, Number>> timelionScan(ObservableList<Series<Number, Number>> series,
            String timelineUsers, Map<String, String> filterMap, String time) {
        return SupplierEx.getHandle(() -> {
            Object policiesSearch = maketimelionSearch(ResourceFXUtils.toFile("kibana/acessosTarefasQuery.json"),
                    timelineUsers, filterMap, time);
            CommonsFX.runInPlatform(
                    () -> convertToSeries(series, JsonExtractor.accessList(policiesSearch, "sheet")));
            return series;
        }, FXCollections.observableArrayList(), e -> LOG.error("ERROR RUNNING {} {}", timelineUsers, e.getMessage()));
    }

    private static void addToSeries(XYChart.Series<Number, Number> java, Object f) {
        Long access2 = Long.valueOf(JsonExtractor.access(f, String.class, 0));
        Long access3 = Long.valueOf(JsonExtractor.access(f, String.class, 1));
        java.getData().add(new XYChart.Data<>(access2, access3));
    }

    private static ObservableList<XYChart.Series<Number, Number>>
            convertToSeries(ObservableList<Series<Number, Number>> series, List<?> access) {
        return access.stream()
                .flatMap(e -> JsonExtractor.accessList(e, "list").stream())
                .map((Object o) -> {
                    XYChart.Series<Number, Number> serie = new XYChart.Series<>();
                    serie.setName(JsonExtractor.access(o, String.class, "label"));
                    JsonExtractor.accessList(o, "data").stream().forEach(f -> addToSeries(serie, f));
                    return serie;
                }).collect(Collectors.toCollection(() -> series));
    }

}
