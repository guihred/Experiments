package kibana;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static kibana.QueryObjects.ACESSOS_SISTEMA_QUERY;
import static kibana.QueryObjects.CLIENT_IP_QUERY;
import static kibana.QueryObjects.URL_QUERY;

import extract.web.CIDRUtils;
import extract.web.JsonExtractor;
import extract.web.WhoIsScanner;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class ConsultasInvestigator extends Application {

    private static final List<String> APPLICATION_LIST =
            Arrays.asList("consultas.inss.gov.br", "vip-pmeuinssprxr.inss.gov.br", "refisprod.dataprev.gov.br",
                    "vip-auxilioemergencial.dataprev.gov.br", "auxilio.dataprev.gov.br");
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
    private final List<QueryObjects> queryList = new ArrayList<>();
    private ObservableMap<String, String> filter = FXCollections.observableHashMap();

    @FXML
    private ComboBox<String> ipCombo;
    @FXML
    private Text thresholdText;
    @FXML
    private Slider threshold;
    @FXML
    private ComboBox<String> uidCombo;

    @FXML
    private LineChart<Number, Number> timelineUsuarios;
    @FXML
    private SplitPane splitPane0;

    @FXML
    private LineChart<Number, Number> timelineIPs;

    public List<QueryObjects> getQueryList() {
        return queryList;
    }

    public void initialize() {

        ExtractUtils.addAuthorizationConfig();
        thresholdText.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> String.format(Locale.ENGLISH, "%s (%.2f)", "Threshold", threshold.getValue()),
                        threshold.valueProperty()));
        String count = "doc_count";
        QueryObjects configureTable =
                configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, "key", count);
        RunnableEx.runNewThread(() -> configureTable.makeKibanaQuery(filter, days.getValue()));
        configureTable(CLIENT_IP_QUERY, "consultasQuery.json", consultasTable, "key", count).setAllowEmpty(false);
        configureTable(URL_QUERY, "requestedPath.json", pathsTable, "key", count).setGroup("^/.*").setAllowEmpty(false);
        configureTimeline(ACESSOS_SISTEMA_QUERY, TimelionApi.TIMELINE_USERS, timelineUsuarios, uidCombo);
        configureTimeline(CLIENT_IP_QUERY, TimelionApi.TIMELINE_IPS, timelineIPs, ipCombo);
        configureTable(CLIENT_IP_QUERY, "geridQuery.json", ipsTable, "key", "value").setAllowEmpty(false);
        SimpleListViewBuilder.of(filterList).onKey(KeyCode.DELETE, e -> {
            String string = filter.get(e.getKey());
            String newFilterValue = Stream.of(string.split("\n")).filter(t -> !Objects.equals(e.getValue(), t))
                    .collect(Collectors.joining("\n"));
            if (StringUtils.isBlank(newFilterValue)) {
                filter.remove(e.getKey());
            } else {
                filter.put(e.getKey(), newFilterValue);
            }
        }).pasteable(s -> {
            addToFilter(s);
            return null;
        }).copiable().multipleSelection();
        filter.addListener((Change<? extends String, ? extends String> change) -> {
            List<Entry<String,
                    String>> newComposition =
                            change.getMap().entrySet().stream()
                                    .flatMap(entry -> Stream.of(entry.getValue().split("\n")).distinct().sorted()
                                            .map(s -> JsonExtractor.newEntry(entry.getKey(), s)))
                                    .collect(Collectors.toList());
            filterList.getItems().removeIf(s -> !newComposition.contains(s));
            filterList.getItems().addAll(newComposition.stream().filter(s -> !filterList.getItems().contains(s))
                    .collect(Collectors.toList()));
        });
        splitPane0.setDividerPositions(1. / 10);
    }

    public void makeAutomatedNetworkSearch() {
        RunnableEx.runNewThread(() -> {
            List<QueryObjects> queries = queryList.stream()
                    .filter(q -> q.getLineChart() == null && QueryObjects.CLIENT_IP_QUERY.equals(q.getQuery()))
                    .collect(Collectors.toList());
            ConsultasHelper.networkSearch(filter, queries, getApplicationList(), days.getValue(),
                    progress.progressProperty());
        });
    }

    public void makeAutomatedSearch() {
        RunnableEx.runNewThread(() -> {
            List<QueryObjects> queries =
                    queryList.stream().filter(q -> q.getLineChart() == null).collect(Collectors.toList());
            List<String> applicationList = getApplicationList();
            String queryField = QueryObjects.ACESSOS_SISTEMA_QUERY;
            ConsultasHelper.automatedSearch(queryField, queries, applicationList, progress.progressProperty(),
                    days.getValue(), filter, threshold.getValue());
        });
    }

    public void onActionClear() {
        resultsFilter.setText("");
        filter.clear();
        onActionKibanaScan();
    }

    public void onActionKibanaScan() {
        RunnableEx.runNewThread(() -> {
            CommonsFX.update(progress.progressProperty(), 0);
            queryList.parallelStream().forEach(query -> {
                RunnableEx.measureTime(query.getQueryFile(), () -> {
                    if (query.getLineChart() == null) {
                        makeKibanaQuery(query);
                        return;
                    }
                    makeTimelionQuery(query);
                });
                addProgress(1. / queryList.size());
            });
            CommonsFX.update(progress.progressProperty(), 1);
        });
    }

    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> itemsBySheet = queryList.stream().filter(e -> e.getTable() != null)
                .collect(toMap(QueryObjects::getQueryFile, QueryObjects::getItems));
        List<String> collect2 =
                queryList.stream().filter(e -> e.getTable() != null).flatMap(e -> e.getTable().getColumns().stream())
                        .map(TableColumn::getText).distinct().collect(toList());
        for (String text : collect2) {
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/investigation.xlsx");
        ExcelService.getExcel(itemsBySheet, mapa, outFile);
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
            String tableFullName = filter.values().stream().map(s -> s.replaceAll(".+/(.+)", "$1"))
                    .map(s -> s.replaceAll("[\\?=]+", "")).collect(joining());
            File ev = ResourceFXUtils.getOutFile("csv/" + table.getId() + tableFullName + ".csv");
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
        CommonsFX.addProgress(progress.progressProperty(), d);
    }

    private void addToFilter(String s) {
        if (s.matches("[\\.\\w]+=.+")) {
            String[] entry = s.split("=");
            filter.merge(entry[0], entry[1], ConsultasHelper::merge);
            return;
        }
        if (s.matches(WhoIsScanner.IP_REGEX)) {
            filter.merge(CLIENT_IP_QUERY, s, ConsultasHelper::merge);
            return;
        }
        if (s.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+")) {
            filter.merge(CLIENT_IP_QUERY, CIDRUtils.addressToPattern(s), ConsultasHelper::merge);
            return;
        }
        if (s.startsWith("/")) {
            String fieldQuery = URL_QUERY;
            String fixParam = ConsultasHelper.fixParam(fieldQuery, s);
            filter.merge(fieldQuery, fixParam, ConsultasHelper::merge);
            return;
        }
        if (!StringUtils.isNumeric(s)) {
            filter.merge(ACESSOS_SISTEMA_QUERY, s, ConsultasHelper::merge);
        }
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, ipsTable2, params);
        queryList.add(fieldObjects);
        return fieldObjects.configureTable(resultsFilter, e -> {
            for (Map<String, String> map : e) {
                String s = map.values().stream().findFirst().orElse("key");
                String fixParam = ConsultasHelper.fixParam(userNameQuery, s);
                filter.merge(userNameQuery, fixParam, ConsultasHelper::merge);
            }
            resultsFilter.setText("");
            onActionKibanaScan();
        });
    }

    private QueryObjects configureTimeline(String field, String userNameQuery, LineChart<Number, Number> lineChart,
            ComboBox<String> combo) {
        return configureTimeline(field, queryList, userNameQuery, lineChart, combo);
    }

    private List<String> getApplicationList() {
        if (!acessosSistemaTable.getSelectionModel().getSelectedItems().isEmpty()) {
            return acessosSistemaTable.getSelectionModel().getSelectedItems().stream().map(e -> e.get("key"))
                    .collect(toList());
        }
        return APPLICATION_LIST;
    }

    private void makeKibanaQuery(QueryObjects queryObjects) {
        queryObjects.makeKibanaQuery(filter, days.getSelectionModel().getSelectedItem());
    }

    private void makeTimelionQuery(QueryObjects queryObjects) {
        queryObjects.makeTimelionQuery(filter, days.getSelectionModel().getSelectedItem());
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
