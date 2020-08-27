package simplebuilder;

import static utils.RunnableEx.runIf;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import utils.ConsumerEx;
import utils.FunctionEx;

public class FileChooserBuilder {
    private DirectoryChooser chooser = new DirectoryChooser();
    private FileChooser fileChooser = new FileChooser();
    private ConsumerEx<File> onSelect;
    private String name;

    public Button buildOpenButton() {
        return FileChooserBuilder.newButton(name, openFileAction());
    }

    public Button buildOpenDirectoryButton() {
        return FileChooserBuilder.newButton(name, openDirectoryAction());
    }

    public Button buildSaveButton() {
        return FileChooserBuilder.newButton(name, saveFileAction());
    }

    public FileChooserBuilder extensions(String filter, String... extensions) {
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(filter, extensions));
        return this;
    }

    public FileChooserBuilder initialDir(File initialDir) {
        chooser.setInitialDirectory(initialDir);
        fileChooser.setInitialDirectory(initialDir);
        return this;
    }

    public FileChooserBuilder initialFilename(String initialDir) {
        fileChooser.setInitialFileName(initialDir);
        return this;
    }

    public FileChooserBuilder name(String name1) {
        name = name1;
        return this;
    }

    public FileChooserBuilder onSelect(ConsumerEx<File> onSelect1) {
        onSelect = onSelect1;
        return this;
    }

    public EventHandler<ActionEvent> openDirectoryAction() {
        return e -> {
            Node target = (Node) e.getTarget();
            runIf(target.getScene().getWindow(), window -> runIf(chooser.showDialog(window), onSelect));
        };
    }

    public EventHandler<ActionEvent> openFileAction() {
        return e -> {
            EventTarget target2 = e.getTarget();
            Node target = target2 instanceof Node ? (Node) target2 : null;
            File fileChosen = fileChooser.showOpenDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
            runIf(fileChosen, onSelect);
        };
    }

    public EventHandler<ActionEvent> openFileMultipleAction(ConsumerEx<List<File>> onSelect0) {
        return e -> {
            Node target = e.getTarget() instanceof Node ? (Node) e.getTarget() : null;
            List<File> fileChosen =
                    fileChooser.showOpenMultipleDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
            runIf(fileChosen, onSelect0);
        };
    }

    public EventHandler<ActionEvent> saveFileAction() {
        return e -> {
            Node target = e.getTarget() instanceof Node ? (Node) e.getTarget() : null;
            File fileChosen = fileChooser.showSaveDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
            runIf(fileChosen, onSelect);
        };
    }

    public FileChooserBuilder title(String title) {
        fileChooser.setTitle(title);
        chooser.setTitle(title);
        return this;
    }

    public static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
        Button button = new Button(nome);
        button.setId(nome);
        button.setOnAction(onAction);
        return button;
    }
}