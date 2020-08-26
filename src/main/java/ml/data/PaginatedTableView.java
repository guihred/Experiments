package ml.data;

import static java.util.stream.DoubleStream.concat;
import static java.util.stream.DoubleStream.of;
import static simplebuilder.SimpleTableViewBuilder.prefWidthColumns;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.FunctionEx;
import utils.RunnableEx;

public final class PaginatedTableView extends VBox {
    private final IntegerProperty pageSize = new SimpleIntegerProperty(20);
    private TableView<Integer> table = new TableView<>();
    private Pagination pagination = new Pagination(pageSize.get(), 0);
    private IntegerProperty maxSize = new SimpleIntegerProperty(0);
    private FilteredList<Integer> filteredItems;
    private ObservableList<Integer> items;
    private TextField textField = new TextField();

    public PaginatedTableView() {
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pagination.currentPageIndexProperty().addListener(ob -> updateItems());
        pageSize.addListener(ob -> updateItems());
        maxSize.addListener(ob -> updateItems());
        pagination.setPageCount(0);
        TableColumn<Integer, Number> e2 = new TableColumn<>("Nº");
        e2.setCellValueFactory(m -> new SimpleObjectProperty<>(m.getValue()));
        table.getColumns().add(e2);
        pagination.pageCountProperty().bind(maxSize.divide(pageSize).add(1));
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setOnKeyReleased(e -> SimpleTableViewBuilder.copyContent(table, e));
        updateItems();
        getChildren().add(table);
        textField.textProperty().addListener((ob, o, n) -> RunnableEx.runIf(filteredItems,
                i -> i.setPredicate(e -> containsString(n, e))));
        SimpleTableViewBuilder.of(table)
                .onSortClicked(e -> RunnableEx.runIf(items,
                        i -> table.getColumns().stream().filter(c -> c.getText().equals(e.getKey())).findFirst()
                                .ifPresent(col -> items.sort(getComparator(col, e)))));

        getChildren().add(new HBox(new SimpleComboBoxBuilder<Integer>().items(10, 20, 50, 100)
                .onChange((old, val) -> pageSize.set(val)).select((Integer) 10).build(), pagination, textField));
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

    public void setColumnsWidth(double... array) {
        prefWidthColumns(table, concat(of(3.), of(array)).toArray());
    }

    public void setListSize(int maxSize) {
        this.maxSize.set(maxSize);
    }

    private boolean containsString(String n, Integer e) {
        return StringUtils.isBlank(n) || table.getColumns().stream()
                .anyMatch(c -> StringUtils.containsIgnoreCase(Objects.toString(c.getCellData(e)), n));
    }

    private void updateItems() {
        int intValue = pagination.getCurrentPageIndex();
        int size = pageSize.get();
        items = IntStream.range(intValue * size, intValue * size + size).boxed().filter(i -> i < maxSize.get())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        filteredItems = items.filtered(e -> containsString(textField.getText(), e));
        table.setItems(filteredItems);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Comparator<Integer> getComparator(TableColumn<Integer, ?> col, Entry<String, SortType> e) {
        Comparator<Integer> comparing = Comparator.comparing(m -> {
            Object cellData = col.getCellData(m);
            return (Comparable) (cellData instanceof Comparable ? cellData : Objects.toString(cellData));
        });
        return e.getValue() == SortType.ASCENDING ? comparing : comparing.reversed();
    }

}