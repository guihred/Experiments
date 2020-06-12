
package schema.sngpc;

import static schema.sngpc.JsonExtractor.newMap;
import static schema.sngpc.JsonExtractor.readJsonFile;
import static simplebuilder.SimpleButtonBuilder.newButton;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
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
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import simplebuilder.StageHelper;
import utils.FunctionEx;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class JsonViewer extends Application {
    private ObjectProperty<File> fileProp = new SimpleObjectProperty<>();
    private TreeView<Map<String, String>> tree;

    public File getFile() {
        return fileProp.get();
    }

    public void setFile(File fileProp) {
        this.fileProp.set(fileProp);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Json Viewer");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.show();
        fileProp.addListener((ob, old, val) -> RunnableEx.runInPlatform(() -> readJsonFile(tree, val)));
    }

    private Parent createSplitTreeListDemoNode() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        TableView<Map<String, String>> sideTable =
                new SimpleTableViewBuilder<Map<String, String>>().items(list).build();
        tree = new SimpleTreeViewBuilder<Map<String, String>>().root(newMap("Root", null))
                .onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();
        Button importJsonButton = StageHelper.chooseFile("Import Json", "Import Json", fileProp::set, "Json", "*.json");
        Button exportExcel = newButton("Export excel", e -> exportToExcel(sideTable, fileProp.get()));
        SplitPane splitPane = new SplitPane(tree, sideTable);
        VBox vBox = new VBox(new HBox(importJsonButton, exportExcel), splitPane);
        vBox.setMinSize(400, 400);
        tree.prefHeightProperty().bind(vBox.heightProperty());
        sideTable.prefHeightProperty().bind(vBox.heightProperty());
        return vBox;
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

    private static void exportToExcel(TableView<Map<String, String>> tree, File file) {

        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new HashMap<>();
        List<Map<String, String>> collect = tree.getItems();
        List<String> collect2 =
                collect.stream().flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList());
        for (String key : collect2) {
            mapa.put(key, t -> t.get(key));
        }

        File outFile = ResourceFXUtils.getOutFile(file.getName().replaceAll(".json", ".xlsx"));
        ExcelService.getExcel(collect, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
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

        if (newValue == null) {
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
