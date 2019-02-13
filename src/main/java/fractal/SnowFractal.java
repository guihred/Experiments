package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class SnowFractal extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SnowflakeFractal treeFractal = new SnowflakeFractal();

        VBox limitSlider = CommonsFX.newSlider("Limit", 1, 100, treeFractal.limitProperty());
        primaryStage.setTitle("Tree Fractal");
        primaryStage.setScene(new Scene(new VBox(limitSlider, treeFractal)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
