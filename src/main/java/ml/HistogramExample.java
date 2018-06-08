package ml;

import java.util.Map;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;

public class HistogramExample extends Application {


	@Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

        // PieGraph canvas = new PieGraph();
        // PointGraph canvas = new PointGraph();
        HistogramGraph canvas = new HistogramGraph();

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
//        List<Entry<Number, Number>> points = x.createNumberEntries("longitude", "latitude");
        Map<Double, Long> histogram = x.histogram("population", 55);
        // Map<String, Long> collect = histogram.entrySet().stream()
        // .collect(Collectors.toMap(t -> t.getKey() >= 6 ? "Others" :
        // String.format("%.0f Rooms", t.getKey()),
        // Entry<Double, Long>::getValue,
        // (a, b) -> a + b));

		root.getChildren().add(canvas);
//        canvas.setHistogram(collect);
        canvas.setHistogram(histogram);
        // canvas.setPoints(points);
        // root.getChildren().add(newSlider("Radius", 1, 375, canvas.radius));
        root.getChildren().add(newSlider("Line", 1, 10, canvas.lineSize));
        root.getChildren().add(newSlider("Padding", 10, 50, canvas.layout));
        root.getChildren().add(newSlider("X Bins", 10, 30, canvas.bins));
        root.getChildren().add(newSlider("Y Bins", 10, 30, canvas.ybins));

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

