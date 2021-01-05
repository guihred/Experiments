package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import com.google.common.collect.ImmutableMap;
import extract.JsonExtractor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.ListHelper;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.DateFormatUtils;
import utils.QuickSortML;
import utils.ex.ConsumerEx;

public class QueryObjects {
    public static final String MDC_UID_KEYWORD = "mdc.uid.keyword";
    public static final String URL_QUERY = "request.keyword";

    public static final String CLIENT_IP_QUERY = "clientip.keyword";
    public static final String ACESSOS_SISTEMA_QUERY = "dtpsistema.keyword";
    public static final String USER_NAME_QUERY = "http.user-name.keyword";
    private static final ImmutableMap<String, String> REPLACEMENT_MAP = ImmutableMap.<String, String>builder()
            .put(USER_NAME_QUERY, MDC_UID_KEYWORD).put(ACESSOS_SISTEMA_QUERY, "dtpsistema").build();

    private final String query;
    private boolean allowEmpty = true;

    private String queryFile;

    private String[] params;

    private String group = "";

    private TableView<Map<String, String>> table;

    private final ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());
    private final ObservableList<Series<Number, Number>> series = observableArrayList();

    private LineChart<Number, Number> lineChart;

    public QueryObjects(String query, String queryFile, LineChart<Number, Number> lineChart) {
        this.query = query;
        this.queryFile = queryFile;
        this.lineChart = lineChart;

    }

    public QueryObjects(String query, String queryFile, TableView<Map<String, String>> table, String[] params) {
        this.query = query;
        this.queryFile = queryFile;
        this.table = table;
        this.params = params;
    }

    public QueryObjects configureTable(TextField resultsFilter, ConsumerEx<List<Map<String, String>>> onClick) {
        ObservableList<Map<String, String>> ipItems2 = getItems();
        final int columnWidth = 120;
        table.prefWidthProperty().bind(Bindings.selectDouble(table.parentProperty(), "width").add(-columnWidth));
        table.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2.filtered(e -> true)));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(table).copiable().savable().onDoubleClickMany(onClick)
                .onSortClicked((col, ascending) -> QuickSortML.sortMapList(ipItems2, col, ascending));
        return this;
    }

    public QueryObjects configureTimeline(ComboBox<String> combo) {
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
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
        CommonsFX.runInPlatform(() -> getItems().clear());
        CommonsFX.runInPlatform(() -> {
            if (getTable().getColumns().isEmpty()) {
                SimpleTableViewBuilder.addColumns(getTable(),
                        remap.stream().flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList()));
            }
            getItems().addAll(remap);
        });
        return remap;
    }

    public void makeTimelionQuery(Map<String, String> filter, Integer days) {
        ObservableList<Series<Number, Number>> data = getSeries();
        CommonsFX.runInPlatformSync(() -> {
            data.clear();
            getLineChart().getYAxis().setAutoRanging(true);
        });
        Map<String, String> hashMap = new HashMap<>(filter);
        REPLACEMENT_MAP.entrySet().forEach(e -> {
            if (hashMap.containsKey(e.getKey())) {
                hashMap.put(e.getValue(), hashMap.remove(e.getKey()));
            }
        });

        TimelionApi.timelionScan(data, getQueryFile(), filter, "now-" + days * 24 + "h");
    }

    public List<Map<String, String>> searchRemap(Map<String, String> filter1, Integer days) {
        Map<String, String> nsInformation =
                KibanaApi.makeKibanaSearch("kibana/" + getQueryFile(), days, filter1, getParams());
        return JsonExtractor.remap(nsInformation, getGroup());
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    public QueryObjects setGroup(String group) {
        this.group = group;
        return this;
    }


}