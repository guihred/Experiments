package ml;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.PointGraph;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.ResourceFXUtils;
public class PointsExample extends Application {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("Points Graph Example");

        BorderPane root = new BorderPane();
        Scene theScene = new Scene(root, 1000, 500);
		theStage.setScene(theScene);

        PointGraph canvas = new PointGraph();
        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
        VBox vBox = new VBox();
        vBox.getChildren().add(newSlider("Line", 1, 50, canvas.lineSizeProperty()));
        vBox.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
        vBox.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
        vBox.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
        vBox.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());
        root.setLeft(vBox);
        ObservableList<String> itens = FXCollections.observableArrayList();
        canvas.statsProperty().addListener((Observable o) -> itens.setAll(canvas.statsProperty().keySet()));
        canvas.setDatagram(x);

		ListView<String> xSelected = createSelection(itens, canvas.xHeaderProperty());
		ListView<String> ySelected = createSelection(itens, canvas.yHeaderProperty());

        root.setCenter(new HBox(canvas, xSelected, ySelected));


		theStage.show();
	}

    private ListView<String> createSelection(ObservableList<String> itens, StringProperty xHeader) {
        ListView<String> ySelected = new ListView<>(itens);
        ySelected.setCellFactory(ComboBoxListCell.forListView(itens));
        ySelected.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
        ySelected.selectionModelProperty().get().getSelectedItems().addListener((Change<? extends String> c) -> {
            c.next();
            if (!c.getAddedSubList().isEmpty()) {
                xHeader.set(c.getAddedSubList().get(0));
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

