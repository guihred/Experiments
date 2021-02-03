package ml;

import static simplebuilder.SimpleSliderBuilder.newSlider;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.graph.TimelineGraph;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CSVUtils;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

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

        String[] list = CSVUtils.getDataframeCSVs();
        DataframeML x = DataframeBuilder.builder(ResourceFXUtils.getOutFile(list[0])).setMaxSize(MAX_ROWS).build();
        LOG.info("Available Columns {}", x.cols());
        String indicatorName = "Indicator Name";
        canvas.prefWidth(500);
        canvas.setTitle(x.list(indicatorName).get(0).toString());
        left.getChildren().add(newSlider("Radius", 1, 500, canvas.radiusProperty()));
        final int max = 50;
        left.getChildren().add(newSlider("Line", 1, max, canvas.lineSizeProperty()));
        left.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(newSlider("X Bins", 1, max, canvas.binsProperty()));
        left.getChildren().add(newSlider("Y Bins", 1, max, canvas.ybinsProperty()));
        ProgressIndicator progress = new ProgressIndicator(0);
        left.getChildren().add(progress);
        CheckBox e = new CheckBox("Show Labels");
        e.selectedProperty().bindBidirectional(canvas.showLabelsProperty());
        left.getChildren().add(e);
        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.xProportionProperty().addListener(o -> itens.setAll(sortedLabels(canvas.colorsProperty())));
        ListView<Entry<String, Color>> listVies = new ListView<>(itens);
        MapCallback<String, Color> selectedProperty =
                new MapCallback<>(canvas.colorsProperty(), canvas::drawGraph);
        listVies.setCellFactory(
                l -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));

        String countryNameColumn = x.cols().stream().findFirst().orElse("﻿Country Name");
        canvas.setHistogram(x, countryNameColumn);
        itens.setAll(sortedLabels(canvas.colorsProperty()));
        SimpleComboBoxBuilder<String> items = new SimpleComboBoxBuilder<String>().items(list);
        items.onSelect(s -> RunnableEx.runNewThread(() -> DataframeBuilder.builder(ResourceFXUtils.getOutFile(s))
                .setMaxSize(MAX_ROWS).build(progress.progressProperty()), x2 -> CommonsFX.runInPlatform(() -> {
                    canvas.setTitle(x2.list(indicatorName).get(0).toString());
                    canvas.setHistogram(x2, countryNameColumn);
                    itens.setAll(sortedLabels(canvas.colorsProperty()));
                })));
        left.getChildren().add(items.select(0).build());
        left.getChildren().add(SimpleButtonBuilder.newButton("Export", d -> ImageFXUtils.take(canvas)));
        root.setCenter(new HBox(canvas, listVies));
        theStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

    private static List<Entry<String, Color>> sortedLabels(ObservableMap<String, Color> colorsProperty) {
        return colorsProperty.entrySet().stream().sorted(Comparator.comparing(Entry<String, Color>::getKey))
                .collect(Collectors.toList());
    }
}
