package ml;

import static utils.CommonsFX.newSlider;
import static utils.ResourceFXUtils.getOutFile;

import extract.UnZip;
import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ml.data.CSVUtils;
import ml.data.Country;
import ml.data.DataframeML;
import ml.graph.ColorPattern;
import ml.graph.WorldMapGraph;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ImageFXUtils;

public class WorldMapExample extends Application implements HasLogging {

    @Override
    public void start(final Stage theStage) {
        theStage.setTitle("World Map Example");
        Country.loadPaths();
        BorderPane root = new BorderPane();
        Scene theScene = new Scene(root, WorldMapGraph.WIDTH / 2, WorldMapGraph.HEIGHT / 2);
        theStage.setScene(theScene);
        WorldMapGraph canvas = new WorldMapGraph();
        HBox left = new HBox();
        root.setTop(left);
        left.getChildren().add(newSlider("Labels", 1, 10, canvas.binsProperty()));
        left.getChildren().add(newSlider("Font Size", 1, 60, canvas.fontSizeProperty()));
        ComboBox<ColorPattern> patternCombo = new SimpleComboBoxBuilder<ColorPattern>()
            .items(ColorPattern.values())
            .onSelect(canvas.patternProperty()::set)
            .select(0)
            .build();

        String[] list = getDataframeCSVs();
        ComboBox<String> yearCombo = new SimpleComboBoxBuilder<String>().items("2016")
            .select(0)
            .onSelect(canvas.valueHeaderProperty()::set)
            .build();

        DataframeML x = DataframeML.builder("out/WDIDataEG.ELC.ACCS.ZS.csv").build();
        canvas.valueHeaderProperty().set("2016");
        canvas.setDataframe(x,
            x.cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name"));

        ComboBox<String> statisticsCombo = new SimpleComboBoxBuilder<String>()
            .items(list).onSelect(s -> {
                DataframeML x2 = DataframeML.builder("out/" + s).build();
                ObservableList<String> itens = x2
                    .cols().stream().filter(StringUtils::isNumeric).sorted()
                    .filter(e -> x2.list(e).stream().anyMatch(Objects::nonNull))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
                yearCombo.setItems(itens);
                if (!itens.contains(canvas.valueHeaderProperty().get())) {
                    yearCombo.getSelectionModel().select(itens.size() - 1);
                }
                String countryColumn = x2
                    .cols().stream().filter(e -> e.contains("untry N")).findFirst().orElse("﻿Country Name");
                canvas.setDataframe(x2, countryColumn);
            }).select(0).build();

        left.getChildren().add(statisticsCombo);
        left.getChildren().add(yearCombo);
        left.getChildren().add(patternCombo);
        final Canvas canvas1 = canvas;
        left.getChildren().add(CommonsFX.newButton("Export", e -> ImageFXUtils.take(canvas1)));

        root.setCenter(canvas);
        theStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

    private static String[] getDataframeCSVs() {
        File file = getOutFile();
        String[] list = file.list((dir, name) -> name.matches("WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv"));
        if (list.length == 0) {
            File outFile = getOutFile("WDIData.csv");
            if (!outFile.exists()) {
                UnZip.extractZippedFiles(new File(UnZip.ZIPPED_FILE_FOLDER));
            }
            CSVUtils.splitFile(outFile.getAbsolutePath(), 3);
            CSVUtils.splitFile(getOutFile("API_21_DS2_en_csv_v2_10576945.csv").getAbsolutePath(), 3);
            return file.list((dir, name) -> name.matches("WDIData.+.csv|API_21_DS2_en_csv_v2_10576945.+.csv"));
        }
        return list;
    }
}
