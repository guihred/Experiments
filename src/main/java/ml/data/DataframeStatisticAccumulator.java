package ml.data;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataframeStatisticAccumulator {
    /**
     * 
     */
    private final Map<String, List<Object>> dataframe;
    private int count;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double max = Double.NEGATIVE_INFINITY;
    private String header;
    private Map<String, Integer> countMap = new LinkedHashMap<>();
    private Class<? extends Comparable<?>> format;
    private Map<String, Class<? extends Comparable<?>>> formatMap;

    public DataframeStatisticAccumulator(Map<String, List<Object>> dataframe,
            Map<String, Class<? extends Comparable<?>>> formatMap, String header) {
        this.dataframe = dataframe;
        this.formatMap = formatMap;
        format = formatMap.get(header);
        this.header = header;
    }

    public DataframeStatisticAccumulator accept(Object o) {
        if (format.isInstance(o) && Number.class.isAssignableFrom(format)) {
            acceptNumber((Number) o);
        }
        if (format.isInstance(o) && String.class.isAssignableFrom(format)) {
            acceptString((String) o);
        }
        return this;
    }

    public DataframeStatisticAccumulator combine(DataframeStatisticAccumulator n) {
        count += n.count;
        sum += n.sum;
        min = Math.min(min, n.min);
        max = Math.max(max, n.max);
        n.countMap.forEach((k, v) -> countMap.merge(k, v, (a, b) -> a + b));
        return this;
    }

    public String getBottom() {
        return countMap.entrySet().stream().min(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
    }

    public double getCorrelation(String other) {
        if (format == String.class || formatMap.get(other) == String.class) {
            return 0;
        }

        double mean = sum / count;
        List<Object> variable = dataframe.get(header);
        double sum1 = variable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).map(e -> e - mean)
                .map(e -> e * e).sum();
        double st1 = Math.sqrt(sum1 / (count - 1));

        List<Object> otherVariable = dataframe.get(other);
        double mean2 =
                otherVariable.stream().filter(Number.class::isInstance).map(Number.class::cast)
                        .mapToDouble(Number::doubleValue)
                        .average().getAsDouble();

        double sum2 = otherVariable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean2).map(e -> e * e).sum();
        double st2 = Math.sqrt(sum2);
        double covariance = IntStream.range(0, count).mapToDouble(i -> (((Number) variable.get(i)).doubleValue() - mean)
                * (((Number) otherVariable.get(i)).doubleValue() - mean2)).sum();
        return covariance / st1 / st2;
    }

    public int getCount() {
        return count;
    }

    public Class<? extends Comparable<?>> getFormat() {
        return format;
    }

    public Object getMax() {
        if (format == String.class) {
            return countMap.entrySet().stream().max(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
        }
        return max;
    }

    public Object getMean() {
        if (format == String.class) {
            return countMap.entrySet().stream().max(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
        }

        return count == 0 ? 0 : sum / count;
    }

    public Object getMedian25() {
        if (format == String.class) {
            List<String> array = countMap.entrySet().stream().sorted(comparator()).map(Entry<String, Integer>::getKey)
                    .collect(Collectors.toList());
            if (!array.isEmpty()) {
                return array.get(array.size() / 4);
            }
            return null;
        }
        List<Double> sortedNumber =
                dataframe.get(header).stream().filter(Objects::nonNull).filter(Number.class::isInstance)
                        .map(Number.class::cast).map(Number::doubleValue).sorted().collect(Collectors.toList());
        if (sortedNumber.isEmpty()) {
            return 0;
        }
        return sortedNumber.get(count / 4).doubleValue();
    }

    public Object getMedian50() {
        if (format == String.class) {
            List<String> array = countMap.entrySet().stream().sorted(comparator()).map(Entry<String, Integer>::getKey)
                    .collect(Collectors.toList());
            if (!array.isEmpty()) {
                return array.get(array.size() / 2);
            }
            return null;
        }
        List<Double> numbersList = dataframe.get(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .map(Number::doubleValue).sorted().collect(Collectors.toList());
        if (numbersList.isEmpty()) {
            return 0;
        }
        return numbersList.get(count / 2).doubleValue();
    }

    public Object getMedian75() {
        if (format == String.class) {
            List<String> array = countMap.entrySet().stream().sorted(comparator()).map(Entry<String, Integer>::getKey)
                    .collect(Collectors.toList());
            if (!array.isEmpty()) {
                return array.get(array.size() * 3 / 4);
            }
            return null;
        }
        List<Double> numbers = dataframe.get(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .map(Number::doubleValue).sorted().collect(Collectors.toList());
        if (numbers.isEmpty()) {
            return 0;
        }
        return numbers.get(count * 3 / 4).doubleValue();
    }

    public Object getMin() {
        if (format == String.class) {
            return countMap.entrySet().stream().min(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
        }

        return count == 0 ? 0 : min;
    }

    public double getStd() {
        if (format == String.class) {
            double mean = sum / countMap.size();
            double sum2 = countMap.values().stream().mapToDouble(e -> e).map(e -> e - mean).map(e -> e * e).sum();
            return Math.sqrt(sum2 / (countMap.size() - 1));
        }

        double mean = sum / count;
        double sum2 = dataframe.get(header).stream().filter(Number.class::isInstance).map(Number.class::cast)
                .mapToDouble(Number::doubleValue).map(e -> e - mean).map(e -> e * e).sum();
        return Math.sqrt(sum2 / (count - 1));
    }

    public double getSum() {
        return sum;
    }

    public String getTop() {
        return countMap.entrySet().stream().max(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
    }

    public Set<String> getUnique() {
        return countMap.keySet();
    }

    private void acceptNumber(Number n) {
        count++;
        double o = n.doubleValue();
        sum += o;
        min = Math.min(min, o);
        max = Math.max(max, o);
    }

    private void acceptString(String n) {
        sum++;
        count++;
        countMap.put(n, countMap.computeIfAbsent(n, a -> 0) + 1);
        min = countMap.values().stream().mapToDouble(e -> e).min().orElse(min);
        max = countMap.values().stream().mapToDouble(e -> e).max().orElse(max);
    }

    public static List<Entry<Number, Number>> createNumberEntries(Map<String, List<Object>> dataframe2, int size,
            String feature, String target) {

        List<Object> list = dataframe2.get(feature);
        List<Object> list2 = dataframe2.get(target);
        List<Entry<Number, Number>> data = new ArrayList<>();
        IntStream.range(0, size).filter(i -> i < list.size() && i < list2.size())
                .filter(i -> list.get(i) != null && list2.get(i) != null)
                .forEach(i -> data.add(new AbstractMap.SimpleEntry<>((Number) list.get(i), (Number) list2.get(i))));
        return data;
    }

    public static Map<Double, Long> histogram(Map<String, List<Object>> dataframe, String header, int bins) {
        List<Object> list = dataframe.get(header);
        List<Double> columnList = list.stream().filter(Objects::nonNull).map(Number.class::cast)
                .mapToDouble(Number::doubleValue).boxed().collect(Collectors.toList());
        DoubleSummaryStatistics summaryStatistics = columnList.stream().mapToDouble(e -> e).summaryStatistics();
        double min = summaryStatistics.getMin();
        double max = summaryStatistics.getMax();
        double binSize = (max - min) / bins;
        return columnList.parallelStream()
                .collect(Collectors.groupingBy(e -> Math.ceil(e / binSize) * binSize, Collectors.counting()));
    }

    public static Map<String, Object> rowMap(Map<String, List<Object>> dataframe2, int i) {
        return dataframe2.entrySet().stream().filter(e -> e.getValue().get(i) != null)
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey, e -> e.getValue().get(i),
                        DataframeStatisticAccumulator.throwError(), LinkedHashMap<String, Object>::new));
    }

    public static BinaryOperator<Object> throwError() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    private static Comparator<Entry<String, Integer>> comparator() {
        return Comparator.comparing(Entry<String, Integer>::getValue);
    }
}