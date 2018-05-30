package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import simplebuilder.HasLogging;

public class DataframeML implements HasLogging {

    Map<String, List<Object>> dataframe = new LinkedHashMap<>();
    Map<String, Class<?>> formatMap = new LinkedHashMap<>();
    int size;
    List<Class<?>> formatHierarchy = Arrays.asList(String.class, Integer.class, Long.class, Double.class);
    public static void main(String[] args) {
        DataframeML x = new DataframeML("california_housing_train.csv");
        System.out.println(x);
        x.correlation();
    }

    public DataframeML() {
    }

    public DataframeML(String csvFile) {
        readCSV(csvFile);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        dataframe.forEach((s, l) -> str.append(s + "\t"));
        str.append("\n");
        formatMap.forEach((s, l) -> str.append(String.format(formating(s), l.getSimpleName())));
        str.append("\n");
        for (int i = 0; i < 5; i++) {
            int j = i;
            dataframe.forEach((s, l) -> {
                if (l.size() > j) {
                    str.append(String.format(formating(s), Objects.toString(l.get(j))));
                }
            });
            str.append("\n");
        }
        if (size > 5) {
            str.append("...\n");
        }
        str.append("Size=" + size + " \n");

        return str.toString();
    }

    private String formating(String s) {
        return "%" + s.length() + "s\t";
    }
    
    

    public void readCSV(String csvFile) {
        try (Scanner scanner = new Scanner(new File(csvFile));) {
            List<String> header = CSVUtils.parseLine(scanner.nextLine());
            for (String column : header) {
                dataframe.put(column, new ArrayList<>());
                formatMap.put(column, String.class);
            }

            while (scanner.hasNext()) {
                size++;
                List<String> line2 = CSVUtils.parseLine(scanner.nextLine());
                if (header.size() != line2.size()) {
                    getLogger().error("ERROR FIELDS COUNT");
                }

                for (int i = 0; i < header.size() && i < line2.size(); i++) {
                    dataframe.get(header.get(i)).add(tryNumber(header.get(i), line2.get(i)));
                }
            }
        } catch (FileNotFoundException e) {
            getLogger().error("FILE NOT FOUND", e);
        }
    }

    private Object tryNumber(String header, String field) {
        String number = field;
        Class<?> currentFormat = formatMap.get(header);
        if (field.matches("\\d+\\.0+$") && currentFormat != Double.class) {
            number = field.replaceAll("\\.0+", "");
        }

        try {
            if (formatHierarchy.indexOf(currentFormat) <= formatHierarchy.indexOf(Integer.class)) {
                Integer valueOf = Integer.valueOf(number);
                if (currentFormat != Integer.class) {
                    formatMap.put(header, Integer.class);
                }
                return valueOf;
            }
        } catch (NumberFormatException e) {
            getLogger().trace("FORMAT ERROR", e);
        }
        try {
            if (formatHierarchy.indexOf(currentFormat) <= formatHierarchy.indexOf(Long.class)) {
                Long valueOf = Long.valueOf(number);
                if (currentFormat != Long.class) {
                    formatMap.put(header, Long.class);
                }
                return valueOf;
            }
        } catch (NumberFormatException e) {
            getLogger().trace("FORMAT ERROR", e);
        }
        try {
            if (formatHierarchy.indexOf(currentFormat) <= formatHierarchy.indexOf(Double.class)) {
                Double valueOf = Double.valueOf(number);
                if (currentFormat != Double.class) {
                    formatMap.put(header, Double.class);
                }
                return valueOf;
            }
        } catch (NumberFormatException e) {
            getLogger().trace("FORMAT ERROR", e);
        }
        
        return number;
    }

    public void describe() {
        Map<String, DataframeStatisticAccumulator> collect = dataframe.entrySet().stream()
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
                        e -> e.getValue().stream().collect(
                                () -> new DataframeStatisticAccumulator(e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1, LinkedHashMap::new));
        
        collect.forEach((k, v) -> System.out.printf("\t%s", k));
        System.out.print("\ncount");
        collect.forEach((k, v) -> System.out.printf("\t%" + k.length() + "d", v.getCount()));
        System.out.print("\nmean");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMean()));
        System.out.print("\nstd");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getStd()));
        System.out.print("\nmin");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMin()));
        System.out.print("\n25%");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMedian25()));
        System.out.print("\n50%");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMedian50()));
        System.out.print("\n75%");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMedian75()));
        System.out.print("\nmax");
        collect.forEach((k, v) -> System.out.printf(floatFormating(k), v.getMax()));
        
    }

    public void correlation() {
        Map<String, DataframeStatisticAccumulator> collect = dataframe.entrySet().stream()
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
                        e -> e.getValue().stream().collect(() -> new DataframeStatisticAccumulator(e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1, LinkedHashMap::new));

        Set<String> keySet = formatMap.keySet();
        int pad = keySet.stream().mapToInt(String::length).max().getAsInt();
        System.out.printf("\t");
        keySet.forEach(k -> System.out.printf("\t%" + pad + "s", k));
        System.out.println();
        for (String variable : keySet) {
            System.out.print(variable);
            double self = collect.get(variable).getCorrelation(variable);
            for (String variable2 : keySet) {
                System.out.printf(floatFormating(pad), collect.get(variable).getCorrelation(variable2) / self);
            }
            System.out.println();

        }

    }

    private String floatFormating(String k) {
        int length = k.length();
        return floatFormating(length);
    }

    private String floatFormating(int length) {
        return "\t%" + length + ".1f";
    }
    
    
    public class DataframeStatisticAccumulator{
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

        public DataframeStatisticAccumulator(String header) {
            this.header = header;
            this.format = formatMap.get(header);
        }

        private void acceptNumber(Number n) {
            count++;
            double o = n.doubleValue();
            sum += o;
            min = Math.min(min, o);
            max = Math.max(max, o);

            if (count == DataframeML.this.size / 4) {
                median25 = o;
            }
            if (count == DataframeML.this.size / 2) {
                median50 = o;
            }
            if (count == DataframeML.this.size * 3 / 4) {
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
            double sum2 = dataframe.get(header).stream().map(Number.class::cast).mapToDouble(e -> e.doubleValue())
                    .map(e -> e - mean).map(e -> e * e).sum();
            return Math.sqrt(sum2 / (count - 1));
        }

        double getCorrelation(String other) {
            if (format == String.class||formatMap.get(other)==String.class) {
                return 0;
            }
            
            double mean = sum / count;
            List<Object> variable = dataframe.get(header);
            double sum1 = variable.stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                    .map(e -> e - mean).map(e -> e * e).sum();
            double st1 = Math.sqrt(sum1 / (count - 1));
            
            List<Object> otherVariable = dataframe.get(other);
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
}



