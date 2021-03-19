package ml.data;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import utils.ex.HasLogging;
import utils.ex.PredicateEx;

public class DataframeML extends BaseDataframe {
    public DataframeML() {
    }

    public DataframeML(DataframeML frame) {
        super(frame);
    }

    public <T> void add(Map<String, T> row) {
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

    public DataframeML filter(String header, PredicateEx<Object> v) {
        List<Object> list = dataframe.get(header);
        int off = 0;
        for (int i = 0; i - off < list.size(); i++) {
            int j = i - off;
            Object t = list.get(j);
            if (!PredicateEx.test(v, t)) {
                dataframe.forEach((c, l) -> l.remove(j));
                off++;
            }
        }
        size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return this;
    }

    public DataframeML filterString(String header, PredicateEx<String> v) {
        List<Object> list = dataframe.get(header);
        if (list != null) {
            int off = 0;
            for (int i = 0; i - off < list.size(); i++) {
                int j = i - off;
                if (!PredicateEx.test(v, Objects.toString(list.get(j)))) {
                    dataframe.forEach((c, l) -> l.remove(j));
                    off++;
                }
            }
        }
        size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
        return this;
    }

    public List<Map<String, Object>> findAll(String header, Predicate<Object> v) {
        List<Object> list = dataframe.get(header);
        if (list != null) {
            return IntStream.range(0, list.size()).filter(i -> v.test(list.get(i)))
                    .mapToObj(this::rowMap).collect(Collectors.toList());
        }
        HasLogging.log(1).error("ERROR header \"{}\" does not exist in {}", header, file.getName());
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, T> findFirst(String header, Predicate<Object> v) {
        List<Object> list = dataframe.get(header);
        if (list != null) {
            return (Map<String, T>) IntStream.range(0, list.size()).filter(i -> v.test(list.get(i)))
                    .mapToObj(this::rowMap).findFirst().orElse(null);
        }
        HasLogging.log(1).error("ERROR header \"{}\" does not exist in {}", header, file.getName());
        return null;
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

    @SuppressWarnings("unchecked")
    public List<Object> map(String destination, String header, UnaryOperator<Object> mapper) {
        List<Object> collect = dataframe.get(header).stream().map(mapper).collect(Collectors.toList());
        dataframe.put(destination, collect);
        Class<? extends Object> orElse =
                collect.stream().filter(Objects::nonNull).findFirst().map(Object::getClass).orElse(null);
        putFormat(destination, (Class<? extends Comparable<?>>) orElse);
        return collect;
    }

    public List<Object> map(String header, UnaryOperator<Object> mapper) {
        List<Object> collect = dataframe.get(header).stream().map(mapper).collect(Collectors.toList());
        dataframe.put(header, collect);
        return collect;
    }

    public void only(String header, Predicate<String> v, IntConsumer cons) {
        List<Object> list = list(header);
        for (int i = 0; i < list.size(); i++) {
            if (v.test(Objects.toString(list.get(i)))) {
                cons.accept(i);
            }
        }
    }

    public void sortHeaders(List<String> headersOrder) {
        List<Entry<String, List<Object>>> frameHeader = dataframe.entrySet().stream().collect(Collectors.toList());
        dataframe.clear();
        frameHeader.sort(Comparator.comparing(e -> headersOrder.indexOf(e.getKey())));
        frameHeader.forEach(e -> dataframe.put(e.getKey(), e.getValue()));
    }

    public DoubleSummaryStatistics summary(String header) {
        if (!dataframe.containsKey(header)) {
            return new DoubleSummaryStatistics();
        }
        return list(header).stream().filter(Objects::nonNull).map(Number.class::cast).mapToDouble(Number::doubleValue)
                .summaryStatistics();
    }
}
