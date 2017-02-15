/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch07;

import static fxproexercises.ch07.CommonChartData.getCategoryData;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AreaChartExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String,Number> areaChart = new AreaChart<>(xAxis, yAxis);
		areaChart.setData(getCategoryData());
        areaChart.setTitle("speculations");
        primaryStage.setTitle("AreaChart example");
        StackPane root = new StackPane();
        root.getChildren().add(areaChart);
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

}
