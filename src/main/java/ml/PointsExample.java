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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
public class PointsExample extends Application {


    @Override
	public void start(Stage theStage) {
        theStage.setTitle("Points Graph Example");

        FlowPane root = new FlowPane();
        Scene theScene = new Scene(root, 1100, 600);
		theStage.setScene(theScene);

        PointGraph canvas = new PointGraph();
        DataframeML x = new DataframeML("california_housing_train.csv");
        x.crossFeature("rooms_per_person", d -> (d[0] / d[1]), "total_rooms", "population");
		root.getChildren().add(newSlider("Line", 1, 40, canvas.lineSizeProperty()));
		root.getChildren().add(newSlider("Padding", 10, 100, canvas.layoutProperty()));
		root.getChildren().add(newSlider("X Bins", 1, 30, canvas.binsProperty()));
		root.getChildren().add(newSlider("Y Bins", 1, 30, canvas.ybinsProperty()));
        root.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> ResourceFXUtils.take(canvas)).build());

        ObservableList<String> itens = FXCollections.observableArrayList();
		canvas.statsProperty().addListener((InvalidationListener) o -> itens.setAll(canvas.statsProperty().keySet()));
        canvas.setDatagram(x);

		ListView<String> xSelected = createSelection(itens, canvas.xHeaderProperty());
		ListView<String> ySelected = createSelection(itens, canvas.yHeaderProperty());

        root.getChildren().add(new HBox(xSelected, ySelected, canvas));


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
                xHeader.set(addedSubList.get(0));
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

