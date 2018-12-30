package ml;
import static utils.CommonsFX.newSlider;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.graph.WorldMapGraph;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class WorldMapExample extends Application implements HasLogging {


    @Override
	public void start(final Stage theStage) {
        theStage.setTitle("World Map Example");

        BorderPane root = new BorderPane();
        Scene theScene = new Scene(root, WorldMapGraph.WIDTH / 2, WorldMapGraph.HEIGHT / 2);
		theStage.setScene(theScene);
		WorldMapGraph canvas = new WorldMapGraph();
        HBox left = new HBox();
        root.setTop(left);
        left.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        left.getChildren().add(newSlider("Font Size", 1, 60, canvas.fontSizeProperty()));
        DataframeML x = DataframeML.builder("out/WDIDataEG.ELC.ACCS.ZS.csv").build();
        canvas.valueHeaderProperty().set("2016");
        canvas.setDataframe(x,
                x.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));
        File file = ResourceFXUtils.toFile("out");
        String[] list = file.list((dir, name) -> name.endsWith(".csv"));
        ComboBox<String> yearCombo = new SimpleComboBoxBuilder<String>().items("2016").select("2016")
                .onSelect(canvas.valueHeaderProperty()::set).build();

        ComboBox<String> statisticsCombo = new SimpleComboBoxBuilder<String>().items(list)
                .onSelect(s -> {
                    DataframeML x2 = DataframeML.builder("out/" + s).build();
                    ObservableList<String> itens = x2.cols().stream().filter(StringUtils::isNumeric).sorted()
                            .filter(e -> x2.list(e).stream().anyMatch(Objects::nonNull))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                    yearCombo.setItems(itens);
                    if (!itens.contains(canvas.valueHeaderProperty().get())) {
                        yearCombo.getSelectionModel().select(itens.size() - 1);
                    }
                    canvas.setDataframe(x2,
                            x2.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));
                }).select("WDIDataEG.ELC.ACCS.ZS.csv").build();

        left.getChildren().add(statisticsCombo);
        left.getChildren().add(yearCombo);
        left.getChildren()
                .add(new SimpleButtonBuilder().text("Export").onAction(e -> canvas.takeSnapshot())
                        .build());

        root.setCenter(canvas);
		theStage.show();
	}


    public static void main(final String[] args) {
        launch(args);
    }
}

