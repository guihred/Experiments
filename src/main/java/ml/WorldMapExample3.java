package ml;
import static utils.CommonsFX.newSlider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.WorldMapGraph;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CommonsFX;

public class WorldMapExample3 extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("World Map Example 3");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, WorldMapGraph.WIDTH / 2, WorldMapGraph.HEIGHT / 2);
		theStage.setScene(theScene);
        WorldMapGraph canvas = new WorldMapGraph();

        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        root.getChildren().add(newSlider("Font Size", 1, 60, canvas.fontSizeProperty()));
        DataframeML x = new DataframeML("WDICountry.csv");
        canvas.valueHeaderProperty().set("Currency Unit");
        canvas.setDataframe(x,
                x.cols().stream().filter(e -> e.contains("able N")).findFirst().orElse("﻿Table Name"));
        ComboBox<String> build = new SimpleComboBoxBuilder<String>().items(x.cols()).select("Currency Unit")
                .onSelect(canvas.valueHeaderProperty()::set).build();
        root.getChildren().add(build);
        root.getChildren()
                .add(CommonsFX.newButton("Export", e -> canvas.takeSnapshot()));
        root.getChildren().add(canvas);
		theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }
}

