package kibana;
import static kibana.QueryObjects.ACESSOS_SISTEMA_QUERY;
import static kibana.QueryObjects.CLIENT_IP_QUERY;
import static kibana.QueryObjects.MDC_UID_KEYWORD;
import static kibana.QueryObjects.URL_QUERY;

import ethical.hacker.WhoIsScanner;
import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.CSVUtils;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class ConsultasInvestigator extends Application {

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
        configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, "key", count);
        configureTable(CLIENT_IP_QUERY, "consultasQuery.json", consultasTable, "key", count).setAllowEmpty(false);
        configureTable(URL_QUERY, "requestedPath.json", pathsTable, "key", count).setGroup("^/.*").setAllowEmpty(false);
        configureTimeline(MDC_UID_KEYWORD, TimelionApi.TIMELINE_USERS, timelineUsuarios, uidCombo);
        configureTimeline(CLIENT_IP_QUERY, TimelionApi.TIMELINE_IPS, timelineIPs, ipCombo);
        configureTable(CLIENT_IP_QUERY, "geridQuery.json", ipsTable, "value", "key").setGroup(WhoIsScanner.IP_REGEX)
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
            CommonsFX.update(progress.progressProperty(), 0);
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
            CommonsFX.update(progress.progressProperty(), 1);
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
            String collect =
                    filter.values().stream().map(s -> s.replaceAll(".+/(.+)", "$1")).collect(Collectors.joining());
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
        CommonsFX.update(progress.progressProperty(), progress.getProgress() + d);
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, ipsTable2, params);
        queryList.add(fieldObjects);
        return fieldObjects.configureTable(resultsFilter, e -> {
            for (Map<String, String> map : e) {
                filter.merge(userNameQuery, map.values().stream().findFirst().orElse("key"),
                        (u, v) -> Stream.of(u, v).distinct().collect(Collectors.joining("\n")));
            }
            resultsFilter.setText("");
            onActionKibanaScan();
        });
    }

    private QueryObjects configureTimeline(String field, String userNameQuery, LineChart<Number, Number> lineChart,
            ComboBox<String> combo) {
        return configureTimeline(field, queryList, userNameQuery, lineChart, combo);
    }

    private void makeKibanaQuery(QueryObjects queryObjects) {
        queryObjects.makeKibanaQuery(filter, days.getSelectionModel().getSelectedItem());
    }

    private void makeTimelionQuery(QueryObjects queryObjects) {
        queryObjects.makeTimelionQuery(filter);
    }

    public static QueryObjects configureTimeline(String field, List<QueryObjects> queryList, String userNameQuery,
            LineChart<Number, Number> lineChart, ComboBox<String> combo) {
        QueryObjects fieldObjects = new QueryObjects(field, userNameQuery, lineChart);
        queryList.add(fieldObjects.configureTimeline(combo));
        return fieldObjects;
    }

    public static void main(String[] args) {
        launch(args);
    }



}
