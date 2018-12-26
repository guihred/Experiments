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
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BubbleChartExample extends Application {


	@Override
	public void start(Stage primaryStage) {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
        ObservableList<Series<Number, Number>> chartData = getChartData();
		yAxis.setAutoRanging(false);
        setBounds(yAxis, getStats(chartData, Data<Number, Number>::getYValue));
        xAxis.setAutoRanging(false);
        setBounds(xAxis, getStats(chartData, Data<Number, Number>::getXValue));
        xAxis.setTickUnit(1);
		xAxis.setTickLabelFormatter(new SimpleStringConverter());
		BubbleChart<Number, Number> bubbleChart = new BubbleChart<>(xAxis, yAxis);
        bubbleChart.setData(chartData);
		bubbleChart.setTitle("Speculations");
		primaryStage.setTitle("BubbleChart example");
		StackPane root = new StackPane();
		root.getChildren().add(bubbleChart);
        primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}

}
