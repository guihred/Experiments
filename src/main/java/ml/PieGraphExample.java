package ml;

import static simplebuilder.SimpleButtonBuilder.newButton;
import static simplebuilder.SimpleSliderBuilder.newSlider;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.graph.PieGraph;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.ImageFXUtils;

public class PieGraphExample extends Application {

    private static final int SIZE = 650;

    private ObjectProperty<DataframeML> dataframeObj = new SimpleObjectProperty<>();

    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("Points Graph Example");
        HBox root = new HBox();
        Scene theScene = new Scene(root, SIZE, SIZE);
		theStage.setScene(theScene);
        PieGraph canvas = new PieGraph();
        SimpleComboBoxBuilder<String> onSelect =
                new SimpleComboBoxBuilder<String>().onSelect(e -> canvas.setDataframe(dataframeObj.get(), e));
        dataframeObj.addListener((ob, old, val) -> {
            List<String> collect =
                    val.getFormatMap().entrySet().stream()
                    .map(Entry<String, Class<? extends Comparable<?>>>::getKey).filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            onSelect.items(collect);
            onSelect.select(collect.size() - 1);
        });
        DataframeML dataframe = DataframeBuilder.builder("WDICountry.csv").build();
        dataframeObj.set(dataframe);
        ComboBox<String> build = onSelect.build();
        Button exportButton = newButton("Export", e -> ImageFXUtils.take(canvas));
        VBox radiusSlider = newSlider("Radius", 1, 500, canvas.radiusProperty());
        VBox binsSlider = newSlider("Bins", 1, 50, canvas.binsProperty());
        VBox xSlider = newSlider("X", -SIZE, SIZE, canvas.xOffsetProperty());
        VBox start = newSlider("Start", -180, 180, canvas.startProperty());
        VBox propSlider = newSlider("Legend Distance", 0, 1., canvas.legendsRadiusProperty());
        CheckBox newCheck = CommonsFX.newCheck("", canvas.showLinesProperty());
        Button chooseFile = StageHelper.chooseFile("Choose CSV", "CSV",
                f -> dataframeObj.set(DataframeBuilder.build(f)), "CSV", "*.csv");

        root.getChildren()
                .add(new VBox(newCheck, radiusSlider, binsSlider, start, xSlider, propSlider, build, chooseFile,
                        exportButton));
        root.getChildren().add(new HBox(canvas));
		theStage.show();
        onSelect.select("Region");
	}

    public static void main(final String[] args) {
        launch(args);
    }
}
