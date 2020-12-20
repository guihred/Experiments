package ml.data;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static utils.CommonsFX.update;
import static utils.StringSigaUtils.floatFormating;
import static utils.StringSigaUtils.format;
import static utils.StringSigaUtils.intFormating;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import org.slf4j.Logger;
import utils.CSVUtils;
import utils.QuickSortML;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class DataframeUtils extends DataframeML {

    private static final Logger LOG = HasLogging.log();

    protected DataframeUtils() {
    }

    public static List<String> addHeaders(DataframeML dataframeML, Scanner scanner) {
        List<String> header = getHeaders(scanner);
        dataframeML.stats = SupplierEx.nonNull(dataframeML.stats, new LinkedHashMap<>());
        dataframeML.formatMap.clear();
        for (String column : header) {
            dataframeML.getDataframe().put(column, new ArrayList<>());
            dataframeML.putFormat(column, String.class);
            dataframeML.stats.compute(column, (k, val) -> val != null ? val.reset() : accumulator(dataframeML, column));
        }
        dataframeML.size = 0;
        return header;
    }

    public static List<Entry<Object, Double>> createSeries(DataframeML dataframe, String feature, String target) {
        List<Object> list = dataframe.list(feature);
        List<Object> list2 = dataframe.list(target);

        Map<Object, Double> collect = IntStream.range(0, dataframe.getSize())
                .filter(i -> i < list.size() && i < list2.size())
                .filter(i -> list.get(i) != null && list2.get(i) != null).boxed()
                .collect(Collectors.groupingBy(list::get, LinkedHashMap::new,
                        Collectors.mapping(list2::get, Collectors.summingDouble(t -> ((Number) t).doubleValue()))));

        return collect.entrySet().stream().sorted(Comparator.comparing(Entry<Object, Double>::getValue))
                .collect(Collectors.toList());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<Double> crossDoubleFeature(DataframeML dataframe, String header,
            ToDoubleFunction<Object[]> mapper, String... dependent) {
        List<Double> mappedColumn = IntStream.range(0, dataframe.size).mapToObj(i -> toArray(dataframe, i, dependent))
                .mapToDouble(mapper).boxed().collect(Collectors.toList());
        dataframe.getDataframe().put(header, (List) mappedColumn);
        dataframe.putFormat(header, Double.class);
        return mappedColumn;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<Double> crossFeature(DataframeML dataframe, String header, ToDoubleFunction<double[]> mapper,
            String... dependent) {
        List<Double> newColumn =
                IntStream.range(0, dataframe.size).mapToObj(i -> toDoubleArray(dataframe, i, dependent))
                        .mapToDouble(mapper).boxed().collect(Collectors.toList());
        dataframe.getDataframe().put(header, (List) newColumn);
        dataframe.putFormat(header, Double.class);
        return newColumn;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> crossFeatureObject(DataframeML dataframe, String header, DoubleProperty progress,
            FunctionEx<Object[], T> mapper, String... dependent) {

        AtomicInteger count = new AtomicInteger(0);
        update(progress, 0);
        List<T> mappedColumn = IntStream.range(0, dataframe.size).mapToObj(i -> toArray(dataframe, i, dependent))
                .map(FunctionEx.makeFunction(mapper))
                .peek(i -> update(progress, count.getAndIncrement() / (double) dataframe.size))
                .collect(Collectors.toList());
        Class<? extends Comparable<?>> columnFormat = (Class<? extends Comparable<?>>) mappedColumn.stream()
                .filter(Objects::nonNull).findFirst().map(T::getClass).orElse(null);
        if (columnFormat != null && columnFormat.isArray()) {
            crossArrayFeature(dataframe, header, mappedColumn, columnFormat);
        } else if (columnFormat != null && Collection.class.isAssignableFrom(columnFormat)) {
            crossCollectionFeature(dataframe, header, mappedColumn);
        } else if (columnFormat != null && Map.class.isAssignableFrom(columnFormat)) {
            crossMapFeature(dataframe, header, mappedColumn);
        } else {
            dataframe.getDataframe().put(header, (List) mappedColumn);
            dataframe.putFormat(header, columnFormat);
        }
        update(progress, 1);
        return mappedColumn;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> crossFeatureObject(DataframeML dataframe, String header, FunctionEx<Object[], T> mapper,
            String... dependent) {
        List<T> mappedColumn = IntStream.range(0, dataframe.size).mapToObj(i -> toArray(dataframe, i, dependent))
                .map(FunctionEx.makeFunction(mapper)).collect(Collectors.toList());
        dataframe.getDataframe().put(header, (List) mappedColumn);
        dataframe.putFormat(header, (Class<? extends Comparable<?>>) mappedColumn.stream().filter(Objects::nonNull)
                .findFirst().map(T::getClass).orElse(null));
        return mappedColumn;
    }

    public static void describe(BaseDataframe dataframeML) {
        if (dataframeML.stats == null) {
            dataframeML.stats = DataframeUtils.makeStats(dataframeML);
        }
        DataframeUtils.displayStats(dataframeML.stats);
    }

    public static void displayCorrelation(BaseDataframe dataframe) {
        Map<String, DataframeStatisticAccumulator> stats = makeStats(dataframe);

        StringBuilder s = new StringBuilder();
        List<String> keySet = dataframe.cols();
        int pad = keySet.stream().mapToInt(String::length).max().orElse(1);
        s.append("\n");
        s.append("\t\t");
        keySet.forEach(k -> s.append(String.format("\t%s", k)));
        s.append("\n");
        for (String variable : keySet) {
            String format = "%" + pad + "s";
            s.append(String.format(format, variable));
            double self = stats.get(variable).getCorrelation(variable);
            for (String variable2 : keySet) {
                s.append(String.format(floatFormating(variable2.length()),
                        stats.get(variable).getCorrelation(variable2) / self));
            }
            s.append("\n");

        }
        String correlationTable = s.toString();
        LOG.info(correlationTable);
    }

    public static void displayStats(BaseDataframe dataframe) {
        displayStats(makeStats(dataframe));
    }

    public static void displayStats(Map<String, DataframeStatisticAccumulator> stats) {
        StringBuilder s = new StringBuilder();
        s.append("\n");

        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), k)));
        s.append("\ncount");
        stats.forEach((k, v) -> s.append(String.format(intFormating(len(k, v.getFormat())), v.getCount())));
        s.append("\nmean");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMean())));
        s.append("\nstd");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getStd())));
        s.append("\nmin");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMin())));
        s.append("\nmax");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMax())));
        s.append("\n25%");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMedian25())));
        s.append("\n50%");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMedian50())));
        s.append("\n75%");
        stats.forEach((k, v) -> s.append(format(len(k, v.getFormat()), v.getMedian75())));
        String description = s.toString();
        LOG.info(description);
    }

    public static <T> T getFromList(int j, List<T> list) {
        return list != null && j < list.size() ? list.get(j) : null;
    }

    public static Map<Double, Long> histogram(BaseDataframe dataframeML, String header, int bins) {
        return DataframeStatisticAccumulator.histogram(dataframeML.getDataframe(), header, bins);
    }

    public static Map<String, DataframeStatisticAccumulator> makeStats(BaseDataframe dataframe) {
        return dataframe.getDataframe().entrySet().stream()
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
                        e -> e.getValue().stream().collect(() -> accumulator(dataframe, e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1.combine(m2), LinkedHashMap::new));
    }

    public static Map<String, DataframeStatisticAccumulator> makeStats(File csvFile, DataframeML dataframeML,
            DoubleProperty progress) {
        return SupplierEx.get(() -> mkStats(csvFile, dataframeML, progress));
    }

    public static DataframeML readCSV(File csvFile, DataframeML dataframeML) {
        return readCSV(csvFile, null, dataframeML);
    }

    public static DataframeML readCSV(File csvFile, DoubleProperty progress, DataframeML dataframeML) {
        double totalSize = ResourceFXUtils.computeAttributes(csvFile).size();
        update(progress, 0);
        try (Scanner scanner = new Scanner(csvFile, "UTF-8")) {
            dataframeML.file = csvFile;
            dataframeML.size = 0;

            List<String> header = getHeaders(scanner);
            for (String column : header) {
                dataframeML.getDataframe().put(column, new ArrayList<>());
                dataframeML.putFormat(column, String.class);

            }

            readRows(dataframeML, scanner, header, progress, totalSize);
        } catch (Exception e) {
            LOG.error("ERROR IN FILE {} - {}", csvFile, e.getMessage());
            LOG.trace("FILE NOT FOUND " + csvFile, e);
        }
        update(progress, 1);
        return dataframeML;
    }

    public static void readCSV(String csvFile, DataframeML dataframeML) {
        readCSV(ResourceFXUtils.toFile(csvFile), dataframeML);
    }

    public static void save(DataframeML dataframe, File outFile) {
        RunnableEx.run(() -> {
            List<String> lines = new ArrayList<>();
            List<String> cols = dataframe.cols();
            lines.add(cols.stream().map(e -> "\"" + e + "\"").collect(Collectors.joining(",")));
            dataframe.forEachRow(m -> lines.add(cols.stream().map(e -> m.getOrDefault(e, ""))
                    .map(StringSigaUtils::toStringSpecial).map(s -> s.replaceAll("\"", "\\\""))
                    .map(e -> "\"" + e + "\"").collect(Collectors.joining(","))));
            Files.write(outFile.toPath(), lines);
        });

    }

    public static void sort(DataframeML dataframe, String header) {
        List<Object> list = dataframe.list(header);
        List<List<Object>> trimmedColumns =
                dataframe.getDataframe().entrySet().stream().filter(e -> !e.getKey().equals(header))
                        .map(Entry<String, List<Object>>::getValue).collect(Collectors.toList());

        Class<?> class1 = dataframe.getFormat(header);
        if (class1 == String.class) {
            QuickSortML.sort(typedList(list), (i, j) -> {
                for (List<Object> list2 : trimmedColumns) {
                    Object object = list2.get(i);
                    list2.set(i, list2.get(j));
                    list2.set(j, object);
                }
            }, String::compareTo);
        }

        if (class1 == Double.class) {
            Comparator<Double> compa = Double::compareTo;
            QuickSortML.sort(typedList(list), (i, j) -> {
                for (List<Object> list2 : trimmedColumns) {
                    Object object = list2.get(i);
                    list2.set(i, list2.get(j));
                    list2.set(j, object);
                }
            }, compa.reversed());
        }
        if (class1 == Integer.class) {
            Comparator<Integer> compa = Integer::compareTo;
            QuickSortML.sort(typedList(list), (i, j) -> {
                for (List<Object> list2 : trimmedColumns) {
                    Object object = list2.get(i);
                    list2.set(i, list2.get(j));
                    list2.set(j, object);
                }
            }, compa.reversed());
        }

    }

    public static String toString(DataframeML dataframe) {
        return toString(dataframe, 10);
    }

    public static String toString(DataframeML dataframe, int max) {
        StringBuilder str = new StringBuilder();
        str.append("\n");
        Map<String, Integer> maxFormatMap = new LinkedHashMap<>();
        dataframe.forEach((s, l) -> {
            maxFormatMap.put(s, s.length());
            Class<? extends Comparable<?>> format2 = dataframe.getFormat(s);
            l.stream().limit(max)
                    .forEach(
                            e -> maxFormatMap.merge(s,
                                    format2 == Double.class ? Objects.toString(e).replaceAll("\\.\\d+$", "").length()
                                            : Objects.toString(e).length(),
                                    (t, u) -> Integer.min(Integer.max(t, u), 30)));
        });

        dataframe.forEach((s, l) -> str.append(StringSigaUtils.format(maxFormatMap.get(s), s)));
        str.append("\n");
        dataframe.getFormatMap().forEach((s, l) -> str
                .append(StringSigaUtils.format(maxFormatMap.get(s), FunctionEx.mapIf(l, Class::getSimpleName))));
        str.append("\n");
        for (int i = 0; i < Math.min(dataframe.size, max); i++) {
            int j = i;
            dataframe.forEach((s, l) -> {
                if (l.size() > j) {
                    String string = Objects.toString(l.get(j), "");
                    if (string.length() > maxFormatMap.get(s)) {
                        string = string.substring(0, maxFormatMap.get(s));
                    }
                    str.append(StringSigaUtils.format(maxFormatMap.get(s), string));
                }
            });
            str.append("\n");
        }
        if (dataframe.size > max) {
            str.append("...\n");
        }
        str.append("Size=" + dataframe.size + " \n");
        return str.toString();
    }

    public static void trim(String header, int trimmingSize, DataframeML dataframe) {
        sort(dataframe, header);
        List<Entry<String, List<Object>>> entrySet =
                dataframe.getDataframe().entrySet().stream().collect(Collectors.toList());
        for (int i = 0; i < entrySet.size(); i++) {
            Entry<String, List<Object>> entry = entrySet.get(i);
            List<Object> value = entry.getValue();
            dataframe.getDataframe().put(entry.getKey(), value.subList(trimmingSize, value.size() - trimmingSize - 1));
        }
    }

    public static Object tryNumber(DataframeML dataframeML, String header, String field) {
        if (isBlank(field) || field.matches("-+")) {
            return null;
        }
        Class<?> currentFormat = dataframeML.getFormat(header);
        if (currentFormat == String.class && dataframeML.getSize() > 1
                && dataframeML.list(header).stream().anyMatch(String.class::isInstance)) {
            return field;
        }

        String number = fixNumber(field, currentFormat);

        Object o = StringSigaUtils.tryAsNumber(dataframeML.getFormatMap(), header, currentFormat, number);
        if (o != null) {
            return o;
        }
        if (Number.class.isAssignableFrom(dataframeML.getFormat(header))) {
            dataframeML.putFormat(header, String.class);
            dataframeML.map(header, e -> Objects.toString(e, ""));
        }
        return number;
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> List<T> typedList(List<Object> list) {
        return (List<T>) list;
    }

    protected static void categorizeIfCategorizable(DataframeML dataframe, String key, Object tryNumber) {
        if (dataframe.categories.containsKey(key)) {
            Set<String> set = dataframe.categories.get(key);
            String string = Objects.toString(tryNumber);
            if (!set.contains(string)) {
                set.add(string);
            }
        }
    }

    protected static void createNullRow(List<String> header, Map<String, Object> line2) {
        header.stream().filter(e -> !line2.containsKey(e)).forEach(e -> line2.put(e, null));
    }

    protected static Object mapIfMappable(DataframeML dataframe, String key, Object tryNumber) {
        if (dataframe.mapping.containsKey(key)) {
            return dataframe.mapping.get(key).apply(tryNumber);
        }
        return tryNumber;
    }

    protected static void removeRow(DataframeML dataframe, List<String> header, int i) {
        for (int j = 0; j < i; j++) {
            String key2 = header.get(j);
            List<Object> list = dataframe.list(key2);
            Object remove = list.remove(list.size() - 1);
            if (dataframe.categories.containsKey(key2)) {
                dataframe.categories.get(key2).remove(remove);
            }
        }
        dataframe.size--;
    }

    private static DataframeStatisticAccumulator accumulator(BaseDataframe dataframeML, String h) {
        return new DataframeStatisticAccumulator(dataframeML.dataframe, dataframeML.formatMap, h);
    }

    @SuppressWarnings("unchecked")
    private static void addCrossFeature(DataframeML dataframe) {
        for (Entry<String, Entry<String[], FunctionEx<Object[], ?>>> entry : dataframe.crossFeature.entrySet()) {
            String header = entry.getKey();
            Entry<String[], FunctionEx<Object[], ?>> crossMapping = entry.getValue();
            List<Object> computeIfAbsent = dataframe.getDataframe().computeIfAbsent(header, h -> new ArrayList<>());
            String[] key = crossMapping.getKey();
            Object[] array = Stream.of(key).map(dataframe::list).map(l -> l.get(l.size() - 1)).toArray();
            Object apply = FunctionEx.apply(crossMapping.getValue(), array);
            Class<? extends Object> columnFormat = FunctionEx.mapIf(apply, Object::getClass);
            // dataframe.add
            computeIfAbsent.add(apply);

            if (columnFormat != null && columnFormat.isArray()) {
                crossFeatureArray(dataframe, header, apply, columnFormat);
            } else if (columnFormat != null && Collection.class.isAssignableFrom(columnFormat)) {
                crossFeatureCollection(dataframe, header, apply);
            } else if (columnFormat != null && Map.class.isAssignableFrom(columnFormat)) {
                crossFeatureMap(dataframe, header, apply);
            } else {
                dataframe.putFormat(header, (Class<? extends Comparable<?>>) columnFormat);
            }
        }
    }

    private static void addCrossStats(DataframeML dataframeML) {
        Map<String, Entry<String[], FunctionEx<Object[], ?>>> crossFeature2 = dataframeML.crossFeature;
        for (Entry<String, Entry<String[], FunctionEx<Object[], ?>>> entry : crossFeature2.entrySet()) {
            String header1 = entry.getKey();
            Entry<String[], FunctionEx<Object[], ?>> ob = entry.getValue();
            Object[] array = Stream.of(ob.getKey()).map(k -> dataframeML.getStats().get(k).getObject()).toArray();
            Object apply = FunctionEx.apply(ob.getValue(), array);
            Class<? extends Object> columnFormat = FunctionEx.mapIf(apply, Object::getClass);
            if (columnFormat != null && columnFormat.isArray()) {
                crossStatsArray(dataframeML, header1, apply);
            } else if (columnFormat != null && Collection.class.isAssignableFrom(columnFormat)) {
                crossStatsCollection(dataframeML, header1, apply);
            } else if (columnFormat != null && Map.class.isAssignableFrom(columnFormat)) {
                crossStatsMap(dataframeML, header1, apply);
            } else {
                dataframeML.stats.computeIfAbsent(header1, h -> accumulator(dataframeML, h)).accept(apply);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void crossArrayFeature(DataframeML dataframe, String header, List<T> mappedColumn,
            Class<?> orElse) {
        List<Object[]> m = (List<Object[]>) mappedColumn;
        int max = m.stream().mapToInt(e -> e.length).max().orElse(1);
        for (int j = 0; j < max; j++) {
            int i = j;
            dataframe.getDataframe().put(header + j,
                    m.stream().map(c -> c != null && i < c.length ? c[i] : null).collect(Collectors.toList()));
            dataframe.putFormat(header + j, (Class<? extends Comparable<?>>) orElse.getComponentType());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void crossCollectionFeature(DataframeML dataframe, String header, List<T> mappedColumn) {
        List<Collection<?>> m = (List<Collection<?>>) mappedColumn;
        int max = m.stream().mapToInt(e -> e != null ? e.size() : 0).max().orElse(1);
        for (int j = 0; j < max; j++) {
            int i1 = j;
            List<Object> collect = m.stream().map(c -> c != null ? c.stream().skip(i1).findFirst().orElse(null) : null)
                    .collect(Collectors.toList());
            dataframe.getDataframe().put(header + j, collect);
            dataframe.putFormat(header + j, (Class<? extends Comparable<?>>) collect.stream().filter(Objects::nonNull)
                    .findFirst().map(Object::getClass).orElse(null));
        }
    }

    @SuppressWarnings("unchecked")
    private static void crossFeatureArray(DataframeML dataframe, String header, Object apply,
            Class<? extends Object> columnFormat) {
        Object[] m = (Object[]) apply;
        for (int j = 0; j < m.length; j++) {
            int i = j;
            dataframe.getDataframe().computeIfAbsent(header + j, h -> new ArrayList<>()).add(m[i]);
            dataframe.formatMap.putIfAbsent(header + j,
                    (Class<? extends Comparable<?>>) columnFormat.getComponentType());
        }
    }

    @SuppressWarnings("unchecked")
    private static void crossFeatureCollection(DataframeML dataframe, String header, Object apply) {
        Collection<?> m = (Collection<?>) apply;
        int max = m.size();
        for (int j = 0; j < max; j++) {
            int i1 = j;
            Object collect = m.stream().skip(i1).findFirst().orElse(null);
            dataframe.getDataframe().computeIfAbsent(header + j, h -> new ArrayList<>()).add(collect);
            dataframe.formatMap.putIfAbsent(header + j,
                    (Class<? extends Comparable<?>>) FunctionEx.mapIf(collect, Object::getClass));
        }
    }

    @SuppressWarnings("unchecked")
    private static void crossFeatureMap(DataframeML dataframe, String header, Object apply) {
        Map<?, ?> m = (Map<?, ?>) apply;
        List<Object> max = m.keySet().stream().collect(Collectors.toList());
        for (Object j : max) {
            Object collect = m.get(j);
            dataframe.getDataframe().computeIfAbsent(header + j, h -> new ArrayList<>()).add(collect);
            dataframe.formatMap.putIfAbsent(header + j,
                    (Class<? extends Comparable<?>>) FunctionEx.mapIf(collect, Object::getClass));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void crossMapFeature(DataframeML dataframe, String header, List<T> mappedColumn) {
        List<Map<?, ?>> m = (List<Map<?, ?>>) mappedColumn;
        List<Object> max = m.stream().flatMap(e -> e != null ? e.keySet().stream() : Stream.empty()).distinct()
                .collect(Collectors.toList());
        for (Object i1 : max) {
            List<Object> collect = m.stream().map(c -> c != null ? c.get(i1) : null).collect(Collectors.toList());
            dataframe.getDataframe().put(header + i1, collect);
            dataframe.putFormat(header + i1, (Class<? extends Comparable<?>>) collect.stream().filter(Objects::nonNull)
                    .findFirst().map(Object::getClass).orElse(null));
        }
    }

    private static void crossStatsArray(DataframeML dataframeML, String header1, Object apply) {
        Object[] m = (Object[]) apply;
        for (int j = 0; j < m.length; j++) {
            int i = j;
            dataframeML.stats.computeIfAbsent(header1 + j, h -> accumulator(dataframeML, h)).accept(m[i]);
        }
    }

    private static void crossStatsCollection(DataframeML dataframeML, String header1, Object apply) {
        Collection<?> m = (Collection<?>) apply;
        int max = m.size();
        for (int j = 0; j < max; j++) {
            int i1 = j;
            Object collect = m.stream().skip(i1).findFirst().orElse(null);
            dataframeML.stats.computeIfAbsent(header1 + j, h -> accumulator(dataframeML, h)).accept(collect);
        }
    }

    private static void crossStatsMap(DataframeML dataframeML, String header1, Object apply) {
        Map<?, ?> m = (Map<?, ?>) apply;
        List<Object> max = m.keySet().stream().collect(Collectors.toList());
        for (Object j : max) {
            Object collect = m.get(j);
            dataframeML.stats.computeIfAbsent(header1 + j, h -> accumulator(dataframeML, h)).accept(collect);
        }
    }

    private static boolean filterOut(DataframeML dataframeML, List<String> header, List<String> line2) {
        return line2.isEmpty() || IntStream.range(0, header.size()).anyMatch(i -> {
            String key = header.get(i);
            String field = getFromList(i, line2);
            Object tryNumber = tryNumber(dataframeML, key, field);
            return dataframeML.filters.containsKey(key) && !dataframeML.filters.get(key).test(tryNumber);
        });
    }

    private static String fixNumber(String field, Class<?> currentFormat) {
        if (field.matches("\\d+\\.0+$") && currentFormat != Double.class) {
            return field.replaceAll("\\.0+", "");
        }

        return field.matches("\"*\\d+,\\d+$") ? field.replaceAll("[\",]", "") : field;
    }

    private static List<String> getHeaders(Scanner scanner) {
        String nextLine = scanner.nextLine();
        return CSVUtils.parseLine(nextLine).stream().map(e -> e.replaceAll("\"", ""))
                .map(c -> StringSigaUtils.fixEncoding(c).replaceAll("\\?", "")).collect(Collectors.toList());
    }

    private static int len(String k, Class<? extends Comparable<?>> class1) {
        return Math.max(1, class1 == String.class ? k.length() * 2 : k.length());
    }

    private static Map<String, DataframeStatisticAccumulator> mkStats(File csvFile, DataframeML dataframeML,
            DoubleProperty progress) throws FileNotFoundException {
        dataframeML.file = csvFile;
        long computed = 0;
        double size2 = ResourceFXUtils.computeAttributes(csvFile).size();
        update(progress, 0);
        try (Scanner scanner = new Scanner(csvFile, StandardCharsets.UTF_8.displayName())) {
            List<String> header = addHeaders(dataframeML, scanner);
            CSVUtils defaultCSVUtils = CSVUtils.defaultCSVUtils();
            while (scanner.hasNext()) {
                computed += runLine(dataframeML, header, scanner, defaultCSVUtils);
                double co = computed;
                update(progress, co / size2);
                if (dataframeML.size > dataframeML.maxSize) {
                    break;
                }
            }
            update(progress, 1);
            return dataframeML.stats;
        }
    }

    private static void readRows(DataframeML dataframe, Scanner scanner, List<String> header, DoubleProperty progress,
            double totalSize) {
        CSVUtils defaultCSVUtils = CSVUtils.defaultCSVUtils();
        double co = 0;
        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            co += nextLine.getBytes(StandardCharsets.UTF_8).length;
            List<String> line2 = defaultCSVUtils.getFields(nextLine);
            co += CSVUtils.fixMultipleLines(scanner, defaultCSVUtils, line2);

            update(progress, co / totalSize);
            if (filterOut(dataframe, header, line2)) {
                continue;
            }
            CSVUtils.fixEmptyLine(header, line2, dataframe.size);
            dataframe.size++;
            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = getFromList(i, line2);
                Object tryNumber = tryNumber(dataframe, key, field);
                categorizeIfCategorizable(dataframe, key, tryNumber);
                tryNumber = mapIfMappable(dataframe, key, tryNumber);
                dataframe.list(key).add(tryNumber);
            }
            addCrossFeature(dataframe);

            if (dataframe.size > dataframe.maxSize) {
                return;
            }
        }
    }

    private static long runLine(DataframeML dataframeML, List<String> header, Scanner scanner,
            CSVUtils defaultCSVUtils) {
        String nextLine = scanner.nextLine();
        long co = nextLine.getBytes(StandardCharsets.UTF_8).length;
        List<String> line2 = defaultCSVUtils.getFields(nextLine);
        co += CSVUtils.fixMultipleLines(scanner, defaultCSVUtils, line2);
        if (!filterOut(dataframeML, header, line2)) {
            dataframeML.size++;
            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = getFromList(i, line2);
                Object tryNumber = tryNumber(dataframeML, key, field);
                categorizeIfCategorizable(dataframeML, key, tryNumber);
                tryNumber = mapIfMappable(dataframeML, key, tryNumber);
                DataframeStatisticAccumulator acc = dataframeML.stats.get(key);
                acc.accept(tryNumber);
            }
            addCrossStats(dataframeML);

            CSVUtils.fixEmptyLine(header, line2, dataframeML.size);
        }
        return co;
    }

    private static Object[] toArray(DataframeML dataframe, int i, String... dependent) {
        Object[] d = new Object[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = dataframe.getAt(dependent[j], i);
        }
        return d;
    }

    private static double[] toDoubleArray(DataframeML dataframe, int i, String... dependent) {
        double[] d = new double[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = ((Number) dataframe.getDataframe().get(dependent[j]).get(i)).doubleValue();
        }
        return d;
    }


}
