package kibana;

import gaming.ex21.ListHelper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleComboBoxBuilder;

public class TimelionDisplay extends Application {

    @Override
    public void start(Stage primaryStage) {
        HBox content = getTimelineGraph();
        TabPane tabPane = new TabPane(new Tab("Timeline Usuários", content));
        tabPane.setSide(Side.LEFT);
        primaryStage.setTitle("LineChart example");
        primaryStage.setScene(new Scene(tabPane));
        primaryStage.show();
    }

    private HBox getTimelineGraph() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                return LocalDateTime.parse(string).atZone(ZoneId.systemDefault()).toEpochSecond();
            }

            @Override
            public String toString(Number object) {
                LocalDateTime localTime =
                        Instant.ofEpochMilli(object.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                return localTime.toString();
            }
        });
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        ObservableList<Series<Number, Number>> timelionFullScan = TimelionApi.timelionFullScan("", "now-d");
        ObservableList<String> mapping = ListHelper.mapping(timelionFullScan, Series<Number, Number>::getName);

        FilteredList<Series<Number, Number>> filtered = timelionFullScan.filtered(e -> true);
        lineChart.setData(filtered);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setTitle("[Sistemas] [SatCentral] Timeline Usuários");
        lineChart.setVerticalZeroLineVisible(false);
        mapping.add(0, "");
        ComboBox<String> comboBox = new SimpleComboBoxBuilder<String>().items(mapping).onChange(
                (old, val) -> filtered.setPredicate(e -> StringUtils.isBlank(val) || Objects.equals(e.getName(), val)))
                .build();
        HBox content = new HBox(comboBox, lineChart);
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        return content;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
