package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import extract.ExcelService;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;

public class ConsultasInvestigator extends Application {
    private static final String USER_NAME_QUERY = "\"http.user-name.keyword\": {\n\"query\": \"%s\"\n}";
    private static final int WIDTH = 600;
    @FXML
    private TextField resultsFilter;
    @FXML
    private TextField networkAddress;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TableView<Map<String, String>> commonTable;
    private ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

    public void copyContent(KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            ObservableList<Map<String, String>> selectedItems = commonTable.getSelectionModel().getSelectedItems();
            String collect = selectedItems.stream().map(Map<String, String>::values)
                    .map(l -> l.stream().collect(Collectors.joining("\t"))).collect(Collectors.joining("\n"));
            Map<DataFormat, Object> content = FXCollections.observableHashMap();
            content.put(DataFormat.PLAIN_TEXT, collect);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    public void initialize() {
        final int columnWidth = 120;
        commonTable.prefWidthProperty()
                .bind(Bindings.selectDouble(commonTable.parentProperty(), "width").add(-columnWidth));
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.onDoubleClick(commonTable, e -> {
            networkAddress.setText(String.format(USER_NAME_QUERY, e.get("key")));
            onActionKibanaScan();
        });
    }

    public void onActionClear() {
        networkAddress.setText("");
        onActionKibanaScan();
    }

    public void onActionKibanaScan() {
        items.clear();
        RunnableEx.runNewThread(() -> {
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(0));
            Map<String, String> nsInformation =
                    KibanaApi.makeKibanaSearch("kibana/geridQuery.json", networkAddress.getText(), "key", "value");
            RunnableEx.runInPlatform(() -> {
                progressIndicator.setProgress(progressIndicator.getProgress() + 1);
                if (commonTable.getColumns().isEmpty()) {
                    EthicalHackApp.addColumns(commonTable, nsInformation.keySet());
                }
                List<Map<String, String>> remap = KibanaApi.remap(nsInformation);
                items.addAll(remap);
            });
            RunnableEx.runInPlatform(() -> progressIndicator.setProgress(1));
        });
    }
    public void onExportExcel() {
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        ObservableList<TableColumn<Map<String, String>, ?>> columns = commonTable.getColumns();
        for (TableColumn<Map<String, String>, ?> tableColumn : columns) {
            String text = tableColumn.getText();
            mapa.put(text, t -> t.getOrDefault(text, "-"));
        }
        File outFile = ResourceFXUtils.getOutFile("xlsx/kibana.xlsx");
        ExcelService.getExcel(items, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    @Override
    public void start(final Stage primaryStage) {
        CommonsFX.loadFXML("Consultas Investigator", "ConsultasInvestigator.fxml", this, primaryStage, WIDTH, WIDTH);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
