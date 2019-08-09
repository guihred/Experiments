package ml;
import static utils.CommonsFX.newSlider;

import java.util.Map.Entry;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeML;
import ml.graph.MultiLineGraph;
import utils.CommonsFX;
import utils.ResourceFXUtils;

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

        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        left.getChildren().add(newSlider("Radius", 1, 500, canvas.radiusProperty()));
        left.getChildren().add(newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        left.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
        left.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));

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

        left.getChildren()
            .add(CommonsFX.newButton("Export", e -> ResourceFXUtils.take(canvas)));
        root.setCenter(new HBox(canvas, itensList));
		theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }
}

