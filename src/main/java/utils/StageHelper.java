package utils;

import com.google.common.io.Files;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
import org.slf4j.Logger;

public final class StageHelper {

    private static final Logger LOG = HasLogging.log();

    private StageHelper() {
    }

    public static Button chooseFile(String nome, String title, ConsumerEx<File> onSelect) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return CommonsFX.newButton(nome, e -> {
            Node target = (Node) e.getTarget();
            File showOpenDialog = chooser.showOpenDialog(target.getScene().getWindow());
            if (showOpenDialog != null) {
                ConsumerEx.makeConsumer(onSelect).accept(showOpenDialog);
            }
        });
    }

    public static Button chooseFile(String nome, String title, ConsumerEx<File> onSelect, String filter,
        String... extensions) {
        return CommonsFX.newButton(nome, fileAction(title, onSelect, filter, extensions));
    }

    public static void displayCSSStyler(Scene scene, String pathname) {
        String str = ClassReflectionUtils.displayStyleClass(scene.getRoot());
        HasLogging.log(1).info("{}", str);
        Stage stage2 = new Stage();
        File file = new File("src/main/resources/" + pathname);
        TextArea textArea = new TextArea(getText(file));
        if (file.exists()) {
            RunnableEx.ignore(() -> scene.getStylesheets().add(file.toURI().toURL().toString()));
        }
        stage2.setScene(new Scene(new VBox(textArea, CommonsFX.newButton("_Save", e -> {
            try (PrintStream fileOutputStream = new PrintStream(file, StandardCharsets.UTF_8.name())) {
                fileOutputStream.print(textArea.getText());
                fileOutputStream.flush();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(file.toURI().toURL().toString());
                textArea.requestFocus();
            } catch (Exception e1) {
                LOG.error("", e1);
            }
        }))));
        textArea.prefHeightProperty().bind(stage2.heightProperty().subtract(10));
        stage2.setHeight(500);
        stage2.show();

        if (scene.getWindow() != null) {
            Window window = scene.getWindow();
            final EventHandler<WindowEvent> closeRequest = scene.getWindow().getOnCloseRequest();
            window.setOnCloseRequest(e -> closeBoth(stage2, closeRequest, e));
        }
        scene.windowProperty().addListener((ob, o, n) -> {
            final EventHandler<WindowEvent> closeRequest = n.getOnCloseRequest();
            n.setOnCloseRequest(e -> closeBoth(stage2, closeRequest, e));
        });
    }

    public static Stage displayDialog(final String text, Node button) {
        final Stage stage1 = new Stage();
        final VBox group = new VBox(new Text(text), button);
        group.setAlignment(Pos.CENTER);
        stage1.setScene(new Scene(group));
        stage1.show();
        return stage1;
    }

    public static void displayDialog(final String text, final String buttonMsg, final Runnable c) {
        final Stage stage1 = new Stage();
        final Button button = CommonsFX.newButton(buttonMsg, a -> {
            c.run();
            stage1.close();
        });
        final VBox group = new VBox(new Text(text), button);
        group.setAlignment(Pos.CENTER);
        stage1.setScene(new Scene(group));
        stage1.show();
    }

    public static void displayDialog(String text, String buttonMsg, Supplier<DoubleProperty> c, RunnableEx run) {
        final Stage stage1 = new Stage();
        ProgressIndicator progressIndicator = new ProgressIndicator(0);

        final Button button = CommonsFX.newButton(buttonMsg, a -> {
            DoubleProperty progress = c.get();
            progressIndicator.progressProperty().bind(progress);
            progress.addListener((v, o, n) -> {
                if (n.intValue() == 1) {
                    Platform.runLater(stage1::close);
                    RunnableEx.make(() -> {
                        Thread.sleep(3000);
                        run.run();
                    }).run();
                }
            });
        });
        Text text2 = new Text(text);
        text2.setTextAlignment(TextAlignment.CENTER);
        final VBox group = new VBox(text2, progressIndicator, button);
        group.setAlignment(Pos.CENTER);
        stage1.setScene(new Scene(group));
        stage1.show();
    }

    public static EventHandler<ActionEvent> fileAction(String title, ConsumerEx<File> onSelect, String filter,
        String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(filter, extensions));
        return e -> {
            Node target = (Node) e.getTarget();
            File showOpenDialog = chooser.showOpenDialog(target.getScene().getWindow());
            if (showOpenDialog != null) {
                ConsumerEx.makeConsumer(onSelect).accept(showOpenDialog);
            }
        };
    }

    public static Button selectDirectory(String nome, String title, ConsumerEx<File> onSelect) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        File musicsDirectory = ResourceFXUtils.getUserFolder("Music");
        chooser.setInitialDirectory(musicsDirectory.getParentFile());
        return CommonsFX.newButton(nome, e -> {
            Node target = (Node) e.getTarget();
            Window window = target.getScene().getWindow();
            File selectedFile = chooser.showDialog(window);
            if (selectedFile != null) {
                ConsumerEx.makeConsumer(onSelect).accept(selectedFile);
            }
        });
    }

    private static void closeBoth(Stage stage2, EventHandler<WindowEvent> onCloseRequest, WindowEvent e) {
        if (onCloseRequest != null) {
            onCloseRequest.handle(e);
        }
        stage2.close();
    }

    private static String getText(File file) {
        try {
            if (file.exists()) {
                return Files.toString(file, StandardCharsets.UTF_8);
            }
        } catch (Exception e2) {
            LOG.error("", e2);
        }
        return "";
    }
}
