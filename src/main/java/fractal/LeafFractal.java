package fractal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CommonsFX;

public class LeafFractal extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FernFractal treeFractal = new FernFractal();

        VBox limitSlider = CommonsFX.newSlider("Limit", 100, 100_000, treeFractal.limitProperty());
        VBox xScaleSlider = CommonsFX.newSlider("xScale", 100, 500, treeFractal.xScaleProperty());
        VBox yScaleSlider = CommonsFX.newSlider("yScale", 100, 500, treeFractal.yScaleProperty());
        VBox coefSlider = CommonsFX.newSlider("Coef", 0., 1., treeFractal.coefProperty());
        primaryStage.setTitle("Tree Fractal");
        VBox root = new VBox(new HBox(limitSlider, xScaleSlider, yScaleSlider, coefSlider), treeFractal);
        root.setMinWidth(FernFractal.SIZE);
        root.setMinHeight(2 * FernFractal.SIZE);
        treeFractal.widthProperty().bind(root.widthProperty());
        treeFractal.heightProperty().bind(root.heightProperty());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        treeFractal.draw();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
