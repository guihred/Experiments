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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.graph.PieGraph;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.ex.RunnableEx;

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
        SimpleComboBoxBuilder<String> columnCombo =
                new SimpleComboBoxBuilder<String>().onSelect(e -> canvas.setDataframe(dataframeObj.get(), e));
        dataframeObj.addListener((ob, old, val) -> {
            List<String> fields =
                    val.getFormatMap().entrySet().stream().map(Entry<String, Class<? extends Comparable<?>>>::getKey)
                            .filter(StringUtils::isNotBlank).collect(Collectors.toList());
            columnCombo.items(fields);
            columnCombo.select(fields.size() - 1);
        });
        ProgressIndicator progress = new ProgressIndicator(0);
        DataframeML dataframe = DataframeBuilder.builder("WDICountry.csv").build(progress.progressProperty());
        dataframeObj.set(dataframe);
        Button exportButton = newButton("Export", e -> ImageFXUtils.take(canvas));
        VBox radiusSlider = newSlider("Radius", 1, 500, canvas.radiusProperty());
        VBox binsSlider = newSlider("Bins", 1, 50, canvas.binsProperty());
        VBox xSlider = newSlider("X", -SIZE, SIZE, canvas.xOffsetProperty());
        VBox start = newSlider("Start", -180, 180, canvas.startProperty());
        VBox propSlider = newSlider("Legend Distance", 0, 1., canvas.legendsRadiusProperty());
        CheckBox newCheck = CommonsFX.newCheck("", canvas.showLinesProperty());
        Button chooseFile = new FileChooserBuilder().name("Choose CSV").title("CSV").extensions("CSV", "*.csv")
                .onSelect(f -> RunnableEx.runNewThread(
                        () -> DataframeBuilder.builder(f).build(progress.progressProperty()),
                        o -> CommonsFX.runInPlatform(() -> dataframeObj.set(o))))
                .buildOpenButton();
        root.getChildren()
                .add(new VBox(newCheck, radiusSlider, binsSlider, start, xSlider, propSlider, columnCombo.build(),
                chooseFile, progress, exportButton));
        HBox.setHgrow(canvas, Priority.ALWAYS);
        root.getChildren().add(new HBox(canvas));
        theStage.show();
        columnCombo.select("Region");
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
