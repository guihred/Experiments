
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import simplebuilder.StageHelper;
import utils.FunctionEx;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class JsonViewer extends Application {
    private static final int MAX_COLUMNS = 6;
    private ObjectProperty<File> fileProp = new SimpleObjectProperty<>();
    private ObservableList<File> files = FXCollections.observableArrayList();
    private TreeView<Map<String, String>> tree;
    private List<String> lastSelected = new ArrayList<>();

    public void addFile(File... filesToAdd) {
        RunnableEx.runInPlatform(() -> {
            for (File file : filesToAdd) {
                if (!files.contains(file)) {
                    files.add(file);
                }
            }
            if (filesToAdd.length > 0 && fileProp.get() == null) {
                fileProp.set(filesToAdd[0]);
            }
        });
    }

    public void clear() {
        files.clear();
    }

    public ObservableList<File> getFiles() {
        return files;
    }

    public void setFile(File filesToAdd) {
        fileProp.set(filesToAdd);
    }

    public void setLast() {
        if (!files.isEmpty()) {
            fileProp.set(files.get(files.size() - 1));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Json Viewer");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.show();
    }

    private Parent createSplitTreeListDemoNode() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        TableView<Map<String, String>> sideTable = new SimpleTableViewBuilder<Map<String, String>>().items(list)
                .selectionMode(SelectionMode.MULTIPLE).build();
        tree = new SimpleTreeViewBuilder<Map<String, String>>().root(newMap("Root", null))
                .onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();
        sideTable.setOnKeyPressed(ev -> copyContent(sideTable, ev));

        Button importJsonButton = StageHelper.chooseFile("Import Json", "Import Json", fileProp::set, "Json", "*.json");
        Button exportExcel = newButton("Export excel", e -> exportToExcel(sideTable, fileProp.get()));
        SplitPane splitPane = new SplitPane(tree, sideTable);
        SimpleComboBoxBuilder<File> onChange = new SimpleComboBoxBuilder<File>().items(files).converter(File::getName)
                .onChange((old, val) -> fileProp.set(val));
        fileProp.addListener((ob, old, val) -> RunnableEx.runInPlatform(() -> {
            if (val != null) {
                List<Integer> arrayList = getSelectionOrder();
                readJsonFile(tree, val);
                selectSame(arrayList);
                if (!files.contains(val)) {
                    files.add(val);
                }
                onChange.select(val);
            }
        }));
        VBox vBox = new VBox(new HBox(importJsonButton, exportExcel, onChange.build()), splitPane);
        vBox.setMinSize(400, 400);
        tree.prefHeightProperty().bind(vBox.heightProperty());
        sideTable.prefHeightProperty().bind(vBox.heightProperty());
        return vBox;
    }

    private List<Integer> getSelectionOrder() {
        List<Integer> arrayList = new ArrayList<>();
        RunnableEx.run(() -> {
            TreeItem<Map<String, String>> selectedItem = tree.getSelectionModel().getSelectedItem();
            lastSelected.clear();
            if (selectedItem != null) {
                while (selectedItem.getParent() != null) {
                    int indexOf = selectedItem.getParent().getChildren().indexOf(selectedItem);
                    arrayList.add(0, indexOf);
                    Text graphic = (Text) selectedItem.getGraphic();
                    lastSelected.add(0, graphic.getText());
                    selectedItem = selectedItem.getParent();
                }
            }
        });
        return arrayList;
    }

    private void selectSame(List<Integer> arrayList) {
        RunnableEx.run(() -> {

            TreeItem<Map<String, String>> root = tree.getRoot();
            for (int i = 0; i < lastSelected.size(); i++) {
                Integer integer = arrayList.get(i % arrayList.size());
                String string = lastSelected.get(i);
                if (!root.getChildren().isEmpty()) {
                    root = root.getChildren().stream().filter(e -> ((Text) e.getGraphic()).getText().equals(string))
                            .findFirst().orElse(root.getChildren().get(integer % root.getChildren().size()));
                }
            }
            tree.getSelectionModel().clearSelection();
            tree.getSelectionModel().select(root);
            for (int i = 0; tree.getTreeItem(i) != null; i++) {
                TreeItem<Map<String, String>> treeItem = tree.getTreeItem(i);
                if (treeItem.equals(root)) {
                    tree.getFocusModel().focus(i);
                    break;
                }
            }

        });
    }

    public static void copyContent(TableView<Map<String, String>> sideTable, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            ObservableList<Map<String, String>> selectedItems = sideTable.getSelectionModel().getSelectedItems();
            String collect = selectedItems.stream().map(Map<String, String>::values)
                    .map(l -> l.stream().collect(Collectors.joining("\t"))).collect(Collectors.joining("\n"));
            Map<DataFormat, Object> content = FXCollections.observableHashMap();
            content.put(DataFormat.PLAIN_TEXT, collect);
            Clipboard.getSystemClipboard().setContent(content);
        }
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

    private static void changeDisplayIfTooBig(ObservableList<Map<String, String>> list,
            TableView<Map<String, String>> sideTable) {
        if (list.size() == 1 && sideTable.getColumns().size() > MAX_COLUMNS) {
            Map<String, String> map = list.get(0);
            list.clear();
            sideTable.getColumns().clear();
            addColumns(sideTable, Arrays.asList("Key", "Value"));
            List<Map<String, String>> collect = map.entrySet().stream().map(e -> {
                Map<String, String> newMap = newMap("Key", e.getKey());
                newMap.put("Value", e.getValue());
                return newMap;
            }).collect(Collectors.toList());
            list.addAll(collect);
        }
    }

    private static void exportToExcel(TableView<Map<String, String>> tree, File file) {
        if (file == null) {
            return;
        }

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
        if (newValue == null) {
            return;
        }
        if (newValue.isLeaf()) {
            addColumns(sideTable, newValue.getValue().keySet());
            list.add(newValue.getValue());
            splitList(list, newValue.getValue());
            changeDisplayIfTooBig(list, sideTable);
            return;
        }

        Set<String> valueKeySet = newValue.getValue().keySet();
        addColumns(sideTable, valueKeySet);
        Map<String, String> newItem = new HashMap<>();
        newItem.putAll(newValue.getValue());
        list.add(newItem);
        List<String> keySet = newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                .flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toList());
        if (newItem.isEmpty() && keySet.size() - 1 == newValue.getChildren().size()) {
            keySet.addAll(newValue.getValue().keySet());
            sideTable.getColumns().clear();
            addColumns(sideTable, keySet);
            newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue).forEach(newItem::putAll);
            changeDisplayIfTooBig(list, sideTable);
            return;
        }
        if (valueKeySet.isEmpty()) {
            list.clear();
            addColumns(sideTable, keySet);
            List<Map<String, String>> collect = newValue.getChildren().stream()
                    .map(TreeItem<Map<String, String>>::getValue).collect(Collectors.toList());
            list.addAll(collect);
            return;
        }
        changeDisplayIfTooBig(list, sideTable);

    }

    private static boolean splitList(ObservableList<Map<String, String>> list, Map<String, String> newItem) {
        long count =
                newItem.values().stream().filter(Objects::nonNull).mapToInt(e -> e.split("\n").length)
                        .filter(i -> i > 1).distinct().count();
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
