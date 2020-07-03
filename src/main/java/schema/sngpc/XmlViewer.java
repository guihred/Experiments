
package schema.sngpc;

import static schema.sngpc.XMLExtractor.exportToExcel;
import static schema.sngpc.XMLExtractor.newMap;
import static schema.sngpc.XMLExtractor.onSelectTreeItem;
import static schema.sngpc.XMLExtractor.readXMLFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.w3c.dom.Node;
import simplebuilder.SimpleTreeViewBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ResourceFXUtils;

public class XmlViewer extends Application {
    @FXML
    private TableView<Map<String, String>> sideTable;
    @FXML
    private TreeView<Map<String, String>> tree;

    private final SimpleObjectProperty<File> fileProp = new SimpleObjectProperty<>();

    private final Map<Node, TreeItem<Map<String, String>>> allItems = new HashMap<>();

    public void initialize() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        sideTable.setItems(list);
        tree.setRoot(new TreeItem<>(newMap("Root", null)));
        SimpleTreeViewBuilder.onSelect(tree, newValue -> onSelectTreeItem(list, sideTable, newValue));
        fileProp.addListener((ob, old, val) -> {
            if (val != null) {
                readXMLFile(tree, allItems, val);
            }
        });
        File file = ResourceFXUtils.getFirstPathByExtension(new File("").getAbsoluteFile(), ".xml").toFile();
        fileProp.set(file);
    }

    public void onActionExportExcel() {
        exportToExcel(tree, fileProp.get());
    }

    public void onActionImportXML(ActionEvent e) {
        StageHelper.fileAction("Import XML", fileProp::set, "Xml", "*.xml").handle(e);
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("XML Viewer", "XmlViewer.fxml", this, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
