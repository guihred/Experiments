package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;

public class SnowFractalApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ShellFractal shellFractal = new ShellFractal();
        SnowflakeFractal snowFractal = new SnowflakeFractal();
        PolygonFractal polygonFractal = new PolygonFractal();

        VBox limitSlider = SimpleSliderBuilder.newSlider("Limit", 1, 360, shellFractal.limitProperty());
        VBox angleSlider = SimpleSliderBuilder.newSlider("Angle", 0, 360, shellFractal.deltaAngleProperty());
        VBox spiralsSlider = SimpleSliderBuilder.newSlider("Spirals", 2, 10, shellFractal.spiralsProperty());
        final double min = 1. / 100;
        VBox ratioSlider = SimpleSliderBuilder.newSlider("Ratio", min, 1 - min, polygonFractal.ratioProperty());
        snowFractal.limitProperty().bind(shellFractal.spiralsProperty());
        snowFractal.sizeProperty().bind(shellFractal.deltaAngleProperty());
        polygonFractal.spiralsProperty().bind(shellFractal.spiralsProperty());
        polygonFractal.limitProperty().bind(shellFractal.limitProperty());
        primaryStage.setTitle("Snow Fractal");
        primaryStage
                .setScene(new Scene(
                        new VBox(new HBox(angleSlider, limitSlider, spiralsSlider, ratioSlider),
                                new HBox(polygonFractal, shellFractal, snowFractal))));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
