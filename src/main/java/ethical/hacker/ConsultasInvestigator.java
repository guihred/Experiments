package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;

public class ConsultasInvestigator extends Application {
    private static final String USER_NAME_QUERY = "\"http.user-name.keyword\": {\n\"query\": \"%s\"\n}";
    private static final String CONSULTAS_QUERY =
            "{\n\"match_phrase\": {\n\"mdc.uid.keyword\": {\n\"query\": \"%s\"\n}\n}\n},";
    private static final int WIDTH = 600;
    @FXML
    private TextField resultsFilter;
    @FXML
    private TextField ipsQuery;
    @FXML
    private TextField consultasQuery;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TableView<Map<String, String>> ipsTable;
    @FXML
    private TableView<Map<String, String>> consultasTable;
    private List<QueryObjects> list = new ArrayList<>();

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
        configureTable(CONSULTAS_QUERY, "consultasQuery.json", consultasTable, consultasQuery, "key",
                "doc_count");
    }

    public void onActionClear() {
        resultsFilter.setText("");
        list.forEach(e -> e.getQueryField().setText(""));
        onActionKibanaScan();
    }

    public void onActionKibanaScan() {
        RunnableEx.runNewThread(() -> {
            for (QueryObjects queryObjects : list) {
                RunnableEx.runInPlatform(() -> queryObjects.getItems().clear());
                RunnableEx.runInPlatform(() -> progressIndicator.setProgress(0));
                Map<String, String> nsInformation = KibanaApi.makeKibanaSearch("kibana/" + queryObjects.getQueryFile(),
                        queryObjects.getQueryField().getText(), queryObjects.getParams());
                RunnableEx.runInPlatform(() -> {
                    progressIndicator.setProgress(progressIndicator.getProgress() + 1. / list.size());
                    List<Map<String, String>> remap = queryObjects.getGroup() == 1 ? KibanaApi.remap(nsInformation)
                            : KibanaApi.remap(nsInformation, queryObjects.getGroup());
                    if (queryObjects.getTable().getColumns().isEmpty()) {
                        EthicalHackApp.addColumns(queryObjects.getTable(), remap.stream().findFirst()
                                .map(Map<String, String>::keySet).orElse(Collections.emptySet()));
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
                list.stream().collect(Collectors.toMap(QueryObjects::getQueryFile, QueryObjects::getItems));
        List<String> collect2 = list.stream().flatMap(e -> e.getTable().getColumns().stream()).map(e -> e.getText())
                .distinct().collect(Collectors.toList());
        for (String text : collect2) {
            mapa.put(text, t -> t.getOrDefault(text, "-"));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/investigation.xlsx");
        ExcelService.getExcel(collect, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    @Override
    public void start(final Stage primaryStage) {
        CommonsFX.loadFXML("Consultas Investigator", "ConsultasInvestigator.fxml", this, primaryStage, WIDTH, WIDTH);
    }

    private QueryObjects configureTable(String userNameQuery, String queryFile,
            TableView<Map<String, String>> ipsTable2, TextField networkAddress2, String... params) {
        QueryObjects fieldObjects = new QueryObjects(userNameQuery, queryFile, networkAddress2, ipsTable2, params);
        ObservableList<Map<String, String>> ipItems2 = fieldObjects.getItems();
        list.add(fieldObjects);
        final int columnWidth = 120;
        ipsTable2.prefWidthProperty()
                .bind(Bindings.selectDouble(ipsTable2.parentProperty(), "width").add(-columnWidth));
        ipsTable2.setItems(CommonsFX.newFastFilter(resultsFilter, ipItems2.filtered(e -> true)));
        ipsTable2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.onDoubleClick(ipsTable2, e -> {
            networkAddress2.setText(String.format(userNameQuery, e.values().stream().findFirst().orElse("key")));
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
        private int group = 1;

        private final TextField queryField;

        private final TableView<Map<String, String>> table;

        private final ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

        public QueryObjects(String query, String queryFile, TextField queryField, TableView<Map<String, String>> table,
                String[] params) {
            this.query = query;
            this.queryFile = queryFile;
            this.queryField = queryField;
            this.table = table;
            this.params = params;
        }

        public int getGroup() {
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

        public TextField getQueryField() {
            return queryField;
        }

        public String getQueryFile() {
            return queryFile;
        }

        public TableView<Map<String, String>> getTable() {
            return table;
        }

        public void setGroup(int group) {
            this.group = group;
        }
    }

}
