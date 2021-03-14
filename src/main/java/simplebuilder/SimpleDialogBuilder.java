package simplebuilder;

import static utils.ex.FunctionEx.mapIf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.beans.binding.DoubleExpression;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class SimpleDialogBuilder implements SimpleBuilder<Stage> {

    private static final Logger LOG = HasLogging.log();
    private static final Map<String, Stage> MAPPED_STAGES = new HashMap<>();

    private final Stage stage;
    private VBox group = new VBox(5);

    private Node node;

    public SimpleDialogBuilder() {
        this(false);
    }

    public SimpleDialogBuilder(boolean newStage) {
        stage = newStage ? new Stage() : getStage();
    }

    public SimpleDialogBuilder bindWindow(Node node1) {
        node = node1;
        bindWindow(stage, node1);
        return this;
    }

    public SimpleDialogBuilder bindWindow(Window window) {
        window.showingProperty().addListener((ob, old, n) -> {
            if (!n) {
                CommonsFX.runInPlatform(stage::close);
            }
        });
        return this;
    }

    @Override
    public Stage build() {
        return stage;
    }

    public SimpleDialogBuilder button(String buttonMsg, RunnableEx c) {
        Button button = newButton(buttonMsg, a -> {
            RunnableEx.run(c);
            stage.close();
        });
        group.getChildren().add(button);
        return this;
    }

    public SimpleDialogBuilder button(String buttonMsg, Supplier<DoubleExpression> c, RunnableEx run) {
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        Button button1 = new Button(buttonMsg);
        button1.setId(buttonMsg);
        button1.setOnAction(a -> {
            button1.setDisable(true);
            DoubleExpression progress = c.get();
            progressIndicator.progressProperty().bind(progress);
            progress.addListener((v, o, n) -> {
                if (n.intValue() == 1) {
                    CommonsFX.runInPlatform(stage::close);
                    RunnableEx.ignore(() -> {
                        RunnableEx.sleepSeconds(3);
                        run.run();
                    });
                }
            });
        });
        group.getChildren().addAll(progressIndicator, button1);
        return this;
    }

    public Stage displayDialog() {
        if (node != null) {
            Scene scene = node.getScene();
            if (scene == null || scene.getWindow() == null) {
                return stage;
            }
        }
        stage.setAlwaysOnTop(true);
        stage.setScene(new Scene(group));
        stage.show();
        LOG.info("DIALOG {}", HasLogging.getCurrentLine(1));
        return stage;
    }

    public SimpleDialogBuilder height(double value) {
        stage.setHeight(value);
        return this;
    }

    public SimpleDialogBuilder node(Node button) {
        group.getChildren().add(button);
        return this;
    }

    public SimpleDialogBuilder node(Node... buttons) {
        group.getChildren().add(new HBox(buttons));
        return this;
    }

    public SimpleDialogBuilder resizable(boolean value) {
        stage.setResizable(value);
        return this;
    }

    public <T extends Application> T show(Class<T> app, Object... o) {
        LOG.info("DIALOG {} {}", HasLogging.getCurrentLine(1), app.getSimpleName());
        return SupplierEx.get(() -> {
            T newInstance = ClassReflectionUtils.getInstance(app, o);
            newInstance.start(stage);
            return newInstance;
        });

    }

    public <T extends Application> T show(T newInstance) {
        LOG.info("DIALOG {} {}", HasLogging.getCurrentLine(1), newInstance.getClass().getSimpleName());
        return SupplierEx.get(() -> {
            newInstance.start(stage);
            return newInstance;
        });

    }

    public SimpleDialogBuilder text(String text) {
        group.getChildren().add(new Text(text));
        return this;
    }

    public SimpleDialogBuilder title(String title) {
        stage.setTitle(title);
        return this;
    }

    private final Stage getStage() {
        group.setAlignment(Pos.CENTER);
        return MAPPED_STAGES.computeIfAbsent(HasLogging.getCurrentLine(1), s -> new Stage());
    }

    public static Stage bindWindow(Stage stage, Node node1) {
        Window window = mapIf(mapIf(node1, Node::getScene), Scene::getWindow);
        if (window == null) {
            stage.showingProperty().addListener((ob, old, n) -> {
                if (n) {
                    CommonsFX.runInPlatform(stage::close);
                }
            });
            return stage;
        }

        window.showingProperty().addListener((ob, old, n) -> {
            if (!n) {
                CommonsFX.runInPlatform(stage::close);
            }
        });
        return stage;
    }

    public static void closeStage(EventTarget button) {
        if (button instanceof Stage) {
            ((Stage) button).close();
            return;
        }

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
