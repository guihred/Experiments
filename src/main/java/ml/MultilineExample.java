package ml;

import java.util.Map.Entry;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.MultiLineGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.ImageFXUtils;

public class MultilineExample extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("Multiline Example");

        BorderPane root = new BorderPane();
        Scene theScene = new Scene(root);
		theStage.setScene(theScene);
        VBox left = new VBox();
        root.setLeft(left);
		MultiLineGraph canvas = new MultiLineGraph();

		DataframeML x = DataframeBuilder.build("california_housing_train.csv");
		DataframeUtils.crossFeature(x, "rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        left.getChildren().add(SimpleSliderBuilder.newSlider("Radius", 1, 500, canvas.radiusProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("X Bins", 1, 30, canvas.binsProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));

        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.statsProperty()
                .addListener((InvalidationListener) o -> itens.setAll(canvas.colorsProperty().entrySet()));
        canvas.setTitle("California Housing");
        canvas.setHistogram(x);
        ListView<Entry<String, Color>> itensList = new ListView<>(itens);
		Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(
				canvas.colorsProperty());
        itensList.setCellFactory(
                list -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));
        final Canvas canvas1 = canvas;

        left.getChildren()
            .add(SimpleButtonBuilder.newButton("Export", e -> ImageFXUtils.take(canvas1)));
        root.setCenter(new HBox(canvas, itensList));
		theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }
}

