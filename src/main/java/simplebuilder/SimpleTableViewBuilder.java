package simplebuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import utils.CSVUtils;
import utils.ExcelService;
import utils.ImageFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;

public class SimpleTableViewBuilder<T> extends SimpleRegionBuilder<TableView<T>, SimpleTableViewBuilder<T>> {

    private static final int COLUMN_DEFAULT_WIDTH = 150;

    public SimpleTableViewBuilder() {
        super(new TableView<T>());
    }

    private SimpleTableViewBuilder(TableView<T> node) {
        super(node);
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

    public SimpleTableViewBuilder<T> onDoubleClickMany(final ConsumerEx<List<T>> object) {
        node.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                ConsumerEx.accept(object, node.getSelectionModel().getSelectedItems());
            }
        });
        onKey(KeyCode.ENTER, object);
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

    public SimpleTableViewBuilder<T> onSortClicked(BiConsumer<String, Boolean> c) {
        node.setSortPolicy((TableView<T> o) -> {
            List<TableColumn<T, ?>> sortOrder = o.getSortOrder();
            if (!sortOrder.isEmpty()) {
                TableColumn<T, ?> tableColumn = sortOrder.get(0);
                c.accept(tableColumn.getText(), tableColumn.getSortType() == SortType.ASCENDING);
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

    public static <T, V> TableColumn<T, V> addClosableColumn(TableView<T> node, String name, FunctionEx<T, V> func) {
        TableColumn<T, V> e2 = new TableColumn<>(name);
        addCloseButton(node, e2);
        e2.setCellValueFactory(m -> new SimpleObjectProperty<>(FunctionEx.apply(func, m.getValue())));
        node.getColumns().add(e2);
        return e2;
    }

    public static <T> void addColumns(TableView<Map<String, T>> table, Iterable<String> keySet) {
        table.getColumns().clear();
        keySet.forEach(key -> {
            TableColumn<Map<String, T>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            addCloseButton(table, column);
            table.getColumns().add(column);
        });
        autoColumnsWidth(table);
    }

    public static <T> void autoColumnsWidth(TableView<T> node) {
        double[] array = node.getColumns().stream().mapToDouble(e -> {
            String cellData = StringSigaUtils.toStringSpecial(e.getCellData(0)).split("\n")[0];
            return Math.max(e.getText().length(), cellData.length());
        }).toArray();
        prefWidthColumns(node, array);
    }

    public static <T> void copyContent(TableView<T> table, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            List<Integer> selectedItems = table.getSelectionModel().getSelectedIndices();
            String content = selectedItems.stream()
                    .map(l -> table.getColumns().stream().filter(c -> !"Nº".equals(c.getText()))
                            .map(e -> Objects.toString(e.getCellData(l), "")).collect(Collectors.joining("\t")))
                    .collect(Collectors.joining("\n"));
            ImageFXUtils.setClipboardContent(content);
        }
    }

    public static <S> void equalColumns(TableView<S> table) {
        List<TableColumn<S, ?>> columns = table.getColumns();
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
        return new SimpleTableViewBuilder<>(table);
    }

    public static <T> void onDoubleClick(TableView<T> table2, ConsumerEx<T> object) {
        table2.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                ConsumerEx.foreach(table2.getSelectionModel().getSelectedItems(), object);
            }
        });
        onKeyReleased(table2, KeyCode.ENTER,
                () -> ConsumerEx.foreach(table2.getSelectionModel().getSelectedItems(), object));
    }

    public static <T> void onSelect(TableView<T> table, final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
    }

    public static <S> void prefWidthColumns(TableView<S> table1, double... prefs) {
        List<TableColumn<S, ?>> columns = table1.getColumns();
        double sum = DoubleStream.of(prefs).sum();
        for (int i = 0; i < prefs.length; i++) {
            double pref = prefs[i];
            columns.get(i).prefWidthProperty().bind(table1.widthProperty().multiply(pref / sum));
        }
    }

    public static <T> void saveContent(TableView<T> table, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.S) {
            FileChooserBuilder fileChooserBuilder = new FileChooserBuilder();
            fileChooserBuilder.initialFilename(Objects.toString(table.getId(), "table") + ".csv")
                    .extensions("CSV", "*.csv").extensions("Excel", "*.xlsx")
                    .onSelect(f -> {
                        String extension = fileChooserBuilder.getExtension();
                        if ("CSV".equals(extension)) {
                            CSVUtils.saveToFile(table, f);
                        } else {
                            ExcelService.getExcel(table, f);
                        }
                    }).saveFileAction(ev);

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

    private static<T> void addCloseButton(TableView< T> table, TableColumn< T, ?> column) {
        Hyperlink value = new Hyperlink("X");
        value.setOnAction(e -> {
            table.getColumns().remove(column);
            autoColumnsWidth(table);
        });
        value.setStyle("-fx-text-fill: red;");
        column.setGraphic(value);
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