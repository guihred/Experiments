package ml;

import java.util.Objects;
import java.util.Map.Entry;

import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

final class ColorConverter extends StringConverter<Entry<String, Color>> {
    ObservableMap<String, Color> colors;

    ColorConverter(ObservableMap<String, Color> color) {
        this.colors = color;
    }

    @Override
    public String toString(Entry<String, Color> object) {
        return object.getKey();
    }

    @Override
    public Entry<String, Color> fromString(String string) {
        return colors.entrySet().stream().filter(v -> Objects.equals(v.getKey(), string)).findFirst()
                .orElse(null);
    }
}