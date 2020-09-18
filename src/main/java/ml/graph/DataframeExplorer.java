package ml.graph;

import ethical.hacker.WhoIsScanner;
import extract.ExcelService;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ml.data.*;
import org.slf4j.Logger;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class DataframeExplorer extends ExplorerVariables {

    private static final Logger LOG = HasLogging.log();
    private ObservableList<Entry<String, DataframeStatisticAccumulator>> columns = FXCollections.observableArrayList();
    private ObservableList<Question> questions = FXCollections.observableArrayList();


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
        Entry<String, DataframeStatisticAccumulator> selectedItem = columnsList.getSelectionModel().getSelectedItem();
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

    private static DataframeBuilder builderWithQuestions(File file, List<Question> questions) {
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


    private static Boolean isTypeDisabled(QuestionType q, Entry<String, DataframeStatisticAccumulator> it) {
        return it == null || q == null || !q.matchesClass(it.getValue().getFormat());
    }

}
