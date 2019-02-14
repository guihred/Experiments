package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class SnowFractal extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ShellFractal treeFractal = new ShellFractal();

        VBox limitSlider = CommonsFX.newSlider("Limit", 1, 200, treeFractal.limitProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, 200, treeFractal.deltaAngleProperty());
        VBox spiralsSlider = CommonsFX.newSlider("Angle", 1, 20, treeFractal.spiralsProperty());
        primaryStage.setTitle("Tree Fractal");
        primaryStage.setScene(new Scene(new VBox(new HBox(angleSlider, limitSlider, spiralsSlider), treeFractal)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
