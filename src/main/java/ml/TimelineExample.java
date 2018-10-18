package ml;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import ml.data.DataframeML;
import ml.graph.TimelineGraph;
import org.slf4j.Logger;
import simplebuilder.*;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class TimelineExample extends Application {


    private static final Logger LOG = HasLogging.log();

	@Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");
        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 1050, 600);
		theStage.setScene(theScene);

        TimelineGraph canvas = new TimelineGraph();

        DataframeML x = DataframeML.builder("out/WDIDataGC.TAX.TOTL.GD.ZS.csv")
                .setMaxSize(46)
                .build();
        canvas.setTitle(x.list("Indicator Name").get(0).toString());
		root.getChildren().add(newSlider("Radius", 1, 375, canvas.radiusProperty()));
		root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSizeProperty()));
		root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
		root.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
		root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
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
        ComboBox<String> build2 = new SimpleComboBoxBuilder<String>().items(list).select(0).onSelect(s -> {
            DataframeML x2 = DataframeML.builder("out/" + s).setMaxSize(46).build();
            canvas.setTitle(x2.list("Indicator Name").get(0).toString());
            canvas.setHistogram(x2, countryNameColumn);
            itens.setAll(sortedLabels(canvas.colorsProperty()));
        }).build();

        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(d -> ResourceFXUtils.take(canvas)).build());
        root.getChildren().add(build2);
        root.getChildren().add(listVies);

        root.getChildren().add(canvas);
		theStage.show();
	}

    private VBox newSlider(String string, int min, int max, Property<Number> radius) {
        return new VBox(new Text(string), new SimpleSliderBuilder()
                .min(min)
                .max(max)
                .bindBidirectional(radius)
                .build());
    }

    private List<Entry<String, Color>> sortedLabels(ObservableMap<String, Color> colorsProperty) {
        return colorsProperty
                .entrySet().stream().sorted(Comparator.comparing(Entry<String, Color>::getKey))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

