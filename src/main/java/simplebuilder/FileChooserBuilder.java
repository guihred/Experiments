package simplebuilder;

import static utils.ex.RunnableEx.runIf;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class FileChooserBuilder {
    private static final Map<String, File> LAST_FILES = new LinkedHashMap<>();
    private DirectoryChooser chooser = new DirectoryChooser();
    private FileChooser fileChooser = new FileChooser();
    private ConsumerEx<File> onSelect;
    private String name;
    private String evocationLine;

    public FileChooserBuilder() {
        evocationLine = HasLogging.getCurrentLine(1);
    }

    public Button buildOpenButton() {
        return SimpleButtonBuilder.newButton(name, this::openFileAction);
    }

    public Button buildOpenDirectoryButton() {
        return SimpleButtonBuilder.newButton(name, this::openDirectoryAction);
    }

    public Button buildSaveButton() {
        return SimpleButtonBuilder.newButton(name, this::saveFileAction);
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

    public void openDirectoryAction(ActionEvent e) {
        Node target = (Node) e.getTarget();
        chooser.setInitialDirectory(SupplierEx.nonNull(chooser.getInitialDirectory(), LAST_FILES.get(evocationLine)));
        runIf(target.getScene().getWindow(), window -> {
            File fileChosen = chooser.showDialog(window);
            LAST_FILES.put(evocationLine, fileChosen);
            runIf(fileChosen, onSelect);
        });
    }

    public void openFileAction(ActionEvent e) {
        EventTarget target2 = e.getTarget();
        fileChooser.setInitialDirectory(
                SupplierEx.nonNull(fileChooser.getInitialDirectory(), LAST_FILES.get(evocationLine)));
        Node target = target2 instanceof Node ? (Node) target2 : null;
        File fileChosen = fileChooser.showOpenDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
        LAST_FILES.put(evocationLine, FunctionEx.mapIf(fileChosen, File::getParentFile));
        runIf(fileChosen, onSelect);
    }

    public void openFileMultipleAction(ConsumerEx<List<File>> onSelect0, ActionEvent e) {
        Node target = e.getTarget() instanceof Node ? (Node) e.getTarget() : null;
        fileChooser.setInitialDirectory(
                SupplierEx.nonNull(fileChooser.getInitialDirectory(), LAST_FILES.get(evocationLine)));
        List<File> fileChosen =
                fileChooser.showOpenMultipleDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
        if (!fileChosen.isEmpty()) {
            LAST_FILES.put(evocationLine, fileChosen.stream().findFirst().map(File::getParentFile).orElse(null));
        }
        runIf(fileChosen, onSelect0);
    }

    public void saveFileAction(Event e) {
        Node target = e.getTarget() instanceof Node ? (Node) e.getTarget() : null;
        fileChooser.setInitialDirectory(
                SupplierEx.nonNull(fileChooser.getInitialDirectory(), LAST_FILES.get(evocationLine),
                        ResourceFXUtils.getOutFile()));

        File fileChosen = fileChooser.showSaveDialog(FunctionEx.mapIf(target, t -> t.getScene().getWindow()));
        LAST_FILES.put(evocationLine, FunctionEx.mapIf(fileChosen, File::getParentFile));
        runIf(fileChosen, onSelect);
    }

    public FileChooserBuilder title(String title) {
        fileChooser.setTitle(title);
        chooser.setTitle(title);
        return this;
    }

}