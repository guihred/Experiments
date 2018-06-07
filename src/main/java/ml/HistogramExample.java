package ml;

import java.util.List;
import java.util.Map.Entry;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HistogramExample extends Application {


	@Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

        // PieGraph canvas = new PieGraph();
        PointGraph canvas = new PointGraph();
        // HistogramGraph canvas = new HistogramGraph();

        DataframeML x = new DataframeML("california_housing_train.csv");
        // x.crossFeature("rooms_per_person", d -> d[0] / d[1], "total_rooms",
        // "population");
        List<Entry<Number, Number>> points = x.createNumberEntries("longitude", "latitude");
        // Map<String, Long> collect = histogram.entrySet().stream()
        // .collect(Collectors.toMap(t -> t.getKey() >= 6 ? "Others" :
        // String.format("%.0f Rooms", t.getKey()),
        // Entry<Double, Long>::getValue,
        // (a, b) -> a + b));

		root.getChildren().add(canvas);
        canvas.setPoints(points);

		theStage.show();
	}

    public static void main(String[] args) {
        launch(args);
    }
}

