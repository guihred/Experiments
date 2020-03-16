package simplebuilder;

import java.util.function.BiConsumer;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import utils.ConsumerEx;

public class SimpleListViewBuilder<T> extends SimpleRegionBuilder<ListView<T>, SimpleListViewBuilder<T>> {

    private ListView<T> table;

    public SimpleListViewBuilder() {
        super(new ListView<T>());
        table = node;
    }

    public SimpleListViewBuilder<T> cellFactory(Callback<ListView<T>, ListCell<T>> value) {
        table.setCellFactory(value);
        return this;
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
        node.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                T selectedItem = table.getSelectionModel().getSelectedItem();
                ConsumerEx.makeConsumer(object).accept(selectedItem);
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

    public static <T> void onSelect(ListView<T> table,final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
    }

}