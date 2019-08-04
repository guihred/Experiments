package extract;

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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.PredicateEx;
import utils.ResourceFXUtils;

public class FilesComparator extends Application {

    private static final Logger LOG = HasLogging.log();

    private ObjectProperty<File> directory1 = new SimpleObjectProperty<>();

    private ObjectProperty<File> directory2 = new SimpleObjectProperty<>();
    private MusicHandler musicHandler = new MusicHandler(null);

    private Map<File, Music> fileMap = new ConcurrentHashMap<>();

    private ProgressIndicator progress;

    public ObservableList<File> getSongs(File file, ObservableList<File> musicas, TableView<File> table1) {
        musicas.clear();

        new Thread(() -> {
            List<Path> find = ResourceFXUtils.getPathByExtension(file, ".mp3");
            find.stream().map(Path::toFile).forEach(musicas::add);
            musicas.sort(comparing(FilesComparator::toFileString));
            double d = 1.0 / find.size();
            Platform.runLater(() -> progress.setProgress(0));
            musicas.forEach(t -> {
                Platform.runLater(() -> progress.setProgress(progress.getProgress() + d));
                getFromMap(t);
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

        addTable(root, "File 1", ">", items1, items2, directory1, primaryStage);
        progress = new ProgressIndicator();
        root.getChildren().add(new VBox(10, progress));
        addTable(root, "File 2", "<", items2, items1, directory2, primaryStage);
        Scene value = new Scene(root);
        value.getStylesheets().add(toExternalForm("filesComparator.css"));
        primaryStage.setScene(value);
        primaryStage.setTitle("Files Comparator");
        primaryStage.show();
    }

    private ObservableValue<File> addTable(HBox root, String nome, String title, ObservableList<File> items1,
        ObservableList<File> items2, ObjectProperty<File> dir, Stage primaryStage) {

        TableView<File> table1 = new SimpleTableViewBuilder<File>().items(items1).selectionMode(SelectionMode.MULTIPLE)
            .addColumn(nome, (s, c) -> {
                c.setText(toFileString(s));
                String itemClass = getItemClass(items2, s, c.getStyleClass());
                c.getStyleClass().add(itemClass);
            }).onDoubleClick(e -> musicHandler.handleMousePressed(MusicReader.readTags(e))).prefWidthColumns(1).build();
        table1.setId(nome);
//        new 

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
        Button files1 = CommonsFX.newButton(nome, e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                getSongs(selectedFile, items1, table1);
                dir.setValue(selectedFile);
                updateCells(table1);
            }

        });
        Button copyButton = CommonsFX.newButton(title, e -> copy(dir, table1, items2));
        Button deleteButton = CommonsFX.newButton("X", e -> delete(table1));
        Text text = new Text("");
        text.textProperty().bind(dir.asString());
        root.getChildren().add(new VBox(new HBox(files1, copyButton, deleteButton, text), table1));

        return table1.getSelectionModel().selectedItemProperty();
    }

    private boolean contentEqual(ObservableList<File> items2, File s) {
        return items2.stream().anyMatch(PredicateEx.makeTest(m -> isEqualSong(s, m)));
    }

    private void copy(ObjectProperty<File> dir, TableView<File> table1, ObservableList<File> items2) {
        progress.setProgress(0);
        List<File> selectedItems = new ArrayList<>(table1.getSelectionModel().getSelectedItems());
        new Thread(() -> {
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
        }, "Copy Thread").start();
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

    private Music getFromMap(File m) {
        return fileMap.computeIfAbsent(m, MusicReader::readTags);
    }

    private String getItemClass(ObservableList<File> items2, File s, ObservableList<String> classes) {
        String fileString = toFileString(s);
        classes.removeAll("", "vermelho", "amarelo");
        Optional<File> findFirst = items2.stream().filter(m -> toFileString(m).equals(fileString)).findFirst();
        if (!findFirst.isPresent()) {
            return "vermelho";
        }

        if (isEqualSong(s, findFirst.get())) {
            return "";
        }
        return "amarelo";

    }

    private boolean isEqualSong(File s, File m) {
        return Objects.equals(getFromMap(s), getFromMap(m));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static boolean notRepeated(ObservableList<File> items2, File s) {
        String fileString = toFileString(s);
        return !items2.stream().anyMatch(m -> toFileString(m).equals(fileString));
    }

    private static String toFileString(File s) {
        return s.getParentFile().getName() + "/" + s.getName();
    }

    @SuppressWarnings("unchecked")
    private static void updateCells(TableView<File> table1) {
        Platform.runLater(() -> {
            Parent root = table1.getScene().getRoot();
            for (Node cell : root.lookupAll(".cell")) {
                cell.getStyleClass().removeAll("", "vermelho", "amarelo");
            }
            File selectedItem = table1.getSelectionModel().getSelectedItem();

            String fileString = selectedItem == null ? "" : toFileString(selectedItem);

            for (Node cell : root.lookupAll(".table-view")) {
                TableView<File> tables = (TableView<File>) cell;
                Optional<File> findFirst = tables.getItems().stream().filter(e -> toFileString(e).equals(fileString))
                    .findFirst();
                if (findFirst.isPresent()) {
                    tables.getSelectionModel().select(findFirst.get());
                }
                int selectedIndex = tables.getSelectionModel().getSelectedIndex();
                tables.getItems().sort(comparing(FilesComparator::toFileString));
                tables.scrollTo(0);
                tables.scrollTo(selectedIndex);
            }
        });
    }
}
