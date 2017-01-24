package fxproexercises.starterApp;

import java.util.Arrays;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FxProCH7a extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		ScatterChart<Number, Number> scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);
		scatterChart.setData(getChartData());
		primaryStage.setTitle("Chart App 3");
		StackPane root = new StackPane();
		root.getChildren().add(scatterChart);
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
		for (int i = 2011; i < 2021; i++) {
			java.getData().add(new XYChart.Data<>(i, javaValue));
			javaValue = javaValue + 4 * Math.random() - 2;
			c.getData().add(new XYChart.Data<>(i, cValue));
			cValue = cValue + Math.random() - .5;
			cpp.getData().add(new XYChart.Data<>(i, cppValue));
			cppValue = cppValue + 4 * Math.random() - 2;
		}
		answer.addAll(Arrays.asList(java, c, cpp));
		return answer;
	}
}
