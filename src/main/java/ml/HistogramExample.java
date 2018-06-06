package ml;
import java.util.Map;

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

		HistogramGraph canvas = new HistogramGraph();

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> d[0] / d[1], "total_rooms", "population");
        Map<Double, Long> histogram = x.histogram("total_rooms", 20);
		root.getChildren().add(canvas);
        canvas.setHistogram(histogram);

		theStage.show();
	}

    public static void main(String[] args) {
        launch(args);
    }
}

