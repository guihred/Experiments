package ml;

import static simplebuilder.SimpleSliderBuilder.newSlider;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.ColorPattern;
import ml.graph.HeatGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.ImageFXUtils;
public class HeatGraphExample extends Application {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("Heat Graph Example");
        FlowPane root = new FlowPane();
        int pad = 100;
        Scene theScene = new Scene(root, 1000 + pad, 500 + pad);
		theStage.setScene(theScene);
		HeatGraph canvas = new HeatGraph();
		DataframeML x = DataframeBuilder.build("california_housing_train.csv");
		DataframeUtils.crossFeature(x, "rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        canvas.setTitle("California Housing");
        final int maxBins = 50;
        root.getChildren().add(newSlider("Radius", 10, maxBins, canvas.radiusProperty()));
        root.getChildren().add(newSlider("Line", 1, maxBins, canvas.lineSizeProperty()));
        root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        root.getChildren().add(newSlider("X Bins", 1, maxBins, canvas.binsProperty()));
        root.getChildren().add(newSlider("Y Bins", 1, maxBins, canvas.ybinsProperty()));
        root.getChildren()
                .add(new SimpleComboBoxBuilder<ColorPattern>().items(ColorPattern.values())
                        .selectedItem(canvas.colorPatternProperty()).build());
        ObservableList<String> itens = FXCollections.observableArrayList();
        canvas.statsProperty().addListener((Observable o) -> itens.setAll(canvas.statsProperty().keySet()));
        canvas.setDatagram(x);

		ListView<String> xSelected = createSelection(itens, canvas.xHeaderProperty());
		ListView<String> ySelected = createSelection(itens, canvas.yHeaderProperty());
        ListView<String> zSelected = createSelection(itens, canvas.zHeaderProperty());
        root.getChildren()
                .add(SimpleButtonBuilder.newButton("Export", e -> ImageFXUtils.take(canvas)));
        root.getChildren().add(canvas);
        root.getChildren().add(xSelected);
        root.getChildren().add(ySelected);
        root.getChildren().add(zSelected);
		theStage.show();
	}

    public static void main(final String[] args) {
        launch(args);
    }


    private static ListView<String> createSelection(final ObservableList<String> itens, final StringProperty xHeader) {
        ListView<String> ySelected = new ListView<>(itens);
        ySelected.setCellFactory(ComboBoxListCell.forListView(itens));
        ySelected.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
        ySelected.selectionModelProperty().get().getSelectedItems().addListener((ListChangeListener<String>) c -> {
            c.next();
            c.getAddedSubList().stream().findFirst().ifPresent(xHeader::set);
        });
        ySelected.selectionModelProperty().get().select(0);
        return ySelected;
    }
}

