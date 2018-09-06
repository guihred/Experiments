package ml;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataframeStatisticAccumulator{
    /**
     * 
     */
    private final DataframeML dataframe;
    private int count;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double max = Double.NEGATIVE_INFINITY;
    private String header;
    private Map<String, Integer> countMap = new LinkedHashMap<>();
    private Class<? extends Comparable<?>> format;

    public DataframeStatisticAccumulator(DataframeML dataframeML, String header) {
        dataframe = dataframeML;
        this.header = header;
        format = dataframe.getFormat(header);
    }

    private void acceptNumber(Number n) {
        count++;
        double o = n.doubleValue();
        sum += o;
        min = Math.min(min, o);
        max = Math.max(max, o);
    }

    public Set<String> getUnique() {
        return countMap.keySet();
    }

    public String getTop() {
        return countMap.entrySet().stream().max(Comparator.comparing(Entry<String, Integer>::getValue))
                .map(Entry<String, Integer>::getKey)
                .orElse(null);
    }

    public String getBottom() {
        return countMap.entrySet().stream().min(Comparator.comparing(Entry<String, Integer>::getValue))
                .map(Entry<String, Integer>::getKey).orElse(null);
    }

    private void acceptString(String n) {
        sum++;
        if (!countMap.containsKey(n)) {
            countMap.put(n, 0);
            count++;
        }
        countMap.put(n, countMap.get(n) + 1);

    }

    public void combine(DataframeStatisticAccumulator n) {
        count += n.count;
        sum += n.sum;
        min = Math.min(min, n.min);
        max = Math.max(max, n.max);
        n.countMap.forEach((k, v) -> countMap.merge(k, v, (a, b) -> a + b));
    }

    public double getMean() {
        if (format == String.class) {
            return sum / countMap.size();
        }

        return count == 0 ? 0 : sum / count;
    }


    public double getStd() {
        if (format == String.class) {
            double mean = sum / countMap.size();
            double sum2 = countMap.values().stream().mapToDouble(e -> e)
                    .map(e -> e - mean).map(e -> e * e).sum();
            return Math.sqrt(sum2 / (countMap.size() - 1));
        }

        double mean = sum / count;
        double sum2 = dataframe.list(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean).map(e -> e * e).sum();
        return Math.sqrt(sum2 / (count - 1));
    }

    public double getCorrelation(String other) {
        if (format == String.class || dataframe.getFormat(other) == String.class) {
            return 0;
        }
        
        double mean = sum / count;
        List<Object> variable = dataframe.list(header);
        double sum1 = variable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean).map(e -> e * e).sum();
        double st1 = Math.sqrt(sum1 / (count - 1));
        
        List<Object> otherVariable = dataframe.list(other);
        double mean2 = otherVariable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).average()
                .getAsDouble();
        double sum2 = otherVariable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean2).map(e -> e * e).sum();
        double st2 = Math.sqrt(sum2 );
        double covariance = IntStream.range(0, count)
                .mapToDouble(i -> (((Number) variable.get(i)).doubleValue() - mean)
                        * (((Number) otherVariable.get(i)).doubleValue() - mean2))
                .sum();
        return covariance / st1 / st2;
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


    public int getCount() {
        return count;
    }


    public double getMin() {
        if (format == String.class) {
            return countMap.values().stream().mapToDouble(e -> e).min().orElse(min);
        }

        return count == 0 ? 0 : min;
    }


    public double getMedian25() {
        if (format == String.class) {
            double[] array = countMap.values().stream().mapToDouble(e -> e).sorted().toArray();
            if (array.length > 0) {
                return array[array.length / 4];
            }
        }
        List<Double> sortedNumber = dataframe.list(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .map(Number::doubleValue).sorted()
                .collect(Collectors.toList());
        if (sortedNumber.isEmpty()) {
            return 0;
        }
        return sortedNumber.get(count / 4).doubleValue();
    }


    public double getMedian50() {
        if (format == String.class) {
            double[] array = countMap.values().stream().mapToDouble(e -> e).sorted().toArray();
            if (array.length > 0) {
                return array[array.length / 2];
            }
        }
        List<Double> collect = dataframe.list(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .map(Number::doubleValue).sorted()
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return 0;
        }
        return collect.get(count / 2).doubleValue();
    }


    public double getMedian75() {
        if (format == String.class) {
            double[] array = countMap.values().stream().mapToDouble(e -> e).sorted().toArray();
            if (array.length > 0) {
                return array[array.length * 3 / 4];
            }
        }
        List<Double> collect = dataframe.list(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .map(Number::doubleValue).sorted()
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return 0;
        }
        return collect.get(count * 3 / 4).doubleValue();
    }


    public double getMax() {
        if (format == String.class) {
            return countMap.values().stream().mapToDouble(e -> e).max().orElse(max);
        }
        return max;
    }


    public double getSum() {
        return sum;
    }

    
}