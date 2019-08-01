
package schema.sngpc;

import static schema.sngpc.XMLExtractor.newMap;
import static schema.sngpc.XMLExtractor.readXMLFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.w3c.dom.Node;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;

public class SngpcViewer extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SNGPC Viewer");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode(primaryStage)));
        primaryStage.show();
    }

    private void addColumns(TableView<Map<String, String>> tableView, Collection<String> keySet) {

        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(tableView.widthProperty().divide(keySet.size()).add(-5));
            tableView.getColumns().add(column);
        });
    }

    private Parent createSplitTreeListDemoNode(Window ownerWindow) {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        TableView<Map<String, String>> sideTable = new SimpleTableViewBuilder<Map<String, String>>().items(list)
            .build();
        TreeView<Map<String, String>> tree = new SimpleTreeViewBuilder<Map<String, String>>()
            .root(newMap("Root", null)).onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();

        Map<Node, TreeItem<Map<String, String>>> allItems = new HashMap<>();

        File file = ResourceFXUtils.toFile("FL94_REL758_20181031061530.xml");
        readXMLFile(tree, allItems, file);

        Button importXMLButton = CommonsFX.newButton("Import XML", e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Xml", "*.xml"));
            File newFile = fileChooser.showOpenDialog(ownerWindow);
            if (newFile != null) {
                readXMLFile(tree, allItems, newFile);
            }
        });
        return new VBox(importXMLButton, new SplitPane(tree, sideTable));
    }

    private void onSelectTreeItem(ObservableList<Map<String, String>> list, TableView<Map<String, String>> sideTable,
        TreeItem<Map<String, String>> newValue) {
        list.clear();
        sideTable.getColumns().clear();
        if (newValue != null && newValue.isLeaf()) {
            addColumns(sideTable, newValue.getValue().keySet());
            list.add(newValue.getValue());
        } else if (newValue != null
            && newValue.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf)) {
            List<String> keySet = newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                .flatMap(m -> m.keySet().stream()).collect(Collectors.toList());
            keySet.addAll(newValue.getValue().keySet());
            addColumns(sideTable, keySet);
            Map<String, String> newItem = new HashMap<>();
            newItem.putAll(newValue.getValue());
            list.add(newItem);
            if (keySet.size() - 1 == newValue.getChildren().size()) {
                newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue).forEach(newItem::putAll);
            } else {
                newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue).forEach(list::add);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }






}
