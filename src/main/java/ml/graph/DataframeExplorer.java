package ml.graph;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.data.*;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.FunctionEx;
import utils.HasLogging;
import utils.RunnableEx;

public class DataframeExplorer extends Application {

    private static final Logger LOG = HasLogging.log();
    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private ObservableList<Question> questions = FXCollections.observableArrayList();
    private DataframeML dataframe;
    private PieChart pieChart;
    private ProgressIndicator progress;

    @Override
    public void start(Stage primaryStage) throws Exception {
        questions.addListener(this::onQuestionsChange);
        HBox root = new HBox();
        pieChart = new PieChart();
        LineChart<Number, Number> barChart = new LineChart<>(new NumberAxis(), new NumberAxis());
        VBox vBox2 = new VBox();
        ListView<Entry<String, DataframeStatisticAccumulator>> columnsList =
                new SimpleListViewBuilder<Entry<String, DataframeStatisticAccumulator>>().items(columns)
                        .onSelect((old, val) -> onColumnChosen(barChart, old, val))
                        .cellFactory(Entry<String, DataframeStatisticAccumulator>::getKey).build();
        vBox2.getChildren().add(columnsList);
        root.getChildren().add(vBox2);
        VBox vBox = new VBox(pieChart, barChart);
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        root.getChildren().add(vBox);
        progress = new ProgressIndicator();
        vBox2.getChildren()
                .add(new HBox(StageHelper.chooseFile("Load CSV", "Load CSV", this::addStats, "CSV", "*.csv"), progress)
        );
        HBox.setHgrow(vBox, Priority.ALWAYS);

        HBox hBox = new HBox();
        ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo =
                new SimpleComboBoxBuilder<Entry<String, DataframeStatisticAccumulator>>().items(columns)
                        .converter(Entry<String, DataframeStatisticAccumulator>::getKey).build();
        hBox.getChildren().add(headersCombo);
        ComboBox<QuestionType> questType = new SimpleComboBoxBuilder<QuestionType>()
                .items(QuestionType.values()).cellFactory((q, cell) -> {
                    cell.setText(FunctionEx.mapIf(q, QuestionType::getSign));
                    cell.disableProperty()
                            .bind(Bindings.createBooleanBinding(
                                    () -> isTypeDisabled(q, headersCombo.getSelectionModel().getSelectedItem()),
                                    headersCombo.getSelectionModel().selectedItemProperty()));
                }).build();
        hBox.getChildren().add(questType);
        TextField text = new TextField();
        hBox.getChildren().add(text);
        hBox.getChildren().add(SimpleButtonBuilder.newButton("Add", e -> addQuestion(headersCombo, questType, text)));

        ListView<Question> questionList =
                new SimpleListViewBuilder<Question>().items(questions).onKey(KeyCode.DELETE, questions::remove).build();
        VBox.setVgrow(questionList, Priority.ALWAYS);
        vBox2.getChildren().add(hBox);
        vBox2.getChildren().add(questionList);

        primaryStage.setTitle("Dataframe Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        CommonsFX.addCSS(primaryStage.getScene(), "filesComparator.css");

    }

    private void addQuestion(ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo,
            ComboBox<QuestionType> questType, TextField text) {
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
                RunnableEx.runInPlatform(() -> questions.clear());
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
        if (!dataframe.isLoaded()) {
            Map<String, Integer> countMap = val.getValue().getCountMap();
            pieChart.setData(dataList);
            addToPieChart(dataList, countMap);
            return;
        }

        if (old != null && old.getValue().getFormat() != String.class) {
            addToBarChart(barChart, old, val, pieChart.getTitle());
            if (pieChart.getTitle() != null) {
                String key = val.getKey();
                String title = pieChart.getTitle();
                List<Entry<String, Number>> collect = toPie(dataframe, title, key);
                pieChart.setData(dataList);
                addToPieChart(dataList, collect);
            }
            return;
        }
        if (pieChart.getTitle() != null) {
            pieChart.setData(dataList);
            String title = pieChart.getTitle();
            String key = val.getKey();
            List<Entry<String, Number>> collect = toPie(dataframe, title, key);
            addToPieChart(dataList, collect);
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
                if (arrayList.size() % 100 == 0) {
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

    private static <T> T getSelected(ComboBox<T> headersCombo) {
        T selectedItem = headersCombo.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem;
        }
        ObservableList<T> columns = headersCombo.getItems();
        int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
        return columns.isEmpty() ? null : columns.get(selectedIndex % columns.size());
    }

    private static Boolean isTypeDisabled(QuestionType q, Entry<String, DataframeStatisticAccumulator> it) {
        return it == null
                || q != QuestionType.EQ && q != QuestionType.NE && q != QuestionType.CONTAINS
                        && it.getValue().getFormat() == String.class
                || q == QuestionType.CONTAINS && it.getValue().getFormat() != String.class;
    }

    private static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }
}
