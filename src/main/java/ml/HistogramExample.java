package ml;

import java.util.Map.Entry;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import simplebuilder.SimpleSliderBuilder;

public class HistogramExample extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

        // PieGraph canvas = new PieGraph();
        // PointGraph canvas = new PointGraph();
		// HistogramGraph canvas = new HistogramGraph();
		MultiLineGraph canvas = new MultiLineGraph();

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
//        List<Entry<Number, Number>> points = x.createNumberEntries("longitude", "latitude");
		// Map<Double, Long> histogram = x.histogram("population", 55);
        // Map<String, Long> collect = histogram.entrySet().stream()
        // .collect(Collectors.toMap(t -> t.getKey() >= 6 ? "Others" :
        // String.format("%.0f Rooms", t.getKey()),
        // Entry<Double, Long>::getValue,
        // (a, b) -> a + b));

//        canvas.setHistogram(collect);
        // canvas.setPoints(points);
		root.getChildren().add(newSlider("Radius", 1, 375, canvas.radius));
		root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSize));
		root.getChildren().add(newSlider("Padding", 10, 100, canvas.layout));
		root.getChildren().add(newSlider("X Bins", 1, 30, canvas.bins));
		root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybins));

        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.stats.addListener((InvalidationListener) o -> {
            itens.setAll(canvas.colors.entrySet());
        });
        canvas.setHistogram(x);
        ListView<Entry<String, Color>> e = new ListView<>(itens);
        Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(canvas.colors);
        e.setCellFactory(list -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colors)));

        root.getChildren().add(e);
        root.getChildren().add(canvas);
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

