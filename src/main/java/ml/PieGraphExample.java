package ml;
import static utils.CommonsFX.newSlider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.PieGraph;
import utils.CommonsFX;
import utils.ResourceFXUtils;
public class PieGraphExample extends Application {

    private static final int SIZE = 650;

    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("Points Graph Example");
        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, SIZE, SIZE);
		theStage.setScene(theScene);
        PieGraph canvas = new PieGraph();
        DataframeML x = DataframeML.builder("WDICountry.csv").build();
        canvas.setDataframe(x, "Region");
        Button exportButton = CommonsFX.newButton("Export", e -> ResourceFXUtils.take(canvas));
        VBox radiusSlider = newSlider("Radius", 1, 500, canvas.radiusProperty());
        VBox binsSlider = newSlider("Bins", 1, 50, canvas.binsProperty());
        VBox xSlider = newSlider("X", 1, SIZE, canvas.xOffsetProperty());
        VBox propSlider = newSlider("Legend Distance", 0, 1., canvas.legendsRadiusProperty());
        root.getChildren().add(new HBox(radiusSlider, binsSlider, xSlider, propSlider, exportButton));
        root.getChildren().add(new HBox(canvas));
		theStage.show();
	}


    public static void main(final String[] args) {
        launch(args);
    }
}

