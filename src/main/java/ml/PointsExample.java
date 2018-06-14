package ml;

import java.util.List;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
public class PointsExample extends Application {


    @Override
	public void start(Stage theStage) {
		theStage.setTitle("Timeline Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 1100, 600);
		theStage.setScene(theScene);

        // PieGraph canvas = new PieGraph();
        PointGraph canvas = new PointGraph();
        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        // HistogramGraph canvas = new HistogramGraph();
        // MultiLineGraph canvas = new MultiLineGraph();

//        List<Entry<Number, Number>> points = x.createNumberEntries("longitude", "latitude");
		// Map<Double, Long> histogram = x.histogram("population", 55);
        // Map<String, Long> collect = histogram.entrySet().stream()
        // .collect(Collectors.toMap(t -> t.getKey() >= 6 ? "Others" :
        // String.format("%.0f Rooms", t.getKey()),
        // Entry<Double, Long>::getValue,
        // (a, b) -> a + b));

//        canvas.setHistogram(collect);
        // canvas.setPoints(points);
        // root.getChildren().add(newSlider("Radius", 1, 375, canvas.radius));
		root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSize));
		root.getChildren().add(newSlider("Padding", 10, 100, canvas.layout));
		root.getChildren().add(newSlider("X Bins", 1, 30, canvas.bins));
		root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybins));

        ObservableList<String> itens = FXCollections.observableArrayList();
        canvas.stats.addListener((InvalidationListener) o -> itens.setAll(canvas.stats.keySet()));
        canvas.setDatagram(x);

        ListView<String> xSelected = createSelection(itens, canvas.xHeader);
        ListView<String> ySelected = createSelection(itens, canvas.yHeader);

        root.getChildren().add(canvas);
        root.getChildren().add(xSelected);
        root.getChildren().add(ySelected);


		theStage.show();
	}

    private ListView<String> createSelection(ObservableList<String> itens, StringProperty xHeader) {
        ListView<String> ySelected = new ListView<>(itens);
        ySelected.setCellFactory(ComboBoxListCell.forListView(itens));
        ySelected.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
        ySelected.selectionModelProperty().get().getSelectedItems().addListener((ListChangeListener<String>) c -> {
            c.next();
            List<? extends String> addedSubList = c.getAddedSubList();
            if (!addedSubList.isEmpty()) {
                String string = addedSubList.get(0);
                xHeader.set(string);
            }
        });
        ySelected.selectionModelProperty().get().select(0);
        return ySelected;
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

