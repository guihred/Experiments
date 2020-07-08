package ml.graph;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.DataframeUtils;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.StageHelper;
import utils.RunnableEx;

public class DataframeExplorer extends Application {

    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private DataframeML dataframe;
    private PieChart pieChart;

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox root = new HBox();
        pieChart = new PieChart();
        LineChart<Number, Number> barChart = new LineChart<>(new NumberAxis(), new NumberAxis());
        root.getChildren()
                .add(new SimpleListViewBuilder<Entry<String, DataframeStatisticAccumulator>>().items(columns)
                        .onSelect((old, val) -> onColumnChosen(barChart, old, val))
                        .cellFactory(Entry<String, DataframeStatisticAccumulator>::getKey).build());
        root.getChildren().add(pieChart);
        root.getChildren().add(barChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        root.getChildren().add(StageHelper.chooseFile("Load CSV", "Load CSV", this::addStats, "CSV", "*.csv"));
        primaryStage.setTitle("Dataframe Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    private void addStats(File file) {
        RunnableEx.runNewThread(() -> {
            dataframe = DataframeBuilder.builder(file).makeStats();
            Set<Entry<String, DataframeStatisticAccumulator>> entrySet = dataframe.getStats().entrySet();
            RunnableEx.runInPlatform(() -> columns.setAll(entrySet));
            if (dataframe.getSize() <= 1000) {
                dataframe = DataframeBuilder.build(file);
            }
        });
    }

    private void addToBarChart(LineChart<Number, Number> barChart, Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val, String extra) {

        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        String x = old.getKey();
        String y = val.getKey();
        dataframe.forEachRow(map -> {
            XYChart.Data<Number, Number> e = new XYChart.Data<>((Number) map.get(x), (Number) map.get(y));
            if (extra != null) {
                e.setExtraValue(map.get(extra));
            }
            data.add(e);
        });

        Series<Number, Number> a = new Series<>();
        ObservableList<Series<Number, Number>> value = FXCollections.observableArrayList();
        value.add(a);
        barChart.getXAxis().setLabel(old.getKey());
        barChart.getYAxis().setLabel(val.getKey());
        a.setData(data);
        barChart.setData(value);
    }

    private void onColumnChosen(LineChart<Number, Number> barChart, Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val) {
        if (val == null) {
            return;
        }
        ObservableList<PieChart.Data> dataList = FXCollections.observableArrayList();
        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            pieChart.setTitle(val.getKey());
            pieChart.setData(dataList);
            addToPieChart(dataList, countMap);
            return;
        }
        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(barChart, old, val, pieChart.getTitle());
            if (pieChart.getTitle() != null) {
                String key = val.getKey();
                String title = pieChart.getTitle();
                List<Entry<String, Number>> collect = toPie(title, key);
                addToPieChart(dataList, collect);
            }
            return;
        }
        if (pieChart.getTitle() != null) {
            pieChart.setData(dataList);
            String title = pieChart.getTitle();
            String key = val.getKey();
            List<Entry<String, Number>> collect = toPie(title, key);
            addToPieChart(dataList, collect);
        }
    }

    private List<Entry<String, Number>> toPie(String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream()
                .map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addToList(ObservableList<PieChart.Data> dataList, List<PieChart.Data> arrayList2) {
        RunnableEx.runInPlatform(() -> {
            arrayList2.sort(Comparator.comparing(PieChart.Data::getPieValue));
            dataList.addAll(arrayList2);
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<PieChart.Data> dataList,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<PieChart.Data> arrayList = Collections.synchronizedList(new ArrayList<>());
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                PieChart.Data e = new PieChart.Data(k, v.doubleValue());
                arrayList.add(e);
                if (arrayList.size() % 50 == 0) {
                    addToList(dataList, new ArrayList<>(arrayList));
                    arrayList.clear();
                }
            });
            addToList(dataList, arrayList);
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<PieChart.Data> dataList,
            Map<String, T> countMap) {
        addToPieChart(dataList, countMap.entrySet());
    }
}
