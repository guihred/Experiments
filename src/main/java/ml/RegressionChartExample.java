package ml;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleComboBoxBuilder;
import utils.CSVUtils;
import utils.StringSigaUtils;

public class RegressionChartExample extends Application {
    private DataframeML dataframe;

    @Override
    public void start(Stage primaryStage) {
        String[] list = CSVUtils.getDataframeCSVs();
        dataframe = DataframeBuilder.builder("out/" + list[0]).build();

        String key = "Country Name";
        ObservableList<Series<Number, Number>> data = observableArrayList();
        LineChart<Number, Number> lineChart = lineChart(data, "");
        List<Object> list2 = dataframe.list(key);
        ComboBox<Object> build = new SimpleComboBoxBuilder<>().items(list2)
            .onSelect(country -> onChangeCountry(dataframe, key, lineChart, list2, country)).select(0).build();
        ComboBox<String> file = new SimpleComboBoxBuilder<String>().items(Arrays.asList(list))
            .onSelect(datafile -> onChangeFile(key, lineChart, build, datafile)).select(0).build();
        VBox root = new VBox();
        root.getChildren().add(new HBox(file, build));
        root.getChildren().add(lineChart);

        primaryStage.setTitle("Regression Chart Example");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void onChangeFile(String key, LineChart<Number, Number> lineChart, ComboBox<Object> build,
        String datafile) {
        dataframe = DataframeBuilder.builder("out/" + datafile).build();
        onChangeCountry(dataframe, key, lineChart, dataframe.list(key), build.getSelectionModel().getSelectedItem());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static LineChart<Number, Number> lineChart(ObservableList<Series<Number, Number>> data, String value) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        xAxis.setForceZeroInRange(false);
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setData(data);
        lineChart.setTitle(value);
        return lineChart;
    }

    @SuppressWarnings("unchecked")
    private static void onChangeCountry(DataframeML x, String column, LineChart<Number, Number> data,
        List<Object> list2, Object country) {
        Map<String, Object> rowMap = x.rowMap(list2.indexOf(country));
        List<Entry<String, Object>> collect = rowMap.entrySet().stream().filter(e -> StringUtils.isNumeric(e.getKey()))
            .collect(Collectors.toList());
        RegressionModel regressionModel = new RegressionModel();
        Series<Number, Number> series = regressionModel.createSeries(rowMap.get(column).toString(),
            collect.stream().map(e -> StringSigaUtils.toInteger(e.getKey())).collect(Collectors.toList()),
            collect.stream().map(Entry<String, Object>::getValue).collect(Collectors.toList()));
        Series<Number, Number> expected = regressionModel.getExpectedSeries();
        Series<Number, Number> polinominalSeries = regressionModel.getPolinominalSeries();
        data.setData(observableArrayList(series, expected, polinominalSeries));
        data.setTitle(rowMap.get("Indicator Name").toString());

    }
}
