package paintexp;

import static utils.ResourceFXUtils.convertToURL;

import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class ColorChooser extends Application {
    private static final Logger LOG = HasLogging.log();

    private ColorChooserController controller;

    public Color getCurrentColor() {
        return controller.getCurrentColor();
    }

    public void setCurrentColor(Color color) {
        controller.setCurrentColor(color);
    }

    public void setOnSave(Runnable object) {
        controller.setOnSave(object);
    }

    public void setOnUse(Runnable object) {
        controller.setOnUse(object);
    }

    public void show() {
        try {
            start(new Stage());
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        File file = ResourceFXUtils.toFile("ColorChooser.fxml");
        RunnableEx.remap(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(convertToURL(file));
            Parent content = fxmlLoader.load();
            controller = fxmlLoader.getController();
            Scene scene = new Scene(content, 600, 300);
            primaryStage.setTitle("Color Chooser");
            primaryStage.setScene(scene);
            primaryStage.show();
        }, "ERROR in file " + file);

    }




    public static WritableImage drawTransparentPattern(int size) {
        WritableImage transparentPattern = new WritableImage(size, size);
        return transparentImage(size, transparentPattern);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static WritableImage transparentImage(int size, WritableImage transparentPattern) {
        int squareSize = size / 16;
        for (int x = 0; x < transparentPattern.getWidth(); x++) {
            for (int y = 0; y < transparentPattern.getHeight(); y++) {
                transparentPattern.getPixelWriter().setColor(x, y,
                    x / squareSize % 2 == y / squareSize % 2 ? Color.WHITE : Color.GRAY);
            }
        }
        return transparentPattern;
    }
}