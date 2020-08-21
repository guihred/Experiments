package simplebuilder;

import static utils.RunnableEx.runIf;

import com.google.common.io.Files;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import utils.*;

public final class StageHelper {

    private StageHelper() {
    }

    public static void closeStage(EventTarget button) {
        Optional.ofNullable((Node) button).map(Node::getScene).map(sc -> (Stage) sc.getWindow())
                .ifPresent(Stage::close);
    }

    public static Stage displayCSSStyler(Scene scene, String pathname) {
        String str = TreeElement.displayStyleClass(scene.getRoot());
        HasLogging.log(1).info("{}", str);
        File file = new File("src/main/resources/css/" + pathname);
        TextArea textArea = new TextArea(getText(file));
        if (file.exists()) {
            RunnableEx.ignore(() -> scene.getStylesheets().add(ResourceFXUtils.convertToURL(file).toString()));
        }
        Button saveButton = FileChooserBuilder.newButton("_Save", e -> RunnableEx.run(() -> {
            try (PrintStream fileOutputStream = new PrintStream(file, StandardCharsets.UTF_8.name())) {
                fileOutputStream.print(textArea.getText());
                fileOutputStream.flush();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(ResourceFXUtils.convertToURL(file).toString());
                textArea.requestFocus();
            }
        }));
        Stage stage2 = new SimpleDialogBuilder().node(new VBox(textArea, saveButton)).bindWindow(scene.getRoot())
                .height(500).displayDialog();
        textArea.prefHeightProperty().bind(stage2.heightProperty().subtract(10));

        Window windowc = scene.getWindow();
        closeIfPossible(stage2, windowc);
        scene.windowProperty().addListener((ob, o, window) -> closeIfPossible(stage2, window));
        return stage2;
    }

    private static void closeBoth(Stage stage2, EventHandler<WindowEvent> onCloseRequest, WindowEvent e) {
        runIf(onCloseRequest, o -> o.handle(e));
        stage2.close();
    }

    private static void closeIfPossible(Stage stage2, Window windowc) {
        runIf(windowc, window -> {
            EventHandler<WindowEvent> closeRequest = window.getOnCloseRequest();
            window.setOnCloseRequest(e -> closeBoth(stage2, closeRequest, e));
            window.showingProperty().addListener(e -> closeBoth(stage2, closeRequest, null));
        });
    }

    private static String getText(File file) {
        return file.exists() ? SupplierEx.get(() -> Files.toString(file, StandardCharsets.UTF_8), "") : "";
    }
}
