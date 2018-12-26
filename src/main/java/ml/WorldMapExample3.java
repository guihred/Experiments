package ml;
import static utils.CommonsFX.newSlider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.WorldMapGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.ResourceFXUtils;

public class WorldMapExample3 extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("World Map Example 3");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);
        WorldMapGraph canvas = new WorldMapGraph();

        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        DataframeML x = new DataframeML("WDICountry.csv");
        canvas.valueHeaderProperty().set("Currency Unit");
        canvas.setDataframe(x,
                x.cols().stream().filter(e -> e.contains("able N")).findFirst().orElse("﻿Table Name"));
        Text text = new Text();
        ComboBox<String> build = new SimpleComboBoxBuilder<String>().items(x.cols()).select("Currency Unit")
                .onSelect(s -> {
                    canvas.valueHeaderProperty().set(s);
                    text.setText(s);
                }).build();
        root.getChildren().add(build);
        root.getChildren().add(text);
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());
        root.getChildren().add(canvas);
		theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }
}

