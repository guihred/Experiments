package ml.data;

import static java.util.stream.DoubleStream.concat;
import static java.util.stream.DoubleStream.of;
import static simplebuilder.SimpleTableViewBuilder.prefWidthColumns;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
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
    private ComboBox<Integer> pageSizeCombo;
    @FXML
    private TextField textField;

    public PaginatedTableView() {
        CommonsFX.loadRoot("PaginatedTableView.fxml", this);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pagination.currentPageIndexProperty().addListener(ob -> updateItems());
        pageSize.addListener(ob -> updateItems());
        pageSizeCombo.valueProperty().bindBidirectional(pageSize.asObject());
        maxSize.addListener((ob, o, n) -> {
            updateItems();
            updatePageSizeCombo(n);
        });
        pagination.setPageCount(0);
        pagination.pageCountProperty().bind(
                Bindings.createIntegerBinding(this::getPageCount, maxSize, pageSize, pageSizeCombo.itemsProperty()));
        updateItems();
        textField.textProperty().addListener(
                (ob, o, n) -> RunnableEx.runIf(filteredItems, i -> i.setPredicate(e -> containsString(n, e))));
        SimpleTableViewBuilder.of(table).copiable().savable()
                .onSortClicked(e -> RunnableEx.runIf(items,
                        i -> table.getColumns().stream().filter(c -> c.getText().equals(e.getKey())).findFirst()
                                .ifPresent(col -> items.sort(getComparator(col, e)))));
    }

    public <T> void addColumn(String name, FunctionEx<Integer, T> func) {
        TableColumn<Integer, T> e2 = new TableColumn<>(name);
        Hyperlink value = new Hyperlink("X");
        value.setOnAction(e -> table.getColumns().remove(e2));
        value.setStyle("-fx-text-fill: red;");
        e2.setGraphic(value);
        e2.setCellValueFactory(m -> new SimpleObjectProperty<>(FunctionEx.apply(func, m.getValue())));
        table.getColumns().add(e2);
        updateItems();
    }

    public void clearColumns() {
        TableColumn<Integer, ?> tableColumn = table.getColumns().get(0);
        table.getColumns().clear();
        table.getColumns().add(tableColumn);
    }

    public List<String> getColumns() {
        return table.getColumns().stream().skip(1L).map(TableColumn<Integer, ?>::getText).collect(Collectors.toList());

    }

    public List<List<Object>> getElements() {
        return items.stream().map(i -> table.getColumns().stream().skip(1L).map(c -> (Object) c.getCellData(i))
                .collect(Collectors.toList())).collect(Collectors.toList());

    }

    public List<Integer> getFilteredItems() {
        return filteredItems;
    }

    public void setColumnsWidth(double... array) {
        prefWidthColumns(table, concat(of(3.), of(array)).toArray());
    }

    public void setListSize(int maxSize) {
        this.maxSize.set(maxSize);
    }

    private boolean containsString(String n, Integer e) {
        return StringUtils.isBlank(n) || table.getColumns().stream().map(c -> Objects.toString(c.getCellData(e), ""))
                .anyMatch(str -> StringUtils.containsIgnoreCase(str, n) || PredicateEx.test(s -> s.matches(n), str));
    }

    private int getPageCount() {
        return maxSize.get() / pageSize.get() + (int) Math.signum(maxSize.get() % pageSize.get());
    }

    private void updateItems() {
        int intValue = pagination.getCurrentPageIndex();
        int size = pageSize.get();
        items = IntStream.range(intValue * size, intValue * size + size).boxed().filter(i -> i < maxSize.get())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        filteredItems = items.filtered(e -> containsString(textField.getText(), e));
        table.setItems(filteredItems);
    }

    private void updatePageSizeCombo(Number n) {
        ObservableList<Integer> value = FXCollections.observableArrayList();
        double doubleValue = n.doubleValue();
        double ceil = Math.ceil(Math.log10(doubleValue));
        Integer selectedItem = pageSizeCombo.getSelectionModel().getSelectedItem();
        for (int i = 1; i <= ceil; i++) {
            if (value.isEmpty() || doubleValue > value.get(value.size() - 1)) {
                value.add((int) Math.pow(10, i));
            }
            if (doubleValue > value.get(value.size() - 1)) {
                value.add((int) (Math.pow(10, i) * 2.5));
            }
            if (doubleValue > value.get(value.size() - 1)) {
                value.add((int) Math.pow(10, i) * 5);
            }
        }
        pageSizeCombo.setItems(value);
        pageSizeCombo.getSelectionModel().select(selectedItem);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Comparator<Integer> getComparator(TableColumn<Integer, ?> col, Entry<String, Boolean> e) {
        Comparator<Integer> comparing =
                Comparator.comparing(m -> (Comparable) (col.getCellData(m) instanceof Comparable ? col.getCellData(m)
                        : Objects.toString(col.getCellData(m))));
        return e.getValue() ? comparing : comparing.reversed();
    }

}