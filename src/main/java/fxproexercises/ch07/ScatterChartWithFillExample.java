/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch07;

import static fxproexercises.ch07.CommonChartData.getCategoryData;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ScatterChartWithFillExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setData(getCategoryData());
        scatterChart.setTitle("speculations");
        scatterChart.setAlternativeRowFillVisible(false);
        scatterChart.setAlternativeColumnFillVisible(true);
        primaryStage.setTitle("Scatter Chart Filled Example");
        StackPane root = new StackPane();
        root.getChildren().add(scatterChart);
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }


}
