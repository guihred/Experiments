package ml;
import static utils.CommonsFX.newSlider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.WorldMapGraph2;
import simplebuilder.SimpleButtonBuilder;
import utils.ResourceFXUtils;

public class WorldMapExample2 extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("World Map Example 2");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

		WorldMapGraph2 canvas = new WorldMapGraph2();
        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
		root.getChildren().add(newSlider("Radius", -2, 2, canvas.radiusProperty()));
		root.getChildren().add(newSlider("X", -360, 360, canvas.yScaleProperty()));
		root.getChildren().add(newSlider("Y", -360, 360, canvas.xScaleProperty()));
        DataframeML points = DataframeML.builder("cities.csv")

				.build();
		String latDegree = "Lat Degree";
        points.crossFeatureObject(latDegree, WorldMapExample2::convertToDegrees, "Lat Degree", "Lat Minute");
		String lonDegree = "Lon Degree";
        points.crossFeatureObject(lonDegree, WorldMapExample2::convertToDegrees, "Lon Degree", "Lon Minute");
        canvas.valueHeaderProperty().set("Time");
        canvas.setDataframe(points, "Country");
        canvas.setPoints(latDegree, lonDegree);
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());
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

