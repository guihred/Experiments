package ml;

import java.util.Objects;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

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
        //        System.out.println(x.list("Country").stream().sorted().collect(Collectors.toSet()));
        x.filterString("TRANSACT", "B1_GA"::equalsIgnoreCase);
        x.filterString("Unit Code", "USD"::equalsIgnoreCase);
        System.out.println("COUNTRIES NOT FOUND: " + x.list("Country").stream().distinct().map(Objects::toString)
                .filter(e -> !Country.hasName(e))
                .sorted().collect(Collectors.toSet()));

        x.logln(x);

        canvas.setDataframe(x, "Country");
        //        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList()
        // canvas.stats.addListener((InvalidationListener) o -> {
        // Set<Entry<String, Color>> entrySet = canvas.colors.entrySet();
        // itens.setAll(entrySet);
        // });
		// canvas.setHistogram(x);
        root.getChildren().add(canvas);
		theStage.show();
	}


    public static void main(String[] args) {
        launch(args);
    }
}

