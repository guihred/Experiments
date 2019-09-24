package ml;

import java.util.List;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.HeatGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
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
        root.getChildren().add(SimpleSliderBuilder.newSlider("Radius", 10, 50, canvas.radiusProperty()));
        root.getChildren().add(SimpleSliderBuilder.newSlider("Line", 1, 50, canvas.lineSizeProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("Padding", 10, 100, canvas.layoutProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("X Bins", 1, 30, canvas.binsProperty()));
		root.getChildren().add(SimpleSliderBuilder.newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));

        ObservableList<String> itens = FXCollections.observableArrayList();
		canvas.statsProperty().addListener((InvalidationListener) o -> itens.setAll(canvas.statsProperty().keySet()));
        canvas.setDatagram(x);

		ListView<String> xSelected = createSelection(itens, canvas.xHeaderProperty());
		ListView<String> ySelected = createSelection(itens, canvas.yHeaderProperty());
        final Canvas canvas1 = canvas;
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ImageFXUtils.take(canvas1)).build());

        root.getChildren().add(canvas);
        root.getChildren().add(xSelected);
        root.getChildren().add(ySelected);


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
            List<? extends String> addedSubList = c.getAddedSubList();
            if (!addedSubList.isEmpty()) {
                String string = addedSubList.get(0);
                xHeader.set(string);
            }
        });
        ySelected.selectionModelProperty().get().select(0);
        return ySelected;
    }
}

