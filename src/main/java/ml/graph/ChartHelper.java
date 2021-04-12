package ml.graph;

import static utils.StringSigaUtils.toDouble;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.ToDoubleFunction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import ml.data.BaseDataframe;
import ml.data.DataframeStatisticAccumulator;
import utils.CommonsFX;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;

public final class ChartHelper {

    private ChartHelper() {
    }

    public static void addToLineChart(BaseDataframe dataframeML, LineChart<Number, Number> lineChart,
            Entry<String, DataframeStatisticAccumulator> old, Entry<String, DataframeStatisticAccumulator> val,
            String name) {

        ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
        String x = old.getKey();
        String y = val.getKey();
        Map<Object, Data<Number, Number>> linkedHashMap = new LinkedHashMap<>();
        dataframeML.forEachRow(map -> {
            Data<Number, Number> e = new Data<>((Number) map.get(x), (Number) map.get(y));
            if (name != null) {
                linkedHashMap.merge(map.get(name), e, (o, n) -> {
                    n.setXValue(toDouble(o.getXValue()) + toDouble(n.getXValue()));
                    n.setYValue(toDouble(o.getYValue()) + toDouble(n.getYValue()));
                    return n;
                });
                e.setExtraValue(map.get(name));
            }
            data.add(e);
        });

        Series<Number, Number> a = new Series<>();
        ObservableList<Series<Number, Number>> value = FXCollections.observableArrayList();
        if (name != null) {
            a.setName(name);
        }
        value.add(a);
        lineChart.getXAxis().setLabel(old.getKey());
        lineChart.getYAxis().setLabel(val.getKey());
        a.setData(data);
        lineChart.setData(value);
    }

    public static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> bar2List,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<Data<String, Number>> barList = Collections.synchronizedList(new ArrayList<>());
            Data<String, Number> others = new Data<>("Others", 0);
            countMap.forEach(ConsumerEx.ignore(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                barList.add(new Data<>(k, v));
                if (barList.size() % (ExplorerHelper.MAX_ELEMENTS / 10) == 0) {
                    addToList(bar2List, new ArrayList<>(barList), others, m -> m.getYValue().doubleValue());
                    barList.clear();
                }
            }));
            addToList(bar2List, barList, others, m -> m.getYValue().doubleValue());
        });
    }

    public static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> barList,
            Map<String, T> countMap) {
        addToPieChart(barList, countMap.entrySet());
    }

    public static ObservableList<Series<String, Number>> singleSeries(String val,
            ObservableList<Data<String, Number>> barList2) {
        return FXCollections.singletonObservableList(new Series<>(val, barList2));
    }

    private static void addToList(ObservableList<Data<String, Number>> dataList, List<Data<String, Number>> array,
            Data<String, Number> others, ToDoubleFunction<Data<String, Number>> keyExtractor) {
        CommonsFX.runInPlatformSync(() -> {
            if (dataList.size() >= ExplorerHelper.MAX_ELEMENTS / 4) {
                others.setYValue(keyExtractor.applyAsDouble(others) + array.stream().mapToDouble(keyExtractor).sum());
                if (!dataList.contains(others)) {
                    dataList.add(others);
                }
            } else {
                array.sort(Comparator.comparingDouble(keyExtractor).reversed());
                dataList.addAll(array);
            }
        });
    }
}
