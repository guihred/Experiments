package ml;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;

public class PopulacionalPyramidExample extends Application {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("Populational Pyramid Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 800, 600);
		theStage.setScene(theScene);

		Predicate<String> asPredicate = Pattern.compile("MA|FE").asPredicate();
        DataframeML x = DataframeML.builder("POPULACAO.csv")
                .filter("Unit", "Persons"::equals)
                .filter("SEX", e->asPredicate.test(e.toString()))
                .filter("Subject", e -> e.toString().matches("Population.+\\d+"))
                .addCategory("Country")
                .addCategory("TIME")
                .addMapping("Subject", e -> e.toString().replaceAll("Population.+\\) (.+)", "$1"))
                .build();

		PopulacionalGraph canvas = new PopulacionalGraph();
		root.getChildren().add(newSlider("Prop", 0.1, 2, canvas.lineSizeProperty()));
        root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
		root.getChildren().add(newSlider("MaxPadding", 10, 900, canvas.maxLayoutProperty()));
        root.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
		canvas.widthProperty().bind(root.widthProperty().add(-20));

        Set<String> categorize = x.categorize("Country");
        ComboBox<String> comboBox = new SimpleComboBoxBuilder<String>()
                .items(FXCollections.observableArrayList(categorize.stream().sorted().collect(Collectors.toList())))
                .select(0)
                .onSelect(v -> canvas.countryProperty().set(v)).build();
        ComboBox<Integer> year = new SimpleComboBoxBuilder<Integer>()
                .items(canvas.yearsOptionsProperty())
                .onSelect(v -> canvas.yearProperty().set(v != null ? v : 2000)).build();

		canvas.lineSizeProperty().set(canvas.getHeight() / canvas.getWidth());
		canvas.maxLayoutProperty().set(canvas.getWidth() - canvas.layoutProperty().doubleValue() - 20);

		canvas.setHistogram(x);

        root.getChildren().add(comboBox);
        root.getChildren().add(year);
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

