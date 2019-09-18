package paintexp;

import static utils.ResourceFXUtils.convertToURL;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class ColorChooser extends Application {
    private static final Logger LOG = HasLogging.log();

    private ColorChooserController controller;

    private Stage primaryStage;

    public Color getCurrentColor() {
        return controller.getCurrentColor();
    }

    public void setCurrentColor(Color color) {
        controller.setCurrentColor(color);
    }

    public void setOnSave(Runnable object) {
        controller.setOnSave(() -> {
            object.run();

            primaryStage.close();
        });
    }

    public void setOnUse(Runnable object) {
        controller.setOnUse(() -> {
            object.run();
            primaryStage.close();
        });
    }

    public void show() {
        try {
            start(new Stage());
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public void start(Stage primaryStage1) throws Exception {
        primaryStage = primaryStage1;
        File file = ResourceFXUtils.toFile("ColorChooser.fxml");
        RunnableEx.remap(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(convertToURL(file));
            Parent content = fxmlLoader.load();
            controller = fxmlLoader.getController();
            final int width = 600;
            Scene scene = new Scene(content, width, width / 2);
            primaryStage1.setTitle("Color Chooser");
            primaryStage1.setScene(scene);
            primaryStage1.show();
        }, "ERROR in file " + file);

    }

    public static void main(String[] args) {
        launch(args);
    }
}