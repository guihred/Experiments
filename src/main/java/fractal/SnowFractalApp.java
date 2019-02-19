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
        ShellFractal treeFractal = new ShellFractal();
        SnowflakeFractal snowFractal = new SnowflakeFractal();

        VBox limitSlider = CommonsFX.newSlider("Limit", 1, 360, treeFractal.limitProperty());
        VBox angleSlider = CommonsFX.newSlider("Angle", 0, 360, treeFractal.deltaAngleProperty());
        VBox spiralsSlider = CommonsFX.newSlider("Spirals", 1, 10, treeFractal.spiralsProperty());

        snowFractal.limitProperty().bind(treeFractal.spiralsProperty());
        primaryStage.setTitle("Snow Fractal");
        primaryStage
                .setScene(new Scene(
                        new VBox(new HBox(angleSlider, limitSlider, spiralsSlider), treeFractal, snowFractal)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
