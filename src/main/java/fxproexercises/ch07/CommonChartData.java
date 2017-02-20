package fxproexercises.ch07;

import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public final class CommonChartData {

	private CommonChartData() {
	}
	public static ObservableList<XYChart.Series<Number, Number>> getChartData() {
		double javaValue = 17.56;
		double cValue = 17.06;
		double cppValue = 8.25;
		ObservableList<XYChart.Series<Number, Number>> answer = FXCollections.observableArrayList();
		Series<Number, Number> java = new Series<>();
		java.setName("java");
		Series<Number, Number> c = new Series<>();
		c.setName("C");
		Series<Number, Number> cpp = new Series<>();
		cpp.setName("C++");
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

	public static ObservableList<PieChart.Data> getPieData() {
		return FXCollections.observableArrayList(new PieChart.Data("Java", 17.56),
				new PieChart.Data("C", 17.06), new PieChart.Data("C++", 8.25), new PieChart.Data("C#", 8.20),
				new PieChart.Data("ObjectiveC", 6.8), new PieChart.Data("PHP", 6.0),
				new PieChart.Data("(Visual)Basic", 4.76), new PieChart.Data("Other", 31.37));
	}

	public static ObservableList<XYChart.Series<String, Number>> getCategoryData() {
		double javaValue = 17.56;
		double cValue = 17.06;
		double cppValue = 8.25;
		ObservableList<XYChart.Series<String, Number>> answer = FXCollections.observableArrayList();
		Series<String, Number> java = new Series<>();
		Series<String, Number> c = new Series<>();
		Series<String, Number> cpp = new Series<>();
		java.setName("java");
		c.setName("C");
		cpp.setName("C++");
		for (int i = 2011; i < 2021; i++) {
			java.getData().add(new XYChart.Data<>(Integer.toString(i), javaValue));
			javaValue = javaValue + 4 * Math.random() - .2;
			c.getData().add(new XYChart.Data<>(Integer.toString(i), cValue));
			cValue = cValue + 4 * Math.random() - 2;
			cpp.getData().add(new XYChart.Data<>(Integer.toString(i), cppValue));
			cppValue = cppValue + 4 * Math.random() - 2;
		}
		answer.addAll(Arrays.asList(java, c, cpp));
		return answer;
	}
}
