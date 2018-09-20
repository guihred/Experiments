package ml;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.ResourceFXUtils;
public class PieGraphExample extends Application {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("Points Graph Example");
        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 600, 600);
		theStage.setScene(theScene);
        PieGraph canvas = new PieGraph();
        DataframeML x = new DataframeML("WDICountry.csv");
        canvas.setDataframe(x, "Region");
        Button exportButton = new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas))
                .build();
        VBox radiusSlider = newSlider("Radius", 1, 375, canvas.radiusProperty());
        VBox binsSlider = newSlider("Bins", 1, 50, canvas.binsProperty());
        root.getChildren().add(new HBox(radiusSlider, binsSlider, exportButton));
        root.getChildren().add(new HBox(canvas));
		theStage.show();
	}

    private VBox newSlider(String string, int min, int max, Property<Number> radius) {
        Slider build = new SimpleSliderBuilder().min(min).max(max).build();
        build.valueProperty().bindBidirectional(radius);
        return new VBox(new Text(string), build);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

