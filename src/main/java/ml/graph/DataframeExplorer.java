package ml.graph;

import ethical.hacker.WhoIsScanner;
import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ml.data.*;
import org.slf4j.Logger;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ListHelper;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class DataframeExplorer extends ExplorerVariables {
    private static final int MAX_ELEMENTS = 1000;
    private static final Logger LOG = HasLogging.log();
    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    private Thread currentThread;

    public void addStats(File file) {
        interruptCurrentThread();
        currentThread = RunnableEx.runNewThread(() -> {
            LOG.info("File {} STARTING", file.getName());
            if (getDataframe() != null && !file.equals(getDataframe().getFile())) {
                setDataframe(null);
                CommonsFX.runInPlatformSync(() -> {
                    questions.clear();
                    barChart.setData(FXCollections.emptyObservableList());
                    pieChart.setData(FXCollections.emptyObservableList());
                    lineChart.setData(FXCollections.emptyObservableList());
                });
            }
            readDataframe(file, MAX_ELEMENTS);
            currentThread = null;
        });
    }

    public void initialize() {
        questions.addListener(this::onQuestionsChange);
        lineChart.managedProperty().bind(lineChart.visibleProperty());
        barChart.managedProperty().bind(barChart.visibleProperty());
        pieChart.managedProperty().bind(pieChart.visibleProperty());
        histogram.getColumns().get(0).textProperty().bind(barChart.titleProperty());

        columns.addListener(this::onColumnsChange);
        SimpleTableViewBuilder.of(histogram).multipleSelection().equalColumns().copiable().savable()
                .onKey(KeyCode.ADD, list -> addQuestion(list, true))
                .onKey(KeyCode.SUBTRACT, list -> addQuestion(list, false));
        SimpleListViewBuilder.of(columnsList).items(columns).onSelect(this::onColumnChosen)
                .onKey(KeyCode.DELETE, columns::remove).addContextMenu("_Split", e -> splitByColumn());
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
        SimpleListViewBuilder.of(questionsList).items(questions).onKey(KeyCode.DELETE, questions::remove)
                .onKey(KeyCode.MINUS, this::toggleQuestion).onKey(KeyCode.SUBTRACT, this::toggleQuestion);
    }

    public void onActionAdd() {
        addQuestion();
    }

    public void onActionFillIP() {
        currentThread = RunnableEx.runNewThread(() -> {
            LOG.info("FILLING {} IPS", getDataframe().getFile().getName());
            Entry<String, DataframeStatisticAccumulator> selectedItem =
                    columnsList.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            String ipColumn = selectedItem.getKey();
            DataframeBuilder builder = builderWithQuestions(getDataframe().getFile(), questions);
            SimpleDoubleProperty count = new SimpleDoubleProperty();
            count.divide((double) getDataframe().getSize())
                    .addListener((ob, old, val) -> progress.setProgress(val.doubleValue()));
            setDataframe(WhoIsScanner.fillIPInformation(builder, ipColumn, count));
            File outFile = ResourceFXUtils.getOutFile("csv/" + getDataframe().getFile().getName());
            LOG.info("File {} SAVING IN", outFile);
            DataframeUtils.save(getDataframe(), outFile);
            readDataframe(outFile, MAX_ELEMENTS);
            LOG.info("File {} IPS FILLED", getDataframe().getFile().getName());
            currentThread = null;
        });
    }

    public void onActionLoadCSV(ActionEvent e) {
        new FileChooserBuilder().title("Load CSV").extensions("CSV", "*.csv", "*.xlsx", "*.xls")
                .onSelect(this::addStats).openFileAction(e);
    }

    public void onActionSave(ActionEvent event) {
        if (getDataframe() != null) {
            FileChooserBuilder chooser = new FileChooserBuilder();
            chooser.title("Save File").initialDir(getDataframe().getFile().getParentFile())
                    .initialFilename(getDataframe().getFile().getName()).extensions("Data", "*.csv");
            chooser.onSelect(outFile -> RunnableEx.runNewThread(() -> {
                if (!getDataframe().isLoaded()) {
                    readDataframe(getDataframe().getFile(), getDataframe().getSize());
                }
                DataframeUtils.save(getDataframe(), outFile);
                LOG.info("{} SAVED", outFile);
            })).saveFileAction(event);
        }
    }

    public void show() {
        start(new Stage());
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Dataframe Explorer", "DataframeExplorer.fxml", this, primaryStage);
        CommonsFX.addCSS(primaryStage.getScene(), "progressLoader.css");
    }

    private void addQuestion() {
        QuestionType type = questType.getSelectionModel().getSelectedItem();
        Entry<String, DataframeStatisticAccumulator> selectedItem = getSelected(headersCombo);
        if (type != null && selectedItem != null) {
            String colName = selectedItem.getKey();
            String text2 = text.getText();
            Object tryNumber = getQueryObject(type, colName, text2);
            Question question = new Question(colName, tryNumber, type);
            questions.add(question);
        }
    }

    private void addQuestion(List<Data<String, Number>> list, boolean add) {
        if (list.isEmpty()) {
            return;
        }
        QuestionType type = list.size() == 1 ? QuestionType.EQ : QuestionType.IN;
        String collect = list.stream().map(Data<String, Number>::getXValue).collect(Collectors.joining(";"));
        Entry<String, DataframeStatisticAccumulator> selectedItem =
                columns.stream().filter(e -> e.getKey().equals(barChart.getTitle())).findFirst().orElse(null);
        if (type != null && selectedItem != null) {
            String colName = selectedItem.getKey();
            String text2 = collect;
            Object tryNumber = getQueryObject(type, colName, text2);
            Question question = new Question(colName, tryNumber, type, !add);
            questions.add(question);
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

    private Object getQueryObject(QuestionType type, String colName, String text2) {
        if (type == QuestionType.IN) {
            List<Object> arrayList = new ArrayList<>();
            for (String string : text2.split("[,;\t\n]+")) {
                arrayList.add(DataframeUtils.tryNumber(getDataframe(), colName, string));
            }
            return arrayList;
        }
        return DataframeUtils.tryNumber(getDataframe(), colName, text2);
    }

    private void interruptCurrentThread() {
        RunnableEx.run(() -> {
            if (currentThread != null && currentThread.isAlive()) {
                LOG.info("STOPPING THREAD");
                currentThread.interrupt();
                setDataframe(null);
            }
        });
    }

    private void onColumnChosen(Entry<String, DataframeStatisticAccumulator> old,
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

    private void onColumnsChange(Change<? extends Entry<String, DataframeStatisticAccumulator>> c) {
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

    private void onQuestionsChange(Change<? extends Question> c) {
        while (c.next()) {
            if (getDataframe() != null) {
                if (getDataframe().isLoaded() && c.wasAdded() && !c.wasRemoved()) {
                    for (Question question : c.getAddedSubList()) {
                        getDataframe().filter(question.getColName(), question::answer);
                    }
                    int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
                    columns.setAll(DataframeUtils.makeStats(getDataframe()).entrySet());
                    headersCombo.getSelectionModel().select(selectedIndex);
                } else {
                    addStats(getDataframe().getFile());
                }
            }
        }
    }

    private void readDataframe(File file, int maxSize) {
        DataframeBuilder builder = builderWithQuestions(file, questions);
        if (!ExcelService.isExcel(file)) {
            Set<Entry<String, DataframeStatisticAccumulator>> entrySet = builder.columns();
            setDataframe(builder.dataframe());
            CommonsFX.runInPlatform(() -> columns.setAll(entrySet));
            builder.makeStats(progress.progressProperty());
        }
        if (ExcelService.isExcel(file) || getDataframe().getSize() <= maxSize) {
            setDataframe(builder.build());
            CommonsFX.runInPlatform(() -> dataTable.setListSize(getDataframe().getSize()));
        }
        CommonsFX.runInPlatform(() -> columns.setAll(SupplierEx
                .orElse(getDataframe().getStats(), () -> DataframeUtils.makeStats(getDataframe())).entrySet()));
        LOG.info("File {} READ", file.getName());
    }

    private void splitByColumn() {
        Entry<String, DataframeStatisticAccumulator> selectedItem =
                columnsList.getSelectionModel().getSelectedItem();
        int indexOf = getDataframe().cols().indexOf(selectedItem.getKey());
        CSVUtils.splitFile(getDataframe().getFile(), indexOf);
    }

    private void toggleQuestion(Question t) {
        t.toggleNot();
        questions.set(questions.indexOf(t), t);
    }

    public static void main(String[] args) {
        launch(args);
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
        if (!columns.isEmpty() && selectedIndex >= 0) {
            return columns.get(selectedIndex % columns.size());
        }
        return null;
    }

    private static Object getStatAt(List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList,
            Map<Integer, Map<String, Object>> cache, String key, Integer i) {
        return cache.computeIfAbsent(i, k -> ClassReflectionUtils.getGetterMap(addedSubList.get(k).getValue()))
                .get(key);
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
