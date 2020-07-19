package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import extract.ExcelService;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import schema.sngpc.JsonExtractor;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;

public class ConsultasInvestigator extends Application {
    private static final String USER_NAME_QUERY =
            "{\"match_phrase\": {\"http.user-name.keyword\": {\"query\": \"%s\"}}},";
    private static final String CONSULTAS_QUERY = "{\"match_phrase\": {\"clientip.keyword\": {\"query\": \"%s\"}}},";
    private static final String ACESSOS_SISTEMA_QUERY =
            "{\"match_phrase\": {\"dtpsistema.keyword\": {\"query\": \"%s\"}}},";
    private static final int WIDTH = 600;
    @FXML
    private TextField resultsFilter;
    @FXML
    private Text ipsQuery;
    @FXML
    private Text consultasQuery;
    @FXML
    private Text acessosSistemaQuery;
    @FXML
    private ComboBox<String> fields;
    @FXML
    private Text pathsQuery;
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

    @SuppressWarnings({ "static-method", "unchecked" })
    public void copyContent(KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            TableView<Map<String, String>> target = (TableView<Map<String, String>>) ev.getSource();

            ObservableList<Map<String, String>> selectedItems = target.getSelectionModel().getSelectedItems();
            String collect = selectedItems.stream().map(Map<String, String>::values)
                    .map(l -> l.stream().collect(Collectors.joining("\t"))).collect(Collectors.joining("\n"));
            Map<DataFormat, Object> content = FXCollections.observableHashMap();
            content.put(DataFormat.PLAIN_TEXT, collect);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    public void initialize() {
        configureTable(USER_NAME_QUERY, "geridQuery.json", ipsTable, ipsQuery, "key", "value");
        configureTable(CONSULTAS_QUERY, "consultasQuery.json", consultasTable, consultasQuery, "key", "doc_count")
        ;
        configureTable(ACESSOS_SISTEMA_QUERY, "acessosSistemaQuery.json", acessosSistemaTable, acessosSistemaQuery,
                "key", "doc_count");
        configureTable(ACESSOS_SISTEMA_QUERY, "requestedPath.json", pathsTable, pathsQuery, "key", "doc_count")
                .setGroup("[^\\/\\?\\d]+");
    }

    public void onActionClear() {
        resultsFilter.setText("");
        queryList.forEach(e -> e.getQueryField().setText(""));
        onActionKibanaScan();
    }

    public void onActionKibanaScan() {
        RunnableEx.runNewThread(() -> {
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(0));
            for (QueryObjects queryObjects : queryList) {
                RunnableEx.runInPlatform(() -> queryObjects.getItems().clear());
                Map<String, String> nsInformation = KibanaApi.makeKibanaSearch("kibana/" + queryObjects.getQueryFile(),
                        queryObjects.getQueryField().getText(), queryObjects.getParams());
                RunnableEx.runInPlatform(() -> {
                    progressIndicator.setProgress(progressIndicator.getProgress() + 1. / queryList.size());
                    List<Map<String, String>> remap = KibanaApi.remap(nsInformation, queryObjects.getGroup());
                    if (queryObjects.getTable().getColumns().isEmpty()) {
                        EthicalHackApp.addColumns(queryObjects.getTable(), remap.stream()
                                .flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList()));
                    }
                    queryObjects.getItems().addAll(remap);
                });
            }
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(1));
        });
    }

    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> collect =
                queryList.stream().collect(Collectors.toMap(QueryObjects::getQueryFile, QueryObjects::getItems));
        List<String> collect2 = queryList.stream().flatMap(e -> e.getTable().getColumns().stream())
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
        CommonsFX.loadFXML("Consultas Investigator", "ConsultasInvestigator.fxml", this, primaryStage, WIDTH, WIDTH);
    }

    private void addFields(String queryFile) {
        RunnableEx.run(() -> {
            String content = KibanaApi.getContent(ResourceFXUtils.toFile("kibana/" + queryFile), "", "1", "1");
            Map<String, String> makeMapFromJsonFile =
                    JsonExtractor.makeMapFromJsonFile(content, "field");
            List<String> collect = makeMapFromJsonFile.values().stream().flatMap(e -> Stream.of(e.split("\n")))
                    .distinct().filter(s -> !fields.getItems().contains(s)).collect(Collectors.toList());
            fields.getItems().addAll(collect);
        });
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, Text networkAddress2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, networkAddress2, ipsTable2, params);
        ObservableList<Map<String, String>> ipItems2 = fieldObjects.getItems();
        queryList.add(fieldObjects);
        addFields(queryFile);
        final int columnWidth = 120;
        ipsTable2.prefWidthProperty()
                .bind(Bindings.selectDouble(ipsTable2.parentProperty(), "width").add(-columnWidth));
        ipsTable2.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2.filtered(e -> true)));
        ipsTable2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        networkAddress2.setOnMouseReleased(e -> {
            networkAddress2.setText("");
            onActionKibanaScan();
        });
        SimpleTableViewBuilder.onDoubleClick(ipsTable2, e -> {
            String format = String.format(userNameQuery, e.values().stream().findFirst().orElse("key"));
            networkAddress2.setText(format);
            queryList.forEach(r -> r.getQueryField().setText(format));

            onActionKibanaScan();
        });
        return fieldObjects;
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class QueryObjects {
        private final String query;

        private final String queryFile;
        private final String[] params;
        private String group = "";

        private final Text queryField;

        private final TableView<Map<String, String>> table;

        private final ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

        public QueryObjects(String query, String queryFile, Text queryField, TableView<Map<String, String>> table,
                String[] params) {
            this.query = query;
            this.queryFile = queryFile;
            this.queryField = queryField;
            this.table = table;
            this.params = params;
        }

        public String getGroup() {
            return group;
        }

        public ObservableList<Map<String, String>> getItems() {
            return items;
        }

        public String[] getParams() {
            return params;
        }

        public String getQuery() {
            return query;
        }

        public Text getQueryField() {
            return queryField;
        }

        public String getQueryFile() {
            return queryFile;
        }

        public TableView<Map<String, String>> getTable() {
            return table;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

}
