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
        TreeFractal tree = new TreeFractal();

        VBox radiusSlider = CommonsFX.newSlider("Radius", 1, 360, treeFractal.initialRadiusProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, 2 * Math.PI, treeFractal.deltaAngleProperty());
        final double ratioMax = 0.85;
        VBox ratioSlider = CommonsFX.newSlider("Ratio", 0.5, ratioMax, treeFractal.ratioProperty());
        VBox thicknessSlider = CommonsFX.newSlider("Thickness", 1. / 100, 1., treeFractal.thicknessProperty());
        VBox leafSlider = CommonsFX.newSlider("Leaf", 1, 360, treeFractal.leafProperty());
        tree.initialRadiusProperty().bind(treeFractal.initialRadiusProperty());
        tree.deltaAngleProperty().bind(treeFractal.deltaAngleProperty());
        tree.ratioProperty().bind(treeFractal.ratioProperty());

        primaryStage.setTitle("Tree Fractal");
        primaryStage
                .setScene(new Scene(
                        new VBox(new HBox(thicknessSlider, radiusSlider, ratioSlider, angleSlider, leafSlider),
                                new HBox(treeFractal, tree))));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
