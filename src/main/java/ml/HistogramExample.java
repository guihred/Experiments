package ml;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

        PieGraph canvas = new PieGraph();
        // LineGraph canvas = new LineGraph();
        // HistogramGraph canvas = new HistogramGraph();

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> d[0] / d[1], "total_rooms", "population");
        Map<Double, Long> histogram = x.histogram("rooms_per_person", 20);
        Map<String, Long> collect = histogram.entrySet().stream()
                .collect(Collectors.toMap(t -> String.format("%.0f Rooms", t.getKey()), Entry<Double, Long>::getValue,
                        (a, b) -> a + b));

		root.getChildren().add(canvas);
        canvas.setHistogram(collect);

		theStage.show();
	}

    public static void main(String[] args) {
        launch(args);
    }
}

