package ml;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class RegressionChartExample extends Application {
    public static void main(String[] args) {
		launch(args);
    }

    private LineChart<Number, Number> lineChart(ObservableList<Series<Number, Number>> data, String value) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setData(data);
        lineChart.setTitle(value);
        return lineChart;
    }

    @Override
    public void start(Stage primaryStage) {
        RegressionModel regressionModel = new RegressionModel();
		DataframeML x = new DataframeML("california_housing_train.csv");

        ObservableList<Series<Number, Number>> data = regressionModel.createSeries(x.list("total_rooms"),
                x.list("total_rooms"));
        LineChart<Number, Number> lineChart = lineChart(data,
                String.format("Speculations(%.1f*x + %.1f)", regressionModel.getSlope(), regressionModel.getInitial()));

        ObservableList<Series<Number, Number>> error = regressionModel.getErrorSeries();
		ObservableList<Series<Number, Number>> expected = regressionModel.getExpectedSeries();
        data.addAll(expected);
        LineChart<Number, Number> errorGraph = lineChart(error,
                String.format("Error(%.1f*x + %.1f)", regressionModel.getBestSlope(), regressionModel.getBestInitial()));
        FlowPane root = new FlowPane();

        root.getChildren().add(lineChart);
        root.getChildren().add(errorGraph);

        primaryStage.setTitle("Regrssion Chart Example");
        primaryStage.setScene(new Scene(root, 1000, 400));
        primaryStage.show();
    }
}
