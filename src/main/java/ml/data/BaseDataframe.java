package ml.data;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.PredicateEx;

public class BaseDataframe {

    protected int maxSize = Integer.MAX_VALUE;

    protected final Map<String, List<Object>> dataframe = new LinkedHashMap<>();
    protected Map<String, Set<String>> categories = new LinkedHashMap<>();
    protected final Map<String, Class<? extends Comparable<?>>> formatMap = new LinkedHashMap<>();
    protected Map<String, UnaryOperator<Object>> mapping = new LinkedHashMap<>();
    protected Map<String, Map.Entry<String[], FunctionEx<Object[], ?>>> crossFeature = new LinkedHashMap<>();
    protected int size;
    protected File file;

    protected Map<String, DataframeStatisticAccumulator> stats;

    protected Map<String, PredicateEx<Object>> filters = new LinkedHashMap<>();

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

    public <T extends Comparable<?>> void addCols(List<String> cols, Class<T> classes) {
        for (String string : cols) {
            dataframe.put(string, new ArrayList<>());
            formatMap.put(string, classes);
        }
    }

    public <T extends Comparable<?>> void addCols(String string, Class<T> classes) {
        dataframe.put(string, new ArrayList<>());
        formatMap.put(string, classes);
    }

    public List<String> cols() {
        return dataframe.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseDataframe other = (BaseDataframe) obj;
        return Objects.equals(file, other.file);
    }

    public void forEach(BiConsumer<String, List<Object>> action) {
        dataframe.forEach(action);
    }

    public void forEachRow(Consumer<Map<String, Object>> foreach) {
        for (int i = 0; i < size; i++) {
            foreach.accept(rowMap(i));
        }
    }

    public Object getAt(String header, int i) {
        List<Object> list = dataframe.get(header);
        if (list == null) {
            HasLogging.log(1).error("ERROR header \"{}\" does not exist in {}", header, file.getName());
            return null;
        }
        return list.get(i);
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

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    public boolean isLoaded() {
        int orElse = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return orElse != 0;
    }

    public Class<? extends Comparable<?>> putFormat(String header, Class<? extends Comparable<?>> value) {
        return formatMap.put(header, value);
    }

    public void removeCol(String... cols) {
        for (String string : cols) {
            dataframe.remove(string);
            formatMap.remove(string);
        }
    }

    public Map<String, Object> rowMap(int i) {
        return DataframeStatisticAccumulator.rowMap(dataframe, i);
    }
}