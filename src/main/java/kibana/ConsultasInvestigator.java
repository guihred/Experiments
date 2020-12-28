package kibana;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static kibana.QueryObjects.ACESSOS_SISTEMA_QUERY;
import static kibana.QueryObjects.CLIENT_IP_QUERY;
import static kibana.QueryObjects.URL_QUERY;
import static utils.StringSigaUtils.toDouble;

import extract.CIDRUtils;
import extract.WhoIsScanner;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
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
import ml.graph.IPFill;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class ConsultasInvestigator extends Application {

    private static final Logger LOG = HasLogging.log();
    public static final String IGNORE_IPS_REGEX = "10\\..+|::1|127.0.0.1";
    private static final List<String> APPLICATION_LIST = Arrays.asList("consultas.inss.gov.br",
            "vip-pmeuinssprxr.inss.gov.br", "vip-auxilioemergencial.dataprev.gov.br");
    private static final List<String> EXCLUDE_OWNERS =
            Arrays.asList("CAIXA ECONOMICA FEDERAL", "SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO",
                    "BANCO DO BRASIL S.A.", "Itau Unibanco S.A.", "Google LLC", "BANCO MERCANTIL DO BRASIL S/A");
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
        String count = "doc_count";
        QueryObjects configureTable =
                configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, "key", count);
        RunnableEx.runNewThread(() -> configureTable.makeKibanaQuery(filter, days.getValue()));
        configureTable(CLIENT_IP_QUERY, "consultasQuery.json", consultasTable, "key", count).setAllowEmpty(false);
        configureTable(URL_QUERY, "requestedPath.json", pathsTable, "key", count).setGroup("^/.*").setAllowEmpty(false);
        configureTimeline(ACESSOS_SISTEMA_QUERY, TimelionApi.TIMELINE_USERS, timelineUsuarios, uidCombo);
        configureTimeline(CLIENT_IP_QUERY, TimelionApi.TIMELINE_IPS, timelineIPs, ipCombo);
        configureTable(CLIENT_IP_QUERY, "geridQuery.json", ipsTable, "key", "value").setGroup(WhoIsScanner.IP_REGEX)
                .setAllowEmpty(false);
        SimpleListViewBuilder.of(filterList).onKey(KeyCode.DELETE, e -> filter.remove(e.getKey())).pasteable(s -> {
            addToFilter(s);
            return null;
        }).copiable();
        filter.addListener((Change<? extends String, ? extends String> change) -> {
            if (change.wasRemoved()) {
                filterList.getItems().removeIf(e -> Objects.equals(e.getKey(), change.getKey()));
            }
            if (change.wasAdded()) {
                filterList.getItems().add(new AbstractMap.SimpleEntry<>(change.getKey(), change.getValueAdded()));
            }
        });
        splitPane0.setDividerPositions(1. / 10);
    }

    public void makeAutomatedNetworkSearch() {
        RunnableEx.runNewThread(() -> {
            List<QueryObjects> queries = queryList.stream()
                    .filter(q -> q.getLineChart() == null && QueryObjects.CLIENT_IP_QUERY.equals(q.getQuery()))
                    .collect(Collectors.toList());
            networkSearch(filter, queries, getApplicationList(), days.getValue(), progress.progressProperty());
        });
    }

    public void makeAutomatedSearch() {
        Map<String, String> filter1 = new HashMap<>();
        RunnableEx.runNewThread(() -> {
            CommonsFX.update(progress.progressProperty(), 0);
            List<String> applicationList = getApplicationList();
            List<QueryObjects> queries =
                    queryList.stream().filter(q -> q.getLineChart() == null).collect(Collectors.toList());
            for (String application : applicationList) {
                for (QueryObjects queryObjects : queries) {
                    filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, application);
                    String[] params = queryObjects.getParams();
                    String numberCol = params[queryObjects.getParams().length - 1];
                    List<Map<String, String>> kibanaQuery = queryObjects.searchRemap(filter1, days.getValue());
                    String fieldQuery = queryObjects.getQuery();
                    DoubleSummaryStatistics summaryStatistics = getStatistics(params, numberCol, kibanaQuery);
                    List<Map<String, String>> aboveAvgInfo =
                            getAboveAvgInfo(summaryStatistics, kibanaQuery, numberCol, params);
                    if (!aboveAvgInfo.isEmpty()) {
                        mergeFilter(filter, params, fieldQuery, aboveAvgInfo);
                        LOG.info("\n\t{}\n\t{}\n{}", application, fieldQuery, join(aboveAvgInfo));
                    }
                    CommonsFX.addProgress(progress.progressProperty(), 1. / applicationList.size() / queries.size());
                }
            }
            CommonsFX.update(progress.progressProperty(), 1);
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
            String tableFullName = filter.values().stream().map(s -> s.replaceAll(".+/(.+)", "$1")).collect(joining());
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
        if (s.contains("=")) {
            String[] entry = s.split("=");
            filter.merge(entry[0], entry[1], ConsultasInvestigator::merge);
            return;
        }
        if (s.matches(WhoIsScanner.IP_REGEX)) {
            filter.merge(CLIENT_IP_QUERY, s, ConsultasInvestigator::merge);
            return;
        }
        if (s.startsWith("/")) {
            filter.merge(URL_QUERY, s, ConsultasInvestigator::merge);
            return;
        }
        if (!StringUtils.isNumeric(s)) {
            filter.merge(ACESSOS_SISTEMA_QUERY, s, ConsultasInvestigator::merge);
        }
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, ipsTable2, params);
        queryList.add(fieldObjects);
        return fieldObjects.configureTable(resultsFilter, e -> {
            for (Map<String, String> map : e) {
                filter.merge(userNameQuery, map.values().stream().findFirst().orElse("key"),
                        (u, v) -> of(u, v).distinct().collect(joining("\n")));
            }
            resultsFilter.setText("");
            onActionKibanaScan();
        });
    }

    private QueryObjects configureTimeline(String field, String userNameQuery, LineChart<Number, Number> lineChart,
            ComboBox<String> combo) {
        return configureTimeline(field, queryList, userNameQuery, lineChart, combo);
    }

    private List<Map<String, String>> getAboveAvgInfo(DoubleSummaryStatistics summaryStatistics,
            List<Map<String, String>> makeKibanaQuery, String numberCol, String[] params) {
        if (summaryStatistics.getCount() <= 1 || summaryStatistics.getSum() == 0) {
            return Collections.emptyList();
        }
        double avg = summaryStatistics.getAverage();
        double max = summaryStatistics.getMax();
        double min = summaryStatistics.getMin();
        final double range = (max - min) * .45;
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        return makeKibanaQuery.parallelStream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                .filter(m -> getNumber(numberCol, m) > avg + range)
                .map(e -> completeInformation(params, whoIsScanner, e))
                .filter(m -> !EXCLUDE_OWNERS.contains(m.getOrDefault("as_owner", "")))
                .filter(m -> isNotBlocked(days.getValue(), getFirst(params, m))).collect(toList());
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

    private static Map<String, String> completeInformation(String[] params, WhoIsScanner whoIsScanner,
            Map<String, String> e) {
        String field = getFirst(params, e);
        if (field.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> ipInformation = whoIsScanner.getIpInformation(field);
            ipInformation.remove("last_analysis_stats");
            ipInformation.remove("malicious");
            e.putAll(ipInformation);
        }
        return e;
    }

    private static String getFirst(String[] params, Map<String, String> m) {
        return m.getOrDefault(params[0], m.values().iterator().next());
    }

    private static Double getNumber(String numberCol, Map<String, String> m) {
        return toDouble(m.getOrDefault(numberCol, m.get(numberCol + 0)));
    }

    private static DoubleSummaryStatistics getStatistics(String[] params, String numberCol,
            List<Map<String, String>> makeKibanaQuery) {
        return makeKibanaQuery.stream().filter(m -> !getFirst(params, m).matches(IGNORE_IPS_REGEX))
                .mapToDouble(m -> getNumber(numberCol, m)).summaryStatistics();
    }

    private static boolean isNotBlocked(Integer days, String ip) {
        if (ip.matches(WhoIsScanner.IP_REGEX)) {
            Map<String, String> blocked = KibanaApi.makeKibanaSearch("policiesQuery.json", ip, days, "key");
            if (blocked.values().stream().anyMatch(s -> s.contains("block"))) {
                return false;
            }
        }
        return true;
    }

    private static String join(List<Map<String, String>> collect) {
        return collect.stream().map(e -> "\t" + e.values().stream().collect(joining("\t"))).collect(joining("\n"));
    }

    private static String merge(String a, String b) {
        return concat(of(a.split("\n")), of(b.split("\n"))).distinct().sorted().collect(joining("\n"));
    }

    private static void mergeFilter(Map<String, String> filter, String[] params, String fieldQuery,
            List<Map<String, String>> aboveAvgInfo) {
        CommonsFX.runInPlatform(() -> {
            for (Map<String, String> map : aboveAvgInfo) {
                filter.merge(fieldQuery, getFirst(params, map), ConsultasInvestigator::merge);
            }
        });
    }

    private static void networkSearch(Map<String, String> filter, List<QueryObjects> queries,
            List<String> applicationList, Integer day, DoubleProperty progress) {
        CommonsFX.update(progress, 0);
        Map<String, String> filter1 = new HashMap<>();
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        for (String application : applicationList) {
            for (QueryObjects queryObjects : queries) {
                filter1.put(QueryObjects.ACESSOS_SISTEMA_QUERY, application);
                String[] params = queryObjects.getParams();
                List<Map<String, String>> kibanaQuery = queryObjects.searchRemap(filter1, day);
                List<Map<String, String>> whoIsInfo = kibanaQuery.parallelStream()
                        .filter(m -> !getFirst(params, m).matches(ConsultasInvestigator.IGNORE_IPS_REGEX)).map(e -> {
                            e.putAll(whoIsScanner.getIpInformation(getFirst(params, e)));
                            return e;
                        }).collect(Collectors.toList());
                String numberCol = params[queryObjects.getParams().length - 1];
                Map<String,
                        Double> netHistogram = whoIsInfo.stream().collect(Collectors.groupingBy(
                                m -> IPFill.getKey(m, "as_owner", "") + "\t" + IPFill.getKey(m, "network", "id"),
                                Collectors.summingDouble(m -> getNumber(numberCol, m))));
                DoubleSummaryStatistics summaryStatistics =
                        netHistogram.values().stream().mapToDouble(e -> e).summaryStatistics();
                double avg = summaryStatistics.getAverage();
                double max = summaryStatistics.getMax();
                double min = summaryStatistics.getMin();
                double range = (max - min) * .40;
                List<String> networks = netHistogram.entrySet().stream().filter(m -> m.getValue() > avg + range)
                        .filter(m -> EXCLUDE_OWNERS.stream().noneMatch(ow -> m.getKey().startsWith(ow)))
                        .map(s -> "\t" + s).collect(Collectors.toList());
                if (!networks.isEmpty()) {
                    List<String> nets =
                            networks.stream().map(e -> e.replaceAll(".+\t(.+)", "$1")).collect(Collectors.toList());
                    String queryField = queryObjects.getQuery();
                    LOG.info("\n\tTOP NETWORKS\n\t{}\n\t{}\n{}", application, queryField, networks);
                    List<Map<String, String>> aboveAvgInfo = kibanaQuery.parallelStream()
                            .filter(m -> !getFirst(params, m).matches(ConsultasInvestigator.IGNORE_IPS_REGEX))
                            .filter(e -> nets.stream()
                                    .anyMatch(net -> CIDRUtils.isSameNetworkAddress(net, getFirst(params, e))))
                            .collect(Collectors.toList());
                    mergeFilter(filter, params, queryField, aboveAvgInfo);
                }

                CommonsFX.addProgress(progress, 1. / applicationList.size() / queries.size());
            }
        }
        CommonsFX.update(progress, 1);
    }

}
