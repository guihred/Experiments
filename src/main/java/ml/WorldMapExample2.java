package ml;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.WorldMapGraph;
import ml.graph.WorldMapGraph2;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;

public class WorldMapExample2 extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("World Map Example 2");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, WorldMapGraph.WIDTH / 2, WorldMapGraph.HEIGHT / 2);
		theStage.setScene(theScene);

		WorldMapGraph2 canvas = new WorldMapGraph2();
        root.getChildren().add(SimpleSliderBuilder.newSlider("Labels", 1, 10, canvas.binsProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("Radius", -2, 2, canvas.radiusProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("X", -360, 360, canvas.yScaleProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("Y", -360, 360, canvas.xScaleProperty()));
        root.getChildren().add(CommonsFX.newCheck("Neighbors", canvas.showNeighborsProperty()));
        DataframeML points = DataframeBuilder.builder("cities.csv").build();
		String latDegree = "Lat Degree";
		DataframeUtils.crossFeatureObject(points, latDegree, WorldMapExample2::convertToDegrees, latDegree,
				"Lat Minute");
		String lonDegree = "Lon Degree";
		DataframeUtils.crossFeatureObject(points, lonDegree, WorldMapExample2::convertToDegrees, lonDegree,
				"Lon Minute");
        canvas.valueHeaderProperty().set("Time");
        canvas.setDataframe(points, "Country");
        canvas.setPoints(latDegree, lonDegree);
        final Canvas canvas1 = canvas;
        root.getChildren()
            .add(SimpleButtonBuilder.newButton("Export", e -> ImageFXUtils.take(canvas1)));
        root.getChildren().add(canvas);
        theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }

    private static double convertToDegrees(final Object[] d) {
		String string = d[1].toString();
		int i = string.contains("W") || string.contains("S") ? -1 : 1;
		double angdeg = ((Number) d[0]).doubleValue() + Double.parseDouble(string.replaceAll("\\s\\w", "")) / 60;
		return i * angdeg;
	}
}

