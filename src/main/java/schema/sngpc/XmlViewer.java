
package schema.sngpc;

import static schema.sngpc.XMLExtractor.newMap;
import static schema.sngpc.XMLExtractor.readXMLFile;
import static simplebuilder.SimpleButtonBuilder.newButton;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.w3c.dom.Node;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import simplebuilder.StageHelper;
import utils.FunctionEx;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;

public class XmlViewer extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("XML Viewer");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addColumns(TableView<Map<String, String>> tableView, Collection<String> keySet) {

        keySet.forEach(key -> {
            final TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(tableView.widthProperty().divide(keySet.size()).add(-5));
            tableView.getColumns().add(column);
        });
    }

    private static boolean allChildrenLeaf(TreeItem<Map<String, String>> e) {
        return e.getChildren().stream().allMatch(b -> b.isLeaf());
    }

    private static Parent createSplitTreeListDemoNode() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        TableView<Map<String, String>> sideTable =
                new SimpleTableViewBuilder<Map<String, String>>().items(list).build();
        TreeView<Map<String, String>> tree = new SimpleTreeViewBuilder<Map<String, String>>().root(newMap("Root", null))
                .onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();

        Map<Node, TreeItem<Map<String, String>>> allItems = new HashMap<>();

        File file = ResourceFXUtils.toFile("FL94_REL758_20181031061530.xml");
        SimpleObjectProperty<File> fileProp = new SimpleObjectProperty<>(file);
        readXMLFile(tree, allItems, file);

        Button importXMLButton = StageHelper.chooseFile("Import XML", "Import XML", newFile -> {
            readXMLFile(tree, allItems, newFile);
            fileProp.set(newFile);
        }, "Xml", "*.xml");
        Button exportExcel = newButton("Export excel", e -> exportToExcel(tree, fileProp.get()));
        return new VBox(new HBox(importXMLButton, exportExcel), new SplitPane(tree, sideTable));
    }

    private static void exportToExcel(TreeView<Map<String, String>> tree, File file) {
        TreeItem<Map<String, String>> selectedItem = tree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        ObservableList<TreeItem<Map<String, String>>> lista = FXCollections.observableArrayList();
        lista.add(selectedItem);
        while (lista.stream().anyMatch(e -> !allChildrenLeaf(e))) {
            for (int i = 0; i < lista.size(); i++) {
                TreeItem<Map<String, String>> treeItem = lista.get(i);
                if (!allChildrenLeaf(treeItem)) {
                    lista.addAll(i, lista.remove(i).getChildren());
                    i--;
                }
            }
        }
        List<Map<String, String>> collect = lista.stream().map(e -> e.getValue()).collect(Collectors.toList());
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new HashMap<>();
        int orElse = lista.stream().mapToInt(i -> i.getValue().size()).max().orElse(0);
        for (int j = 0; j < orElse; j++) {
            int k = j;
            mapa.put("" + j, t -> getValue(t, k));
        }

        File outFile = ResourceFXUtils.getOutFile(file.getName().replaceAll(".xml", ".xlsx"));
        ExcelService.getExcel(collect, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    private static Object getValue(Map<String, String> i, int j) {
        Collection<String> values = i.values();
        int k = 0;
        for (String string : values) {
            if (j == k) {
                return string;
            }
            k++;
        }

        return "";
    }

    private static void onSelectTreeItem(ObservableList<Map<String, String>> list,
            TableView<Map<String, String>> sideTable, TreeItem<Map<String, String>> newValue) {
        list.clear();
        sideTable.getColumns().clear();
        if (newValue == null) {
            return;
        }
        if (newValue.isLeaf()) {
            addColumns(sideTable, newValue.getValue().keySet());
            list.add(newValue.getValue());
            return;
        }
        if (newValue.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf)) {
            addColumns(sideTable, newValue.getValue().keySet());
            Map<String, String> newItem = new HashMap<>();
            newItem.putAll(newValue.getValue());
            list.add(newItem);
            if (newValue.getValue().values().stream().allMatch(Objects::isNull)) {
                newItem.clear();
                sideTable.getColumns().clear();
            }

            List<String> keySet = newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                    .flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toList());
            if (newItem.isEmpty()) {
                keySet.addAll(newItem.keySet());
                sideTable.getColumns().clear();
                addColumns(sideTable, keySet);
                List<Map<String, String>> collect = newValue.getChildren().stream()
                        .map(TreeItem<Map<String, String>>::getValue).collect(Collectors.toList());
                for (Map<String, String> t : collect) {
                    if (newItem.keySet().stream().anyMatch(t::containsKey)) {
                        newItem = new SimpleMap();
                        list.add(newItem);
                    }
                    newItem.putAll(t);
                }
            }
            return;
        }
    }
}
