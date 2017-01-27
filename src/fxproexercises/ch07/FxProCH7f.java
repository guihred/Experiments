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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FxProCH7f extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String,Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setData(getChartData());
        areaChart.setTitle("speculations");
        primaryStage.setTitle("AreaChart example");
        StackPane root = new StackPane();
        root.getChildren().add(areaChart);
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

    private ObservableList<XYChart.Series<String, Number>> getChartData() {
        double javaValue = 17.56;
        double cValue = 17.06;
        double cppValue = 8.25;
        ObservableList<XYChart.Series<String, Number>> answer
                = FXCollections.observableArrayList();
        Series<String, Number> java = new Series<>();
        Series<String, Number> c = new Series<>();
        Series<String, Number> cpp = new Series<>();
        java.setName("Java");
        c.setName("C");
        cpp.setName("C++");
        for (int i = 2011; i < 2021; i++) {
			Data<String, Number> data = new XYChart.Data<>(Integer.toString(i), javaValue);
			java.getData().add(data);
            javaValue = javaValue + 4 * Math.random() - .2;
            c.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), cValue));
            cValue = cValue + 4 * Math.random() - 2;
            cpp.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), cppValue));
            cppValue = cppValue + 4 * Math.random() - 2;
        }
        answer.add(java);
        answer.add(c);
        answer.add(cpp);
        return answer;
    }
}
