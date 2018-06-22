package ml;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
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
        root.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        //        DataframeML x = new DataframeML.DataframeBuilder("WDICountry.csv")
        //                .categorize("Short Name")
        //                .categorize("Region")
        //                .build();
        DataframeML x = new DataframeML.DataframeBuilder("POPULACAO.csv")
                .filter("Unit", "Persons"::equals)
                .filter("SEX", "TT"::equals)
                .filter("Country", e -> !e.toString().matches("World|OECD - Total|G7"))
                .filter("SUBJECT", "YP99TLL1_ST"::equals)
                .categorize("Country")
                .categorize("TIME").build();
        System.out.println(x);
        System.out.println(x.categorize("Country"));
		
//        DataframeML x = new DataframeML.DataframeBuilder("globalGDP.csv")
//                .filter("TRANSACT", "B1_GA"::equals)
//                .filter("Unit Code", "USD"::equals).categorize("Country").build();
//        // x.describe();
//        x.logln(x);
//        //        System.out.println(x.list("Country").stream().sorted().collect(Collectors.toSet()));
//        System.out.println("COUNTRIES NOT FOUND: " + x.list("Country").stream().distinct().map(Objects::toString)
//                .filter(e -> !Country.hasName(e))
//                .sorted().collect(Collectors.toSet()));
//
        //        ComboBox<String> year = new SimpleComboBoxBuilder<String>()
        //                .items(x.categorize("TIME").stream().sorted().collect(Collectors.toList())).onSelect(e -> {
        //                    Integer valueOf = Integer.valueOf(e);
        //                    canvas.filter("TIME", valueOf::equals);
        //                    canvas.drawGraph();
        //                }).select("2000").build();
        
        //        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList()
        // canvas.stats.addListener((InvalidationListener) o -> {
        // Set<Entry<String, Color>> entrySet = canvas.colors.entrySet();
        // itens.setAll(entrySet);
        // });
        //        canvas.valueHeaderProperty().set("System of trade");
        canvas.setDataframe(x, "Country");
        //        canvas.coloring();

        //        root.getChildren().add(year);
        root.getChildren().add(canvas);
		theStage.show();
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

