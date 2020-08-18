package ml.data;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BaseDataframe {

    protected int maxSize = Integer.MAX_VALUE;

    protected final Map<String, List<Object>> dataframe = new LinkedHashMap<>();
    protected Map<String, Set<String>> categories = new LinkedHashMap<>();
    protected final Map<String, Class<? extends Comparable<?>>> formatMap = new LinkedHashMap<>();
    protected Map<String, Function<Object, Object>> mapping = new LinkedHashMap<>();
    protected int size;
    protected File file;
    protected Map<String, DataframeStatisticAccumulator> stats;
    protected Map<String, Predicate<Object>> filters = new LinkedHashMap<>();

    public BaseDataframe() {
    }

    public BaseDataframe(BaseDataframe frame) {
        file = frame.file;
        frame.dataframe.forEach((h, l) -> dataframe.put(h, new ArrayList<>(l)));
        formatMap.putAll(frame.formatMap);
        mapping = new LinkedHashMap<>(frame.mapping);
        size = frame.size;
        stats = new LinkedHashMap<>();
        if (frame.stats != null) {
            stats.putAll(frame.stats);
        }
        filters = new LinkedHashMap<>(frame.filters);
    }

    public List<String> cols() {
        return dataframe.keySet().stream().collect(Collectors.toList());
    }

    public Map<String, List<Object>> getDataframe() {
        return dataframe;
    }

    public File getFile() {
        return file;
    }

    public Class<? extends Comparable<?>> getFormat(String header) {
        return formatMap.get(header);
    }

    public Map<String, Class<? extends Comparable<?>>> getFormatMap() {
        return formatMap;
    }

    public int getSize() {
        return size;
    }

    public Map<String, DataframeStatisticAccumulator> getStats() {
        return stats;
    }

    public boolean isLoaded() {
        int orElse = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return orElse == size;
    }

    public Class<? extends Comparable<?>> putFormat(String header, Class<? extends Comparable<?>> value) {
        return formatMap.put(header, value);
    }
}