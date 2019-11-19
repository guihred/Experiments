package ml;



import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.graph.PopulacionalGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.ImageFXUtils;
import utils.SupplierEx;

public class PopulacionalPyramidExample extends Application {

    private static final int DEFAULT_YEAR = 2000;

    @Override
    public void start(final Stage theStage) {
        theStage.setTitle("Populational Pyramid Example");

        BorderPane root = new BorderPane();
        Predicate<String> asPredicate = Pattern.compile("MA|FE").asPredicate();
        String countryHeader = "Country";
        DataframeML x = DataframeBuilder.builder("POPULACAO.csv").filter("Unit", "Persons"::equals)
            .filter("SEX", e -> asPredicate.test(e.toString()))
            .filter("Subject", e -> e.toString().matches("Population.+\\d+")).addCategory(countryHeader)
            .addCategory("TIME").addMapping("Subject", e -> e.toString().replaceAll("Population.+\\) (.+)", "$1"))
            .build();

        PopulacionalGraph canvas = new PopulacionalGraph();
        root.setCenter(canvas);
        VBox left = new VBox();
        root.setLeft(left);
        left.getChildren().add(SimpleSliderBuilder.newSlider("Prop", 1. / 10, 2, canvas.lineSizeProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("Padding", 10, 100, canvas.layoutProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("MaxPadding", 10, 1000, canvas.maxLayoutProperty()));
        left.getChildren().add(SimpleSliderBuilder.newSlider("X Bins", 1, 30, canvas.binsProperty()));
        canvas.widthProperty().bind(root.widthProperty().add(-50));

        Set<String> categorize = x.categorize(countryHeader);
        ObservableList<String> sortedCountries = FXCollections
            .observableArrayList(categorize.stream().sorted().collect(Collectors.toList()));
        ComboBox<String> countryBox = new SimpleComboBoxBuilder<String>().items(sortedCountries).select(0)
            .tooltip("Country")
            .onSelect(country -> canvas.countryProperty().set(country)).build();
        ComboBox<Integer> year = new SimpleComboBoxBuilder<Integer>().items(canvas.yearsOptionsProperty())
            .tooltip("Year")
            .onSelect(yearV -> canvas.yearProperty().set(SupplierEx.nonNull(yearV, DEFAULT_YEAR))).select(0).build();

        canvas.setHistogram(x);

        left.getChildren().add(new Text(countryHeader));
        left.getChildren().add(countryBox);
        left.getChildren().add(new Text("Year"));
        left.getChildren().add(year);
        final Canvas canvas1 = canvas;
        left.getChildren().add(SimpleButtonBuilder.newButton("Export", e -> ImageFXUtils.take(canvas1)));
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
