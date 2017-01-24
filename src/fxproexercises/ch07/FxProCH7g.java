/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch07;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class FxProCH7g extends Application {

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

	private ObservableList<XYChart.Series<Number, Number>> getChartData() {
		double javaValue = 17.56;
		double cValue = 17.06;
		double cppValue = 8.25;
		ObservableList<XYChart.Series<Number, Number>> answer = FXCollections.observableArrayList();
		Series<Number, Number> java = new Series<>();
		Series<Number, Number> c = new Series<>();
		Series<Number, Number> cpp = new Series<>();
		java.setName("java");
		c.setName("C");
		cpp.setName("C++");
		for (int i = 20110; i < 20210; i = i + 10) {
			double diff = Math.random();
			java.getData().add(new XYChart.Data<Number, Number>(i, javaValue, 2 * diff));
			javaValue = Math.max(javaValue + 4 * diff - 2, 0);
			diff = Math.random();
			c.getData().add(new XYChart.Data<Number, Number>(i, cValue, 2 * diff));
			cValue = Math.max(cValue + 4 * diff - 2, 0);
			diff = Math.random();
			cpp.getData().add(new XYChart.Data<Number, Number>(i, cppValue, 2 * diff));
			cppValue = Math.max(cppValue + 4 * diff - 2, 0);
		}
		answer.add(java);
		answer.add(c);
		answer.add(cpp);
		return answer;
	}
}
