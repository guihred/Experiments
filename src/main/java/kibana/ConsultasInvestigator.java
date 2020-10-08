package kibana;

import com.google.common.collect.ImmutableMap;
import extract.ExcelService;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ml.graph.DataframeExplorer;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.ListHelper;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CSVUtils;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class ConsultasInvestigator extends Application {
    private static final String MDC_UID_KEYWORD = "mdc.uid.keyword";
    private static final String USER_NAME_QUERY = "http.user-name.keyword";
    private static final String CLIENT_IP_QUERY = "clientip.keyword";
    private static final String ACESSOS_SISTEMA_QUERY = "dtpsistema.keyword";
    private static final ImmutableMap<String, String> REPLACEMENT_MAP = ImmutableMap.<String, String>builder()
            .put(USER_NAME_QUERY, MDC_UID_KEYWORD).put(ACESSOS_SISTEMA_QUERY, "dtpsistema").build();
    @FXML
    private TextField resultsFilter;
    @FXML
    private ListView<Map.Entry<String, String>> filterList;
    @FXML
    private ComboBox<Integer> days;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private TableView<Map<String, String>> ipsTable;
    @FXML
    private TableView<Map<String, String>> acessosSistemaTable;
    @FXML
    private TableView<Map<String, String>> consultasTable;
    @FXML
    private TableView<Map<String, String>> pathsTable;
    @FXML
    private TabPane tabPane0;
    private List<QueryObjects> queryList = new ArrayList<>();
    private ObservableMap<String, String> filter = FXCollections.observableHashMap();
    @FXML
    private ComboBox<String> ipCombo;
    @FXML
    private ComboBox<String> uidCombo;
    @FXML
    private LineChart<Number, Number> timelineUsuarios;

    @FXML
    private SplitPane splitPane0;
    @FXML
    private LineChart<Number, Number> timelineIPs;

    public void initialize() {
        String count = "doc_count";
        configureTable(CLIENT_IP_QUERY, "consultasQuery.json", consultasTable, "key", count);
        configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, "key", count);
        configureTable(ACESSOS_SISTEMA_QUERY, "requestedPath.json", pathsTable, "key", count).setGroup("^[^\\/\\d].+");
        configureTimeline(MDC_UID_KEYWORD, TimelionApi.TIMELINE_USERS, timelineUsuarios, uidCombo);
        configureTimeline(CLIENT_IP_QUERY, TimelionApi.TIMELINE_IPS, timelineIPs, ipCombo);
        configureTable(USER_NAME_QUERY, "geridQuery.json", ipsTable, "key", "value").setGroup("[^\\d].+")
                .setAllowEmpty(false);
        SimpleListViewBuilder.of(filterList).onKey(KeyCode.DELETE, e -> filter.remove(e.getKey()));
        filter.addListener((Change<? extends String, ? extends String> change) -> {
            if (change.wasRemoved()) {
                filterList.getItems().removeIf(e -> Objects.equals(e.getKey(), change.getKey()));
            }
            if (change.wasAdded()) {
                filterList.getItems().add(new AbstractMap.SimpleEntry<>(change.getKey(), change.getValueAdded()));
            }
        });
        splitPane0.setDividerPositions(0.2);
    }

    public void onActionClear() {
        resultsFilter.setText("");
        filter.clear();
        onActionKibanaScan();
    }

    public void onActionKibanaScan() {
        RunnableEx.runNewThread(() -> {
            setProgress(0);
            for (QueryObjects queryObjects : queryList) {
                RunnableEx.measureTime(queryObjects.getQueryFile(), () -> {
                    if (queryObjects.getLineChart() == null) {
                        makeKibanaQuery(queryObjects);
                        return;
                    }
                    makeTimelionQuery(queryObjects);
                });

                addProgress(1. / queryList.size());
            }
            setProgress(1);
        });
    }

    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> collect = queryList.stream().filter(e -> e.getTable() != null)
                .collect(Collectors.toMap(QueryObjects::getQueryFile, QueryObjects::getItems));
        List<String> collect2 =
                queryList.stream().filter(e -> e.getTable() != null).flatMap(e -> e.getTable().getColumns().stream())
                        .map(TableColumn::getText).distinct().collect(Collectors.toList());
        for (String text : collect2) {
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/investigation.xlsx");
        ExcelService.getExcel(collect, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    public void onOpenDataframe() {
        RunnableEx.run(() -> {
            Tab n = tabPane0.getSelectionModel().getSelectedItem();
            if (n == null) {
                return;
            }
            Parent content = (Parent) n.getContent();
            Node lookup = content.lookup("TableView");
            QueryObjects orElse =
                    queryList.stream().filter(e -> e.getTable() == lookup).findFirst().orElse(queryList.get(0));
            TableView<Map<String, String>> table = orElse.getTable();
            String collect = filter.values().stream().collect(Collectors.joining());
            File ev = ResourceFXUtils.getOutFile("csv/" + table.getId() + collect + ".csv");
            CSVUtils.saveToFile(table, ev);
            new SimpleDialogBuilder().bindWindow(tabPane0).show(DataframeExplorer.class).addStats(ev);
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        final int width = 800;
        CommonsFX.loadFXML("Consultas Investigator", "ConsultasInvestigator.fxml", this, primaryStage, width, width);
    }

    private void addProgress(double d) {
        CommonsFX.runInPlatform(() -> progress.setProgress(progress.getProgress() + d));
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, ipsTable2, params);
        ObservableList<Map<String, String>> ipItems2 = fieldObjects.getItems();
        queryList.add(fieldObjects);
        final int columnWidth = 120;
        ipsTable2.prefWidthProperty()
                .bind(Bindings.selectDouble(ipsTable2.parentProperty(), "width").add(-columnWidth));
        ipsTable2.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2.filtered(e -> true)));
        ipsTable2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(ipsTable2).copiable().savable().onDoubleClickMany(e -> {
            for (Map<String, String> map : e) {
                filter.merge(userNameQuery, map.values().stream().findFirst().orElse("key"),
                        (u, v) -> Objects.equals(u, v) ? u : u + "\n" + v);
            }
            resultsFilter.setText("");
            onActionKibanaScan();
        }).onSortClicked(e -> {
            Comparator<Map<String, String>> comparing =
                    Comparator.comparing(m -> StringUtils.isNumeric(m.get(e.getKey()))
                            ? String.format("%09d", Long.valueOf(m.get(e.getKey())))
                            : Objects.toString(m.get(e.getKey()), ""));
            ipItems2.sort(e.getValue() ? comparing : comparing.reversed());
        })

        ;
        return fieldObjects;
    }

    private QueryObjects configureTimeline(String field, String userNameQuery, LineChart<Number, Number> lineChart,
            ComboBox<String> combo) {
        QueryObjects fieldObjects = new QueryObjects(field, userNameQuery, lineChart);
        queryList.add(fieldObjects);
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                return LocalDateTime.parse(string).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }

            @Override
            public String toString(Number object) {
                return Instant.ofEpochMilli(object.longValue()).atZone(ZoneId.systemDefault()).toLocalTime().toString();
            }
        });
        ObservableList<Series<Number, Number>> timelionFullScan = fieldObjects.getSeries();

        ObservableList<String> mapping = ListHelper.mapping(timelionFullScan, Series<Number, Number>::getName);
        mapping.add(0, "");
        FilteredList<Series<Number, Number>> filtered = timelionFullScan.filtered(e -> true);
        lineChart.setData(filtered);
        combo.setItems(mapping);
        combo.getSelectionModel().selectedItemProperty().addListener((ob, old, val) -> {
            lineChart.getYAxis().setAutoRanging(false);
            filtered.setPredicate(e -> StringUtils.isBlank(val) || Objects.equals(e.getName(), val));
        });
        return fieldObjects;
    }

    private void makeKibanaQuery(QueryObjects queryObjects) {
        if (filter.isEmpty() && !queryObjects.isAllowEmpty()) {
            CommonsFX.runInPlatform(() -> queryObjects.getItems().clear());

            return;
        }
        Map<String, String> nsInformation =
                KibanaApi.makeKibanaSearch(ResourceFXUtils.toFile("kibana/" + queryObjects.getQueryFile()),
                        days.getSelectionModel().getSelectedItem(), filter, queryObjects.getParams());
        List<Map<String, String>> remap = KibanaApi.remap(nsInformation, queryObjects.getGroup());
        CommonsFX.runInPlatform(() -> queryObjects.getItems().clear());
        CommonsFX.runInPlatform(() -> {
            if (queryObjects.getTable().getColumns().isEmpty()) {
                SimpleTableViewBuilder.addColumns(queryObjects.getTable(),
                        remap.stream().flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList()));
            }
            queryObjects.getItems().addAll(remap);
        });
    }

    private void makeTimelionQuery(QueryObjects queryObjects) {
        ObservableList<Series<Number, Number>> data = queryObjects.getSeries();
        CommonsFX.runInPlatformSync(() -> {
            data.clear();
            queryObjects.getLineChart().getYAxis().setAutoRanging(true);
        });
        Map<String, String> hashMap = new HashMap<>(filter);
        REPLACEMENT_MAP.entrySet().forEach(e -> {
            if (hashMap.containsKey(e.getKey())) {
                hashMap.put(e.getValue(), hashMap.remove(e.getKey()));
            }
        });

        TimelionApi.timelionScan(data, queryObjects.getQueryFile(), filter, "now-d");
    }

    private void setProgress(double d) {
        CommonsFX.runInPlatform(() -> progress.setProgress(d));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
