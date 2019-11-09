package simplebuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class SimpleListViewBuilder<T> extends SimpleRegionBuilder<ListView<T>, SimpleListViewBuilder<T>> {

    private ListView<T> table;

    public SimpleListViewBuilder() {
        super(new ListView<T>());
        table = node;
    }


    public SimpleListViewBuilder<T> cellFactory(BiConsumer<T, ListCell<T>> value) {
        return cellFactory(newCellFactory(value));
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

    public SimpleListViewBuilder<T> onDoubleClick(final Consumer<T> object) {
        node.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                T selectedItem = table.getSelectionModel().getSelectedItem();
                object.accept(selectedItem);
            }
        });
        return this;
    }

    public SimpleListViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <C, V extends ListCell<C>> Callback<ListView<C>, ListCell<C>> newCellFactory(
        final BiConsumer<C, V> value) {
        return p -> new CustomableListCell<C>() {
            @Override
            protected void setVisual(C auxMed) {
                value.accept(getItem(), (V) this);
            }

        };
    }

    public abstract static class CustomableListCell<M> extends ListCell<M> {

        protected abstract void setVisual(M auxMed);

        @Override
        protected void updateItem(final M item, final boolean empty) {
            super.updateItem(item, empty);
            setVisual(getItem());
        }
    }

}