package kibana;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static kibana.QueryObjects.DESTINATION_IP_QUERY;
import static kibana.QueryObjects.DESTINATION_PORT_QUERY;
import static kibana.QueryObjects.SOURCE_IP_QUERY;
import static kibana.QueryObjects.USER_NAME;

import ethical.hacker.PortServices;
import extract.web.WhoIsScanner;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import simplebuilder.SimpleDialogBuilder;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class PaloAltoInvestigator extends Application {
    private static final List<String> APPLICATION_LIST = ProjectProperties.getFieldList();
    @FXML
    protected TextField resultsFilter;
    @FXML
    protected ListView<Map.Entry<String, String>> filterList;
    @FXML
    protected ComboBox<Integer> days;
    @FXML
    protected ProgressIndicator progress;
    @FXML
    protected TableView<Map<String, String>> ipsTable;
    @FXML
    protected TableView<Map<String, String>> acessosSistemaTable;
    @FXML
    protected TableView<Map<String, String>> consultasTable;
    @FXML
    protected TableView<Map<String, String>> pathsTable;
    @FXML
    protected TabPane tabPane0;
    protected final List<QueryObjects> queryList = new ArrayList<>();
    protected ObservableMap<String, String> filter = FXCollections.observableHashMap();

    @FXML
    protected Text thresholdText;
    @FXML
    protected Slider threshold;

    @FXML
    protected SplitPane splitPane0;
    @FXML
    protected LineChart<Number, Number> timelineSourceIP;
    @FXML
    protected ComboBox<String> ipCombo;

    public List<QueryObjects> getQueryList() {
        return queryList;
    }

    public void initialize() {
        ExtractUtils.addAuthorizationConfig();
        thresholdText.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> String.format(Locale.ENGLISH, "%s (%.2f)", "Threshold", threshold.getValue()),
                        threshold.valueProperty()));
        String va = "value";
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        QueryObjects configureTable =
                configureTable(DESTINATION_IP_QUERY, "topDestinationQuery.json", acessosSistemaTable, "key", va)
                        .setValueFormat(va, StringSigaUtils::getFileSize)
                        .setMappedColumn("DNS", map -> whoIsScanner.reverseDNS(map.get("key")));
        RunnableEx.runNewThread(() -> configureTable.makeKibanaQuery(filter, days.getValue()));
        configureTable(SOURCE_IP_QUERY, "topSourceQuery.json", consultasTable, "key", va).setAllowEmpty(false)
                .setValueFormat(va, StringSigaUtils::getFileSize);
        configureTimeline(SOURCE_IP_QUERY, TimelionApi.BYTE_BY_IP, timelineSourceIP, ipCombo);
        configureTable(DESTINATION_PORT_QUERY, "topDestinationPortQuery.json", pathsTable, "key", va)
                .setValueFormat(va, StringSigaUtils::getFileSize).setMappedColumn("Service",
                        map -> PortServices.getServiceByPort(StringSigaUtils.toInteger(map.get("key"))).toString());
        configureTable("Application.keyword", "topApplicationQuery.json", ipsTable, "key", va).setValueFormat(va,
                StringSigaUtils::getFileSize);
        QueryObjects.linkFilter(filterList, filter, this::addToFilter);
        splitPane0.setDividerPositions(1. / 10);
    }

    public void makeAutomatedSearch() {
        RunnableEx.runNewThread(() -> {
            List<QueryObjects> queries =
                    queryList.stream().filter(q -> q.getLineChart() == null).collect(Collectors.toList());
            List<String> applicationList = getApplicationList();
            ConsultasHelper.automatedSearch(DESTINATION_IP_QUERY, queries, applicationList, progress.progressProperty(),
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
        CommonsFX.loadFXML("PaloAlto Investigator", "PaloAltoInvestigator.fxml", this, primaryStage, width, width);
    }

    protected void addProgress(double d) {
        CommonsFX.addProgress(progress.progressProperty(), d);
    }

    protected void addToFilter(String s) {
        if (s.matches("[\\.\\w]+=.+")) {
            String[] entry = s.split("=");
            filter.merge(entry[0], entry[1], ConsultasHelper::merge);
            return;
        }
        if (s.matches(WhoIsScanner.IP_REGEX)) {
            filter.merge(SOURCE_IP_QUERY, s, ConsultasHelper::merge);
            return;
        }
        if (s.matches("^[\\w\\.]+@[\\w\\.]+|\\d{11}$")) {
            filter.merge(USER_NAME, s, ConsultasHelper::merge);
            return;
        }
        if (s.matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+")) {
            filter.merge(SOURCE_IP_QUERY, "\\\"" + s + "\\\"", ConsultasHelper::merge);
        }
    }

    protected QueryObjects configureTable(String userNameQuery, String queryFile,
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

    protected QueryObjects configureTimeline(String field, String userNameQuery, LineChart<Number, Number> lineChart,
            ComboBox<String> combo) {
        return configureTimeline(field, queryList, userNameQuery, lineChart, combo);
    }

    protected List<String> getApplicationList() {
        if (!acessosSistemaTable.getSelectionModel().getSelectedItems().isEmpty()) {
            return acessosSistemaTable.getSelectionModel().getSelectedItems().stream().map(e -> e.get("key"))
                    .collect(toList());
        }
        return APPLICATION_LIST;
    }

    protected void makeKibanaQuery(QueryObjects queryObjects) {
        queryObjects.makeKibanaQuery(filter, days.getSelectionModel().getSelectedItem());
    }

    protected void makeTimelionQuery(QueryObjects queryObjects) {
        queryObjects.makeTimelionFullQuery(filter, days.getSelectionModel().getSelectedItem());
    }

    public static QueryObjects configureTimeline(String field, List<QueryObjects> queryList, String userNameQuery,
            LineChart<Number, Number> lineChart, ComboBox<String> combo) {
        QueryObjects fieldObjects = new QueryObjects(field, userNameQuery, lineChart);
        queryList.add(fieldObjects.configureTimeline(combo, StringSigaUtils::getFileSize));
        return fieldObjects;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
