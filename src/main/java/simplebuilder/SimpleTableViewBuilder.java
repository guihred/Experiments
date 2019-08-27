package simplebuilder;

import static utils.FunctionEx.makeFunction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import utils.FunctionEx;

public class SimpleTableViewBuilder<T> extends SimpleRegionBuilder<TableView<T>, SimpleTableViewBuilder<T>> {

    private static final int COLUMN_DEFAULT_WIDTH = 150;
    private TableView<T> table;

    public SimpleTableViewBuilder() {
        super(new TableView<T>());
        table = node;
    }

    public <V extends TableCell<T, Object>> SimpleTableViewBuilder<T> addColumn(final String columnName,
        final BiConsumer<T, V> value) {
        final TableColumn<T, Object> column = new TableColumn<>(columnName);
        column.setCellFactory(newCellFactory(value));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public <S> SimpleTableViewBuilder<T> addColumn(final String columnName, FunctionEx<S, String> func,
        final String propertyName) {
        final TableColumn<T, S> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(c -> new TableCell<T, S>() {
            @Override
            protected void updateItem(final S item, final boolean empty) {
                super.updateItem(item, empty);
                setText(makeFunction(func).apply(item));
            }
        });

        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(String columnName, String property, boolean editable) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        column.setEditable(editable);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName,
        final Callback<TableColumn<T, String>, TableCell<T, String>> value) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(value);
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumns(final String... columnName) {
        for (String columnProp : columnName) {
            TableColumn<T, String> column = new TableColumn<>(columnProp);
            column.setCellValueFactory(new PropertyValueFactory<>(columnProp));
            column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
            table.getColumns().add(column);
        }
        return this;
    }

    public SimpleTableViewBuilder<T> equalColumns() {
        ObservableList<TableColumn<T, ?>> columns = table.getColumns();
        columns.forEach(c -> c.prefWidthProperty().bind(table.prefWidthProperty().divide(columns.size())));
        return this;
    }

    public SimpleTableViewBuilder<T> items(final ObservableList<T> value) {
        table.setItems(value);
        return this;
    }

    public SimpleTableViewBuilder<T> onDoubleClick(final Consumer<T> object) {
        node.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                T selectedItem = table.getSelectionModel().getSelectedItem();
                object.accept(selectedItem);
            }
        });
        return this;
    }

    public SimpleTableViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

    public SimpleTableViewBuilder<T> prefWidthColumns(double... prefs) {
        ObservableList<TableColumn<T, ?>> columns = table.getColumns();
        double sum = DoubleStream.of(prefs).sum();
        for (int i = 0; i < prefs.length; i++) {
            double pref = prefs[i];
            columns.get(i).prefWidthProperty().bind(table.widthProperty().multiply(pref / sum));
        }
        return this;

    }

    public SimpleTableViewBuilder<T> scrollTo(int value) {
        table.scrollTo(value);
        return this;
    }

    public SimpleTableViewBuilder<T> selectionMode(SelectionMode value) {
        table.getSelectionModel().setSelectionMode(value);
        return this;
    }

    public SimpleTableViewBuilder<T> sortable(boolean value) {
        table.getColumns().forEach(e -> e.setSortable(value));
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <C, V extends TableCell<C, Object>>
        Callback<TableColumn<C, Object>, TableCell<C, Object>> newCellFactory(final BiConsumer<C, V> value) {
        return p -> new CustomableTableCell<C, Object>() {
            @Override
            protected void setStyleable(final C auxMed) {
                value.accept(auxMed, (V) this);
            }

        };
    }

    public abstract static class CustomableTableCell<M, X> extends TableCell<M, X> {

        protected abstract void setStyleable(M auxMed);

        @Override
        protected void updateItem(final X item, final boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            int size = getTableView().getItems().size();
            if (index >= 0 && index < size) {
                M auxMed = getTableView().getItems().get(index);
                setStyleable(auxMed);
            }
        }
    }

}