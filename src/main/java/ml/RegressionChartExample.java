package ml;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class RegressionChartExample extends Application {
    public static void main(String[] args) {
        RegressionChartExample.launch(args);
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
        ObservableList<Series<Number, Number>> data = regressionModel.asSeries();
        LineChart<Number, Number> lineChart = lineChart(data,
                String.format("Speculations(%.1f*x + %.1f)", regressionModel.slope, regressionModel.initial));

        ObservableList<Series<Number, Number>> error = regressionModel.getErrorSeries();
        ObservableList<Series<Number, Number>> expected = regressionModel.asExpectedSeries();
        data.addAll(expected);
        LineChart<Number, Number> errorGraph = lineChart(error,
                String.format("Error(%.1f*x + %.1f)", regressionModel.bestSlope, regressionModel.bestInitial));
        FlowPane root = new FlowPane();

        root.getChildren().add(lineChart);
        root.getChildren().add(errorGraph);

        primaryStage.setTitle("LineChart example");
        primaryStage.setScene(new Scene(root, 1000, 400));
        primaryStage.show();
    }
}

class RegressionModel {

    private static final int MAX_SIZE = 20;
    double slope;
    double initial;

    private int c;

    private List<Double> values;

    double bestSlope;
    double bestInitial;
    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> asExpectedSeries() {
        Series<Number, Number> series = new Series<>();
        c = 0;
        series.setName("Prediction");
        List<Data<Number, Number>> collect = IntStream.range(0, MAX_SIZE)
                .mapToObj(i -> toData(i, bestInitial + bestSlope * i)).collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(collect));
        return FXCollections.observableArrayList(series);
    }
    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> asSeries() {
        Series<Number, Number> series = new Series<>();
        series.setName("Numbers");
        DoubleStream sorted = doubleStream();
        values = sorted.boxed().collect(Collectors.toList());
        values.sort(Comparator.comparing(e -> Math.signum(slope) * e));
        c = 0;
        List<Data<Number, Number>> collect = values.stream().map(this::mapToData)
                .collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(collect));
        return FXCollections.observableArrayList(series);
    }

    private DoubleStream doubleStream() {
        Random random = new Random();
        slope = (random.nextDouble() - .5) * 10;
        initial = (random.nextDouble() - .5) * 10;
        c = 0;
        return DoubleStream.generate(() -> random(random)).limit(MAX_SIZE);
    }

    private double random(Random random) {
        double e = (random.nextDouble() - .5) * 5;
        return initial + e + slope * c++;
    }

    double learningRate = 0.001;

    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> getErrorSeries() {
        Series<Number, Number> series = new Series<>();
        Series<Number, Number> series2 = new Series<>();
        ObservableList<Data<Number, Number>> slopeList = FXCollections.observableArrayList();
        ObservableList<Data<Number, Number>> interceptList = FXCollections.observableArrayList();
        series.setData(slopeList);
        series2.setData(interceptList);
        series.setName("Slope Error");
        series2.setName("Intercept Error");
        bestSlope = 50;
        bestInitial = 0;
        for (int i = 0; i < 1000; i++) {
            c = 0;
            double adjust = values.stream().mapToDouble(e -> e).map(y -> (-y + bestInitial + bestSlope * c++) * (c - 1))
                    .sum() * 2
                    / values.size()
                    * learningRate;
            bestSlope -= adjust;
            c = 0;
            double loss = values.stream().mapToDouble(e -> e).map(e -> -e + bestInitial + bestSlope * c++)
                    .map(e -> Math.abs(e)).sum();
            slopeList.add(toData(bestSlope, loss));
        }
        for (int i = 0; i < 5000; i++) {

            c = 0;
            double adjust = values.stream().mapToDouble(e -> e).map(y -> -y + bestInitial + bestSlope * c++).sum() * 2
                    / values.size() * learningRate;
            bestInitial -= adjust;
            c = 0;
            double loss = values.stream().mapToDouble(e -> e).map(e -> -e + bestInitial + bestSlope * c++)
                    .map(e -> e * e).sum();
            interceptList.add(toData(bestInitial, loss));

        }
        return FXCollections.observableArrayList(series2, series);
    }

    private Data<Number, Number> mapToData(double e) {
        return new Data<>(c++, e);
    }

    private Data<Number, Number> toData(double e1, double e2) {
        return new Data<>(e1, e2);
    }

}
