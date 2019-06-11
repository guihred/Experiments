package extract;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class FilesComparator extends Application {

    private static final Logger LOG = HasLogging.log();

    private ObjectProperty<File> directory1 = new SimpleObjectProperty<>();

    private ObjectProperty<File> directory2 = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox root = new HBox();

        ObservableList<File> items1 = FXCollections.observableArrayList();
        ObservableList<File> items2 = FXCollections.observableArrayList();
        addTable(root, "File 1", "->", items1, items2, directory1, primaryStage);
        addTable(root, "File 2", "<-", items2, items1, directory2, primaryStage);
        Scene value = new Scene(root);
        value.getStylesheets().add(ResourceFXUtils.toExternalForm("filesComparator.css"));
        primaryStage.setScene(value);
        primaryStage.setTitle("Files Comparator");
        primaryStage.show();
    }

    private ObservableValue<File> addTable(HBox root, String nome, String title, ObservableList<File> items1,
        ObservableList<File> items2, ObjectProperty<File> dir, Stage primaryStage) {

        TableView<File> table1 = new SimpleTableViewBuilder<File>().items(items1).addColumn(nome, (s, c) -> {
            String str = toFileString(s);
            c.setText(str);
            c.getStyleClass().clear();
            c.getStyleClass().add(notRepeated(items2, s) ? "vermelho" : "");
        }).build();

//        new 
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carregar Pasta de Músicas");
        Button files1 = CommonsFX.newButton(nome, e -> {
            File selectedFile = chooser.showDialog(primaryStage);
            if (selectedFile != null) {
                getMusicas(selectedFile, items1);
                dir.setValue(selectedFile);
            }
        });
        root.getChildren().add(new VBox(files1, table1));

        root.getChildren().add(CommonsFX.newButton(title, e -> copy(dir, table1, items2)));

        return table1.getSelectionModel().selectedItemProperty();
    }

    private void copy(ObjectProperty<File> dir, TableView<File> table1, ObservableList<File> items2) {
        File selectedItem = table1.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !notRepeated(items2, selectedItem)) {
            return;
        }

        File file = dir.get();
        ObjectProperty<File> dir2 = dir == directory1 ? directory2 : directory1;
        String newFile = selectedItem.getAbsolutePath().replace(file.getAbsolutePath(), "");
        try {
            File file2 = new File(dir2.get(), newFile);
            Files.copy(selectedItem.toPath(), file2.toPath());
            items2.add(file2);
        } catch (Exception e1) {
            LOG.error("", e1);
        }
    }

    private boolean notRepeated(ObservableList<File> items2, File s) {
        String fileString = toFileString(s);
        return !items2.stream().anyMatch(m -> toFileString(m).equals(fileString));
    }

    private String toFileString(File s) {
        return s.getParentFile().getName() + "/" + s.getName();
    }

    public static ObservableList<File> getMusicas(File file, ObservableList<File> musicas) {
        Path start = file.toPath();
        musicas.clear();
        try (Stream<Path> find = Files.find(start, 6, (dir, name) -> dir.toFile().getName().endsWith(".mp3"))) {
            find.forEach(e -> musicas.add(e.toFile()));
        } catch (Exception e) {
            LOG.trace("", e);
        }
        return musicas;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
