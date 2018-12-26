/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch07;

import static fxpro.ch07.CommonChartData.getChartData;
import static fxpro.ch07.CommonChartData.getStats;
import static fxpro.ch07.CommonChartData.setBounds;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
public class ScatterChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        ObservableList<Series<Number, Number>> chartData = getChartData();
        setBounds(xAxis, getStats(chartData, Data<Number, Number>::getXValue));
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFormatter(new SimpleStringConverter());
        setBounds(yAxis, getStats(chartData, Data<Number, Number>::getYValue));
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setData(chartData);
        scatterChart.setTitle("Speculations");
        primaryStage.setTitle("Scatter Chart");
        StackPane root = new StackPane();
        root.getChildren().add(scatterChart);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
