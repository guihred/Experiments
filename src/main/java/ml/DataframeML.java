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
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
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
        logln(x);
        // x.describe();
        // x.apply("median_house_value", d -> d / 1000);
        x.crossFeature("rooms_per_person", d -> d[0] / d[1], "total_rooms", "population");
        logln(x);
        // x.correlation();
    }

    private static void logln(Object x) {
        System.out.println(x);
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

    public void apply(String header, DoubleUnaryOperator mapper) {
        dataframe.put(header, dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(mapper).boxed().collect(Collectors.toList()));
        formatMap.put(header, Double.class);
    }

    public void crossFeature(String header, ToDoubleFunction<double[]> mapper, String... dependent) {
        dataframe.put(header, IntStream.range(0, size).mapToObj(i -> toDoubleArray(i, dependent)).mapToDouble(mapper)
                .boxed().collect(Collectors.toList()));
        formatMap.put(header, Double.class);
    }

    private double[] toDoubleArray(int i, String... dependent) {
        double[] d = new double[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = ((Number) dataframe.get(dependent[j]).get(i)).doubleValue();
        }
        return d;
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
            getLogger().trace("FORMAT ERROR INTEGER", e);
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
                                () -> new DataframeStatisticAccumulator(this, e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1, LinkedHashMap::new));
        
        collect.forEach((k, v) -> log("\t%s", k));
        log("\ncount");
        collect.forEach((k, v) -> log("\t%" + k.length() + "d", v.getCount()));
        log("\nmean");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMean()));
        log("\nstd");
        collect.forEach((k, v) -> log(floatFormating(k), v.getStd()));
        log("\nmin");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMin()));
        log("\n25%%");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMedian25()));
        log("\n50%%");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMedian50()));
        log("\n75%%");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMedian75()));
        log("\nmax");
        collect.forEach((k, v) -> log(floatFormating(k), v.getMax()));
        logln();
        
    }

    void log(String s, Object... e) {
        System.out.printf(s, e);
    }

    void logln() {
        System.out.println();
    }

    public void correlation() {
        Map<String, DataframeStatisticAccumulator> collect = dataframe.entrySet().stream()
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
                        e -> e.getValue().stream().collect(() -> new DataframeStatisticAccumulator(this, e.getKey()),
                                DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                        (m1, m2) -> m1, LinkedHashMap::new));

        logln();
        Set<String> keySet = formatMap.keySet();
        int pad = keySet.stream().mapToInt(String::length).max().getAsInt();
        log("\t\t");
        keySet.forEach(k -> log("\t%s", k));
        logln();
        for (String variable : keySet) {
            log("%" + pad + "s", variable);
            double self = collect.get(variable).getCorrelation(variable);
            for (String variable2 : keySet) {
                log(floatFormating(variable2.length()), collect.get(variable).getCorrelation(variable2) / self);
            }
            logln();

        }

    }

    private String floatFormating(String k) {
        int length = k.length();
        return floatFormating(length);
    }

    private String floatFormating(int length) {
        return "\t%" + length + ".1f";
    }


}



