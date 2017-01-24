/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch07;

/**
 *
 * @author Note
 */
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

public class FxProCH7b extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(2011);
        xAxis.setUpperBound(2021);
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<Integer, Double> scatterChart = new ScatterChart(xAxis, yAxis);
        scatterChart.setData(getChartData());
        scatterChart.setTitle("Speculations");
        primaryStage.setTitle("Chart App 3");
        StackPane root = new StackPane();
        root.getChildren().add(scatterChart);
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

    private ObservableList<XYChart.Series<Integer, Double>> getChartData() {
        double javaValue = 17.56;
        double cValue = 17.06;
        double cppValue = 8.25;
        ObservableList<XYChart.Series<Integer, Double>> answer = FXCollections.observableArrayList();
        Series<Integer, Double> java = new Series<>();
        java.setName("java");
        Series<Integer, Double> c = new Series<>();
        c.setName("C");
        Series<Integer, Double> cpp = new Series<>();
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
}
