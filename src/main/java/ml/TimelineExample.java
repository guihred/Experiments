package ml;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.graph.TimelineGraph;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.HasLogging;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;

public class TimelineExample extends Application {


    private static final int MAX_ROWS = 46;
    private static final Logger LOG = HasLogging.log();

	@Override
	public void start(final Stage theStage) {
		theStage.setTitle("Timeline Example");
        BorderPane root = new BorderPane();
        VBox left = new VBox();
        Scene theScene = new Scene(root);
		theStage.setScene(theScene);
        root.setLeft(left);
        TimelineGraph canvas = new TimelineGraph();

        String[] list = ResourceFXUtils.toFile("out")
            .list((dir, name) -> name.matches("WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv"));
        DataframeML x = DataframeBuilder.builder("out/" + list[0])
                .setMaxSize(MAX_ROWS)
                .build();
        LOG.info("Available Columns {}", x.cols());
        canvas.prefWidth(500);
        canvas.setTitle(x.list("Indicator Name").get(0).toString());
        left.getChildren().add(SimpleSliderBuilder.newSlider("Radius", 1, 500, canvas.radiusProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("X Bins", 1, 30, canvas.binsProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.xProportionProperty()
                .addListener(o -> itens.setAll(sortedLabels(canvas.colorsProperty())));
        ListView<Entry<String, Color>> listVies = new ListView<>(itens);
		Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(
                canvas.colorsProperty(), canvas::drawGraph);
        listVies.setCellFactory(
            l -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));

        String countryNameColumn = x.cols().stream().findFirst().orElse("﻿Country Name");
        canvas.setHistogram(x, countryNameColumn);
        itens.setAll(sortedLabels(canvas.colorsProperty()));
        ComboBox<String> indicators = new SimpleComboBoxBuilder<String>().items(list)
            .select(0).onSelect(s -> {
                DataframeML x2 = DataframeBuilder.builder("out/" + s).setMaxSize(MAX_ROWS).build();
                canvas.setTitle(x2.list("Indicator Name").get(0).toString());
                canvas.setHistogram(x2, countryNameColumn);
                itens.setAll(sortedLabels(canvas.colorsProperty()));
            }).build();

        left.getChildren().add(indicators);
        final Canvas canvas1 = canvas;
        left.getChildren().add(SimpleButtonBuilder.newButton("Export", d -> ImageFXUtils.take(canvas1)));
        root.setCenter(new HBox(canvas, listVies));

		theStage.show();
	}


    public static void main(final String[] args) {
        launch(args);
    }

    private static List<Entry<String, Color>> sortedLabels(final ObservableMap<String, Color> colorsProperty) {
        return colorsProperty.entrySet().stream().sorted(Comparator.comparing(Entry<String, Color>::getKey))
                .collect(Collectors.toList());
    }
}

