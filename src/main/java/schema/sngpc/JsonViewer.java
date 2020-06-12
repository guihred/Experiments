
package schema.sngpc;

import static schema.sngpc.JsonExtractor.newMap;
import static schema.sngpc.JsonExtractor.readJsonFile;
import static simplebuilder.SimpleButtonBuilder.newButton;

import extract.ExcelService;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
//import org.w3c.dom.Node;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import simplebuilder.StageHelper;
import utils.FunctionEx;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;

public class JsonViewer extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Json Viewer");
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

    private static Parent createSplitTreeListDemoNode() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        TableView<Map<String, String>> sideTable =
                new SimpleTableViewBuilder<Map<String, String>>().items(list).build();
        TreeView<Map<String, String>> tree = new SimpleTreeViewBuilder<Map<String, String>>().root(newMap("Root", null))
                .onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();

        SimpleObjectProperty<File> fileProp = new SimpleObjectProperty<>();
        Path firstPathByExtension = ResourceFXUtils.getFirstPathByExtension(ResourceFXUtils.getOutFile(), ".json");
        if (firstPathByExtension != null) {
            fileProp.set(firstPathByExtension.toFile());
            readJsonFile(tree, firstPathByExtension.toFile());
        }
        Button importJsonButton = StageHelper.chooseFile("Import Json", "Import Json", newFile -> {
            readJsonFile(tree, newFile);
            fileProp.set(newFile);
        }, "Json", "*.json");
        Button exportExcel = newButton("Export excel", e -> exportToExcel(tree, fileProp.get()));
        SplitPane splitPane = new SplitPane(tree, sideTable);
        VBox vBox = new VBox(new HBox(importJsonButton, exportExcel), splitPane);
        tree.prefHeightProperty().bind(vBox.heightProperty());
        sideTable.prefHeightProperty().bind(vBox.heightProperty());
        return vBox;
    }

    private static void exportToExcel(TreeView<Map<String, String>> tree, File file) {
        TreeItem<Map<String, String>> selectedItem = tree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        ObservableList<TreeItem<Map<String, String>>> lista = selectedItem.getChildren();
        Map<String, FunctionEx<TreeItem<Map<String, String>>, Object>> mapa = new HashMap<>();
        int orElse = lista.stream().mapToInt(i -> i.getChildren().size()).max().orElse(0);
        for (int j = 0; j < orElse; j++) {
            int k = j;
            mapa.put("" + j, t -> getValue(t, k));
        }

        File outFile = ResourceFXUtils.getOutFile(file.getName().replaceAll(".json", ".xlsx"));
        ExcelService.getExcel(lista, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    private static Object getValue(TreeItem<Map<String, String>> i, int j) {
        ObservableList<TreeItem<Map<String, String>>> children = i.getChildren();
        if (j < children.size()) {
            return children.get(j).getValue().values().stream().collect(Collectors.joining("\n"));
        }

        return FunctionEx.mapIf(i.getValue(), m -> m.values().toString(), "");
    }

    private static void onSelectTreeItem(ObservableList<Map<String, String>> list,
            TableView<Map<String, String>> sideTable, TreeItem<Map<String, String>> newValue) {
        list.clear();
        sideTable.getColumns().clear();
        if (newValue != null && newValue.isLeaf()) {
            addColumns(sideTable, newValue.getValue().keySet());
            list.add(newValue.getValue());
            splitList(list, newValue.getValue());
            return;
        }

        if (newValue == null || !newValue.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf)) {
            return;
        }

        Set<String> valueKeySet = newValue.getValue().keySet();
        addColumns(sideTable, valueKeySet);
        Map<String, String> newItem = new HashMap<>();
        newItem.putAll(newValue.getValue());
        list.add(newItem);
        List<String> keySet = newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                .flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toList());
        if (keySet.size() - 1 == newValue.getChildren().size()) {
            keySet.addAll(newValue.getValue().keySet());
            sideTable.getColumns().clear();
            addColumns(sideTable, keySet);
            newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue).forEach(newItem::putAll);
        }
        if (valueKeySet.isEmpty()) {
            list.clear();
            addColumns(sideTable, keySet);
            List<Map<String, String>> collect = newValue.getChildren().stream()
                    .map(TreeItem<Map<String, String>>::getValue).collect(Collectors.toList());
            list.addAll(collect);
        }


    }

    private static boolean splitList(ObservableList<Map<String, String>> list, Map<String, String> newItem) {
        long count =
                newItem.values().stream().mapToInt(e -> e.split("\n").length).filter(i -> i > 1).distinct().count();
        if (count != 1) {
            return false;
        }
        Set<Entry<String, String>> entrySet = newItem.entrySet();
        list.clear();
        for (Entry<String, String> entry : entrySet) {
            List<Map<String, String>> collect = Stream.of(entry.getValue().split("\n"))
                    .map(e -> newMap(entry.getKey(), e)).collect(Collectors.toList());
            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> map = list.get(i);
                    map.putAll(collect.get(i));
                }
                continue;
            }
            list.addAll(collect);
        }
        return true;
    }
}
