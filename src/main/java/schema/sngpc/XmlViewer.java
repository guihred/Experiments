
package schema.sngpc;

import static schema.sngpc.XMLExtractor.newMap;
import static schema.sngpc.XMLExtractor.readXMLFile;
import static simplebuilder.SimpleButtonBuilder.newButton;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        for (String key : keySet) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(tableView.widthProperty().divide(keySet.size()).add(-5));
            tableView.getColumns().add(column);
        }
    }

    private static void addHeader(List<Map<String, String>> finalList, Map<String, String> collect) {
        if (!collect.keySet().stream().allMatch(e -> e.matches("\\w+\\d+"))) {
            Map<String, String> collect3 = collect.keySet().stream().collect(toLinkedHashMap2());
            if (!finalList.contains(collect3)) {
                finalList.add(collect3);
            }
        }
    }

    private static boolean anyChildLeaf(TreeItem<Map<String, String>> e) {
        return e.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf);
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

        List<Map<String, String>> finalList = getFinalList(selectedItem);
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        int orElse = finalList.stream().mapToInt(Map<String, String>::size).max().orElse(0);
        for (int j = 0; j < orElse; j++) {
            int k = j;
            mapa.put("" + j, t -> getValue(t, k));
        }

        File outFile = ResourceFXUtils.getOutFile(file.getName().replaceAll(".xml", ".xlsx"));
        ExcelService.getExcel(finalList, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    private static List<Map<String, String>> getFinalList(TreeItem<Map<String, String>> selectedItem) {
        ObservableList<TreeItem<Map<String, String>>> lista = FXCollections.observableArrayList();
        lista.add(selectedItem);
        List<Map<String, String>> finalList = new ArrayList<>();
        while (!lista.isEmpty()) {
            TreeItem<Map<String, String>> treeItem = lista.remove(0);
            if (anyChildLeaf(treeItem)) {
                Map<String, String> collect = toMap(treeItem);
                addHeader(finalList, collect);
                finalList.add(collect);
            } else if (treeItem.isLeaf()) {
                finalList.add(treeItem.getValue());
            } else {
                lista.addAll(0, treeItem.getChildren());
            }
        }
        finalList.removeIf(Map<String, String>::isEmpty);
        return finalList;
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

    private static boolean isArrayType(List<Set<Map.Entry<String, String>>> entryList) {
        return entryList.stream().flatMap(Set<Entry<String, String>>::stream).map(Entry<String, String>::getKey)
                .distinct().count() < entryList.size();
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
        }
    }

    private static Collector<Map.Entry<String, String>, ?, Map<String, String>> toLinkedHashMap() {
        return Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue,
                (a, b) -> a + "\n" + b, LinkedHashMap::new);
    }

    private static Collector<String, ?, Map<String, String>> toLinkedHashMap2() {
        return Collectors.toMap(e -> e, e -> e, (a, b) -> a + "\n" + b, LinkedHashMap::new);
    }

    private static Map<String, String> toMap(TreeItem<Map<String, String>> treeItem) {
        List<Set<Map.Entry<String, String>>> entryList = treeItem.getChildren().stream().map(e -> {
            if (e.isLeaf()) {
                return e.getValue().entrySet();
            }
            return Stream
                    .concat(e.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                            .flatMap(n -> n.entrySet().stream()), e.getValue().entrySet().stream())
                    .collect(Collectors.toSet());
        }).collect(Collectors.toList());
        if (isArrayType(entryList)) {
            // Is a Array type
            return entryList.stream().flatMap(Set<Entry<String, String>>::stream)
                    .collect(Collectors.groupingBy(Entry<String, String>::getKey,
                            Collectors.mapping(Entry<String, String>::getValue, Collectors.toList())))
                    .entrySet().stream()
                    .flatMap(en -> IntStream.range(0, en.getValue().size())
                            .mapToObj(j -> XMLExtractor.newEntry(en.getKey() + j, en.getValue().get(j))))
                    .collect(toLinkedHashMap());
        }
        return treeItem.getChildren().stream().flatMap(e -> Stream.concat(Stream.of(e), e.getChildren().stream()))
                .flatMap(e -> e.getValue().entrySet().stream()).collect(toLinkedHashMap());
    }
}
