package ml.data;

import static java.util.stream.DoubleStream.concat;
import static java.util.stream.DoubleStream.of;
import static simplebuilder.SimpleTableViewBuilder.prefWidthColumns;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import simplebuilder.SimpleComboBoxBuilder;
import utils.FunctionEx;

public final class PaginatedTableView extends VBox {
    private final IntegerProperty pageSize = new SimpleIntegerProperty(20);
    private TableView<Integer> table = new TableView<>();
    private Pagination pagination = new Pagination(pageSize.get(), 0);
    private IntegerProperty maxSize = new SimpleIntegerProperty(0);

    public PaginatedTableView() {
        pagination.currentPageIndexProperty().addListener(ob -> updateItems());
        pageSize.addListener(ob -> updateItems());
        maxSize.addListener(ob -> updateItems());
        pagination.setPageCount(0);
        TableColumn<Integer, Number> e2 = new TableColumn<>("Nº");
        e2.setCellValueFactory(m -> new SimpleObjectProperty<>(m.getValue()));
        table.getColumns().add(e2);
        pagination.pageCountProperty().bind(maxSize.divide(pageSize).add(1));
        VBox.setVgrow(table, Priority.ALWAYS);
        updateItems();
        getChildren().add(table);
        getChildren().add(new HBox(new SimpleComboBoxBuilder<Integer>().items(10, 20, 50, 100)
                .onChange((old, val) -> pageSize.set(val)).select((Integer) 10).build(), pagination));
    }

    public <T> void addColumn(String name, FunctionEx<Integer, T> func) {
        TableColumn<Integer, T> e2 = new TableColumn<>(name);
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

    private void updateItems() {
        int intValue = pagination.getCurrentPageIndex();
        int size = pageSize.get();
        table.setItems(IntStream.range(intValue * size, intValue * size + size).boxed().filter(i -> i < maxSize.get())
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

}