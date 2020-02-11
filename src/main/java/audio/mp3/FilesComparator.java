package audio.mp3;

import static extract.FilesComparatorHelper.*;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;
import static utils.ResourceFXUtils.toExternalForm;

import extract.Music;
import extract.MusicReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.StageHelper;
import utils.*;

public class FilesComparator extends Application {

    private static final Logger LOG = HasLogging.log();

    private final ObjectProperty<File> directory1 = new SimpleObjectProperty<>();
    private final ObjectProperty<File> directory2 = new SimpleObjectProperty<>();
    private Map<String, ObjectProperty<File>> directoryMap = new HashMap<>();

    private ProgressIndicator progress;
    private final Map<File, Music> fileMap = new ConcurrentHashMap<>();

    public void addSongsToTable(TableView<File> table1, File selectedFile) {
        ObservableList<File> items = table1.getItems();
        ObjectProperty<File> dir1 = directoryMap.get(table1.getId());
        getSongs(selectedFile, items, table1);
        dir1.setValue(selectedFile);
    }

    public double getProgress() {
        return progress.getProgress();
    }

    public ObservableList<File> getSongs(File file, ObservableList<File> musicas, TableView<File> table1) {
        musicas.clear();
        updateProgress(0);
        new Thread(() -> {
            LOG.info("Scanning {}", file);
            List<Path> find = ResourceFXUtils.getPathByExtension(file, ".mp3");
            find.stream().map(Path::toFile).forEach(musicas::add);

            double d = 1.0 / find.size();
            for (int i = 0; i < musicas.size(); i++) {
                File t = musicas.get(i);
                updateProgress(progress.getProgress() + d);
                getFromMap(t, fileMap);
            }
            table1.sort();
            updateProgress(1);
            updateCells(table1);
            LOG.info("{} Songs from {}", musicas.size(), file);
        }).start();
        return musicas;
    }

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox();
        ObservableList<File> items1 = synchronizedObservableList(observableArrayList());
        ObservableList<File> items2 = synchronizedObservableList(observableArrayList());

        addTable(root, "File 1", ">", items1, items2, directory1);
        progress = new ProgressIndicator();
        final int minWidth = 40;
        progress.setMinSize(minWidth, minWidth);
        root.getChildren().add(new VBox(10, progress));
        addTable(root, "File 2", "<", items2, items1, directory2);
        final int width = 550;
        final int height = 400;
        Scene value = new Scene(root, width, height);
        value.getStylesheets().add(toExternalForm("filesComparator.css"));
        primaryStage.setScene(value);
        primaryStage.setTitle("Files Comparator");
        primaryStage.show();
    }

    private ObservableValue<File> addTable(HBox root, String nome, String title, ObservableList<File> items1,
        ObservableList<File> items2, ObjectProperty<File> dir) {
        TableView<File> table1 = new SimpleTableViewBuilder<File>().items(items1).selectionMode(SelectionMode.MULTIPLE)
            .addColumn(nome, (s, c) -> {
                c.setText(toFileString(s));
                String itemClass = getItemClass(items2, s, c.getStyleClass(), fileMap);
                c.getStyleClass().add(itemClass);
            }).onDoubleClick(e -> MusicHandler.handleMousePressed(MusicReader.readTags(e))).prefWidthColumns(1).build();

        table1.setId(nome);
        directoryMap.put(nome, dir);
        Button files1 = StageHelper.selectDirectory(nome, "Carregar Pasta de Músicas",
            selectedFile -> addSongsToTable(table1, selectedFile));
        Button copyButton = SimpleButtonBuilder.newButton(title, e -> copy(dir, table1, items2));
        Button deleteButton = SimpleButtonBuilder.newButton("X", e -> delete(table1));
        Text text = new Text("");
        text.textProperty().bind(dir.asString());
        root.getChildren().add(new VBox(new HBox(files1, copyButton, deleteButton, text), table1));

        return table1.getSelectionModel().selectedItemProperty();
    }

    private boolean contentEqual(ObservableList<File> items2, File s) {
        return items2.stream().anyMatch(PredicateEx.makeTest(m -> isEqualSong(s, m, fileMap)));
    }

    private void copy(ObjectProperty<File> dir, TableView<File> table1, ObservableList<File> items2) {
        progress.setProgress(0);
        List<File> selectedItems = new ArrayList<>(table1.getSelectionModel().getSelectedItems());
        new Thread(() -> copySelectedFiles(dir, table1, items2, selectedItems), "Copy Thread").start();
    }

    private void copySelectedFiles(ObjectProperty<File> dir, TableView<File> table1, ObservableList<File> items2,
        List<File> selectedItems) {
        for (File selectedItem : selectedItems) {
            if (selectedItem == null || !notRepeated(items2, selectedItem) && contentEqual(items2, selectedItem)) {
                continue;
            }
            File file = dir.get();
            ObjectProperty<File> dir2 = dir == directory1 ? directory2 : directory1;
            String newFile = selectedItem.getAbsolutePath().replace(file.getAbsolutePath(), "");
            RunnableEx.run(() -> {
                File file2 = new File(dir2.get(), newFile);
                if (!file2.getParentFile().exists()) {
                    file2.getParentFile().mkdir();
                }
                boolean exists = file2.exists();
                Files.deleteIfExists(file2.toPath());
                CrawlerTask.copy(selectedItem, file2);
                if (!exists) {
                    items2.add(file2);
                } else {
                    fileMap.put(file2, MusicReader.readTags(file2));
                    fileMap.put(selectedItem, MusicReader.readTags(selectedItem));
                }
                double d = 1.0 / selectedItems.size();
                progress.setProgress(progress.getProgress() + d);
                updateCells(table1);
            });
        }
        updateProgress(1);
    }

    private void updateProgress(double a) {
        Platform.runLater(() -> progress.setProgress(a));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void delete(TableView<File> table1) {
        ObservableList<File> selectedItems = table1.getSelectionModel().getSelectedItems();
        for (File file : selectedItems) {
            File selectedItem = file;
            if (selectedItem != null) {
                RunnableEx.run(() -> {
                    Files.delete(selectedItem.toPath());
                    ObservableList<File> items = table1.getItems();
                    items.remove(selectedItem);
                });
            }
        }
        updateCells(table1);
    }

}
