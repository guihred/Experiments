package ml.data;

import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataframeML extends BaseDataframe {
    public DataframeML() {
    }

    public DataframeML(DataframeML frame) {
        super(frame);
    }

    public void add(Map<String, Object> row) {
        row.forEach(this::add);
    }

    @SuppressWarnings("unchecked")
    public void add(String header, Object obj) {
        List<Object> list = list(header);
        if (list == null) {
            dataframe.put(header, new ArrayList<>());
            formatMap.put(header, (Class<? extends Comparable<?>>) obj.getClass());
            list = list(header);
        }
        list.add(obj);
        size = Math.max(size, list.size());
    }

    public void addAll(Object... obj) {
        Collection<List<Object>> values = dataframe.values();
        int i = 0;
        for (Iterator<List<Object>> iterator = values.iterator(); iterator.hasNext();) {
            List<Object> list = iterator.next();
            list.add(obj[i]);
            i++;
        }
        size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
    }

    public void apply(String header, DoubleUnaryOperator mapper) {
        dataframe.put(header, dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(mapper).boxed().collect(Collectors.toList()));
        formatMap.put(header, Double.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> categorize(String header) {
        if (categories.containsKey(header)) {
            return categories.get(header);
        }
        Set hashSet = new HashSet<>(dataframe.get(header));
        Set<String> checkedSet = hashSet;
        categories.put(header, checkedSet);
        return checkedSet;
    }

    public DataframeML filter(String header, Predicate<Object> v) {
        List<Object> list = dataframe.get(header);
        int off = 0;
        for (int i = 0; i - off < list.size(); i++) {
            int j = i - off;
            Object t = list.get(j);
            if (t == null || !v.test(t)) {
                dataframe.forEach((c, l) -> l.remove(j));
                off++;
            }
        }
        size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return this;
    }

    public DataframeML filterString(String header, Predicate<String> v) {
        List<Object> list = dataframe.get(header);
        if (list != null) {
            int off = 0;
            for (int i = 0; i - off < list.size(); i++) {
                int j = i - off;
                if (!v.test(Objects.toString(list.get(j)))) {
                    dataframe.forEach((c, l) -> l.remove(j));
                    off++;
                }
            }
        }
        size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return this;
    }



    public Set<Object> freeCategory(String header) {
        return new HashSet<>(dataframe.get(header));
    }

    public Map<String, Long> histogram(String header) {
        List<Object> list = dataframe.get(header);
        List<String> stringList =
                list.stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.toList());
        return stringList.parallelStream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> list(String header) {
        List<T> list = (List<T>) dataframe.get(header);
        if (list != null || header == null) {
            return list;
        }
        return (List<T>) dataframe.keySet().stream().filter(Objects::nonNull)
                .filter(e -> Stream.of(header.split(" ")).allMatch(e::contains)).findFirst().map(dataframe::get)
                .orElse(null);
    }

    public void map(String header, UnaryOperator<Object> mapper) {
        dataframe.put(header, dataframe.get(header).stream().map(mapper).collect(Collectors.toList()));
    }

    public void only(String header, Predicate<String> v, IntConsumer cons) {
        List<Object> list = list(header);
        for (int i = 0; i < list.size(); i++) {
            if (v.test(Objects.toString(list.get(i)))) {
                cons.accept(i);
            }
        }
    }


    public DoubleSummaryStatistics summary(String header) {
        if (!dataframe.containsKey(header)) {
            return new DoubleSummaryStatistics();
        }
        return list(header).stream().filter(Objects::nonNull).map(Number.class::cast).mapToDouble(Number::doubleValue)
                .summaryStatistics();
    }
}
