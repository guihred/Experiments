package ml;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;

public class TimelineExample extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 1050, 600);
		theStage.setScene(theScene);

        TimelineGraph canvas = new TimelineGraph();

        DataframeML x = DataframeML.builder("out/WDIDataGC.TAX.TOTL.GD.ZS.csv")
                .setMaxSize(46)
                .build();
        canvas.setTitle(x.list("Indicator Name").get(0).toString());
		root.getChildren().add(newSlider("Radius", 1, 375, canvas.radiusProperty()));
		root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSizeProperty()));
		root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
		root.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
		root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
        ObservableList<Entry<String, Color>> itens = FXCollections.observableArrayList();
        canvas.xProportionProperty()
                .addListener((InvalidationListener) o -> itens.setAll(sortedLabels(canvas.colorsProperty())));
        ListView<Entry<String, Color>> listVies = new ListView<>(itens);
		Callback<Entry<String, Color>, ObservableValue<Boolean>> selectedProperty = new MapCallback<>(
                canvas.colorsProperty(), () -> canvas.drawGraph());
        listVies.setCellFactory(
                list -> new CheckColorItemCell(selectedProperty, new ColorConverter(canvas.colorsProperty())));
        canvas.setHistogram(x);
        itens.setAll(sortedLabels(canvas.colorsProperty()));
        File file = new File("out");
        String[] list = file.list();
        ComboBox<String> build2 = new SimpleComboBoxBuilder<String>().items(list).select(0).onSelect(s -> {
            DataframeML x2 = DataframeML.builder("out/" + s).setMaxSize(46).build();
            canvas.setTitle(x2.list("Indicator Name").get(0).toString());
            canvas.setHistogram(x2);
            itens.setAll(sortedLabels(canvas.colorsProperty()));
        }).build();

        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(d -> ResourceFXUtils.take(canvas)).build());
        root.getChildren().add(build2);
        root.getChildren().add(listVies);

        root.getChildren().add(canvas);
		theStage.show();
	}

    private List<Entry<String, Color>> sortedLabels(ObservableMap<String, Color> colorsProperty) {
        return colorsProperty
                .entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).collect(Collectors.toList());
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

