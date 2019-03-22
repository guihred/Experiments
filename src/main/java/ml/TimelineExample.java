package ml;
import static utils.CommonsFX.newSlider;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeML;
import ml.graph.TimelineGraph;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CommonsFX;
import utils.HasLogging;
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

        DataframeML x = DataframeML.builder("out/WDIDataGC.TAX.TOTL.GD.ZS.csv")
                .setMaxSize(MAX_ROWS)
                .build();
        canvas.prefWidth(500);
        canvas.setTitle(x.list("Indicator Name").get(0).toString());
        left.getChildren().add(newSlider("Radius", 1, 500, canvas.radiusProperty()));
        left.getChildren().add(newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        left.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
        left.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.xProportionProperty()
                .addListener(o -> itens.setAll(sortedLabels(canvas.colorsProperty())));
        ListView<Entry<String, Color>> listVies = new ListView<>(itens);
		Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(
                canvas.colorsProperty(), canvas::drawGraph);
        listVies.setCellFactory(
                list -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));

        LOG.info("Available Columns {}", x.cols());
        String countryNameColumn = x.cols().stream().findFirst().orElse("﻿Country Name");
        canvas.setHistogram(x, countryNameColumn);
        itens.setAll(sortedLabels(canvas.colorsProperty()));
        String[] list = ResourceFXUtils.toFile("out").list((dir, name) -> name.endsWith(".csv"));
        ComboBox<String> indicators = new SimpleComboBoxBuilder<String>().items(list)
            .select(0).onSelect(s -> {
                DataframeML x2 = DataframeML.builder("out/" + s).setMaxSize(MAX_ROWS).build();
                canvas.setTitle(x2.list("Indicator Name").get(0).toString());
                canvas.setHistogram(x2, countryNameColumn);
                itens.setAll(sortedLabels(canvas.colorsProperty()));
            }).build();

        left.getChildren().add(indicators);
        left.getChildren().add(CommonsFX.newButton("Export", d -> ResourceFXUtils.take(canvas)));
        root.setCenter(new HBox(canvas, listVies));

		theStage.show();
	}


    private List<Entry<String, Color>> sortedLabels(final ObservableMap<String, Color> colorsProperty) {
        return colorsProperty.entrySet().stream().sorted(Comparator.comparing(Entry<String, Color>::getKey))
                .collect(Collectors.toList());
    }

    public static void main(final String[] args) {
        launch(args);
    }
}

