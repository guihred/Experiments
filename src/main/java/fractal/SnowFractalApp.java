package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class SnowFractalApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ShellFractal shellFractal = new ShellFractal();
        SnowflakeFractal snowFractal = new SnowflakeFractal();
        PolygonFractal polygonFractal = new PolygonFractal();

        VBox limitSlider = CommonsFX.newSlider("Limit", 1, 360, shellFractal.limitProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, 360, shellFractal.deltaAngleProperty());
        VBox spiralsSlider = CommonsFX.newSlider("Spirals", 1, 10, shellFractal.spiralsProperty());
        VBox ratioSlider = CommonsFX.newSlider("Ratio", 0.01, 0.99, polygonFractal.ratioProperty());

        snowFractal.limitProperty().bind(shellFractal.spiralsProperty());
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
