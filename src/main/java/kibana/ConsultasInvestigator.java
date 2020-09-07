package kibana;

import com.google.common.collect.ImmutableMap;
import ethical.hacker.EthicalHackApp;
import ethical.hacker.WhoIsScanner;
import extract.ExcelService;
import gaming.ex21.ListHelper;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import schema.sngpc.JsonExtractor;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;

public class ConsultasInvestigator extends Application {
    private static final String MDC_IP = "mdc.ip";
    private static final String MDC_UID_KEYWORD = "mdc.uid.keyword";
    private static final String USER_NAME_QUERY = "http.user-name.keyword";
    private static final String CLIENT_IP_QUERY = "clientip.keyword";
    private static final String ACESSOS_SISTEMA_QUERY = "dtpsistema.keyword";
    private static final ImmutableMap<String, String> REPLACEMENT_MAP = ImmutableMap.<String, String>builder()
            .put(USER_NAME_QUERY, MDC_UID_KEYWORD).put(CLIENT_IP_QUERY, MDC_IP).build();
    @FXML
    private TextField resultsFilter;
    @FXML
    private Text filterText;
    @FXML
    private ComboBox<String> fields;
    @FXML
    private ComboBox<Integer> days;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TableView<Map<String, String>> ipsTable;
    @FXML
    private TableView<Map<String, String>> acessosSistemaTable;
    @FXML
    private TableView<Map<String, String>> consultasTable;
    @FXML
    private TableView<Map<String, String>> pathsTable;

    private List<QueryObjects> queryList = new ArrayList<>();
    private ObservableMap<String, String> filter = FXCollections.observableHashMap();
    @FXML
    private ComboBox<String> ipCombo;
    @FXML
    private ComboBox<String> uidCombo;
    @FXML
    private LineChart<Number, Number> timelineUsuarios;

    @FXML
    private LineChart<Number, Number> timelineIPs;

    @SuppressWarnings({ "static-method" })
    public void copyContent(KeyEvent ev) {
        TableView<?> target = (TableView<?>) ev.getSource();
        SimpleTableViewBuilder.copyContent(target, ev);
    }

