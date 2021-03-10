package ethical.hacker;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class PEPythonApp extends Application {
    @FXML
    protected TextField resultsFilter;
    @FXML
    protected ListView<String> filterList;
    @FXML
    protected ProgressIndicator progressIndicator;
    @FXML
    protected TableView<Map<String, String>> commonTable;
    protected ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());

    public void initialize() {
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(commonTable).copiable().savable()
                .onSortClicked((c, a) -> QuickSortML.sortMapList(items, c, a));
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable();
    }

    public void onActionPeScan(ActionEvent e) {

        items.clear();
        resultsFilter.setText("");
        CommonsFX.update(progressIndicator.progressProperty(), 0);
        new FileChooserBuilder().extensions("All Files", "*.*").title("Scan PE").name("Scan PE")
                .openFileMultipleAction(s -> scanFiles(s), e);
    }

    public void onActionPeScanMultiple(ActionEvent e) {

        items.clear();
        resultsFilter.setText("");
        CommonsFX.update(progressIndicator.progressProperty(), 0);
        new FileChooserBuilder().extensions("All Files", "*.*").name("Scan PE").onSelect(f -> {
            List<File> collect = FileTreeWalker.getFirstFileMatch(f, p -> p.toFile().isFile()).stream()
                    .map(Path::toFile).collect(Collectors.toList());
            scanFiles(collect);
        }).openDirectoryAction(e);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int width = 600;
        CommonsFX.loadFXML("PE Analyzer", "PEPythonApp.fxml", this, primaryStage, width, width);
    }

    private void scanFiles(List<File> asList) {
        ObservableList<String> items2 = filterList.getItems();
        for (File file : asList) {
            items2.add(file.getName());
        }
        List<String> cols = commonTable.getColumns().stream().map(TableColumn::getText).collect(Collectors.toList());

        RunnableEx.runNewThread(() -> {
            for (File file : asList) {
                Map<String, SupplierEx<String>> linkedHashMap = getAnalysis(file);
                Map<String, String> ns = new LinkedHashMap<>();
                for (Entry<String, SupplierEx<String>> ip : linkedHashMap.entrySet()) {
                    if (cols.isEmpty() || cols.contains(ip.getKey())) {
                        ns.put(ip.getKey(), SupplierEx.get(ip.getValue()));
                    }
                    CommonsFX.addProgress(progressIndicator.progressProperty(),
                            1. / linkedHashMap.size() / asList.size());
                }
                CommonsFX.runInPlatform(() -> {
                    if (commonTable.getColumns().isEmpty()) {
                        SimpleTableViewBuilder.addColumns(commonTable, ns.keySet());
                    }
                    items.add(ns);
                    if (items.size() == asList.size()) {
                        CommonsFX.update(progressIndicator.progressProperty(),1);
                    }
                    SimpleTableViewBuilder.autoColumnsWidth(commonTable);
                });
            }
        });
    }

    public static String getFileType(File file) {
        List<String> executeInConsoleInfo = simpleExecution("python/magicExample.py", file);
        return executeInConsoleInfo.stream().findFirst().orElse(null);
    }

    public static List<String> getSections(File file) {
        return simpleExecution("python/pedisplaySections.py", file);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String fullPath(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/").replaceAll("^/", "");
    }

    private static Map<String, SupplierEx<String>> getAnalysis(File s) {
        Map<String, SupplierEx<String>> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("Name", s::getName);
        linkedHashMap.put("File Type", () -> getFileType(s));
        linkedHashMap.put("Timestamp", () -> join(getTimestamp(s)));
        linkedHashMap.put("Sections", () -> join(getSections(s)));
        linkedHashMap.put("Exports", () -> join(getExports(s)));
        linkedHashMap.put("Imports", () -> join(getImports(s)));
        linkedHashMap.put("YaraRules", () -> join(getYaraRules(s)));
        return linkedHashMap;
    }

    private static List<String> getExports(File s) {
        return simpleExecution("python/peexportsExample.py", s);
    }

    private static List<String> getImports(File s) {
        return simpleExecution("python/peimportExample.py", s);
    }

    private static List<String> getTimestamp(File s) {
        return simpleExecution("python/peDisplayTimestamp.py", s);
    }

    private static List<String> getYaraRules(File s) {
        return simpleExecution("python/yaraRules.py", s);
    }

    private static String join(List<String> sections2) {
        return sections2.stream().map(s -> s.replaceAll("b'|'$|\\\\x00", "")).collect(Collectors.joining("\n"));
    }


    private static List<String> simpleExecution(String magicPath, File file) {
        String file2 = fullPath(ResourceFXUtils.toFile(magicPath));
        String format = String.format("python %s %s", file2, fullPath(file));
        return ConsoleUtils.executeInConsoleInfo(format);
    }
}
