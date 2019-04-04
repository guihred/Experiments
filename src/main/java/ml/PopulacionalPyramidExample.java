package ml;

import static utils.CommonsFX.newButton;
import static utils.CommonsFX.newSlider;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.PopulacionalGraph;
import simplebuilder.SimpleComboBoxBuilder;
import utils.ResourceFXUtils;

public class PopulacionalPyramidExample extends Application {


    private static final int DEFAULT_YEAR = 2000;



    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("Populational Pyramid Example");

        BorderPane root = new BorderPane();

		Predicate<String> asPredicate = Pattern.compile("MA|FE").asPredicate();
        String countryHeader = "Country";
        DataframeML x = DataframeML.builder("POPULACAO.csv")
                .filter("Unit", "Persons"::equals)
                .filter("SEX", e->asPredicate.test(e.toString()))
                .filter("Subject", e -> e.toString().matches("Population.+\\d+"))
                .addCategory(countryHeader)
                .addCategory("TIME")
                .addMapping("Subject", e -> e.toString().replaceAll("Population.+\\) (.+)", "$1"))
                .build();

		PopulacionalGraph canvas = new PopulacionalGraph();
        root.setCenter(canvas);
        VBox left = new VBox();
        root.setLeft(left);
        left.getChildren().add(newSlider("Prop", 1. / 10, 2, canvas.lineSizeProperty()));
        left.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(newSlider("MaxPadding", 10, 1000, canvas.maxLayoutProperty()));
        left.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
        canvas.widthProperty().bind(root.widthProperty().add(-50));

        Set<String> categorize = x.categorize(countryHeader);
        ObservableList<String> observableArrayList = FXCollections.observableArrayList(categorize.stream().sorted().collect(Collectors.toList()));
        ComboBox<String> countryBox = new SimpleComboBoxBuilder<String>()
            .items(observableArrayList)
            .select(0)
            .onSelect(country -> canvas.countryProperty().set(country)).build();
        ComboBox<Integer> year = new SimpleComboBoxBuilder<Integer>()
            .items(canvas.yearsOptionsProperty())
            .onSelect(yearV -> canvas.yearProperty().set(yearV != null ? yearV : DEFAULT_YEAR))
            .select(0).build();

		canvas.setHistogram(x);

        left.getChildren().add(new Text(countryHeader));
        left.getChildren().add(countryBox);
        left.getChildren().add(new Text("Year"));
        left.getChildren().add(year);
        left.getChildren().add(newButton("Export", e -> ResourceFXUtils.take(canvas)));
        double ratio = 3. / 4;
        final int width = 800;
        Scene theScene = new Scene(root, width, width * ratio);
        canvas.lineSizeProperty().set(ratio);
        theStage.setScene(theScene);
		theStage.show();
	}



    public static void main(final String[] args) {
        launch(args);
    }
}

