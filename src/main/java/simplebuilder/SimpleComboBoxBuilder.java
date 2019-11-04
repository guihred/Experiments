package simplebuilder;

import static simplebuilder.SimpleListViewBuilder.newCellFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import javafx.util.StringConverter;
import utils.FunctionEx;

public class SimpleComboBoxBuilder<T> extends SimpleRegionBuilder<ComboBox<T>, SimpleComboBoxBuilder<T>> {

    protected ComboBox<T> comboBox;

    public SimpleComboBoxBuilder() {
        super(new ComboBox<>());
        comboBox = region;

    }

    public SimpleComboBoxBuilder<T> items(Collection<T> value) {
        if (value instanceof ObservableList) {
            comboBox.setItems((ObservableList<T>) value);
        } else {
            comboBox.setItems(FXCollections.observableArrayList(value));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimpleComboBoxBuilder<T> items(T... value) {
        return items(Arrays.asList(value));
    }

    public SimpleComboBoxBuilder<T> nullOption(String option) {
        StringConverter<T> converter = comboBox.getConverter();

        StringConverter<T> stringConverter = new StringConverter<T>() {

            @Override
            public T fromString(String string) {
                if (string.equals(option)) {
                    return null;
                }
                return converter.fromString(string);
            }

            @Override
            public String toString(T object) {
                return FunctionEx.mapIf(object, converter::toString, option);
            }

        };
        comboBox.setConverter(stringConverter);
        return this;
    }

    public SimpleComboBoxBuilder<T> onChange(BiConsumer<T, T> obj) {
        comboBox.getSelectionModel().selectedItemProperty()
            .addListener((ob, old, newValue) -> obj.accept(old, newValue));
        return this;
    }

    public SimpleComboBoxBuilder<T> onSelect(Consumer<T> obj) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((ob, old, newValue) -> obj.accept(newValue));
        return this;
    }

    public SimpleComboBoxBuilder<T> select(int index) {
        comboBox.getSelectionModel().select(index);
        return this;
    }

    public SimpleComboBoxBuilder<T> select(T obj) {
        comboBox.getSelectionModel().select(obj);
        return this;
    }

    public SimpleComboBoxBuilder<T> tooltip(String text) {
        comboBox.setTooltip(new Tooltip(text));
        return this;
    }

    public static <F> Callback<ListView<F>, ListCell<F>> cellStyle(ComboBox<F> comboBox, Function<F, String> func) {
        return newCellFactory((item, cell) -> {
            cell.setText(comboBox.getConverter().toString(item));
            cell.setStyle(func.apply(item));
        });
    }

}