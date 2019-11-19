package ml;

import java.util.Map.Entry;
import java.util.Objects;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public final class ColorConverter extends StringConverter<Entry<String, Color>> {
    private ObservableMap<String, Color> colors;

    public ColorConverter(ObservableMap<String, Color> color) {
        colors = color;
    }

    @Override
    public Entry<String, Color> fromString(String string) {
        return colors.entrySet().stream().filter(v -> Objects.equals(v.getKey(), string)).findFirst().orElse(null);
    }

    @Override
    public String toString(Entry<String, Color> object) {
        return object.getKey();
    }
}