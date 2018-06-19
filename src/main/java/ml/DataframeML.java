package ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import simplebuilder.HasLogging;

public class DataframeML implements HasLogging {

	private static final int FRAME_MAX_SIZE = Integer.MAX_VALUE;
    private static final List<Class<?>> formatHierarchy = Arrays.asList(String.class, Integer.class, Long.class,
			Double.class);

    public static void main(String[] args) {
        // DataframeML x = new DataframeML("california_housing_train.csv")
        DataframeML x = new DataframeML("POPULACAO.csv");
        x.logln(x);
        // x.describe()
		// x.filterString("Flag Codes", "B"::equalsIgnoreCase);
        x.logln(x);
        // x.correlation()
    }
	Map<String, List<Object>> dataframe = new LinkedHashMap<>();
    Map<String, Class<?>> formatMap = new LinkedHashMap<>();
	private int size;

    private Map<String, DataframeStatisticAccumulator> stats;

    public DataframeML() {
    }

    public DataframeML(String csvFile) {
        readCSV(csvFile);
    }

    public void apply(String header, DoubleUnaryOperator mapper) {
        dataframe.put(header, dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
                .map(mapper).boxed().collect(Collectors.toList()));
        formatMap.put(header, Double.class);
    }

	public void map(String header, Function<Object, Object> mapper) {
		dataframe.put(header, dataframe.get(header).stream().map(mapper).collect(Collectors.toList()));
	}

    public Set<String> cols() {
        return dataframe.keySet();
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

    public List<Entry<Number, Number>> createNumberEntries(String feature, String target) {
	    List<Object> list = dataframe.get(feature);
	    List<Object> list2 = dataframe.get(target);
	    List<Entry<Number, Number>> data = new ArrayList<>();
	    IntStream.range(0, size)
	    .filter(i -> list.get(i) != null && list2.get(i) != null)
	    .forEach((int i) -> data.add(new AbstractMap.SimpleEntry<>((Number) list.get(i), (Number) list2.get(i))));
        return data;
	}

    @SuppressWarnings("unchecked")
	public ObservableList<Series<Number, Number>> createNumberSeries(String feature, String target) {
		Series<Number, Number> series = new Series<>();
		series.setName(feature + " X " + target);

		List<Object> list = dataframe.get(feature);
		List<Object> list2 = dataframe.get(target);
		ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
		IntStream.range(0, size)
				.filter(i -> list.get(i) != null && list2.get(i) != null)
				.forEach((int i) -> data.add(new Data<>((Number) list.get(i), (Number) list2.get(i))));
		series.setData(data);
		return FXCollections.observableArrayList(series);
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Double> crossFeature(String header, ToDoubleFunction<double[]> mapper, String... dependent) {
        List<Double> collect = IntStream.range(0, size).mapToObj(i -> toDoubleArray(i, dependent)).mapToDouble(mapper)
                .boxed().collect(Collectors.toList());
        dataframe.put(header, (List) collect);
        formatMap.put(header, Double.class);
        return collect;
    }

    public void describe() {
        if (stats == null) {
            stats = dataframe.entrySet().stream()
                    .collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
                            e -> e.getValue().stream().collect(
                                    () -> new DataframeStatisticAccumulator(this, e.getKey()),
                                    DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
                            (m1, m2) -> m1, LinkedHashMap::new));
        }
        
        stats.forEach((k, v) -> log("\t%s", k));
        log("\ncount");
        stats.forEach((k, v) -> log("\t%" + k.length() + "d", v.getCount()));
        log("\nmean");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMean()));
        log("\nstd");
        stats.forEach((k, v) -> log(floatFormating(k), v.getStd()));
        log("\nmin");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMin()));
        log("\n25%%");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMedian25()));
        log("\n50%%");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMedian50()));
        log("\n75%%");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMedian75()));
        log("\nmax");
        stats.forEach((k, v) -> log(floatFormating(k), v.getMax()));
        logln();
        
    }

    public void filterString(String header, Predicate<String> v) {
        List<Object> list = dataframe.get(header);
        for (int i = 0; i < list.size(); i++) {
            if (!v.test(Objects.toString(list.get(i)))) {
                int j = i;
                dataframe.forEach((c, l) -> l.remove(j));
                i--;
                size--;
            }
        }
    }

    public void only(String header, Predicate<String> v, IntConsumer cons) {
        List<Object> list = dataframe.get(header);
        for (int i = 0; i < list.size(); i++) {
            if (v.test(Objects.toString(list.get(i)))) {
                cons.accept(i);
            }
        }
    }

    private String floatFormating(int length) {
        return "\t%" + length + ".1f";
    }
	private String floatFormating(String k) {
        int length = k.length();
        return floatFormating(length);
    }

    public void forEach(BiConsumer<String, List<Object>> action) {
        dataframe.forEach(action);
    }

	private String formating(String s) {
        return "%" + s.length() + "s\t";
    }
    public int getSize() {
        return size;
    }

    public Map<Double, Long> histogram(String header, int bins) {
        List<Object> list = dataframe.get(header);
        List<Double> collect = list.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).boxed()
                .collect(Collectors.toList());
        DoubleSummaryStatistics summaryStatistics = collect.stream().mapToDouble(e -> e).summaryStatistics();
        double min = summaryStatistics.getMin();
        double max = summaryStatistics.getMax();
        double binSize = (max - min) / bins;
        return collect.parallelStream()
                .collect(Collectors.groupingBy(e -> Math.floor(e / binSize) * binSize, Collectors.counting()));

    }

    List<Object> list(String header) {
		return dataframe.get(header);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	<T> List<T> list(String header, Class<T> c) {
		List list = dataframe.get(header);
		return Collections.checkedList(list, c);
	}

    List<Object> row(int i) {
        return dataframe.values().stream().map(e -> e.get(i)).collect(Collectors.toList());
    }

    void log(String s, Object... e) {
        System.out.printf(s, e);
    }

    void logln() {
        System.out.println();
    }

    public void readCSV(String csvFile) {
        try (Scanner scanner = new Scanner(new File(csvFile));) {
            List<String> header = CSVUtils.parseLine(scanner.nextLine()).stream().map(e -> e.replaceAll("\"", ""))
                    .collect(Collectors.toList());
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
                if (size > FRAME_MAX_SIZE) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            getLogger().error("FILE NOT FOUND", e);
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public DoubleSummaryStatistics summary(String header) {
        return list(header).stream().map(Number.class::cast)
                .mapToDouble(Number::doubleValue).summaryStatistics();
    }

    private double[] toDoubleArray(int i, String... dependent) {
        double[] d = new double[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = ((Number) dataframe.get(dependent[j]).get(i)).doubleValue();
        }
        return d;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        dataframe.forEach((s, l) -> str.append(s + "\t"));
        str.append("\n");
        formatMap.forEach((s, l) -> str.append(String.format(formating(s), l.getSimpleName())));
        str.append("\n");
        for (int i = 0; i < 10; i++) {
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


}



