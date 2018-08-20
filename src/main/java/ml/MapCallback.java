package ml;

import java.util.Map.Entry;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.util.Callback;

public final class MapCallback<T, E> implements Callback<Entry<T, E>, ObservableValue<Boolean>> {
    private final ObservableMap<T, E> mapValues;
    private Runnable[] run;

    public MapCallback(ObservableMap<T, E> colors, Runnable... run) {
        this.mapValues = colors;
        this.run = run;
    }

    @Override
    public ObservableValue<Boolean> call(Entry<T, E> c) {
        SimpleBooleanProperty simpleBooleanProperty = new SimpleBooleanProperty(mapValues.containsKey(c.getKey()));
        simpleBooleanProperty.addListener((obs, old, newVal) -> {
            if (newVal) {
                mapValues.put(c.getKey(), c.getValue());
            } else {
                mapValues.remove(c.getKey());
            }
            for (int i = 0; i < run.length; i++) {
                run[i].run();
            }
        });
        return simpleBooleanProperty;
    }
}