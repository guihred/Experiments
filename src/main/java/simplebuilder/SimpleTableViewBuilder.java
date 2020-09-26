package simplebuilder;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import utils.CSVUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class SimpleTableViewBuilder<T> extends SimpleRegionBuilder<TableView<T>, SimpleTableViewBuilder<T>> {

    private static final int COLUMN_DEFAULT_WIDTH = 150;

    public SimpleTableViewBuilder() {
        super(new TableView<T>());
    }

    public <V> SimpleTableViewBuilder<T> addClosableColumn(String name, FunctionEx<T, V> func) {
        addClosableColumn(node, name, func);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName,
            final BiConsumer<T, TableCell<T, Object>> value) {
        final TableColumn<T, Object> column = new TableColumn<>(columnName);
        column.setCellFactory(newCellFactory(value));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        node.getColumns().add(column);
        return this;
    }

    public <V> SimpleTableViewBuilder<T> addColumn(final String columnName, final FunctionEx<T, V> value) {
        final TableColumn<T, V> column = new TableColumn<>(columnName);
        column.setCellValueFactory(m -> new SimpleObjectProperty<>(FunctionEx.apply(value, m.getValue())));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        node.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName) {
        final TableColumn<T, ?> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setId(propertyName);
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        node.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(String columnName, String property, boolean editable) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setId(property);
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        column.setEditable(editable);
        node.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumns(final String... columnName) {
        for (String columnProp : columnName) {
            TableColumn<T, String> column = new TableColumn<>(columnProp);
            column.setId(columnProp);
            column.setCellValueFactory(new PropertyValueFactory<>(columnProp));
            column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
            node.getColumns().add(column);
        }
        return this;
    }

    public SimpleTableViewBuilder<T> copiable() {
        onKeyReleased(e -> copyContent(node, e));
        return this;
    }

    public SimpleTableViewBuilder<T> equalColumns() {
        equalColumns(node);
        return this;
    }

    public SimpleTableViewBuilder<T> items(final ObservableList<T> value) {
        node.setItems(value);
        return this;
    }

    public SimpleTableViewBuilder<T> multipleSelection() {
        node.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return this;
    }

    public SimpleTableViewBuilder<T> onDoubleClick(final ConsumerEx<T> object) {
        SimpleTableViewBuilder.onDoubleClick(node, object);
        return this;
    }

    public SimpleTableViewBuilder<T> onKey(KeyCode code, ConsumerEx<List<T>> object) {
        SimpleNodeBuilder.onKeyReleased(node, e -> {
            if (code == e.getCode()) {
                List<T> selectedItem = node.getSelectionModel().getSelectedItems();
                ConsumerEx.accept(object, selectedItem);
            }
        });

        return this;

    }

    public SimpleTableViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        node.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

    public SimpleTableViewBuilder<T> onSortClicked(ConsumerEx<Entry<String, Boolean>> c) {
        node.setSortPolicy((TableView<T> o) -> {
            ObservableList<TableColumn<T, ?>> sortOrder = o.getSortOrder();
            if (!sortOrder.isEmpty()) {
                TableColumn<T, ?> tableColumn = sortOrder.get(0);
                SortType sortType = tableColumn.getSortType();
                ConsumerEx.accept(c,
                        new AbstractMap.SimpleEntry<>(tableColumn.getText(), sortType == SortType.ASCENDING));
            }
            return true;
        });
        return this;
    }

    public SimpleTableViewBuilder<T> prefWidthColumns(double... prefs) {
        prefWidthColumns(node, prefs);
        return this;
    }

    public SimpleTableViewBuilder<T> savable() {
        onKeyReleased(e -> saveContent(node, e));
        return this;
    }

    public SimpleTableViewBuilder<T> scrollTo(int value) {
        node.scrollTo(value);
        return this;
    }

    public SimpleTableViewBuilder<T> sortable(boolean value) {
        node.getColumns().forEach(e -> e.setSortable(value));
        return this;
    }

    public static <T,V> void addClosableColumn(TableView<T> node,String name, FunctionEx<T, V> func) {
        TableColumn<T, V> e2 = new TableColumn<>(name);
        Hyperlink value = new Hyperlink("X");
        value.setOnAction(e -> node.getColumns().remove(e2));
        value.setStyle("-fx-text-fill: red;");
        e2.setGraphic(value);
        e2.setCellValueFactory(m -> new SimpleObjectProperty<>(FunctionEx.apply(func, m.getValue())));
        node.getColumns().add(e2);
    }

    public static <T> void addColumns(final TableView<Map<String, T>> simpleTableViewBuilder,
        final Collection<String> keySet) {
        simpleTableViewBuilder.getColumns().clear();
        keySet.forEach(key -> {
            TableColumn<Map<String, T>, String> column = new TableColumn<>(key);
            column.setSortable(true);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(simpleTableViewBuilder.widthProperty().divide(keySet.size()).add(-5));
            simpleTableViewBuilder.getColumns().add(column);
        });
    }

    public static <T> void copyContent(TableView<T> table, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            ObservableList<Integer> selectedItems = table.getSelectionModel().getSelectedIndices();
            String collect =
                    selectedItems
                            .stream().map(l -> table.getColumns().stream()
                                    .map(e -> Objects.toString(e.getCellData(l), "")).collect(Collectors.joining("\t")))
                            .collect(Collectors.joining("\n"));
            Map<DataFormat, Object> content = FXCollections.observableHashMap();
            content.put(DataFormat.PLAIN_TEXT, collect);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    public static <S> void equalColumns(TableView<S> table) {
        ObservableList<TableColumn<S, ?>> columns = table.getColumns();
        prefWidthColumns(table, columns.stream().mapToDouble(e -> 1).toArray());
    }

    public static <C, M> Callback<TableColumn<C, M>, TableCell<C, M>>
            newCellFactory(final BiConsumer<C, TableCell<C, M>> value) {
        return p -> new CustomableTableCell<C, M>() {
            @Override
            protected void setStyleable(final C auxMed) {
                value.accept(auxMed, this);
            }

        };
    }

    public static <V> SimpleTableViewBuilder<V> of(TableView<V> table) {
        SimpleTableViewBuilder<V> simpleTableViewBuilder = new SimpleTableViewBuilder<>();
        simpleTableViewBuilder.node = table;
        return simpleTableViewBuilder;
    }

    public static <T> void onDoubleClick(TableView<T> table2, ConsumerEx<T> object) {
        table2.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                T selectedItem = table2.getSelectionModel().getSelectedItem();
                ConsumerEx.accept(object, selectedItem);
            }
        });
        EventHandler<? super KeyEvent> onKeyReleased = table2.getOnKeyReleased();
        table2.setOnKeyReleased(e -> {
            RunnableEx.runIf(onKeyReleased, onKey -> onKey.handle(e));
            if (e.getCode() == KeyCode.ENTER) {
                T selectedItem = table2.getSelectionModel().getSelectedItem();
                ConsumerEx.accept(object, selectedItem);
            }
        });
    }

    public static <T> void onSelect(TableView<T> table, final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
    }

    public static <S> void prefWidthColumns(TableView<S> table1, double... prefs) {
        ObservableList<TableColumn<S, ?>> columns = table1.getColumns();
        double sum = DoubleStream.of(prefs).sum();
        for (int i = 0; i < prefs.length; i++) {
            double pref = prefs[i];
            columns.get(i).prefWidthProperty().bind(table1.widthProperty().multiply(pref / sum));
        }
    }

    public static <T> void saveContent(TableView<T> table, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.S) {
            new FileChooserBuilder().initialFilename(Objects.toString(table.getId(), "table") + ".csv")
                    .extensions("CSV", "*.csv").onSelect(f -> CSVUtils.saveToFile(table, f)).saveFileAction(ev);

        }
    }

    public static <S, T> Callback<TableColumn<T, S>, TableCell<T, S>> setFormat(FunctionEx<S, String> func) {
        return c -> new TableCell<T, S>() {
            @Override
            protected void updateItem(final S item, final boolean empty) {
                super.updateItem(item, empty);
                setText(FunctionEx.apply(func, item));
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
                return;
            }
            if (getStyleClass().size() > 4) {
                getStyleClass().remove(4, getStyleClass().size());
            }
            setText(null);
            setGraphic(null);
        }
    }

}