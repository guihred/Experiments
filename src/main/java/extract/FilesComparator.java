package extract;

import static extract.FilesComparatorHelper.*;
import static java.util.Comparator.comparing;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;
import static utils.ResourceFXUtils.toExternalForm;

import audio.mp3.Music;
import audio.mp3.MusicHandler;
import audio.mp3.MusicReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import simplebuilder.SimpleTableViewBuilder;
import utils.*;

public class FilesComparator extends Application {

    private static final Logger LOG = HasLogging.log();

    private final ObjectProperty<File> directory1 = new SimpleObjectProperty<>();

    private final ObjectProperty<File> directory2 = new SimpleObjectProperty<>();
    private final MusicHandler musicHandler = new MusicHandler(null);

    private final Map<File, Music> fileMap = new ConcurrentHashMap<>();

    private ProgressIndicator progress;

    public ObservableList<File> getSongs(File file, ObservableList<File> musicas, TableView<File> table1) {
        musicas.clear();

        new Thread(() -> {
            List<Path> find = ResourceFXUtils.getPathByExtension(file, ".mp3");
            find.stream().map(Path::toFile).forEach(musicas::add);
            musicas.sort(comparing(FilesComparatorHelper::toFileString));
            double d = 1.0 / find.size();
            Platform.runLater(() -> progress.setProgress(0));
            musicas.forEach(t -> {
                Platform.runLater(() -> progress.setProgress(progress.getProgress() + d));
                getFromMap(t, fileMap);
            });
            Platform.runLater(() -> progress.setProgress(1));
            updateCells(table1);
        }).start();
        return musicas;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox root = new HBox();

        ObservableList<File> items1 = synchronizedObservableList(observableArrayList());
        ObservableList<File> items2 = synchronizedObservableList(observableArrayList());

        addTable(root, "File 1", ">", items1, items2, directory1);
        progress = new ProgressIndicator();
        root.getChildren().add(new VBox(10, progress));
        addTable(root, "File 2", "<", items2, items1, directory2);
        Scene value = new Scene(root);
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
            }).onDoubleClick(e -> musicHandler.handleMousePressed(MusicReader.readTags(e))).prefWidthColumns(1).build();
        table1.setId(nome);
//        new 

        Button files1 = StageHelper.selectDirectory(nome, "Carregar Pasta de Músicas", selectedFile -> {
            getSongs(selectedFile, items1, table1);
            dir.setValue(selectedFile);
            updateCells(table1);
        });
        Button copyButton = CommonsFX.newButton(title, e -> copy(dir, table1, items2));
        Button deleteButton = CommonsFX.newButton("X", e -> delete(table1));
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
            try {
                File file2 = new File(dir2.get(), newFile);
                if (!file2.getParentFile().exists()) {
                    file2.getParentFile().mkdir();
                }
                boolean exists = file2.exists();
                Files.deleteIfExists(file2.toPath());
                Files.copy(selectedItem.toPath(), file2.toPath());
                if (!exists) {
                    items2.add(file2);
                } else {
                    fileMap.put(file2, MusicReader.readTags(file2));
                    fileMap.put(selectedItem, MusicReader.readTags(selectedItem));
                }
                double d = 1.0 / selectedItems.size();
                progress.setProgress(progress.getProgress() + d);
                updateCells(table1);
            } catch (Exception e1) {
                LOG.error("", e1);
            }
        }
    }

    private void delete(TableView<File> table1) {
        ObservableList<File> selectedItems = table1.getSelectionModel().getSelectedItems();
        for (File file : selectedItems) {
            File selectedItem = file;
            if (selectedItem != null) {
                try {
                    Files.delete(selectedItem.toPath());
                    table1.getItems().remove(selectedItem);
                } catch (IOException e1) {
                    LOG.error("", e1);
                }
            }
        }
        updateCells(table1);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
