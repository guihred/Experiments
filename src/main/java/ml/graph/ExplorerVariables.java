package ml.graph;

import ethical.hacker.WhoIsScanner;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ml.data.*;
import simplebuilder.ListHelper;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public abstract class ExplorerVariables extends Application {
    protected static final int MAX_ELEMENTS = 1000;
    @FXML
    protected ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo;
    @FXML
    protected ListView<Entry<String, DataframeStatisticAccumulator>> columnsList;
    @FXML
    protected AutocompleteField text;
    @FXML
    protected Button fillIP;
    @FXML
    protected ListView<Question> questionsList;
    @FXML
    protected ComboBox<QuestionType> questType;
    protected final ObjectProperty<DataframeML> dataframe = new SimpleObjectProperty<>();
    @FXML
    protected PaginatedTableView dataTable;
    @FXML
    protected TableView<XYChart.Data<String, Number>> histogram;
    @FXML
    protected ProgressIndicator progress;
    @FXML
    protected LineChart<Number, Number> lineChart;
    @FXML
    protected PieChart pieChart;

    @FXML
    protected BarChart<String, Number> barChart;

    protected Thread currentThread;

    public ExplorerVariables() {
        dataframe.addListener((ob, old, val) -> CommonsFX.runInPlatform(() -> {
            String fileName = FunctionEx.mapIf(getDataframe(), d -> d.getFile().getName(), "");
            ((Stage) questionsList.getScene().getWindow()).setTitle(String.format("Dataframe Explorer (%s)", fileName));
        }));
    }

    public DataframeML getDataframe() {
        return dataframe.get();
    }

    public void setDataframe(DataframeML dataframe) {
        this.dataframe.set(dataframe);
    }

    protected Object getQueryObject(QuestionType type, String colName, String text2) {
        if (type == QuestionType.IN) {
            List<Object> arrayList = new ArrayList<>();
            for (String string : text2.split("[,;\t\n]+")) {
                arrayList.add(DataframeUtils.tryNumber(getDataframe(), colName, string));
            }
            return arrayList;
        }
        return DataframeUtils.tryNumber(getDataframe(), colName, text2);
    }

    protected void interruptCurrentThread() {
        RunnableEx.run(() -> {
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
                setDataframe(null);
            }
        });
    }

    protected void onColumnChosen(Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val) {
        if (val == null) {
            return;
        }

        fillIP.setDisable(true);
        RunnableEx.runNewThread(() -> {
            Set<String> unique = val.getValue().getUnique();
            return unique.isEmpty() || !unique.stream().allMatch(s -> s != null && s.matches(WhoIsScanner.IP_REGEX));
        }, e -> CommonsFX.runInPlatform(() -> fillIP.setDisable(e)));
        ObservableList<Data<String, Number>> barList = FXCollections.observableArrayList();
        ObservableList<PieChart.Data> pieData = ListHelper.mapping(barList,
                e -> new PieChart.Data(String.format("(%d) %s", e.getYValue().intValue(), e.getXValue()),
                        e.getYValue().doubleValue()));
        histogram.setItems(barList);
        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            addToPieChart(barList, countMap);
            return;
        }
        if (!getDataframe().isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            addToPieChart(barList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(getDataframe(), old, val, barChart.getTitle());
            if (barChart.getTitle() != null) {
                String key = val.getKey();
                String title = barChart.getTitle();
                barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
                pieChart.setData(pieData);
                addToPieChart(barList, toPie(getDataframe(), title, key));
            }
            return;
        }
        if (barChart.getTitle() != null) {
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            String title = barChart.getTitle();
            String key = val.getKey();
            addToPieChart(barList, toPie(getDataframe(), title, key));
        }
    }

    protected void onColumnsChange(Change<? extends Entry<String, DataframeStatisticAccumulator>> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                dataTable.clearColumns();
            }
            List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList = c.getList();
            if (!getDataframe().isLoaded()) {
                Map<Integer, Map<String, Object>> cache = new HashMap<>();
                List<String> asList = Arrays.asList("Header", "Mean", "Max", "Min", "Distinct", "Median25", "Median50",
                        "Median75", "Sum");
                for (String key : asList) {
                    dataTable.addColumn(key, i -> getStatAt(addedSubList, cache, key.toLowerCase(), i));
                }
                dataTable.setListSize(addedSubList.size());
                double[] array = asList.stream().mapToDouble(e -> Math
                        .max(Objects.toString(getStatAt(addedSubList, cache, e.toLowerCase(), 0)).length(), e.length()))
                        .toArray();
                dataTable.setColumnsWidth(array);
            } else {
                addedSubList.forEach(entry -> dataTable.addColumn(entry.getKey(),
                        i -> getDataframe().getDataframe().get(entry.getKey()).get(i)));
                dataTable.setListSize(getDataframe().getSize());
                double[] array = addedSubList.stream()
                        .mapToDouble(
                                e -> Math.max(Objects.toString(e.getValue().getTop()).length(), e.getKey().length()))
                        .toArray();
                dataTable.setColumnsWidth(array);

            }
        }
    }

    private void addToBarChart(DataframeML dataframeML, Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val, String name) {

        ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
        String x = old.getKey();
        String y = val.getKey();
        Map<Object, Data<Number, Number>> linkedHashMap = new LinkedHashMap<>();
        dataframeML.forEachRow(map -> {
            Data<Number, Number> e = new Data<>((Number) map.get(x), (Number) map.get(y));
            if (name != null) {
                linkedHashMap.merge(map.get(name), e, (o, n) -> {
                    n.setXValue(o.getXValue().doubleValue() + n.getXValue().doubleValue());
                    n.setYValue(o.getYValue().doubleValue() + n.getYValue().doubleValue());
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

    private static void addToList(ObservableList<Data<String, Number>> dataList, List<Data<String, Number>> array,
            Data<String, Number> others, Function<Data<String, Number>, Double> keyExtractor) {
        CommonsFX.runInPlatformSync(() -> {
            if (dataList.size() >= MAX_ELEMENTS / 4) {
                others.setYValue(keyExtractor.apply(others) + array.stream().mapToDouble(keyExtractor::apply).sum());
                if (!dataList.contains(others)) {
                    dataList.add(others);
                }
            } else {
                array.sort(Comparator.comparing(keyExtractor).reversed());
                dataList.addAll(array);
            }
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> bar2List,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<Data<String, Number>> barList = Collections.synchronizedList(new ArrayList<>());
            Data<String, Number> others = new Data<>("Others", 0);
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                barList.add(new Data<>(k, v));
                if (barList.size() % (MAX_ELEMENTS / 10) == 0) {
                    addToList(bar2List, new ArrayList<>(barList), others, m -> m.getYValue().doubleValue());
                    barList.clear();
                }
            });
            addToList(bar2List, barList, others, m -> m.getYValue().doubleValue());
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> barList,
            Map<String, T> countMap) {
        addToPieChart(barList, countMap.entrySet());
    }

    private static Object getStatAt(List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList,
            Map<Integer, Map<String, Object>> cache, String key, Integer i) {
        return cache.computeIfAbsent(i, k -> ClassReflectionUtils.getGetterMap(addedSubList.get(k).getValue()))
                .get(key);
    }

    private static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }
}
