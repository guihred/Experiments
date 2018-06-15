package ml;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;

public class WorldMapExample extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

		WorldMapGraph canvas = new WorldMapGraph();
        // root.getChildren().add(newSlider("Radius", 1, 375, canvas.radius));
        // root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSize));
        // root.getChildren().add(newSlider("Padding", 10, 100, canvas.layout));
        // root.getChildren().add(newSlider("X Bins", 1, 30, canvas.bins));
        // root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybins));
        DataframeML x = new DataframeML("globalGDP.csv");
        // x.describe();
        x.logln(x);
        System.out.println(x.list("Country").stream().sorted().collect(Collectors.toSet()));
        x.filterString("TRANSACT", "B1_GA"::equalsIgnoreCase);
        x.filterString("Unit Code", "USD"::equalsIgnoreCase);
        System.out.println(x.list("Country").stream().sorted().collect(Collectors.toSet()));

        x.logln(x);

        canvas.setDataframe(x);
        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        // canvas.stats.addListener((InvalidationListener) o -> {
        // Set<Entry<String, Color>> entrySet = canvas.colors.entrySet();
        // itens.setAll(entrySet);
        // });
		// canvas.setHistogram(x);
        root.getChildren().add(canvas);
		theStage.show();
	}

    private VBox newSlider(String string, int min, int max, Property<Number> radius) {
        Slider build = new SimpleSliderBuilder().min(min).max(max).build();
        build.valueProperty().bindBidirectional(radius);
        return new VBox(new Text(string), build);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

