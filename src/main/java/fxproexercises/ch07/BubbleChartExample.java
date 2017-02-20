/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch07;
import static fxproexercises.ch07.CommonChartData.getChartData;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class BubbleChartExample extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(30);
		xAxis.setAutoRanging(false);
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(20110);
		xAxis.setUpperBound(20201);
		xAxis.setTickUnit(10);
		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number n) {
				return String.valueOf(n.intValue() / 10);
			}

			@Override
			public Number fromString(String s) {
				return Integer.valueOf(s) * 10;
			}
		});
		BubbleChart<Number, Number> bubbleChart = new BubbleChart<>(xAxis, yAxis);
		bubbleChart.setData(getChartData());
		bubbleChart.setTitle("Speculations");
		primaryStage.setTitle("BubbleChart example");
		StackPane root = new StackPane();
		root.getChildren().add(bubbleChart);
		primaryStage.setScene(new Scene(root, 400, 250));
		primaryStage.show();
	}

}