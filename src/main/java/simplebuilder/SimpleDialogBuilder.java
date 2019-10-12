package simplebuilder;

import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.RunnableEx;

public class SimpleDialogBuilder implements SimpleBuilder<Stage> {

    private static final Logger LOG = HasLogging.log();

    private Stage stage = new Stage();
    private VBox group = new VBox(5);

    private Node node;

    public SimpleDialogBuilder() {
        group.setAlignment(Pos.CENTER);
    }

    public SimpleDialogBuilder bindWindow(Node node1) {
        node = node1;
        Window window = node1.getScene().getWindow();
        if (window == null) {
            return this;
        }

        window.showingProperty().addListener((ob, old, n) -> {
            if (!n) {
                Platform.runLater(stage::close);
            }
        });
        return this;
    }

    public SimpleDialogBuilder bindWindow(Window window) {
        window.showingProperty().addListener((ob, old, n) -> {
            if (!n) {
                Platform.runLater(stage::close);
            }
        });
        return this;
    }

    @Override
    public Stage build() {
        return stage;
    }

    public SimpleDialogBuilder button(Node button) {
        group.getChildren().add(button);
        return this;
    }

    public SimpleDialogBuilder button(String buttonMsg, Runnable c) {
        Button button = newButton(buttonMsg, a -> {
            c.run();
            stage.close();
        });
        group.getChildren().add(button);
        return this;
    }

    public SimpleDialogBuilder button(String buttonMsg, Supplier<DoubleProperty> c, RunnableEx run) {
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        Button button = newButton(buttonMsg, a -> {
            DoubleProperty progress = c.get();
            progressIndicator.progressProperty().bind(progress);
            progress.addListener((v, o, n) -> {
                if (n.intValue() == 1) {
                    Platform.runLater(stage::close);
                    RunnableEx.run(() -> {
                        final int millis = 3000;
                        Thread.sleep(millis);
                        run.run();
                    });
                }
            });
        });
        group.getChildren().addAll(progressIndicator, button);
        return this;
    }

    public Stage displayDialog() {
        if (node != null) {
            Scene scene = node.getScene();
            if (scene == null || scene.getWindow() == null) {
                return stage;
            }
        }

        stage.setScene(new Scene(group));
        stage.show();
        LOG.info("DIALOG {}", HasLogging.getCurrentLine(1));
        return stage;
    }

    public SimpleDialogBuilder text(String text) {
        group.getChildren().add(new Text(text));
        return this;
    }

    public static void closeStage(EventTarget button) {
        Node button2 = (Node) button;
        ((Stage) button2.getScene().getWindow()).close();
    }

    private static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
        Button button = new Button(nome);
        button.setId(nome);
        button.setOnAction(onAction);
        return button;
    }
}
