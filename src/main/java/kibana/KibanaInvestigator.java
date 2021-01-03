package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
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
                .pasteable(s -> StringSigaUtils.getMatches(s, "(\\d+\\.\\d+\\.\\d+\\.\\d+/*\\d*)"));
    }

    public void onActionKibanaScan() {
        items.clear();
        CommonsFX.update(progressIndicator.progressProperty(), 0);
        ObservableList<String> items2 = filterList.getItems();
        List<SimpleDoubleProperty> progresses = IntStream.range(0, items2.size())
                .mapToObj(i -> new SimpleDoubleProperty(0)).collect(Collectors.toList());
        DoubleProperty totalProgress = new SimpleDoubleProperty(0);
        totalProgress.addListener(
                (ob, old, val) -> progressIndicator.progressProperty().setValue(val.doubleValue() / items2.size()));
        Iterator<SimpleDoubleProperty> iterator = progresses.iterator();
        for (String ip : items2) {
            SimpleDoubleProperty progress = iterator.next();
            progress.addListener(
                    ob -> totalProgress.set(progresses.stream().mapToDouble(DoubleProperty::get).sum()));
            RunnableEx.runNewThread(
                    () -> KibanaApi.kibanaFullScan(ip, days.getSelectionModel().getSelectedItem(), progress),
                    ns -> CommonsFX.runInPlatform(() -> addToTable(items2, ns)));
        }
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

    private void addToTable(ObservableList<String> items2, Map<String, String> ns) {
        if (commonTable.getColumns().isEmpty()) {
            SimpleTableViewBuilder.addColumns(commonTable, ns.keySet());
        }
        items.add(ns);
        CommonsFX.update(progressIndicator.progressProperty(),
                Math.max(progressIndicator.getProgress(), items.size() / (double) items2.size()));
        SimpleTableViewBuilder.autoColumnsWidth(commonTable);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
