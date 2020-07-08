package ml.graph;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox root = new HBox();

        PieChart pieChart = new PieChart();
        root.getChildren().add(new SimpleListViewBuilder<Entry<String, DataframeStatisticAccumulator>>().items(columns)
                .onSelect((old, val) -> {
                    if (val == null) {
                        return;
                    }
                    ObservableList<Data> dataList = FXCollections.observableArrayList();
                    pieChart.setData(dataList);
                    Class<? extends Comparable<?>> format = val.getValue().getFormat();
                    if (format == String.class) {
                        Map<String, Integer> countMap = val.getValue().getCountMap();
                        pieChart.setTitle(val.getKey());
                        addToPieChart(dataList, countMap);
                    } else if (pieChart.getTitle() != null) {
                        List<Entry<Object, Object>> createSeries =
                                DataframeUtils.createSeries(dataframe, pieChart.getTitle(), val.getKey());
                        List<Entry<String, Number>> collect = createSeries.stream()
                                .map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                                .collect(Collectors.toList());
                        addToPieChart(dataList, collect);
                    } else {
                        addToPieChart(dataList, val.getValue().getCountMap());
                    }

                }).cellFactory(Entry<String, DataframeStatisticAccumulator>::getKey).build());
        root.getChildren().add(pieChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        root.getChildren().add(StageHelper.chooseFile("Load CSV", "Load CSV", this::addStats, "CSV", "*.csv"));
        primaryStage.setTitle("Dataframe Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    private void addStats(File file) {
        RunnableEx.runNewThread(() -> {
            dataframe = DataframeBuilder.builder(file).makeStats();
            Set<Entry<String, DataframeStatisticAccumulator>> entrySet =
                    dataframe.getStats().entrySet();
            RunnableEx.runInPlatform(() -> columns.setAll(entrySet));
            if (dataframe.getSize() < 1000) {
                dataframe = DataframeBuilder.build(file);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addToList(ObservableList<Data> dataList, List<Data> arrayList2) {
        RunnableEx.runInPlatform(() -> {
            arrayList2.sort(
                    Comparator.comparing(Data::getPieValue));
            dataList.addAll(arrayList2);
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<Data> dataList,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<Data> arrayList = Collections.synchronizedList(new ArrayList<>());
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                Data e = new Data(k, v.doubleValue());
                arrayList.add(e);
                if (arrayList.size() % 50 == 0) {
                    addToList(dataList, new ArrayList<>(arrayList));
                    arrayList.clear();
                }
            });
            addToList(dataList, arrayList);
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<Data> dataList, Map<String, T> countMap) {
        addToPieChart(dataList, countMap.entrySet());
    }
}
