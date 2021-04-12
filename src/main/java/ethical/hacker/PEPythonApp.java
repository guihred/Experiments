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
import org.apache.commons.lang.StringUtils;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.*;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class PEPythonApp extends Application {
    private static final String YARA_64 = ProjectProperties.getField();
    @FXML
    protected TextField resultsFilter;
    @FXML
    protected ListView<File> filterList;
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
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable().cellFactory(File::getName);
    }

    public void onActionPeScan(ActionEvent e) {

        CommonsFX.update(progressIndicator.progressProperty(), 0);
        String buttonName = "Scan PE";
        new FileChooserBuilder().extensions("All Files", "*.*").title(buttonName).name(buttonName)
                .openFileMultipleAction(s -> {
                    items.clear();
                    resultsFilter.setText("");
                    scanFiles(s);
                }, e);
    }

    public void onActionPeScanMultiple(ActionEvent e) {

        CommonsFX.update(progressIndicator.progressProperty(), 0);
        new FileChooserBuilder().extensions("All Files", "*.*").name("Scan PE").onSelect(f -> {
            items.clear();
            resultsFilter.setText("");
            List<File> filesToScan = FileTreeWalker.getFirstFileMatch(f, p -> p.toFile().isFile()).stream()
                    .map(Path::toFile).collect(Collectors.toList());
            scanFiles(filesToScan);
        }).openDirectoryAction(e);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int width = 600;
        CommonsFX.loadFXML("PE Analyzer", "PEPythonApp.fxml", this, primaryStage, width, width);
    }

    private void runAnalysis(List<File> filesToAnalyze, ObservableList<File> items2, List<String> tableColumns) {
        for (File file : items2) {
            Map<String, SupplierEx<String>> linkedHashMap = getAnalysis(file);
            Map<String, String> ns = new LinkedHashMap<>();
            for (Entry<String, SupplierEx<String>> ip : linkedHashMap.entrySet()) {
                if (tableColumns.isEmpty() || tableColumns.contains(ip.getKey())) {
                    ns.put(ip.getKey(), SupplierEx.get(ip.getValue()));
                }
                CommonsFX.addProgress(progressIndicator.progressProperty(),
                        1. / linkedHashMap.size() / filesToAnalyze.size());
            }
            CommonsFX.runInPlatform(() -> {
                if (commonTable.getColumns().isEmpty()) {
                    SimpleTableViewBuilder.addColumns(commonTable, ns.keySet());
                }
                items.add(ns);
                if (items.size() == filesToAnalyze.size()) {
                    CommonsFX.update(progressIndicator.progressProperty(), 1);
                }
                SimpleTableViewBuilder.autoColumnsWidth(commonTable);
            });
        }
    }

    private void scanFiles(List<File> asList) {
        ObservableList<File> items2 = filterList.getItems();
        for (File file : asList) {
            if (!items2.contains(file)) {
                items2.add(file);
            }
        }
        List<String> cols = commonTable.getColumns().stream().map(TableColumn::getText).collect(Collectors.toList());

        RunnableEx.runNewThread(() -> runAnalysis(asList, items2, cols));
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
        linkedHashMap.put("Rules", () -> join(getYara64(s)));
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

    private static List<String> getYara64(File sourceFile) {
        File file = ResourceFXUtils.toFile("rules/all_index.yar");
        String fullPath = fullPath(sourceFile);
        String format = String.format("%s -f -m %s  %s", YARA_64, file, fullPath);
        return ConsoleUtils.executeInConsoleInfo(format).stream()
                .map(f -> f.replaceAll(",?(version|author)=\".+?\",?", "")).map(f -> f.replaceAll("\\w+=", ""))
                .map(f -> f.replace(fullPath, "")).collect(Collectors.toList())

        ;
    }

    private static String join(List<String> sections2) {
        return sections2.stream().map(s -> s.replaceAll("b'|'$|\\\\x00", "")).collect(Collectors.joining("\n"));
    }

    private static List<String> simpleExecution(String magicPath, File file, String... s) {
        String file2 = fullPath(ResourceFXUtils.toFile(magicPath));
        String format = String.format("python %s %s %s", file2, fullPath(file), StringUtils.join(s, " "));
        return ConsoleUtils.executeInConsoleInfo(format);
    }
}
