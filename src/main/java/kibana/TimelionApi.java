package kibana;

import extract.web.JsonExtractor;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ProjectProperties;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class TimelionApi extends KibanaApi {
    private static final String TIMELION_URL = ProjectProperties.getField();
    private static final Logger LOG = HasLogging.log();
    public static final String TIMELINE_USERNAME =
            ".es(index=dtp-waf*,q=\\\"http.user-name.keyword:*\\\",split=http.user-name.keyword:12).label('$1','.*>.*:(.*)>.*')";

    public static final String TIMELINE_SISTEMAS =
            ".es(index=*apache-prod*,q=\\\"dtptype:nginx OR dtptype:apache OR dtptype:varnish\\\","
                    + "split=dtpsistema.keyword:12).label('$1','.*>.*:(.*)>.*')";
    public static final String TIMELINE_IPS =
            ".es(index=*apache-prod*,q=\\\"dtptype:nginx OR dtptype:apache OR dtptype:varnish\\\","
                    + "split=clientip.keyword:12).label('$1','.*>.*:(.*)>.*')";
    public static final String BYTE_BY_IP =
            ".es(index=dtp-pl*,split=SourceIP:12,metric='sum:Bytes').label('$1','.*>.*:(.*)>.*')";

    private TimelionApi() {
    }

    public static ObservableList<Series<Number, Number>> timelionFullScan(ObservableList<Series<Number, Number>> series,
            String timelionQuery, Map<String, String> filterMap, String time) {
        return runTimelionScan(series, timelionQuery, filterMap, time, "timelionQuery.json");
    }

    public static ObservableList<Series<Number, Number>> timelionScan(ObservableList<Series<Number, Number>> series,
            String timelineUsers, Map<String, String> filterMap, String time) {
        return runTimelionScan(series, timelineUsers, filterMap, time, "acessosTarefasQuery.json");
    }

    private static void addToSeries(XYChart.Series<Number, Number> java, Object f) {
        Object access = JsonExtractor.access(f, Object.class, 0);
        Long access2 = Long.class.cast(access);
        Integer access3 = JsonExtractor.access(f, Integer.class, 1);
        java.getData().add(new XYChart.Data<>(access2, access3));
    }

    private static ObservableList<XYChart.Series<Number, Number>>
            convertToSeries(ObservableList<Series<Number, Number>> series, List<?> access) {
        return access.stream().flatMap(e -> JsonExtractor.accessList(e, "list").stream()).map((Object o) -> {
            XYChart.Series<Number, Number> serie = new XYChart.Series<>();
            serie.setName(JsonExtractor.access(o, String.class, "label"));
            List<Object> accessList = JsonExtractor.accessList(o, "data");
            ConsumerEx.foreach(accessList, f -> addToSeries(serie, f));
            return serie;
        }).collect(Collectors.toCollection(() -> series));
    }

    private static Map<String, Object> maketimelionSearch(File file, String timelionQuery, Map<String, String> search,
            String time) {
        return SupplierEx.get(() -> {
            String values = search.values().stream().collect(Collectors.joining());
            String replaceAll = timelionQuery.replaceAll(".+split=(.+?):.+", "$1");

            File outFile = newJsonFile(replaceAll + Stream.of(values, time).collect(Collectors.joining()));
            if (JsonExtractor.isNotRecentFile(outFile)) {
                String keywords = convertSearchKeywords(search);
                String content = getContent(file, timelionQuery, keywords, time);
                getFromURLJson(TIMELION_URL, content, outFile);
            }
            return JsonExtractor.accessMap(JsonExtractor.toFullObject(outFile));
        });
    }

    private static ObservableList<Series<Number, Number>> runTimelionScan(ObservableList<Series<Number, Number>> series,
            String timelineUsers, Map<String, String> filterMap, String time, String file) {
        return SupplierEx.getHandle(() -> {
            Map<String, Object> policiesSearch = maketimelionSearch(kibanaFile(file), timelineUsers, filterMap, time);
            if (policiesSearch.containsKey("sheet")) {
                List<Object> accessList = JsonExtractor.accessList(policiesSearch, "sheet");
                CommonsFX.runInPlatform(() -> convertToSeries(series, accessList));
            }
            return series;
        }, FXCollections.observableArrayList(), e -> LOG.error("ERROR RUNNING {} {}", timelineUsers, e.getMessage()));
    }

}
