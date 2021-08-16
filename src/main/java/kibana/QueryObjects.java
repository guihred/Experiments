package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import extract.web.JsonExtractor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.ListHelper;
import simplebuilder.SimpleConverter;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class QueryObjects {
    public static final String URL_QUERY = "request.keyword";
    public static final String POLICY_NAME = "policy-name.keyword";
    public static final String USER_NAME = "http.user-name.keyword";

    public static final String SOURCE_IP_QUERY = "SourceIP";
    public static final String DESTINATION_PORT_QUERY = "DestinationPort";
    public static final String DESTINATION_IP_QUERY = "DestinationIP";
    public static final String CLIENT_IP_QUERY = "clientip";
    public static final String ACESSOS_SISTEMA_QUERY = "dtpsistema";
    private final String query;
    private Map<String, FunctionEx<String, String>> formatMap = new LinkedHashMap<>();
    private Map<String, FunctionEx<Map<String, String>, String>> mappedColumn = new LinkedHashMap<>();
    private boolean allowEmpty = true;

    private final String queryFile;

    private final String[] params;

    private String group = "";

    private final TableView<Map<String, String>> table;

    private final ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());
    private final ObservableList<Series<Number, Number>> series = observableArrayList();

    private final LineChart<Number, Number> lineChart;

    public QueryObjects(String query, String queryFile, LineChart<Number, Number> lineChart) {
        this.query = query;
        this.queryFile = queryFile;
        this.lineChart = lineChart;
        params = new String[] {};
        table = null;
    }

    public QueryObjects(String query, String queryFile, TableView<Map<String, String>> table, String[] params) {
        this.query = query;
        this.queryFile = queryFile;
        this.table = table;
        this.params = params;
        lineChart = null;
    }

    public QueryObjects configureTable(TextField resultsFilter, ConsumerEx<List<Map<String, String>>> onClick) {
        ObservableList<Map<String, String>> ipItems2 = getItems();
        table.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        String splitMergeCamelCase =
                StringSigaUtils
                        .changeCase(StringSigaUtils.splitMergeCamelCase(queryFile.replaceAll("Query|\\.json", "")));
        table.parentProperty().addListener((ob, old, val) -> RunnableEx.runIf(val,
                v -> ((Tab) ClassReflectionUtils.getFieldValue(v, "tab")).setText(splitMergeCamelCase)));
        SimpleTableViewBuilder.of(table).copiable().savable().onDoubleClickMany(onClick)
                .onSortClicked((col, ascending) -> QuickSortML.sortMapList(ipItems2, col, ascending));
        return this;
    }

    public QueryObjects configureTimeline(ComboBox<String> combo) {
        return configureTimeline(combo, StringSigaUtils::toStringSpecial);
    }

    public QueryObjects configureTimeline(ComboBox<String> combo, Function<Number, String> func) {
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        final String FORMAT = "H:m:s\nd/M/yy";
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                return DateFormatUtils.toNumber(FORMAT, string);
            }

            @Override
            public String toString(Number object) {
                return DateFormatUtils.format(FORMAT, object.longValue());
            }
        });
        yAxis.setTickLabelFormatter(new SimpleConverter<>(func));
        QueryObjects fieldObjects = new QueryObjects(query, queryFile, lineChart);
        ObservableList<Series<Number, Number>> timelionFullScan = fieldObjects.getSeries();

        ObservableList<String> mapping = ListHelper.mapping(timelionFullScan, Series<Number, Number>::getName);
        mapping.add(0, "");
        FilteredList<Series<Number, Number>> filtered = timelionFullScan.filtered(e -> true);
        lineChart.setData(filtered);
        combo.setItems(mapping);
        combo.getSelectionModel().selectedItemProperty().addListener((ob, old, val) -> {
            lineChart.getYAxis().setAutoRanging(false);
            filtered.setPredicate(e -> StringUtils.isBlank(val) || e != null && Objects.equals(e.getName(), val));
        });
        return fieldObjects;
    }

    public String getGroup() {
        return group;
    }

    public ObservableList<Map<String, String>> getItems() {
        return items;
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    public String[] getParams() {
        return params;
    }

    public String getQuery() {
        return query;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public ObservableList<Series<Number, Number>> getSeries() {
        return series;
    }

    public TableView<Map<String, String>> getTable() {
        return table;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public List<Map<String, String>> makeKibanaQuery(Map<String, String> filter1, Integer days) {
        if (filter1.isEmpty() && !isAllowEmpty()) {
            CommonsFX.runInPlatform(() -> getItems().clear());
            return getItems();
        }
        List<Map<String, String>> remap = searchRemap(filter1, days);
        mappedColumn.forEach((k, v) -> remap.forEach(map -> map.computeIfAbsent(k, k0 -> FunctionEx.apply(v, map))));
        CommonsFX.runInPlatform(() -> getItems().clear());
        CommonsFX.runInPlatform(() -> {
            if (getTable().getColumns().isEmpty()) {
                List<String> columns =
                        remap.stream().flatMap(e -> e.keySet().stream()).filter(t -> !mappedColumn.keySet().contains(t))
                                .distinct().collect(Collectors.toList());
                columns.addAll(0, mappedColumn.keySet());
                SimpleTableViewBuilder.addColumns(getTable(), columns);
            }
            formatMap.forEach(
                    (k, v) -> remap.forEach(map -> map.computeIfPresent(k, (k0, v0) -> FunctionEx.apply(v, v0, v0))));
            getItems().addAll(remap);
        });
        return remap;
    }

    public void makeTimelionFullQuery(Map<String, String> filter, Integer days) {
        ObservableList<Series<Number, Number>> data = getSeries();
        CommonsFX.runInPlatformSync(() -> {
            data.clear();
            getLineChart().getYAxis().setAutoRanging(true);
        });
        TimelionApi.timelionFullScan(data, getQueryFile(), filter, "now-" + days * 24 + "h");
    }

    public void makeTimelionQuery(Map<String, String> filter, Integer days) {
        ObservableList<Series<Number, Number>> data = getSeries();
        CommonsFX.runInPlatformSync(() -> {
            data.clear();
            getLineChart().getYAxis().setAutoRanging(true);
        });
        TimelionApi.timelionScan(data, getQueryFile(), filter, "now-" + days * 24 + "h");
    }

    public List<Map<String, String>> searchRemap(Map<String, String> filter1, Integer days) {
        if (StringUtils.isBlank(getGroup())) {

            Map<String, Object> makeKibanaSearchObj =
                    KibanaApi.makeKibanaSearchObj(getQueryFile(), days, filter1, params);
            return JsonExtractor.remapObj(makeKibanaSearchObj);
        }
        Map<String, String> nsInformation = KibanaApi.makeKibanaSearch(getQueryFile(), days, filter1, params);
        return JsonExtractor.remap(nsInformation, getGroup());
    }

    public QueryObjects setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public QueryObjects setGroup(String group) {
        this.group = group;
        return this;
    }

    public QueryObjects setMappedColumn(String key, FunctionEx<Map<String, String>, String> mappedColumn0) {
        mappedColumn.put(key, mappedColumn0);
        return this;
    }

    public QueryObjects setValueFormat(String key, FunctionEx<String, String> valueFormat) {
        formatMap.put(key, valueFormat);
        return this;
    }

    public static void linkFilter(ListView<Entry<String, String>> filterList2, ObservableMap<String, String> filter,
            ConsumerEx<String> onPaste) {
        SimpleListViewBuilder.of(filterList2).onKey(KeyCode.DELETE, e -> {
            String string = filter.get(e.getKey());
            String newFilterValue = Stream.of(string.split("\n")).filter(t -> !Objects.equals(e.getValue(), t))
                    .collect(Collectors.joining("\n"));
            if (StringUtils.isBlank(newFilterValue)) {
                filter.remove(e.getKey());
            } else {
                filter.put(e.getKey(), newFilterValue);
            }
        }).pasteable(s -> {
            onPaste.accept(s);
            return null;
        }).copiable().multipleSelection();
        filter.addListener((Change<? extends String, ? extends String> change) -> {
            List<Entry<String,
                    String>> newComposition =
                            change.getMap().entrySet().stream()
                                    .flatMap(entry -> Stream.of(entry.getValue().split("\n")).distinct().sorted()
                                            .map(s -> JsonExtractor.newEntry(entry.getKey(), s)))
                                    .collect(Collectors.toList());
            filterList2.getItems().removeIf(s -> !newComposition.contains(s));
            filterList2.getItems().addAll(newComposition.stream().filter(s -> !filterList2.getItems().contains(s))
                    .collect(Collectors.toList()));
        });
    }

}