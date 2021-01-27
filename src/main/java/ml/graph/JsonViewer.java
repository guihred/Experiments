
package ml.graph;

import static extract.web.JsonExtractor.newMap;
import static extract.web.JsonExtractor.readJsonFile;

import extract.web.JsonExtractor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.*;
import utils.*;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class JsonViewer extends Application {
    private static final int MAX_COLUMNS = 6;
    private ObjectProperty<File> fileProp = new SimpleObjectProperty<>();
    private ObservableList<File> files = FXCollections.observableArrayList();
    @FXML
    private TreeView<Map<String, String>> tree;
    private List<String> lastSelected = new ArrayList<>();
    @FXML
    private TableView<Map<String, String>> sideTable;
    @FXML
    private ComboBox<File> comboBox3;
    @FXML
    private TextField toCSV;
    @FXML
    private TextField search;

    public void addFile(File... filesToAdd) {

        CommonsFX.runInPlatform(() -> {
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

    public void initialize() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        SimpleTableViewBuilder.of(sideTable).items(CommonsFX.newFastFilter(search, list.filtered(e -> true))).copiable()
                .savable().multipleSelection();
        SimpleTreeViewBuilder.of(tree).root(newMap("Root", null))
                .onSelect(newValue -> onSelectTreeItem(list, sideTable, newValue)).build();
        SimpleComboBoxBuilder<File> onChange =
                SimpleComboBoxBuilder.of(comboBox3).items(files).onChange((old, val) -> fileProp.set(val));
        fileProp.addListener((ob, old, val) -> CommonsFX.runInPlatform(() -> {
            if (val != null) {
                onChange.select(val);
                List<Integer> arrayList = getSelectionOrder();
                readJsonFile(tree, val);
                selectSame(arrayList);
                if (!files.contains(val)) {
                    files.add(val);
                }
            }
        }));
    }

    public void onActionExportExcel() {
        exportToExcel(sideTable, fileProp.get());
    }

    public void onActionImportJson(ActionEvent e) {
        new FileChooserBuilder().name("Import Json").title("Import Json").extensions("Json", "*.json")
                .onSelect(fileProp::set).openFileAction(e);
    }

    @SuppressWarnings("unchecked")
    public void onKeyReleased(KeyEvent k) throws IOException {
        if (k.getCode() == KeyCode.ENTER) {
            String text = toCSV.getText();
            String[] split2 = text.split(":");
            File value = comboBox3.getValue();
            Map<String, String> mkae = JsonExtractor.makeMapFromJsonFile(value, split2[0].split("[, ]+"));
            List<?> remap2 = JsonExtractor.remap(mkae, text.contains(":") ? split2[1] : "");
            File outFile = ResourceFXUtils.getOutFile("csv/" + value.getName().replaceAll("\\.json", ".csv"));
            CSVUtils.appendLines(outFile, (List<Map<String, Object>>) remap2);
            new SimpleDialogBuilder().bindWindow(comboBox3).show(DataframeExplorer.class).addStats(outFile);
        }
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
        CommonsFX.loadFXML("Json Viewer", "JsonViewer.fxml", this, primaryStage);
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
            List<Map<String, String>> remappedEntry = map.entrySet().stream().map(e -> {
                Map<String, String> newMap = newMap("Key", e.getKey());
                newMap.put("Value", e.getValue());
                return newMap;
            }).collect(Collectors.toList());
            list.addAll(remappedEntry);
        }
    }

    private static void exportToExcel(TableView<Map<String, String>> tree, File file) {
        if (file == null) {
            return;
        }

        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new HashMap<>();
        List<Map<String, String>> treeItems = tree.getItems();
        List<String> flatItems =
                treeItems.stream().flatMap(e -> e.keySet().stream()).distinct().collect(Collectors.toList());
        for (String key : flatItems) {
            mapa.put(key, t -> t.get(key));
        }

        File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll(".json", ".xlsx"));
        ExcelService.getExcel(treeItems, mapa, outFile);
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
            JsonExtractor.splitList(list, newValue.getValue());
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
            List<Map<String, String>> childrenValues = newValue.getChildren().stream()
                    .map(TreeItem<Map<String, String>>::getValue).collect(Collectors.toList());
            list.addAll(childrenValues);
            return;
        }
        changeDisplayIfTooBig(list, sideTable);

    }
}
