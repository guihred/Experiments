package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class Fractal extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        OrganicTreeFractal treeFractal = new OrganicTreeFractal();

        VBox radiusSlider = CommonsFX.newSlider("Radius", 1, 200, treeFractal.initialRadiusProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, 2 * Math.PI, treeFractal.deltaAngleProperty());
        VBox ratioSlider = CommonsFX.newSlider("Ratio", 0.5, 0.85, treeFractal.ratioProperty());
        VBox thicknessSlider = CommonsFX.newSlider("Thickness", 0.01, 1., treeFractal.thicknessProperty());
        VBox leafSlider = CommonsFX.newSlider("Leaf", 1, 200, treeFractal.leafProperty());
        primaryStage.setTitle("Tree Fractal");
        primaryStage
                .setScene(new Scene(
                        new VBox(new HBox(thicknessSlider, radiusSlider, ratioSlider, angleSlider, leafSlider),
                                treeFractal)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
