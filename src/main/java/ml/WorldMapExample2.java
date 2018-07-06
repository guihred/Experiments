package ml;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleSliderBuilder;

public class WorldMapExample2 extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

		WorldMapGraph2 canvas = new WorldMapGraph2();
        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
		root.getChildren().add(newSlider("Radius", -2, 2, canvas.radiusProperty()));
		root.getChildren().add(newSlider("X", -360, 360, canvas.yScaleProperty()));
		root.getChildren().add(newSlider("Y", -360, 360, canvas.xScaleProperty()));
        DataframeML points = new DataframeML.DataframeBuilder(ResourceFXUtils.toFullPath("cities.csv"))

				.build();
		String latDegree = "Lat Degree";
        points.crossFeatureObject(latDegree, WorldMapExample2::convertToDegrees, "Lat Degree", "Lat Minute");
		String lonDegree = "Lon Degree";
        points.crossFeatureObject(lonDegree, WorldMapExample2::convertToDegrees, "Lon Degree", "Lon Minute");
        canvas.valueHeaderProperty().set("Time");
        canvas.setDataframe(points, "Country");
		canvas.setPoints(points, latDegree, lonDegree);
        root.getChildren().add(canvas);
		theStage.show();
	}

	private static double convertToDegrees(Object[] d) {
		String string = d[1].toString();
		int i = string.contains("W") || string.contains("S") ? -1 : 1;
		double angdeg = ((Number) d[0]).doubleValue() + Double.parseDouble(string.replaceAll("\\s\\w", "")) / 60;
		return i * angdeg;
	}


    private VBox newSlider(String string, double min, int max, Property<Number> radius) {
        Slider build = new SimpleSliderBuilder().min(min).max(max).build();
        build.valueProperty().bindBidirectional(radius);
		build.setBlockIncrement((max - min) / 100);

        return new VBox(new Text(string), build);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

