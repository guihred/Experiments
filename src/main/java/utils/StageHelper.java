package utils;

import static utils.RunnableEx.runIf;

import com.google.common.io.Files;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public final class StageHelper {

    private StageHelper() {
    }

    public static Button chooseFile(String nome, String title, ConsumerEx<File> onSelect) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return newButton(nome, e -> {
            Node target = (Node) e.getTarget();
            File fileChosen = chooser.showOpenDialog(target.getScene().getWindow());
            runIf(fileChosen, f -> ConsumerEx.makeConsumer(onSelect).accept(f));
        });
    }

    public static Button chooseFile(String nome, String title, ConsumerEx<File> onSelect, String filter,
        String... extensions) {
        return newButton(nome, fileAction(title, onSelect, filter, extensions));
    }

    public static void closeStage(EventTarget button) {
        Optional.ofNullable((Node) button).map(Node::getScene).map(sc -> (Stage) sc.getWindow())
            .ifPresent(Stage::close);

    }

    public static void displayCSSStyler(Scene scene, String pathname) {
        String str = TreeElement.displayStyleClass(scene.getRoot());
        HasLogging.log(1).info("{}", str);
        Stage stage2 = new Stage();
        File file = new File("src/main/resources/" + pathname);
        TextArea textArea = new TextArea(getText(file));
        if (file.exists()) {
            RunnableEx.ignore(() -> scene.getStylesheets().add(ResourceFXUtils.convertToURL(file).toString()));
        }
        Button saveButton = newButton("_Save", e -> RunnableEx.run(() -> {
            try (PrintStream fileOutputStream = new PrintStream(file, StandardCharsets.UTF_8.name())) {
                fileOutputStream.print(textArea.getText());
                fileOutputStream.flush();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(ResourceFXUtils.convertToURL(file).toString());
                textArea.requestFocus();
            }
        }));
        stage2.setScene(new Scene(new VBox(textArea, saveButton)));
        textArea.prefHeightProperty().bind(stage2.heightProperty().subtract(10));
        stage2.setHeight(500);
        stage2.show();

        runIf(scene.getWindow(), window -> {
            EventHandler<WindowEvent> closeRequest = window.getOnCloseRequest();
            window.setOnCloseRequest(e -> closeBoth(stage2, closeRequest, e));
        });
        scene.windowProperty().addListener((ob, o, n) -> {
            EventHandler<WindowEvent> closeRequest = n.getOnCloseRequest();
            n.setOnCloseRequest(e -> closeBoth(stage2, closeRequest, e));
        });
    }

    public static EventHandler<ActionEvent> fileAction(String title, ConsumerEx<File> onSelect, String filter,
        String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(filter, extensions));
        return e -> {
            Node target = (Node) e.getTarget();
            File fileChosen = chooser.showOpenDialog(target.getScene().getWindow());
            runIf(fileChosen, f -> ConsumerEx.makeConsumer(onSelect).accept(f));
        };
    }

    public static EventHandler<ActionEvent> fileAction(String title, File initialDir, ConsumerEx<File> onSelect,
        String filter, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialDirectory(initialDir);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(filter, extensions));
        return e -> {
            Node target = (Node) e.getTarget();
            File fileChosen = chooser.showOpenDialog(target.getScene().getWindow());
            runIf(fileChosen, f -> ConsumerEx.makeConsumer(onSelect).accept(f));
        };
    }

    public static Button selectDirectory(String nome, String title, ConsumerEx<File> onSelect) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");
        chooser.setInitialDirectory(musicsDirectory);
        return newButton(nome, e -> {
            Node target = (Node) e.getTarget();
            runIf(target.getScene().getWindow(),
                window -> runIf(chooser.showDialog(window), file -> ConsumerEx.makeConsumer(onSelect).accept(file)));
        });
    }

    private static void closeBoth(Stage stage2, EventHandler<WindowEvent> onCloseRequest, WindowEvent e) {
        runIf(onCloseRequest, o -> o.handle(e));
        stage2.close();
    }

    private static String getText(File file) {
        return file.exists() ? SupplierEx.get(() -> Files.toString(file, StandardCharsets.UTF_8), "") : "";
    }

    private static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
        Button button = new Button(nome);
        button.setId(nome);
        button.setOnAction(onAction);
        return button;
    }
}
