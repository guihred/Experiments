package simplebuilder;

import static simplebuilder.SimpleListViewBuilder.newCellFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;

public class SimpleComboBoxBuilder<T> extends SimpleRegionBuilder<ComboBox<T>, SimpleComboBoxBuilder<T>> {

    public SimpleComboBoxBuilder() {
        super(new ComboBox<>());
    }

    public SimpleComboBoxBuilder(Collection<T> value) {
        super(new ComboBox<T>());
        items(value);
    }

    private SimpleComboBoxBuilder(ComboBox<T> combo) {
        super(combo);
    }

    public SimpleComboBoxBuilder<T> cellFactory(BiConsumer<T, ListCell<T>> value2) {
        region.setCellFactory(SimpleListViewBuilder.newCellFactory(value2));
        return this;
    }

    public SimpleComboBoxBuilder<T> converter(FunctionEx<T, String> func) {
        region.setConverter(new SimpleConverter<>(FunctionEx.makeFunction(func)));
        return this;
    }

    public final SimpleComboBoxBuilder<T> items(Collection<T> value) {
        if (value instanceof ObservableList) {
            region.setItems((ObservableList<T>) value);
        } else {
            region.setItems(FXCollections.observableArrayList(value));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimpleComboBoxBuilder<T> items(T... value) {
        return items(Arrays.asList(value));
    }

    public SimpleComboBoxBuilder<T> onChange(BiConsumer<T, T> obj) {
        region.getSelectionModel().selectedItemProperty().addListener((ob, old, newValue) -> obj.accept(old, newValue));
        return this;
    }

    public SimpleComboBoxBuilder<T> onSelect(ConsumerEx<T> obj) {
        region.getSelectionModel().selectedItemProperty()
                .addListener((ob, old, newValue) -> ConsumerEx.accept(obj, newValue));
        return this;
    }

    public SimpleComboBoxBuilder<T> select(int index) {
        region.getSelectionModel().select(index);
        return this;
    }

    public SimpleComboBoxBuilder<T> select(T obj) {
        region.getSelectionModel().select(obj);
        return this;
    }

    public SimpleComboBoxBuilder<T> selectedItem(Property<T> val) {
        region.getSelectionModel().select(val.getValue());
        val.bind(region.getSelectionModel().selectedItemProperty());
        return this;

    }

    public SimpleComboBoxBuilder<T> tooltip(String text) {
        region.setTooltip(new Tooltip(text));
        return this;
    }

    public static <F> Callback<ListView<F>, ListCell<F>> cellStyle(ComboBox<F> comboBox, Function<F, String> func) {
        return newCellFactory((item, cell) -> {
            cell.setText(comboBox.getConverter().toString(item));
            cell.setStyle(func.apply(item));
        });
    }

    public static <V> SimpleComboBoxBuilder<V> of(ComboBox<V> combo) {
        return new SimpleComboBoxBuilder<>(combo);

    }

}