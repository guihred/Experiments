package ml.graph;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ml.data.*;
import simplebuilder.FileChooserBuilder;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.CSVUtils;
import utils.CommonsFX;
import utils.ExcelService;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class DataframeExplorer extends ExplorerVariables implements HasLogging {
    private static String[] EXTENSIONS = { "*.xls", "*.xlsx", "*.csv" };
    public void addStats(File file) {
        interruptCurrentThread();
        currentThread = RunnableEx.runNewThread(() -> {
            getLogger().info("File {} STARTING", file.getName());
            if (getDataframe() != null && !file.equals(getDataframe().getFile())) {
                setDataframe(null);
                CommonsFX.runInPlatformSync(() -> {
                    questions.clear();
                    barChart.setData(FXCollections.emptyObservableList());
                    pieChart.setData(FXCollections.emptyObservableList());
                    lineChart.setData(FXCollections.emptyObservableList());
                });
            }
            readDataframe(file, ExplorerHelper.MAX_ELEMENTS);
            currentThread = null;
        });
    }

    public void initialize() {
        questions.addListener(this::onQuestionsChange);

        columns.addListener(this::onColumnsChange);
        histogram.setList(barList);
        histogram.addColumn(barChart.titleProperty(), i -> barList.get(i).getXValue());
        histogram.addColumn("Value", i -> barList.get(i).getYValue());
        histogram.onKey(KeyCode.ADD,
                list -> addQuestion(list.stream().map(barList::get).collect(Collectors.toList()), true));
        histogram.onKey(KeyCode.SUBTRACT,
                list -> addQuestion(list.stream().map(barList::get).collect(Collectors.toList()), false));
        histogram.setColumnsWidth(10, 5);
        statistics.onDoubleClick(i -> columnsList.getSelectionModel().select(i));
        SimpleListViewBuilder.of(columnsList).items(columns).copiable().multipleSelection()
                .onSelect(this::onColumnChosen)
                .onKey(KeyCode.ADD, () -> retainOnly(columnsList.getSelectionModel().getSelectedItems()))
                .onKey(KeyCode.SUBTRACT, () -> retainOnly(
                        columns.stream().filter(e -> e.getValue().getDistinct() > 0).collect(Collectors.toList())))
                .onKey(KeyCode.DELETE, col -> {
                    columns.remove(col);
                    getDataframe().removeCol(col.getKey());
                }).addContextMenu("Spli_t", e -> splitByColumn()).addContextMenu("Merge", new FileChooserBuilder()
                        .extensions("Data", EXTENSIONS).title("Choose File to merge").onSelect(f -> {
                            if (getDataframe() != null) {
                                RunnableEx.runNewThread(() -> {
                                    DataframeBuilder.builder(f).build(progress.progressProperty())
                                            .forEachRow(getDataframe()::add);
                                    CommonsFX.runInPlatform(() -> dataTable.setListSize(getDataframe().getSize()));
                                    CommonsFX.runInPlatform(() -> columns.setAll(
                                            SupplierEx.get(() -> DataframeUtils.makeStats(getDataframe())).entrySet()));

                                });

                            }
                        })::openFileAction)
                .addContextMenu("_Sort By",
                        e -> RunnableEx.runIf(columnsList.getSelectionModel().getSelectedItem(),
                                item -> DataframeUtils.sort(getDataframe(), item.getKey())))
                .addContextMenu("Add _Mapping",
                        e0 -> Mapping.showDialog(barChart, columnsList.getSelectionModel().getSelectedItems().stream()
                                .map(Entry<String, DataframeStatisticAccumulator>::getKey).toArray(String[]::new),
                                getDataframe(), this::addStats));
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
                            () -> QuestionType.isTypeDisabled(q, headersCombo.getSelectionModel().getSelectedItem()),
                            headersCombo.getSelectionModel().selectedItemProperty()));
        }).converter(QuestionType::getSign);
        SimpleListViewBuilder.of(questionsList).items(questions).onKey(KeyCode.DELETE, questions::remove)
                .onKey(KeyCode.MINUS, this::toggleQuestion).onKey(KeyCode.SUBTRACT, this::toggleQuestion).copiable()
                .pasteable(s -> Question.parseQuestion(getDataframe(), s));
        // RunnableEx.runNewThread(Mapping::getMethods);
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

    public void onActionAddMinus() {
        QuestionType type = questType.getSelectionModel().getSelectedItem();
        Entry<String, DataframeStatisticAccumulator> selectedItem = getSelected(headersCombo);
        if (type != null && selectedItem != null) {
            String colName = selectedItem.getKey();
            String text2 = text.getText();
            Object tryNumber = getQueryObject(type, colName, text2);
            Question question = new Question(colName, tryNumber, type, true);
            questions.add(question);
        }
    }

    public void onActionFillIP() {
        currentThread = RunnableEx.runNewThread(() -> {
            getLogger().info("FILLING {} IPS", getDataframe().getFile().getName());
            Entry<String, DataframeStatisticAccumulator> selectedItem =
                    columnsList.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            String ipColumn = selectedItem.getKey();
            DataframeBuilder builder = Question.builderWithQuestions(getDataframe().getFile(), questions);
            setDataframe(ExplorerHelper.fillIPInformation(builder, ipColumn, progress.progressProperty()));
            String out = "csv/" + getDataframe().getFile().getName();
            File outFile = ResourceFXUtils.getOutFile(out);
            getLogger().info("SAVING File {}", out);
            DataframeUtils.save(getDataframe(), outFile);
            readDataframe(outFile, ExplorerHelper.MAX_ELEMENTS);
            getLogger().info("File {} IPS FILLED", getDataframe().getFile().getName());
            currentThread = null;
        });
    }

    public void onActionLoadCSV(ActionEvent e) {
        new FileChooserBuilder().title("Load CSV").extensions("CSV", EXTENSIONS)
                .onSelect(this::addStats).openFileAction(e);
    }

    public void onActionSave(ActionEvent event) {
        if (getDataframe() != null) {
            FileChooserBuilder chooser = new FileChooserBuilder();
            chooser.title("Save File").initialDir(getDataframe().getFile().getParentFile())
                    .initialFilename(getDataframe().getFile().getName()).extensions("Data", "*.csv");
            chooser.onSelect(this::save).saveFileAction(event);
        }
    }

    public void readDataframe(File file, int maxSize) {
        DataframeBuilder builder = Question.builderWithQuestions(file, questions);
        if (!ExcelService.isExcel(file)) {
            Set<Entry<String, DataframeStatisticAccumulator>> entrySet = builder.columns();
            setDataframe(builder);
            CommonsFX.runInPlatform(() -> columns.setAll(entrySet));
            builder.makeStats(progress.progressProperty());
        }
        if (ExcelService.isExcel(file) || maxSize == 0 || getDataframe().getSize() <= maxSize) {
            setDataframe(builder.build(progress.progressProperty()));
            CommonsFX.runInPlatform(() -> dataTable.setListSize(getDataframe().getSize()));
        }
        CommonsFX.runInPlatform(() -> columns.setAll(SupplierEx
                .orElse(getDataframe().getStats(), () -> DataframeUtils.makeStats(getDataframe())).entrySet()));
        getLogger().info("File {} READ", file.getName());
    }

    public Thread save(File outFile) {
        return RunnableEx.runNewThread(() -> {
            List<String> cols = getDataframe().cols();
            if (!getDataframe().isLoaded()) {
                DataframeBuilder builder = Question.builderWithQuestions(getDataframe().getFile(), questions);
                setDataframe(builder.build(progress.progressProperty()));
                CommonsFX.runInPlatform(() -> dataTable.setListSize(getDataframe().getSize()));
                CommonsFX.runInPlatform(() -> columns.setAll(SupplierEx
                        .orElse(getDataframe().getStats(), () -> DataframeUtils.makeStats(getDataframe())).entrySet()));
            }
            DataframeML dataframe2 = getDataframe();
            dataframe2.cols().stream().filter(t -> !cols.contains(t)).forEach(dataframe2::removeCol);
            DataframeUtils.save(dataframe2, outFile);
            getLogger().info("{} SAVED", outFile);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Dataframe Explorer", "DataframeExplorer.fxml", this, primaryStage);
        CommonsFX.addCSS(primaryStage.getScene(), "progressLoader.css");
    }

    private void onQuestionsChange(Change<? extends Question> c) {
        c.next();
        if (getDataframe() == null) {
            return;
        }
        if (getDataframe().isLoaded() && c.wasAdded() && !c.wasRemoved()) {
            for (Question question : c.getList()) {
                if (question.getType() == QuestionType.DISTINCT) {
                    ((Set<?>) question.getOb()).clear();
                }
                getDataframe().filter(question.getColName(), question);
            }
            int selectedIndex = headersCombo.getSelectionModel().getSelectedIndex();
            columns.setAll(DataframeUtils.makeStats(getDataframe()).entrySet());
            headersCombo.getSelectionModel().select(selectedIndex);

            CommonsFX.runInPlatform(() -> dataTable.setListSize(getDataframe().getSize()));
        } else {
            addStats(getDataframe().getFile());
        }
    }

    private void retainOnly(List<Entry<String, DataframeStatisticAccumulator>> selectedItems) {
        List<Entry<String, DataframeStatisticAccumulator>> notSelectedCols =
                columns.stream().filter(s -> !selectedItems.contains(s)).collect(Collectors.toList());
        columns.removeAll(notSelectedCols);
        getDataframe().removeCol(notSelectedCols.stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .toArray(String[]::new));
    }

    private void splitByColumn() {
        RunnableEx.runIf(columnsList.getSelectionModel().getSelectedItem(),
                s -> CSVUtils.splitFile(getDataframe().getFile(), getDataframe().cols().indexOf(s.getKey())));
    }

    private void toggleQuestion(Question t) {
        t.toggleNot();
        questions.set(questions.indexOf(t), t);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
