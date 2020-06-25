package ml.data;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static utils.StringSigaUtils.floatFormating;
import static utils.StringSigaUtils.format;
import static utils.StringSigaUtils.formating;
import static utils.StringSigaUtils.intFormating;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;

public class DataframeUtils extends DataframeML {

    private static final Logger LOG = HasLogging.log();

    protected DataframeUtils() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<Double> crossFeature(DataframeML dataframe, String header, ToDoubleFunction<double[]> mapper,
            String... dependent) {
        List<Double> newColumn =
                IntStream.range(0, dataframe.size).mapToObj(i -> toDoubleArray(dataframe, i, dependent))
                        .mapToDouble(mapper).boxed().collect(Collectors.toList());
        dataframe.getDataframe().put(header, (List) newColumn);
        dataframe.getFormatMap().put(header, Double.class);
        return newColumn;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<Double> crossFeatureObject(DataframeML dataframe, String header,
            ToDoubleFunction<Object[]> mapper, String... dependent) {
        List<Double> mappedColumn = IntStream.range(0, dataframe.size).mapToObj(i -> toArray(dataframe, i, dependent))
                .mapToDouble(mapper).boxed().collect(Collectors.toList());
        dataframe.getDataframe().put(header, (List) mappedColumn);
        dataframe.getFormatMap().put(header, Double.class);
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
        Set<String> keySet = dataframe.cols();
        int pad = keySet.stream().mapToInt(String::length).max().getAsInt();
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
                        e -> e.getValue().stream().collect(
                                () -> new DataframeStatisticAccumulator(dataframe.getDataframe(),
                                        dataframe.getFormatMap(), e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1.combine(m2), LinkedHashMap::new));
    }

    public static void readCSV(File csvFile, DataframeML dataframeML) {
        try (Scanner scanner = new Scanner(csvFile, "UTF-8")) {
            List<String> header = CSVUtils.parseLine(scanner.nextLine()).stream().map(e -> e.replaceAll("\"", ""))
                    .collect(Collectors.toList());
            for (String column : header) {
                dataframeML.getDataframe().put(column, new ArrayList<>());
                dataframeML.putFormat(column, String.class);
            }

            readRows(dataframeML, scanner, header);
        } catch (Exception e) {
            LOG.error("ERROR IN FILE {} - {}", csvFile, e.getMessage());
            LOG.trace("FILE NOT FOUND " + csvFile, e);
        }
    }

    public static void readCSV(String csvFile, DataframeML dataframeML) {
        readCSV(ResourceFXUtils.toFile(csvFile), dataframeML);
    }

    public static String toString(DataframeML dataframe) {
        StringBuilder str = new StringBuilder();
        str.append("\n");
        dataframe.forEach((s, l) -> str.append(s + "\t"));
        str.append("\n");
        dataframe.getFormatMap().forEach((s, l) -> str.append(String.format(formating(s), l.getSimpleName())));
        str.append("\n");
        for (int i = 0; i < 10; i++) {
            int j = i;
            dataframe.forEach((s, l) -> {
                if (l.size() > j) {
                    String string = Objects.toString(l.get(j), "");
                    if (string.length() > s.length() + 3) {
                        string = string.substring(0, s.length() + 3);
                    }
                    str.append(String.format(formating(s), string));
                }
            });
            str.append("\n");
        }
        if (dataframe.size > 10) {
            str.append("...\n");
        }
        str.append("Size=" + dataframe.size + " \n");
        return str.toString();
    }

    public static void trim(String header, int trimmingSize, DataframeML dataframe) {
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
        List<Entry<String, List<Object>>> entrySet =
                dataframe.getDataframe().entrySet().stream().collect(Collectors.toList());
        for (int i = 0; i < entrySet.size(); i++) {
            Entry<String, List<Object>> entry = entrySet.get(i);
            List<Object> value = entry.getValue();
            dataframe.getDataframe().put(entry.getKey(), value.subList(trimmingSize, value.size() - trimmingSize - 1));
        }
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

    protected static void createNullRow(List<String> header, List<String> line2) {
        if (line2.size() < header.size()) {
            long maxSize2 = header.size() - (long) line2.size();
            line2.addAll(Stream.generate(() -> "").limit(maxSize2).collect(Collectors.toList()));
        }
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

    protected static Object tryNumber(DataframeML dataframeML, String header, String field) {
        if (isBlank(field)) {
            return null;
        }
        Class<?> currentFormat = dataframeML.getFormat(header);
        if (currentFormat == String.class && dataframeML.getSize() > 1
                && dataframeML.list(header).stream().anyMatch(String.class::isInstance)) {
            return field;
        }

        String number = field;
        if (field.matches("\\d+\\.0+$") && currentFormat != Double.class) {
            number = field.replaceAll("\\.0+", "");
        }

        try {
            return StringSigaUtils.tryNumber(dataframeML.getFormatMap(), Integer.class, currentFormat, number, header,
                    Integer::valueOf);
        } catch (Exception e) {
            LOG.trace("FORMAT ERROR ", e);
        }
        try {
            return StringSigaUtils.tryNumber(dataframeML.getFormatMap(), Long.class, currentFormat, number, header,
                    Long::valueOf);
        } catch (Exception e) {
            LOG.trace("FORMAT ERROR", e);
        }
        try {
            return StringSigaUtils.tryNumber(dataframeML.getFormatMap(), Double.class, currentFormat, number, header,
                    Double::valueOf);
        } catch (Exception e) {
            LOG.trace("FORMAT ERROR", e);
        }
        if (Number.class.isAssignableFrom(dataframeML.getFormat(header))) {
            dataframeML.getFormatMap().put(header, String.class);
            dataframeML.map(header, e -> Objects.toString(e, ""));
        }
        return number;
    }

    private static int len(String k, Class<? extends Comparable<?>> class1) {
        return class1 == String.class ? k.length() * 2 : k.length();
    }

    private static void readRows(DataframeML dataframe, Scanner scanner, List<String> header) {
        while (scanner.hasNext()) {
            dataframe.size++;
            List<String> line2 = CSVUtils.parseLine(scanner.nextLine());
            if (header.size() != line2.size()) {
                LOG.error("ERROR FIELDS COUNT");
                createNullRow(header, line2);
            }

            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = getFromList(i, line2);
                Object tryNumber = tryNumber(dataframe, key, field);
                if (dataframe.filters.containsKey(key) && !dataframe.filters.get(key).test(tryNumber)) {
                    removeRow(dataframe, header, i);
                    break;
                }
                categorizeIfCategorizable(dataframe, key, tryNumber);
                tryNumber = mapIfMappable(dataframe, key, tryNumber);

                dataframe.list(key).add(tryNumber);
            }
            if (dataframe.size > dataframe.maxSize) {
                break;
            }
        }
    }

    private static Object[] toArray(DataframeML dataframe, int i, String... dependent) {
        Object[] d = new Object[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = dataframe.getDataframe().get(dependent[j]).get(i);
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
