package ml.graph;

import ethical.hacker.WhoIsScanner;
import gaming.ex21.ListHelper;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ml.data.*;
import org.slf4j.Logger;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.StageHelper;
import utils.*;

public class DataframeExplorer extends Application {
    private static final int MAX_ELEMENTS = 1000;
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo;
    @FXML
    private ListView<Entry<String, DataframeStatisticAccumulator>> columnsList;
    @FXML
    private AutocompleteField text;
    @FXML
    private Button fillIP;
    @FXML
    private ListView<Question> questionsList;
    @FXML
    private ComboBox<QuestionType> questType;
    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    private DataframeML dataframe;
    @FXML
    private PaginatedTableView dataTable;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;
    private Thread currentThread;

    public void initialize() {
        questions.addListener(this::onQuestionsChange);
        lineChart.managedProperty().bind(lineChart.visibleProperty());
        barChart.managedProperty().bind(barChart.visibleProperty());
        pieChart.managedProperty().bind(pieChart.visibleProperty());
        columns.addListener((Change<? extends Entry<String, DataframeStatisticAccumulator>> c) -> onColumnsChange(
                dataframe, dataTable, c));
        SimpleListViewBuilder.of(columnsList).items(columns).onSelect(this::onColumnChosen);
        lineChart.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> !lineChart.getData().isEmpty(), lineChart.dataProperty()));
        pieChart.visibleProperty().bind(pieChart.titleProperty().isNotEmpty());
        barChart.visibleProperty()
                .bind(Bindings.createBooleanBinding(() -> !barChart.getData().isEmpty(), barChart.dataProperty()));
        SimpleComboBoxBuilder.of(headersCombo).items(columns)
                .converter(Entry<String, DataframeStatisticAccumulator>::getKey).onChange((old, val) -> text
                        .setEntries(FunctionEx.mapIf(val, v -> v.getValue().getUnique(), Collections.emptySet())));

        SimpleComboBoxBuilder.of(questType).cellFactory((q, cell) -> {
            cell.setText(FunctionEx.mapIf(q, QuestionType::getSign));
            cell.disableProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> isTypeDisabled(q, headersCombo.getSelectionModel().getSelectedItem()),
                            headersCombo.getSelectionModel().selectedItemProperty()));
        }).converter(QuestionType::getSign);
        SimpleListViewBuilder.of(questionsList).items(questions).onKey(KeyCode.DELETE, questions::remove);
    }

    public void onActionAdd() {
        addQuestion();
    }

    public void onActionFillIP() {
        RunnableEx.runNewThread(() -> {
            LOG.info("FILLING {} IPS", dataframe.getFile().getName());

            DataframeBuilder builder = builderWithQuestions(dataframe.getFile(), questions);
            String ipColumn = columnsList.getSelectionModel().getSelectedItem().getKey();
            dataframe = WhoIsScanner.fillIPInformation(builder, ipColumn);

            File outFile = ResourceFXUtils.getOutFile("csv/" + dataframe.getFile().getName());
            DataframeUtils.save(dataframe, outFile);
            int maxSize = MAX_ELEMENTS;
            readDataframe(outFile, maxSize);
            LOG.info("File {} IPS FILLED", dataframe.getFile().getName());
        });
    }

    public void onActionLoadCSV(ActionEvent e) {
        StageHelper.fileAction("Load CSV", this::addStats, "CSV", "*.csv").handle(e);
    }

    public void onActionSave() {
        if (dataframe != null) {

            FileChooser fileChooser2 = new FileChooser();
            fileChooser2.setTitle("Save File");
            fileChooser2.setInitialFileName(dataframe.getFile().getName());
            fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Data", "*.csv"));
            File outFile = fileChooser2.showSaveDialog(text.getScene().getWindow());
            if (outFile != null) {
                RunnableEx.runNewThread(() -> {
                    if (!dataframe.isLoaded()) {
                        readDataframe(dataframe.getFile(), dataframe.getSize());
                    }
                    DataframeUtils.save(dataframe, outFile);
                    LOG.info("{} SAVED", outFile);
                });
            }
        }
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
        interruptCurrentThread();

        currentThread = RunnableEx.runNewThread(() -> {
            LOG.info("File {} STARTING", file.getName());
            if (dataframe != null && !file.equals(dataframe.getFile())) {
                RunnableEx.runInPlatform(() -> {
                    questions.clear();
                    barChart.setData(FXCollections.emptyObservableList());
                    pieChart.setData(FXCollections.emptyObservableList());
                    lineChart.setData(FXCollections.emptyObservableList());
                });
            }
            int maxSize = MAX_ELEMENTS;
            readDataframe(file, maxSize);
        });
    }

    private void interruptCurrentThread() {
        RunnableEx.run(() -> {
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
                dataframe = null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void onColumnChosen(Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val) {
        if (val == null) {
            return;
        }

        fillIP.setDisable(true);
        RunnableEx.runNewThread(
                () -> {
                    Set<String> unique = val.getValue().getUnique();
                    return unique.isEmpty()
                            || !unique.stream().allMatch(s -> s != null && s.matches(WhoIsScanner.IP_REGEX));
                },
                e -> RunnableEx.runInPlatform(() -> fillIP.setDisable(e)));
        ObservableList<XYChart.Data<String, Number>> barList = FXCollections.observableArrayList();
        ObservableList<PieChart.Data> pieData =
                ListHelper.mapping(barList, e -> new PieChart.Data(e.getXValue(), e.getYValue().doubleValue()));
        Class<? extends Comparable<?>> format = val.getValue().getFormat();
        if (format == String.class) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setTitle(val.getKey());
            pieChart.setTitle(val.getKey());
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            addToPieChart(barList, countMap);
            return;
        }
        if (!dataframe.isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
            addToPieChart(barList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(lineChart, dataframe, old, val, barChart.getTitle());
            if (barChart.getTitle() != null) {
                String key = val.getKey();
                String title = barChart.getTitle();
                List<Entry<String, Number>> collect = toPie(dataframe, title, key);
                barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
                pieChart.setData(pieData);
                addToPieChart(barList, collect);
            }
            return;
        }
        if (barChart.getTitle() != null) {
            barChart.setData(FXCollections.observableArrayList(new Series<>(val.getKey(), barList)));
            pieChart.setData(pieData);
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
                int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
                columns.setAll(DataframeUtils.makeStats(dataframe).entrySet());
                headersCombo.getSelectionModel().select(selectedIndex);
            } else {
                addStats(dataframe.getFile());
            }
        }
    }

    private void readDataframe(File file, int maxSize) {
        DataframeBuilder builder = builderWithQuestions(file,questions);
        Set<Entry<String, DataframeStatisticAccumulator>> entrySet = builder.columns();
        dataframe = builder.dataframe();
        RunnableEx.runInPlatform(() -> columns.setAll(entrySet));
        builder.makeStats(progress.progressProperty());
        RunnableEx.runInPlatform(() -> columns.setAll(dataframe.getStats().entrySet()));
        if (dataframe.getSize() <= maxSize) {
            dataframe = builder.build();
            RunnableEx.runInPlatform(() -> dataTable.setListSize(dataframe.getSize()));
        }
        LOG.info("File {} read", file.getName());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void addToBarChart(LineChart<Number, Number> lineChart, DataframeML dataframe,
            Entry<String, DataframeStatisticAccumulator> old,
            Entry<String, DataframeStatisticAccumulator> val, String name) {

        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        String x = old.getKey();
        String y = val.getKey();
        Map<Object, XYChart.Data<Number, Number>> linkedHashMap = new LinkedHashMap<>();
        dataframe.forEachRow(map -> {
            XYChart.Data<Number, Number> e = new XYChart.Data<>((Number) map.get(x), (Number) map.get(y));
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

    private static void addToList(ObservableList<XYChart.Data<String, Number>> dataList,
            List<XYChart.Data<String, Number>> arrayList2, XYChart.Data<String, Number> others,
            Function<XYChart.Data<String, Number>, Double> keyExtractor) {
        RunnableEx.runInPlatformSync(() -> {
            if (dataList.size() >= MAX_ELEMENTS / 4) {
                others.setYValue(keyExtractor.apply(others) + arrayList2.stream()
                        .mapToDouble(keyExtractor::apply).sum());
                if (!dataList.contains(others)) {
                    dataList.add(others);
                }
            } else {
                arrayList2.sort(Comparator.comparing(keyExtractor));
                dataList.addAll(arrayList2);
            }
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<XYChart.Data<String, Number>> bar2List,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<XYChart.Data<String, Number>> barList = Collections.synchronizedList(new ArrayList<>());
            XYChart.Data<String, Number> others = new XYChart.Data<>("Others", 0);
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                barList.add(new XYChart.Data<>(k, v));
                if (barList.size() % (MAX_ELEMENTS / 10) == 0) {
                    addToList(bar2List, new ArrayList<>(barList), others, m -> m.getYValue().doubleValue());
                    barList.clear();
                }
            });
            addToList(bar2List, barList, others, m -> m.getYValue().doubleValue());
        });
    }

    private static <T extends Number> void addToPieChart(ObservableList<XYChart.Data<String, Number>> barList,
            Map<String, T> countMap) {
        addToPieChart(barList, countMap.entrySet());
    }

    private static DataframeBuilder builderWithQuestions(File file, ObservableList<Question> questions) {
        DataframeBuilder builder = DataframeBuilder.builder(file);
        for (Question question : questions) {
            builder.filter(question.getColName(), question::answer);
        }
        return builder;
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

    private static Object getStatAt(List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList,
            Map<Integer, Map<String, Object>> cache, String key, Integer i) {
        return cache.computeIfAbsent(i, k -> ClassReflectionUtils.getGetterMap(addedSubList.get(k).getValue()))
                .get(key);
    }

    private static Boolean isTypeDisabled(QuestionType q, Entry<String, DataframeStatisticAccumulator> it) {
        return it == null || q == null || !q.matchesClass(it.getValue().getFormat());
    }

    private static void onColumnsChange(DataframeML dataframe,PaginatedTableView dataTable,Change<? extends Entry<String, DataframeStatisticAccumulator>> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                dataTable.clearColumns();
            }
            List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList = c.getList();
            if (!dataframe.isLoaded()) {
                Map<Integer, Map<String, Object>> cache = new HashMap<>();
                for (String key : Arrays.asList("Header", "Format", "Count", "Max", "Mean", "Min", "Median25",
                        "Median50", "Median75", "Sum")) {
                    dataTable.addColumn(key, i -> getStatAt(addedSubList, cache, key.toLowerCase(), i));
                }

                dataTable.setListSize(addedSubList.size());
            } else {
                addedSubList.forEach(entry -> dataTable.addColumn(entry.getKey(),
                        i -> dataframe.getDataframe().get(entry.getKey()).get(i)));
                dataTable.setListSize(dataframe.getSize());
                double[] array =
                        addedSubList.stream()
                                .mapToDouble(e -> Math.max(Objects.toString(e.getValue().getTop()).length(),
                                        e.getKey().length()))
                                .toArray();
                dataTable.setColumnsWidth(array);

            }
        }
    }

    private static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }
}
