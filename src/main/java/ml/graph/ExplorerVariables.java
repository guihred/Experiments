package ml.graph;

import static utils.StringSigaUtils.toDouble;

import extract.WhoIsScanner;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import ml.data.*;
import simplebuilder.ListHelper;
import utils.CommonsFX;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public abstract class ExplorerVariables extends Application {
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
    protected PaginatedTableView statistics;
    @FXML
    protected PaginatedTableView dataTable;
    @FXML
    protected PaginatedTableView histogram;
    @FXML
    protected ProgressIndicator progress;
    @FXML
    protected LineChart<Number, Number> lineChart;
    @FXML
    protected PieChart pieChart;

    @FXML
    protected BarChart<String, Number> barChart;

    protected Thread currentThread;

    protected ObservableList<Entry<String, DataframeStatisticAccumulator>> columns =
            FXCollections.observableArrayList();

    protected ObservableList<Question> questions = FXCollections.observableArrayList();
    protected ObservableList<Data<String, Number>> barList = FXCollections.observableArrayList();

    public ExplorerVariables() {
        dataframe.addListener((ob, old, val) -> CommonsFX.runInPlatform(() -> {
            String fileName = FunctionEx.mapIf(getDataframe(), d -> d.getFile().getName(), "");
            FunctionEx.mapIf2(questionsList, Node::getScene, s -> (Stage) s.getWindow())
                    .setTitle(String.format("Dataframe Explorer (%s)", fileName));
        }));
    }

    public void addQuestion(List<Data<String, Number>> list, boolean add) {
        if (list.isEmpty()) {
            return;
        }
        QuestionType type = list.size() == 1 ? QuestionType.EQ : QuestionType.IN;
        String selectedElements = list.stream().map(Data<String, Number>::getXValue).collect(Collectors.joining(";"));
        Entry<String, DataframeStatisticAccumulator> selectedItem =
                columns.stream().filter(e -> e.getKey().equals(barChart.getTitle())).findFirst().orElse(null);
        if (type != null && selectedItem != null) {
            String colName = selectedItem.getKey();
            String text2 = selectedElements;
            Object tryNumber = getQueryObject(type, colName, text2);
            Question question = new Question(colName, tryNumber, type, !add);
            questions.add(question);
        }
    }

    public DataframeML getDataframe() {
        return dataframe.get();
    }

    public void setDataframe(DataframeML dataframe) {
        this.dataframe.set(dataframe);
    }

    protected Object getQueryObject(QuestionType type, String colName, String text2) {
        return Question.getQueryObject(getDataframe(), type, colName, text2);
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
        barList.clear();
        ObservableList<PieChart.Data> pieData = ListHelper.mapping(barList,
                e -> new PieChart.Data(String.format("(%d) %s", e.getYValue().intValue(), e.getXValue()),
                        e.getYValue().doubleValue()));

        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            ExplorerHelper.addToPieChart(barList, countMap);
            return;
        }
        if (!getDataframe().isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            ExplorerHelper.addToPieChart(barList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(getDataframe(), old, val, barChart.getTitle());
            if (barChart.getTitle() != null) {
                String key = val.getKey();
                String title = barChart.getTitle();
                barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
                pieChart.setData(pieData);
                ExplorerHelper.addToPieChart(barList, ExplorerHelper.toPie(getDataframe(), title, key));
            }
            return;
        }
        if (barChart.getTitle() != null) {
            barChart.setData(FXCollections.singletonObservableList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            String title = barChart.getTitle();
            String key = val.getKey();
            ExplorerHelper.addToPieChart(barList, ExplorerHelper.toPie(getDataframe(), title, key));
        }
    }

    protected void onColumnsChange(Change<? extends Entry<String, DataframeStatisticAccumulator>> c) {
        c.next();
        if (c.wasRemoved()) {
            dataTable.clearColumns();
            statistics.clearColumns();
        }
        List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList = c.getList();
        ExplorerHelper.addEntries(statistics, addedSubList);
        if (!getDataframe().isLoaded()) {
            ExplorerHelper.addEntries(dataTable, addedSubList);
        } else {
            addedSubList.forEach(
                    entry -> dataTable.addColumn(entry.getKey(), i -> getDataframe().getAt(entry.getKey(), i)));
            dataTable.setListSize(getDataframe().getSize());
            double[] array = addedSubList.stream()
                    .mapToDouble(e -> Math.max(ExplorerHelper.getTopLength(e), e.getKey().length())).toArray();
            dataTable.setColumnsWidth(array);
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
}
