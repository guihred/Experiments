package simplebuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class SimpleComboBoxBuilder<T> extends SimpleRegionBuilder<ComboBox<T>, SimpleComboBoxBuilder<T>> {

    protected ComboBox<T> comboBox;

	public SimpleComboBoxBuilder() {
        super(new ComboBox<>());
        comboBox = region;

	}

    public SimpleComboBoxBuilder<T> converter(StringConverter<T> value) {
        comboBox.setConverter(value);

        return this;
    }

    @SuppressWarnings("unchecked")
    public SimpleComboBoxBuilder<T> items(T... value) {
        return items(Arrays.asList(value));
    }

    public SimpleComboBoxBuilder<T> items(Collection<T> value) {
        if (value instanceof ObservableList) {
            comboBox.setItems((ObservableList<T>) value);
        } else {
            comboBox.setItems(FXCollections.observableArrayList(value));
        }
        return this;
    }

    public SimpleComboBoxBuilder<T> nullOption(String option) {
        StringConverter<T> converter = comboBox.getConverter();

        StringConverter<T> stringConverter = new StringConverter<T>() {

            @Override
            public String toString(T object) {
                if (object == null) {
                    return option;
                }

                return converter.toString(object);
            }

            @Override
            public T fromString(String string) {
                if (string.equals(option)) {
                    return null;
                }
                return converter.fromString(string);
            }

        };
        comboBox.setConverter(stringConverter);
        return this;
    }

    public SimpleComboBoxBuilder<T> select(T obj) {
        comboBox.getSelectionModel().select(obj);
        return this;
    }

    public SimpleComboBoxBuilder<T> select(int index) {
        comboBox.getSelectionModel().select(index);
        return this;
    }

    public SimpleComboBoxBuilder<T> onSelect(Consumer<T> obj) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((ob, old, newValue) -> {
            obj.accept(newValue);
        });
        return this;
    }

    public SimpleComboBoxBuilder<T> onChange(BiConsumer<T, T> obj) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((ob, old, newValue) -> {
            obj.accept(old, newValue);
        });
        return this;
    }



}