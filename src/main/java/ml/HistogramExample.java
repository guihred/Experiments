package ml;

import java.util.Map.Entry;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeML;
import ml.graph.HistogramGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.ResourceFXUtils;

public class HistogramExample extends Application {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("Histogram Example");

        FlowPane root = new FlowPane();
        final int width = 800;
        final int height = 600;
        Scene theScene = new Scene(root, width, height);
		theStage.setScene(theScene);

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        HistogramGraph canvas = new HistogramGraph();
        canvas.setTitle("California Housing");
        root.getChildren().add(newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        root.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
        root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));

        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.statsProperty().addListener((InvalidationListener) o -> {
            Set<Entry<String, Color>> entrySet = canvas.colorsProperty().entrySet();
            itens.setAll(entrySet);
        });
        canvas.setHistogram(x);
        ListView<Entry<String, Color>> itensList = new ListView<>(itens);
        Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(
                canvas.colorsProperty());
        itensList.setCellFactory(
                list -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());

        root.getChildren().add(itensList);
        root.getChildren().add(canvas);
		theStage.show();
	}

    private VBox newSlider(String string, int min, int max, Property<Number> radius) {
        return new VBox(new Text(string),
                new SimpleSliderBuilder().min(min).max(max).bindBidirectional(radius).build());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

