package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import ethical.hacker.TracerouteScanner;
import extract.ExcelService;
import java.io.File;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

public class KibanaInvestigator extends Application {
    private static final int WIDTH = 600;
    @FXML
    private TextField resultsFilter;
    @FXML
    private TextField networkAddress;
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
        SimpleTableViewBuilder.of(commonTable).copiable().savable();
        networkAddress.setText(TracerouteScanner.IP_TO_SCAN);
    }

    public void onActionKibanaScan() {
        items.clear();
        RunnableEx.runNewThread(() -> {
            String text = networkAddress.getText();
            if (StringUtils.isNotBlank(text)) {
                String[] split = text.split("[,\n\t; ]+");
                CommonsFX.update(progressIndicator.progressProperty(), 0);
                for (int i = 0; i < split.length; i++) {
                    String ip = split[i];
                    Map<String, String> nsInformation =
                            KibanaApi.kibanaFullScan(ip, days.getSelectionModel().getSelectedItem());
                    CommonsFX.update(progressIndicator.progressProperty(), (i + 1.) / split.length);
                    CommonsFX.runInPlatform(() -> {
                        if (commonTable.getColumns().isEmpty()) {
                            SimpleTableViewBuilder.addColumns(commonTable, nsInformation.keySet());
                        }
                        items.add(nsInformation);
                    });
                }
                CommonsFX.update(progressIndicator.progressProperty(), 1);
            }
        });
    }

    public void onExportExcel() {
        File outFile = ResourceFXUtils.getOutFile("xlsx/kibana.xlsx");
        ExcelService.getExcel(items, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    @Override
    public void start(final Stage primaryStage) {
        CommonsFX.loadFXML("Kibana Investigator", "KibanaInvestigator.fxml", this, primaryStage, WIDTH, WIDTH);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
