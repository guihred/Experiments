package kibana;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import org.apache.commons.lang3.StringUtils;
import schema.sngpc.JsonExtractor;
import utils.ResourceFXUtils;
import utils.SupplierEx;

public final class TimelionApi extends KibanaApi {

    public static final String TIMELINE_USERS =
            ".es(index=inss-*-prod*,q=\"dtpsistema:portalatendimento\",split=mdc.uid.keyword:12).label('$1','.*>.*:(.*)>.*')";

    private TimelionApi() {
    }

    public static void main(String[] args) {
        TimelionApi.timelionFullScan("", "now-1d");
    }

    public static Object maketimelionSearch(File file, String timelionQuery, Map<String, String> search, String time) {
        return SupplierEx.get(() -> {
            String values = search.values().stream().collect(Collectors.joining());
            File outFile = newJsonFile(
                    Stream.of(values, time).collect(Collectors.joining()) + file.getName().replaceAll("\\.json", ""));
            if (!outFile.exists() || oneDayModified(outFile)) {
                String keywords = search
                        .entrySet().stream().map(e -> String
                                .format("{\"match_phrase\": {\"%s\": {\"query\": \"%s\"}}},", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n"));
                String content = getContent(file, timelionQuery, keywords, time);
                getFromURL("https://n321p000124.fast.prevnet/api/timelion/run", content, outFile);
            }
            return JsonExtractor.toObject(outFile);
        });
    }

    public static ObservableList<XYChart.Series<Number, Number>> timelionFullScan(String user, String time) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(user)) {
            map.put("mdc.uid.keyword", user);
        }
        return timelionScan(TIMELINE_USERS, map, time);
    }

    @SuppressWarnings("rawtypes")
    private static Object access(Object root, Object... param) {
        Object o = root;
        for (Object object : param) {
            if (object instanceof String) {
                o = ((Map) o).get(object);
            }
            if (object instanceof Integer) {
                o = ((List) o).get(((Integer) object).intValue());
            }
        }
        return o;

    }

    @SuppressWarnings({ "unchecked" })
    private static ObservableList<XYChart.Series<Number, Number>> convertToSeries(Object access) {
        return ((List<Object>) access).stream().flatMap(e -> ((List<Object>) access(e, "list")).stream())
                .map((Object o) -> {
                    XYChart.Series<Number, Number> java = new XYChart.Series<>();
                    java.setName(Objects.toString(access(o, "label")));
                    ((List<Object>) access(o, "data")).stream().forEach(f -> {
                        Long access2 = Long.valueOf(Objects.toString(access(f, 0)));
                        Long access3 = Long.valueOf(Objects.toString(access(f, 1)));
                        java.getData().add(new XYChart.Data<>(access2, access3));
                    });
                    return java;
                }).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private static ObservableList<Series<Number, Number>> timelionScan(String timelineUsers, Map<String, String> map,
            String time) {
        return SupplierEx.get(() -> {
            Object policiesSearch = maketimelionSearch(ResourceFXUtils.toFile("kibana/acessosTarefasQuery.json"),
                    timelineUsers, map, time);
            return convertToSeries(access(policiesSearch, "sheet"));
        }, FXCollections.emptyObservableList());
    }

}
