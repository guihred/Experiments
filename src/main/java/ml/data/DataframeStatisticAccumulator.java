package ml.data;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import utils.ex.SupplierEx;

public class DataframeStatisticAccumulator {
    /**
     * 
     */
    private final Map<String, List<Object>> dataframe;
    private int count;
    private int distinct;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double max = Double.NEGATIVE_INFINITY;
    private final String header;
    private Map<String, Integer> countMap = new LinkedHashMap<>();
    private final Map<Number, Integer> distributionMap = new LinkedHashMap<>();
    private final Map<String, Class<? extends Comparable<?>>> formatMap;

    public DataframeStatisticAccumulator(Map<String, List<Object>> dataframe,
            Map<String, Class<? extends Comparable<?>>> formatMap, String header) {
        this.dataframe = dataframe;
        this.formatMap = formatMap;
        this.header = header;
    }

    public DataframeStatisticAccumulator accept(Object o) {
        if (getFormat().isInstance(o) && Number.class.isAssignableFrom(getFormat())) {
            acceptNumber((Number) o);
        }
        if (getFormat().isInstance(o) && String.class.isAssignableFrom(getFormat())) {
            acceptString((String) o);
        }
        count++;
        return this;
    }

    public DataframeStatisticAccumulator combine(DataframeStatisticAccumulator n) {
        count += n.count;
        sum += n.sum;
        min = Math.min(min, n.min);
        max = Math.max(max, n.max);
        n.countMap.forEach((k, v) -> countMap.merge(k, v, (a, b) -> a + b));
        n.distributionMap.forEach((k, v) -> distributionMap.merge(k, v, (a, b) -> a + b));
        distinct = getUnique().size();
        return this;
    }

    public String getBottom() {
        if (getFormat() == String.class) {
            return SupplierEx.get(() -> countMap.entrySet().stream().min(comparator())
                    .map(Entry<String, Integer>::getKey).orElse(null));
        }
        return Objects.toString(min);
    }

    public double getCorrelation(String other) {
        if (getFormat() == String.class || formatMap.get(other) == String.class) {
            return 0;
        }

        double mean = sum / count;
        List<Object> variable = dataframe.get(header);
        double sum1 = variable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).map(e -> e - mean)
                .map(e -> e * e).sum();
        double st1 = Math.sqrt(sum1 / (count - 1));

        List<Object> otherVariable = dataframe.get(other);
        double mean2 = otherVariable.stream().filter(Number.class::isInstance).map(Number.class::cast)
                .mapToDouble(Number::doubleValue).average().getAsDouble();

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

    public Map<String, Integer> getCountMap() {
        if (!countMap.isEmpty()) {
            return countMap;
        }
        countMap = distributionMap.entrySet().stream().sorted(Comparator.comparing(Entry<Number, Integer>::getValue))
                .collect(Collectors.toMap(e -> Objects.toString(e.getKey()), Entry<Number, Integer>::getValue,
                        (a, b) -> a + b, LinkedHashMap::new));
        return countMap;
    }

    public int getDistinct() {
        return distinct;
    }

    public Class<? extends Comparable<?>> getFormat() {
        return formatMap.get(header);
    }

    public String getHeader() {
        return header;
    }

    public Object getMax() {
        if (getFormat() == String.class) {
            return SupplierEx.get(() -> countMap.entrySet().stream().max(comparator())
                    .map(Entry<String, Integer>::getKey).orElse(null));
        }
        return max;
    }

    public Object getMean() {
        if (getFormat() == String.class) {
            return getMedian50();
        }

        return count == 0 ? 0 : sum / count;
    }

    public Object getMedian25() {
        return getByProportion(1. / 4);
    }

    public Object getMedian50() {
        return getByProportion(1. / 2);
    }

    public Object getMedian75() {
        return getByProportion(3. / 4);
    }

