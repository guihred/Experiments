package paintexp;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.CommonsFX;

public class ColorChooser extends Application {

    private ColorChooserController controller = new ColorChooserController();

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

    @Override
    public void start(Stage primaryStage1) {
        primaryStage = primaryStage1;
        final int width = 600;
        CommonsFX.loadFXML("Color Chooser", "ColorChooser.fxml", controller, primaryStage1, width, width / 2);

    }

    public static void main(String[] args) {
        launch(args);
    }
}