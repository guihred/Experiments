package fxpro.ch07;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

public final class CommonChartData {

    private static final int BEGIN_YEAR = 2011;
    private static final double CPP_AVG = 8.25;
    private static final double C_AVG = 17.06;
    private static final double JAVA_AVG = 17.56;
    private static final ObservableList<PieChart.Data> PIE_DATA = FXCollections.observableArrayList(
        new PieChart.Data("Java", JAVA_AVG), new PieChart.Data("C", C_AVG), new PieChart.Data("C++", CPP_AVG),
        new PieChart.Data("C#", 8.20), new PieChart.Data("ObjectiveC", 6.8), new PieChart.Data("PHP", 6.0),
        new PieChart.Data("(Visual)Basic", 4.76), new PieChart.Data("Other", 31.37));

    private CommonChartData() {
    }

    public static ObservableList<XYChart.Series<String, Number>> getCategoryData() {
        double javaValue = JAVA_AVG;
        double cValue = C_AVG;
        double cppValue = CPP_AVG;
        ObservableList<XYChart.Series<String, Number>> answer = FXCollections.observableArrayList();
        Series<String, Number> java = new Series<>();
        Series<String, Number> c = new Series<>();
        Series<String, Number> cpp = new Series<>();
        java.setName("java");
        c.setName("C");
        cpp.setName("C++");
        for (int i = 0; i < 10; i++) {
            String year = Integer.toString(i + BEGIN_YEAR);
            java.getData().add(new XYChart.Data<>(year, javaValue));
            final double variation = .2;
            javaValue += 4 * Math.random() - variation;
            c.getData().add(new XYChart.Data<>(year, cValue));
            cValue += 4 * Math.random() - 2;
            cpp.getData().add(new XYChart.Data<>(year, cppValue));
            cppValue += 4 * Math.random() - 2;
        }
        answer.addAll(Arrays.asList(java, c, cpp));
        return answer;
    }

    public static ObservableList<XYChart.Series<Number, Number>> getChartData() {
        double javaValue = JAVA_AVG;
        double cValue = C_AVG;
        double cppValue = CPP_AVG;
        ObservableList<XYChart.Series<Number, Number>> answer = FXCollections.observableArrayList();
        Series<Number, Number> java = new Series<>();
        java.setName("java");
        Series<Number, Number> c = new Series<>();
        c.setName("C");
        Series<Number, Number> cpp = new Series<>();
        cpp.setName("C++");
        for (int i = BEGIN_YEAR; i < BEGIN_YEAR + 10; i++) {
            java.getData().add(new XYChart.Data<>(i, javaValue));
            javaValue += 4 * Math.random() - 2;
            c.getData().add(new XYChart.Data<>(i, cValue));
            cValue += Math.random() - .5;
            cpp.getData().add(new XYChart.Data<>(i, cppValue));
            cppValue += 4 * Math.random() - 2;
        }
        answer.addAll(Arrays.asList(java, c, cpp));
        return answer;
    }

    public static ObservableList<PieChart.Data> getPieData() {
        return PIE_DATA;
    }

    public static DoubleSummaryStatistics getStats(ObservableList<Series<Number, Number>> chartData,
        Function<? super Data<Number, Number>, ? extends Number> mapper) {
        return chartData.stream().map(Series<Number, Number>::dataProperty)
            .map(ObjectProperty<ObservableList<Data<Number, Number>>>::get)
            .flatMap(ObservableList<Data<Number, Number>>::stream).map(mapper).mapToDouble(Number::doubleValue)
            .summaryStatistics();
    }

    public static void setBounds(NumberAxis yAxis, DoubleSummaryStatistics yStats) {
        yAxis.setLowerBound(Math.floor(yStats.getMin() / 10) * 10);
        yAxis.setUpperBound(Math.ceil(yStats.getMax() / 10) * 10);
    }
}