    public Object getMin() {
        if (getFormat() == String.class) {
            return countMap.entrySet().stream().min(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
        }

        return count == 0 ? 0 : min;
    }

    public double getStd() {
        if (getFormat() == String.class) {
            double mean = sum / countMap.size();
            double sum2 =
                    SupplierEx.get(() -> countMap.values().stream().mapToDouble(e -> e - mean).map(e -> e * e).sum(),
                            0.);
            return Math.sqrt(sum2 / (countMap.size() - 1));
        }

        List<Object> list = dataframe.get(header);
        double mean = sum / count;
        if (list != null && !list.isEmpty()) {
            double sum2 = list.stream().filter(Number.class::isInstance).map(Number.class::cast)
                    .mapToDouble(Number::doubleValue).map(e -> e - mean).map(e -> e * e).sum();
            return Math.sqrt(sum2 / (count - 1));
        }
        double sum2 = SupplierEx.getIgnore(
                () -> distributionMap.entrySet().stream().flatMap(e -> Stream.generate(e::getKey).limit(e.getValue()))
                        .mapToDouble(Number::doubleValue).map(e -> e - mean).map(e -> e * e).sum(),
                0.)

        ;
        return Math.sqrt(sum2 / (count - 1));

    }

    public double getSum() {
        return sum;
    }

    public String getTop() {
        if (getFormat() != String.class) {
            return Objects.toString(max);
        }

        return countMap.entrySet().stream().max(comparator()).map(Entry<String, Integer>::getKey).orElse(null);
    }

    public Set<String> getUnique() {
        if (getFormat() != String.class) {
            return SupplierEx.get(
                    () -> distributionMap.keySet().stream().map(Objects::toString).collect(Collectors.toSet()),
                    Collections.emptySet());
        }
        return countMap.keySet();
    }

    public DataframeStatisticAccumulator reset() {
        count = 0;
        sum = 0;
        min = Double.MAX_VALUE;
        max = Double.NEGATIVE_INFINITY;
        countMap.clear();
        distributionMap.clear();
        distinct = 0;
        return this;
    }

    @Override
    public String toString() {
        Class<? extends Comparable<?>> format = getFormat();
        return format == String.class ? String.format("{format: %s, distinct: %d}", format.getSimpleName(), distinct)
                : String.format("{format: %s, median: %s}", format.getSimpleName(), getMedian50());
    }

    private void acceptNumber(Number n) {

        double o = n.doubleValue();
        sum += o;
        min = Math.min(min, o);
        max = Math.max(max, o);
        Integer merge = distributionMap.merge(n, 1, (a, b) -> a + b);
        if (merge == 1) {
            distinct++;
        }
    }

    private void acceptString(String n) {
        sum++;

        Integer merge = countMap.merge(n, 1, (a, b) -> a + b);
        if (merge == 1) {
            distinct++;
        }
        min = countMap.values().stream().mapToDouble(e -> e).min().orElse(min);
        max = countMap.values().stream().mapToDouble(e -> e).max().orElse(max);
    }

    private Object getByProportion(double d) {
        if (getFormat() == String.class) {
            return getByProportion(d, countMap);
        }
        if (!dataframe.get(header).isEmpty()) {
            return dataframe.get(header).stream().sorted().skip((long) (d * count)).findFirst().orElse(null);
        }
        List<Number> array = SupplierEx.getIgnore(
                () -> distributionMap.entrySet().stream().flatMap(e -> Stream.generate(e::getKey).limit(e.getValue()))
                        .sorted(Comparator.comparing(Number::doubleValue)).collect(Collectors.toList()),
                Collections.emptyList());
        if (!array.isEmpty()) {
            return array.get((int) (array.size() * d));
        }
        return null;
    }

    private <T> Object getByProportion(double d, Map<T, Integer> countMap2) {
        List<Entry<T, Integer>> array = SupplierEx.getIgnore(
                () -> countMap2.entrySet().stream().sorted(comparator()).collect(Collectors.toList()),
                Collections.emptyList());
        if (array.isEmpty()) {
            return null;
        }
        double sizes = count * d;
        for (Entry<T, Integer> entry : array) {
            sizes -= entry.getValue();
            if (sizes < 0) {
                return entry.getKey();
            }
        }
        return array.get((int) (array.size() * d)).getKey();
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

    private static Comparator<Entry<?, Integer>> comparator() {
        return Comparator.comparing(Entry<?, Integer>::getValue);
    }
}