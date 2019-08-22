package schema.sngpc;

import java.util.HashMap;

public final class SimpleMap extends HashMap<String, String> {
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