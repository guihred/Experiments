package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class Fractal extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeFractal treeFractal = new TreeFractal();

        VBox radiusSlider = CommonsFX.newSlider("Radius", 1, 100, treeFractal.initialRadiusProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, Math.PI, treeFractal.deltaAngleProperty());
        VBox ratioSlider = CommonsFX.newSlider("Ratio", 0.5, 0.85, treeFractal.ratioProperty());
        primaryStage.setTitle("Tree Fractal");
        primaryStage.setScene(new Scene(new VBox(radiusSlider, ratioSlider, angleSlider, treeFractal)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
