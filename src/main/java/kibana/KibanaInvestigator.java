package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import extract.ExcelService;
import extract.QuickSortML;
import java.io.File;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
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
        RunnableEx.runNewThread(() -> {
            CommonsFX.update(progressIndicator.progressProperty(), 0);
            ObservableList<String> items2 = filterList.getItems();
            for (int i = 0; i < items2.size(); i++) {
                String ip = items2.get(i);
                Map<String, String> nsInformation =
                        KibanaApi.kibanaFullScan(ip, days.getSelectionModel().getSelectedItem());
                CommonsFX.update(progressIndicator.progressProperty(), (i + 1.) / items2.size());
                CommonsFX.runInPlatform(() -> {
                    if (commonTable.getColumns().isEmpty()) {
                        SimpleTableViewBuilder.addColumns(commonTable, nsInformation.keySet());
                    }
                    items.add(nsInformation);
                });
            }
            CommonsFX.update(progressIndicator.progressProperty(), 1);

        });
    }

    public void onActionKibanaScanParallel() {
        items.clear();
        RunnableEx.runNewThread(() -> {
            CommonsFX.update(progressIndicator.progressProperty(), 0);
            ObservableList<String> items2 = filterList.getItems();
            items2.parallelStream().map(s -> {
                String ip = s;
                Map<String, String> nsInformation =
                        KibanaApi.kibanaFullScan(ip, days.getSelectionModel().getSelectedItem());
                CommonsFX.addProgress(progressIndicator.progressProperty(), 1. / items2.size());
                return nsInformation;
            }).peek(ns -> CommonsFX.runInPlatform(() -> items.add(ns))).forEach(ns -> CommonsFX.runInPlatform(() -> {
                if (commonTable.getColumns().isEmpty()) {
                    SimpleTableViewBuilder.addColumns(commonTable, ns.keySet());
                }
            }));
            CommonsFX.update(progressIndicator.progressProperty(), 1);

        });
    }

    public void onExportExcel() {
        File outFile = ResourceFXUtils.getOutFile("xlsx/kibana.xlsx");
        ExcelService.getExcel(items, outFile);
        ImageFXUtils.openInDesktop(outFile);
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
