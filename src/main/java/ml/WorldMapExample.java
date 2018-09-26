package ml;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.WorldMapGraph;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.*;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class WorldMapExample extends Application implements HasLogging {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("World Map Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);
		WorldMapGraph canvas = new WorldMapGraph();
        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        DataframeML x = DataframeML.builder("out/WDIDataEG.ELC.ACCS.ZS.csv").build();
        canvas.valueHeaderProperty().set("2016");
        canvas.setDataframe(x,
                x.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));
        Text text = new Text();
        File file = ResourceFXUtils.toFile("out");
        String[] list = file.list((dir, name) -> name.endsWith(".csv"));
        ComboBox<String> build2 = new SimpleComboBoxBuilder<String>().items("2016").select("2016")
                .onSelect(canvas.valueHeaderProperty()::set).build();

        ComboBox<String> build = new SimpleComboBoxBuilder<String>().items(list).select("WDIDataEG.ELC.ACCS.ZS.csv")
                .onSelect(s -> {
                    DataframeML x2 = DataframeML.builder("out/" + s).build();
                    ObservableList<String> itens = FXCollections.observableArrayList(
                            x2.cols().stream().filter(StringUtils::isNumeric).sorted()
                                    .filter(e -> x2.list(e).stream().anyMatch(Objects::nonNull))
                                    .collect(Collectors.toList()));

                    build2.setItems(itens);
                    updateIndicatorName(text, x2);
                    if (!itens.contains(canvas.valueHeaderProperty().get())) {
                        build2.getSelectionModel().select(itens.size() - 1);
                    }
                    canvas.setDataframe(x2,
                            x2.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));
                }).build();

        root.getChildren().add(build);
        root.getChildren().add(build2);
        root.getChildren().add(text);
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());

        root.getChildren().add(canvas);
		theStage.show();
	}

    private VBox newSlider(String string, double min, int max, Property<Number> radius) {
        Slider build = new SimpleSliderBuilder().min(min).max(max).build();
        build.valueProperty().bindBidirectional(radius);
        return new VBox(new Text(string), build);
    }

    private void updateIndicatorName(Text text, DataframeML x2) {

        try {
            text.setText(x2.list("Indicator Name").get(0).toString());
        } catch (Exception e) {
            getLogger().error("ERROR CHANGING INDICATOR", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

