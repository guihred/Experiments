package ml;

import java.io.File;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;

public class WorldMapExample extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

		WorldMapGraph canvas = new WorldMapGraph();
        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        //        DataframeML x = new DataframeML.DataframeBuilder("POPULACAO.csv")
        //                .filter("Unit", "Persons"::equals)
        //                .filter("SEX", "TT"::equals)
        //                .filter("Country", e -> !e.toString().matches("World|OECD - Total|G7"))
        //                .filter("SUBJECT", "YP99TLL1_ST"::equals)
        //                .categorize("Country")
        //                .categorize("TIME").build();

        DataframeML x = new DataframeML.DataframeBuilder("out/WDIDataEG.ELC.ACCS.ZS.csv").build();
        canvas.valueHeaderProperty().set("2016");
        canvas.setDataframe(x,
                x.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));
        File file = new File("out");
        Text text = new Text();
        String[] list = file.list();
        ComboBox<String> build = new SimpleComboBoxBuilder<String>().items(list).select("WDIDataEG.ELC.ACCS.ZS.csv")
                .onSelect(s -> {
                    DataframeML x2 = new DataframeML.DataframeBuilder("out/" + s).build();
                    canvas.valueHeaderProperty().set("2016");

                    extracted(text, x2);
                    canvas.setDataframe(x2,
                            x2.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));

        }).build();

        root.getChildren().add(build);
        root.getChildren().add(text);
        root.getChildren().add(canvas);
		theStage.show();
	}

    private void extracted(Text text, DataframeML x2) {

        try {
            text.setText(x2.list("Indicator Name").get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox newSlider(String string, double min, int max, Property<Number> radius) {
        Slider build = new SimpleSliderBuilder().min(min).max(max).build();
        build.valueProperty().bindBidirectional(radius);
        return new VBox(new Text(string), build);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

