/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch07;

import ethical.hacker.TimelionApi;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
public class LineChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
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
        ObservableList<Series<Number, Number>> timelionFullScan = TimelionApi.timelionFullScan("03113712998", "now-d");
        lineChart.setData(timelionFullScan);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle("[Sistemas] [SatCentral] Timeline Usuários");
        lineChart.setVerticalZeroLineVisible(false);
        primaryStage.setTitle("LineChart example");
        StackPane root = new StackPane();
        root.getChildren().add(lineChart);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
