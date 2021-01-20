
package fxml.utils;

import static fxml.utils.XMLExtractor.exportToExcel;
import static fxml.utils.XMLExtractor.newMap;
import static fxml.utils.XMLExtractor.onSelectTreeItem;
import static fxml.utils.XMLExtractor.readXMLFile;

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
import org.slf4j.Logger;
import org.w3c.dom.Node;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.FileTreeWalker;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class XmlViewer extends Application {
    private static final Logger LOG = HasLogging.log();
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
            RunnableEx.make(()->{
                if (val != null) {
                    readXMLFile(tree, allItems, val);
                }
            }, e -> {
                LOG.error("ERROR READING {} ", val, e);
                fileProp.set(old);
            }).run();
        });
        File file = FileTreeWalker.getFirstPathByExtension(new File("").getAbsoluteFile(), ".xml").toFile();
        fileProp.set(file);
    }

    public void onActionExportExcel() {
        exportToExcel(tree, fileProp.get());
    }

    public void onActionImportXML(ActionEvent e) {
        new FileChooserBuilder().title("Import XML").extensions("Xml/Html", "*.xml", "*.html")
                .onSelect(fileProp::set)
                .openFileAction(e);
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("XML Viewer", "XmlViewer.fxml", this, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
