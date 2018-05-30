package ml;

import java.util.List;
import java.util.stream.IntStream;

public class DataframeStatisticAccumulator{
    /**
     * 
     */
    private final DataframeML dataframe;
    private int count;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double median25;
    private double median50;
    private double median75;
    private double max = Double.NEGATIVE_INFINITY;
    String unique, top, freq;
    private String header;
    private Class<?> format;

    public DataframeStatisticAccumulator(DataframeML dataframeML, String header) {
        dataframe = dataframeML;
        this.header = header;
        this.format = dataframe.formatMap.get(header);
    }

    private void acceptNumber(Number n) {
        count++;
        double o = n.doubleValue();
        sum += o;
        min = Math.min(min, o);
        max = Math.max(max, o);

        if (count == dataframe.size / 4) {
            median25 = o;
        }
        if (count == dataframe.size / 2) {
            median50 = o;
        }
        if (count == dataframe.size * 3 / 4) {
            median75 = o;
        }
    }

    public void combine(DataframeStatisticAccumulator n) {
        count += n.count;

        sum += n.sum;

        min = Math.min(min, n.min);
        max = Math.max(max, n.max);
    }
    double getMean() {
        return sum / count;
    }


    double getStd() {
        if (format == String.class) {
            return 0;
        }

        double mean = sum / count;
        double sum2 = dataframe.dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean).map(e -> e * e).sum();
        return Math.sqrt(sum2 / (count - 1));
    }

    double getCorrelation(String other) {
        if (format == String.class||dataframe.formatMap.get(other)==String.class) {
            return 0;
        }
        
        double mean = sum / count;
        List<Object> variable = dataframe.dataframe.get(header);
        double sum1 = variable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(e -> e - mean).map(e -> e * e).sum();
        double st1 = Math.sqrt(sum1 / (count - 1));
        
        List<Object> otherVariable = dataframe.dataframe.get(other);
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
        if (format.isInstance(o)) {
            if (format == Integer.class || format == Long.class || format == Double.class) {
                acceptNumber((Number) o);
            }
        }
        return this;
    }

    public int getCount() {
        return count;
    }


    public double getMin() {
        return min;
    }


    public double getMedian25() {
        return median25;
    }


    public double getMedian50() {
        return median50;
    }


    public double getMedian75() {
        return median75;
    }


    public double getMax() {
        return max;
    }


    public double getSum() {
        return sum;
    }

    
}