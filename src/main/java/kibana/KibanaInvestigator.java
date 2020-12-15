package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.io.File;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ml.graph.DataframeExplorer;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.RunnableEx;

public class KibanaInvestigator extends Application {
    @FXML
    private TextField resultsFilter;
    @FXML
    private ListView<String> filterList;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ComboBox<Integer> days;

    @FXML
    private TableView<Map<String, String>> commonTable;
    private ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

    public void initialize() {
        final int columnWidth = 120;
        commonTable.prefWidthProperty()
                .bind(Bindings.selectDouble(commonTable.parentProperty(), "width").add(-columnWidth));
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(commonTable).copiable().savable()
                .onSortClicked((c, a) -> QuickSortML.sortMapList(items, c, a));
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable()
                .pasteable(s -> StringSigaUtils.getMatches(s, "(\\d+\\.\\d+\\.\\d+\\.\\d+)"));
    }

    public void onActionKibanaScan() {
        items.clear();
        CommonsFX.update(progressIndicator.progressProperty(), 0);
        ObservableList<String> items2 = filterList.getItems();
        DoubleExpression totalProgress = new SimpleDoubleProperty(0);
        for (String ip : items2) {
            SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
            RunnableEx.runNewThread(
                    () -> KibanaApi.kibanaFullScan(ip, days.getSelectionModel().getSelectedItem(), progress),
                    ns -> CommonsFX.runInPlatform(() -> {
                        if (commonTable.getColumns().isEmpty()) {
                            SimpleTableViewBuilder.addColumns(commonTable, ns.keySet());
                        }
                        items.add(ns);
                        if (items.size() == items2.size()) {
                            CommonsFX.update(progressIndicator.progressProperty(), 1);
                        }

                    }));
            totalProgress = totalProgress.add(progress);
        }
        CommonsFX.bind(totalProgress.divide(items2.size()), progressIndicator.progressProperty());
    }

    public void onOpenDataframe() {
        RunnableEx.run(() -> {
            TableView<Map<String, String>> table = commonTable;
            File ev = ResourceFXUtils.getOutFile("csv/" + table.getId() + ".csv");
            CSVUtils.saveToFile(table, ev);
            new SimpleDialogBuilder().bindWindow(commonTable).show(DataframeExplorer.class).addStats(ev);
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        final int width = 600;
        CommonsFX.loadFXML("Kibana Investigator", "KibanaInvestigator.fxml", this, primaryStage, width, width);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
