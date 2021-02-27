package ml.graph;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.Question;
import ml.data.QuestionType;
import simplebuilder.ListHelper;
import utils.CommonsFX;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.fx.AutocompleteField;
import utils.fx.PaginatedTableView;

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
    protected final SimpleObjectProperty<DataframeML> dataframe = new SimpleObjectProperty<>();
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

    protected final ObservableList<Data<String, Number>> barList = FXCollections.observableArrayList();

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
                currentThread.checkAccess();
                currentThread.interrupt();
                HasLogging.log().info("THREAD INTERRUPTED");
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
        RunnableEx.runNewThread(
                () -> val.getValue().getDistinct() == 0 || !val.getValue().getUnique().stream()
                        .allMatch(s -> s != null && s.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")),
                e -> CommonsFX.runInPlatform(() -> fillIP.setDisable(e)));
        barList.clear();
        ObservableList<PieChart.Data> pieData = ListHelper.mapping(barList,
                e -> new PieChart.Data(String.format("(%d) %s", e.getYValue().intValue(), e.getXValue()),
                        e.getYValue().doubleValue()));

        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            barChart.setData(ChartHelper.singleSeries(val.getKey(), barList));
            pieChart.setData(pieData);
            ChartHelper.addToPieChart(barList, countMap);
            return;
        }
        if (!getDataframe().isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setData(ChartHelper.singleSeries(val.getKey(), barList));
            pieChart.setData(pieData);
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            ChartHelper.addToPieChart(barList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            ChartHelper.addToLineChart(getDataframe(), lineChart, old, val, barChart.getTitle());
            if (barChart.getTitle() != null) {
                String key = val.getKey();
                String title = barChart.getTitle();
                barChart.setData(ChartHelper.singleSeries(val.getKey(), barList));
                pieChart.setData(pieData);
                ChartHelper.addToPieChart(barList, ExplorerHelper.toPie(getDataframe(), title, key));
            }
            return;
        }
        if (barChart.getTitle() != null) {
            barChart.setData(ChartHelper.singleSeries(val.getKey(), barList));
            pieChart.setData(pieData);
            String title = barChart.getTitle();
            String key = val.getKey();
            ChartHelper.addToPieChart(barList, ExplorerHelper.toPie(getDataframe(), title, key));
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


    protected static <T> T getSelected(ComboBox<T> headersCombo) {
        T selectedItem = headersCombo.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem;
        }
        List<T> columns = headersCombo.getItems();
        int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
        if (!columns.isEmpty() && selectedIndex >= 0) {
            return columns.get(selectedIndex % columns.size());
        }
        return null;
    }
}
