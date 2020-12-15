package utils;

import java.util.LinkedHashMap;

public final class SimpleMap extends LinkedHashMap<String, String> {
    private String value;

    public SimpleMap() {
        value = null;
    }

    public SimpleMap(String key, String value) {
        this.value = value;
        put(key, value);
    }
    @Override
    public String toString() {
        return value;
    }
}