    public void initialize() {
        configureTable(USER_NAME_QUERY, "geridQuery.json", ipsTable, "key", "value");
        String count = "doc_count";
        configureTable(CLIENT_IP_QUERY, "consultasQuery.json", consultasTable, "key", count);
        configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, "key", count);
        configureTable(ACESSOS_SISTEMA_QUERY, "requestedPath.json", pathsTable, "key", count).setGroup("[^\\/\\?\\d]+");
        configureTimeline(MDC_UID_KEYWORD, TimelionApi.TIMELINE_USERS, timelineUsuarios, uidCombo);
        configureTimeline(CLIENT_IP_QUERY, TimelionApi.TIMELINE_IPS, timelineIPs, ipCombo);
        filterText.textProperty().bind(Bindings.createStringBinding(
                () -> filter.entrySet().stream().map(Objects::toString).collect(Collectors.joining("\n")), filter));
    }

    public void onActionClear() {
        resultsFilter.setText("");
        filter.clear();
        onActionKibanaScan();
    }

    public void onActionFillIP() {
        RunnableEx.runNewThread(() -> {
            ObservableList<Map<String, String>> items = consultasTable.getItems();
            if (items.isEmpty()) {
                return;
            }
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(0));
            WhoIsScanner whoIsScanner = new WhoIsScanner();
            for (Map<String, String> map : items) {
                Map<String, String> ipInformation = whoIsScanner.getIpInformation(map.get("key"));
                map.put("Rede", ipInformation.getOrDefault("network", ipInformation.get("CanonicalHostName")));
                map.put("Owner", ipInformation.getOrDefault("as_owner", ipInformation.get("asname")));
                map.put("Country", ipInformation.getOrDefault("country", ipInformation.get("ascountry")));
                if (progressIndicator.getProgress() == 0) {
                    RunnableEx.runInPlatform(() -> EthicalHackApp.addColumns(consultasTable, map.keySet()));
                }
                RunnableEx.runInPlatform(
                        () -> progressIndicator.setProgress(progressIndicator.getProgress() + 1. / items.size()));
            }
            RunnableEx.runInPlatform(() -> EthicalHackApp.addColumns(consultasTable, items.get(0).keySet()));
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(1));
        });
    }

    public void onActionKibanaScan() {
        RunnableEx.runNewThread(() -> {
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(0));
            for (QueryObjects queryObjects : queryList) {
                if (queryObjects.getLineChart() != null) {
                    ObservableList<Series<Number, Number>> data = queryObjects.getSeries();
                    RunnableEx.runInPlatformSync(() -> {
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
                } else {
                    RunnableEx.runInPlatform(() -> queryObjects.getItems().clear());
                    Map<String, String> nsInformation =
                            KibanaApi.makeKibanaSearch(ResourceFXUtils.toFile("kibana/" + queryObjects.getQueryFile()),
                                    days.getSelectionModel().getSelectedItem(), filter, queryObjects.getParams());
                    RunnableEx.runInPlatform(() -> {
                        List<Map<String, String>> remap = KibanaApi.remap(nsInformation, queryObjects.getGroup());
                        if (queryObjects.getTable().getColumns().isEmpty()) {
                            EthicalHackApp.addColumns(queryObjects.getTable(), remap.stream()
                                    .flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList()));
                        }
                        queryObjects.getItems().addAll(remap);
                    });
                }
                RunnableEx.runInPlatform(
                        () -> progressIndicator.setProgress(progressIndicator.getProgress() + 1. / queryList.size()));
            }
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(1));
        });
    }

    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> collect =
                queryList.stream().collect(Collectors.toMap(QueryObjects::getQueryFile, QueryObjects::getItems));
        List<String> collect2 =
                queryList.stream().filter(e -> e.getTable() != null).flatMap(e -> e.getTable().getColumns().stream())
                        .map(TableColumn<Map<String, String>, ?>::getText).distinct().collect(Collectors.toList());
        for (String text : collect2) {
            mapa.put(text, t -> t.getOrDefault(text, ""));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/investigation.xlsx");
        ExcelService.getExcel(collect, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    @Override
    public void start(final Stage primaryStage) {
        final int width = 600;
        CommonsFX.loadFXML("Consultas Investigator", "ConsultasInvestigator.fxml", this, primaryStage, width, width);
    }

    private List<String> addFields(String queryFile) {
        return SupplierEx.get(() -> {
            String content = KibanaApi.getContent(ResourceFXUtils.toFile("kibana/" + queryFile), "", "1", "1");
            Map<String, String> makeMapFromJsonFile = JsonExtractor.makeMapFromJsonFile(content, "field");
            List<String> collect = makeMapFromJsonFile.values().stream().flatMap(e -> Stream.of(e.split("\n")))
                    .distinct().filter(s -> !fields.getItems().contains(s)).collect(Collectors.toList());
            fields.getItems().addAll(collect);
            return makeMapFromJsonFile.values().stream().collect(Collectors.toList());
        });
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, ipsTable2, params);
        ObservableList<Map<String, String>> ipItems2 = fieldObjects.getItems();
        queryList.add(fieldObjects);
        addFields(queryFile);
        final int columnWidth = 120;
        ipsTable2.prefWidthProperty()
                .bind(Bindings.selectDouble(ipsTable2.parentProperty(), "width").add(-columnWidth));
        ipsTable2.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2.filtered(e -> true)));
        ipsTable2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(ipsTable2).copiable().onDoubleClick(e -> {
            filter.put(userNameQuery, e.values().stream().findFirst().orElse("key"));
            onActionKibanaScan();
        });
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
        String key = REPLACEMENT_MAP.asMultimap().inverse().get(field).stream().findFirst().orElse(field);
        combo.getSelectionModel().selectedItemProperty().addListener((ob, old, val) -> {
            if (StringUtils.isBlank(val)) {
                filter.remove(key);
            } else {
                filter.put(key, val);
            }
            lineChart.getYAxis().setAutoRanging(false);
            filtered.setPredicate(e -> StringUtils.isBlank(val) || Objects.equals(e.getName(), val));
        });
        return fieldObjects;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
