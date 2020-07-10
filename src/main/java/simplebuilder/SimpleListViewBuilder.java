package simplebuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import utils.ConsumerEx;
import utils.FunctionEx;

public class SimpleListViewBuilder<T> extends SimpleRegionBuilder<ListView<T>, SimpleListViewBuilder<T>> {

    private ListView<T> table;
    private Map<KeyCode, ConsumerEx<T>> mapKey = new HashMap<>();

    public SimpleListViewBuilder() {
        super(new ListView<T>());
        table = node;
    }

    public SimpleListViewBuilder<T> cellFactory(Callback<ListView<T>, ListCell<T>> value) {
        table.setCellFactory(value);
        return this;
    }

    public SimpleListViewBuilder<T> cellFactory(FunctionEx<T, String> func) {
        return cellFactory(newCellFactory((t, cell) -> cell.setText(FunctionEx.apply(func, t))));
    }

    public SimpleListViewBuilder<T> fixedCellSize(double value) {
        table.setFixedCellSize(value);
        return this;
    }

    public SimpleListViewBuilder<T> items(final ObservableList<T> value) {
        table.setItems(value);
        return this;
    }

    public SimpleListViewBuilder<T> onDoubleClick(final ConsumerEx<T> object) {
        onDoubleClick(table, object);
        return this;
    }

    public SimpleListViewBuilder<T> onKey(KeyCode code, ConsumerEx<T> object) {
        mapKey.put(code, object);
        table.setOnKeyReleased(e -> {
            if (mapKey.containsKey(e.getCode())) {
                ConsumerEx.makeConsumer(mapKey.get(e.getCode())).accept(table.getSelectionModel().getSelectedItem());
            }
        });

        return this;

    }

    public SimpleListViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

    public static <C> Callback<ListView<C>, ListCell<C>> newCellFactory(final BiConsumer<C, ListCell<C>> value) {
        return p -> new ListCell<C>() {
            @Override
            protected void updateItem(final C item, final boolean empty) {
                super.updateItem(item, empty);
                value.accept(getItem(), this);
            }

        };
    }

    public static <C> Callback<ListView<C>, ListCell<C>> newCellFactory(final FunctionEx<C, String> func) {
        return newCellFactory((t, cell) -> cell.setText(FunctionEx.apply(func, t)));
    }

    public static <C> void onDoubleClick(ListView<C> table2, final ConsumerEx<C> object) {
        table2.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                C selectedItem = table2.getSelectionModel().getSelectedItem();
                ConsumerEx.makeConsumer(object).accept(selectedItem);
            }
        });
        if (table2.getOnKeyReleased() == null) {
            table2.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    C selectedItem = table2.getSelectionModel().getSelectedItem();
                    ConsumerEx.makeConsumer(object).accept(selectedItem);
                }
            });
        }
    }

    public static <T> void onSelect(ListView<T> table, final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
    }

}