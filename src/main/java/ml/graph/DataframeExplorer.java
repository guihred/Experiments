package ml.graph;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ml.data.*;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.FunctionEx;
import utils.HasLogging;
import utils.RunnableEx;

public class DataframeExplorer extends Application {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo;
    @FXML
    private ListView<Entry<String, DataframeStatisticAccumulator>> columnsList;
    @FXML
    private TextField text;
    @FXML
    private ListView<Question> questionsList;
    @FXML
    private ComboBox<QuestionType> questType;
    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    private DataframeML dataframe;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private BarChart<String, Number> barChart;

    public void initialize() {
        questions.addListener(this::onQuestionsChange);
        lineChart.managedProperty().bind(lineChart.visibleProperty());
        barChart.managedProperty().bind(barChart.visibleProperty());
        new SimpleListViewBuilder<>(columnsList).items(columns).onSelect(this::onColumnChosen)
                .cellFactory(Entry<String, DataframeStatisticAccumulator>::getKey);
        lineChart.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> !lineChart.getData().isEmpty(), lineChart.dataProperty()));
        barChart.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> !barChart.getData().isEmpty(), barChart.dataProperty()));
        new SimpleComboBoxBuilder<>(headersCombo).items(columns)
                .converter(Entry<String, DataframeStatisticAccumulator>::getKey);
        new SimpleComboBoxBuilder<>(questType).cellFactory((q, cell) -> {
            cell.setText(FunctionEx.mapIf(q, QuestionType::getSign));
            cell.disableProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> isTypeDisabled(q, headersCombo.getSelectionModel().getSelectedItem()),
                            headersCombo.getSelectionModel().selectedItemProperty()));
        }).converter(QuestionType::getSign);
        new SimpleListViewBuilder<>(questionsList).items(questions).onKey(KeyCode.DELETE, questions::remove);
    }

    public void onActionAdd() {
        addQuestion();
    }

    public void onActionLoadCSV(ActionEvent e) {
        StageHelper.fileAction("Load CSV", this::addStats, "CSV", "*.csv").handle(e);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CommonsFX.loadFXML("Dataframe Explorer", "DataframeExplorer.fxml", this, primaryStage);
        CommonsFX.addCSS(primaryStage.getScene(), "progressLoader.css");
    }

    private void addQuestion() {
        QuestionType type = questType.getSelectionModel().getSelectedItem();
        Entry<String, DataframeStatisticAccumulator> selectedItem = getSelected(headersCombo);
        if (type != null && selectedItem != null) {
            String colName = selectedItem.getKey();
            Object tryNumber = DataframeUtils.tryNumber(dataframe, colName, text.getText());
            Question question = new Question(colName, tryNumber, type);
            questions.add(question);
        }
    }

    private void addStats(File file) {
        RunnableEx.runNewThread(() -> {
            LOG.info("File {} STARTING", file.getName());
            if (dataframe != null && !file.equals(dataframe.getFile())) {
                RunnableEx.runInPlatform(() -> {
                    questions.clear();
                    barChart.setData(FXCollections.emptyObservableList());
                    lineChart.setData(FXCollections.emptyObservableList());
                });
            }

            DataframeBuilder builder = DataframeBuilder.builder(file);
            for (Question question : questions) {
                builder.filter(question.getColName(), question::answer);
            }
            dataframe = builder.makeStats(progress.progressProperty());
            Set<Entry<String, DataframeStatisticAccumulator>> entrySet = dataframe.getStats().entrySet();
            RunnableEx.runInPlatform(() -> columns.setAll(entrySet));
            if (dataframe.getSize() <= 1000) {
                dataframe = builder.build();
            }
            LOG.info("File {} read", file.getName());
        });
    }

    private void addToBarChart(Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val, String extra) {

        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        String x = old.getKey();
        String y = val.getKey();
        Map<Object, XYChart.Data<Number, Number>> linkedHashMap = new LinkedHashMap<>();
        dataframe.forEachRow(map -> {
            XYChart.Data<Number, Number> e = new XYChart.Data<>((Number) map.get(x), (Number) map.get(y));
            if (extra != null) {
                linkedHashMap.merge(map.get(extra), e, (o, n) -> {
                    n.setXValue(o.getXValue().doubleValue() + n.getXValue().doubleValue());
                    n.setYValue(o.getYValue().doubleValue() + n.getYValue().doubleValue());
                    return n;
                });
                e.setExtraValue(map.get(extra));
            }
            data.add(e);
        });

        Series<Number, Number> a = new Series<>();
        ObservableList<Series<Number, Number>> value = FXCollections.observableArrayList();
        if (extra != null) {
            a.setName(extra);
        }
        value.add(a);
        lineChart.getXAxis().setLabel(old.getKey());
        lineChart.getYAxis().setLabel(val.getKey());
        a.setData(data);
        lineChart.setData(value);
    }

    @SuppressWarnings("unchecked")
    private void onColumnChosen(Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val) {
        if (val == null) {
            return;
        }
        ObservableList<XYChart.Data<String, Number>> barList = FXCollections.observableArrayList();
        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setTitle(val.getKey());
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            addToPieChart(barList, countMap);
            return;
        }
        if (!dataframe.isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            addToPieChart(barList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(old, val, barChart.getTitle());
            if (barChart.getTitle() != null) {
                String key = val.getKey();
                String title = barChart.getTitle();
                List<Entry<String, Number>> collect = toPie(dataframe, title, key);
                barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
                addToPieChart(barList, collect);
            }
            return;
        }
        if (barChart.getTitle() != null) {
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            String title = barChart.getTitle();
            String key = val.getKey();
            List<Entry<String, Number>> collect = toPie(dataframe, title, key);
            addToPieChart(barList, collect);
        }
    }

    private void onQuestionsChange(Change<? extends Question> c) {
        while (c.next()) {
            if (dataframe.isLoaded() && c.wasAdded() && !c.wasRemoved()) {
                for (Question question : c.getAddedSubList()) {
                    dataframe.filter(question.getColName(), question::answer);
                }
                columns.setAll(DataframeUtils.makeStats(dataframe).entrySet());
            } else {
                addStats(dataframe.getFile());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static <T, U extends Comparable<? super U>> void addToList(ObservableList<T> dataList, List<T> arrayList2,
            Function<T, U> keyExtractor) {
        RunnableEx.runInPlatform(() -> {
            arrayList2.sort(Comparator.comparing(keyExtractor));
            dataList.addAll(arrayList2);
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<XYChart.Data<String, Number>> bar2List,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<PieChart.Data> pieList = Collections.synchronizedList(new ArrayList<>());
            List<XYChart.Data<String, Number>> barList = Collections.synchronizedList(new ArrayList<>());
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                barList.add(new XYChart.Data<>(k, v));
                if (barList.size() % 100 == 0) {
                    addToList(bar2List, new ArrayList<>(barList), m -> m.getYValue().doubleValue());
                    pieList.clear();
                    barList.clear();
                }
            });
            addToList(bar2List, barList, m -> m.getYValue().doubleValue());
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<XYChart.Data<String, Number>> barList,
            Map<String, T> countMap) {
        addToPieChart(barList, countMap.entrySet());
    }

    private static <T> T getSelected(ComboBox<T> headersCombo) {
        T selectedItem = headersCombo.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem;
        }
        ObservableList<T> columns = headersCombo.getItems();
        int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
        return columns.isEmpty() || selectedIndex < 0 ? null : columns.get(selectedIndex % columns.size());
    }

    private static Boolean isTypeDisabled(QuestionType q, Entry<String, DataframeStatisticAccumulator> it) {
        return it == null || q == null || !q.matchesClass(it.getValue().getFormat());
    }

    private static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }
}
