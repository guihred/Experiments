package fxpro.ch07;

import static fxpro.ch07.CommonChartData.getPieData;

import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PieChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        PieChart pieChart = new PieChart();

        pieChart.setData(getPieData());
        pieChart.setTitle("Tiobe index");
        pieChart.setLegendSide(Side.LEFT);
        pieChart.setClockwise(false);
        pieChart.setLabelsVisible(false);
        primaryStage.setTitle("PieChart");
        StackPane root = new StackPane();
        root.getChildren().add(pieChart);
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
