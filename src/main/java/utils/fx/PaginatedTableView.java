package utils.fx;

import static java.util.stream.DoubleStream.concat;
import static java.util.stream.DoubleStream.of;
import static simplebuilder.SimpleTableViewBuilder.prefWidthColumns;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.QuickSortML;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.PredicateEx;
import utils.ex.RunnableEx;

public final class PaginatedTableView extends VBox {

    @FXML
    private IntegerProperty pageSize;
    @FXML
    private TableView<Integer> table;

    @FXML
    private Pagination pagination;

    @FXML
    private IntegerProperty maxSize;
    private ObservableList<Integer> items;
    private FilteredList<Integer> filteredItems;
    @FXML
    private TableColumn<Integer, Number> numberColumn;
    @FXML
    private ComboBox<Number> pageSizeCombo;
    @FXML
    private TextField textField;

    public PaginatedTableView() {
        CommonsFX.loadRoot("PaginatedTableView.fxml", this);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        numberColumn.setCellValueFactory(s -> new SimpleIntegerProperty(s.getValue()));
        pagination.currentPageIndexProperty().addListener((ob, old, val) -> updateItems());
        pageSize.addListener((ob, old, val) -> updateItems());
        CommonsFX.bindBidirectional(pageSizeCombo.valueProperty(), pageSize);
        maxSize.addListener((ob, o, n) -> {
            updateItems();
            updatePageSizeCombo(n.intValue());
        });
        pagination.setPageCount(0);
        pagination.pageCountProperty().addListener((ob, old, val) -> updateItems());
        updateItems();
        textField.textProperty().addListener(
                (ob, o, n) -> RunnableEx.runIf(filteredItems, i -> i.setPredicate(e -> containsString(n, e))));
        SimpleTableViewBuilder.of(table).copiable().savable()
                .onKey(KeyCode.DELETE, i -> items.removeAll(table.getSelectionModel().getSelectedItems()))
                .onSortClicked((colName, ascending) -> RunnableEx.runIf(items,
                        i -> table.getColumns().stream().filter(c -> c.getText().equals(colName)).findFirst()
                                .ifPresent(col -> items.sort(QuickSortML.getComparator(col, ascending)))));

    }

    public <T> void addColumn(String name, FunctionEx<Integer, T> func) {
        SimpleTableViewBuilder.addClosableColumn(table, name, func);
        updateItems();
    }

    public <T> void addColumn(StringProperty name, FunctionEx<Integer, T> func) {
        SimpleTableViewBuilder.addClosableColumn(table, name.get(), func).textProperty().bind(name);
        updateItems();
    }


    public void clearColumns() {
        TableColumn<Integer, ?> tableColumn = table.getColumns().get(0);
        table.getColumns().clear();
        table.getColumns().add(tableColumn);
    }

    public List<String> getColumns() {
        return table.getColumns().stream().skip(1L).map(TableColumn::getText).collect(Collectors.toList());

    }

    public List<List<Object>> getElements() {
        return items.stream().map(i -> table.getColumns().stream().skip(1L).map(c -> (Object) c.getCellData(i))
                .collect(Collectors.toList())).collect(Collectors.toList());

    }

    public List<Integer> getFilteredItems() {
        return filteredItems;
    }

    public TableView<Integer> getTable() {
        return table;
    }

    public void onDoubleClick(ConsumerEx<Integer> object) {
        SimpleTableViewBuilder.of(table).onDoubleClick(object);
    }

    public void onKey(KeyCode code, ConsumerEx<List<Integer>> e) {
        SimpleTableViewBuilder.of(table).onKey(code, e);
    }

    public void setColumnsWidth(double... array) {
        prefWidthColumns(table, concat(of(3.), of(array)).toArray());
    }

    public <T>void setList(ObservableList<T> barList) {
        barList.addListener((ListChangeListener<T>) c -> {
            c.next();
            maxSize.set(c.getList().size());
        });
    }

    public void setListSize(int maxSize) {
        this.maxSize.set(maxSize);
    }

    private boolean containsString(String n, Integer e) {
        return StringUtils.isBlank(n)
                || table.getColumns().stream().map(c -> StringSigaUtils.toStringSpecial(c.getCellData(e))).anyMatch(
                        str -> StringUtils.containsIgnoreCase(str, n) || PredicateEx.test(s -> s.matches(n), str));
    }

    private int getPageCount() {
        int size = Math.max(pageSize.get(), 10);
        return maxSize.get() / size + (int) Math.signum(maxSize.get() % size);
    }

    private void updateItems() {
        int intValue = pagination.getCurrentPageIndex();
        int size = pageSize.get();
        items = IntStream.range(intValue * size, intValue * size + size).boxed().filter(i -> i < maxSize.get())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        filteredItems = items.filtered(e -> containsString(textField.getText(), e));
        table.setItems(filteredItems);
        pagination.setPageCount(getPageCount());
    }

    private void updatePageSizeCombo(int n) {
        ObservableList<Number> value = FXCollections.observableArrayList();
        double ceil = Math.ceil(Math.log10(n));
        Integer selectedItem = pageSizeCombo.getSelectionModel().getSelectedItem().intValue();
        for (int i = 1; i <= ceil; i++) {
            if (value.isEmpty() || n > last(value)) {
                value.add((int) Math.pow(10, i));
            }
            if (n > last(value)) {
                value.add((int) (Math.pow(10, i) * 2.5));
            }
            if (n > last(value)) {
                value.add((int) Math.pow(10, i) * 5);
            }
        }
        pageSizeCombo.setItems(value);
        pageSizeCombo.getSelectionModel().select(selectedItem);
    }

    private static int last(ObservableList<Number> value) {
        return value.get(value.size() - 1).intValue();
    }

}