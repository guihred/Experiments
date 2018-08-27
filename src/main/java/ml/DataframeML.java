package ml;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public class DataframeML implements HasLogging {

    private static final int FRAME_MAX_SIZE = Integer.MAX_VALUE;
    private int maxSize = FRAME_MAX_SIZE;
    private static final List<Class<?>> formatHierarchy = Arrays.asList(String.class, Integer.class, Long.class,
			Double.class);

    public static void main(String[] args) {
		DataframeML x = new DataframeML("POPULACAO.csv");
		x.describe();
    }

    public static DataframeBuilder builder(String csvFile) {
        return new DataframeBuilder(csvFile);
    }

    private Map<String, List<Object>> dataframe = new LinkedHashMap<>();
	protected Map<String, Set<String>> categories = new LinkedHashMap<>();
    private Map<String, Class<?>> formatMap = new LinkedHashMap<>();
	protected Map<String, Function<Object, Object>> mapping = new LinkedHashMap<>();
	private int size;

    private Map<String, DataframeStatisticAccumulator> stats;
	protected Map<String, Predicate<Object>> filters = new HashMap<>();

    public DataframeML() {
    }

    public DataframeML(String csvFile) {
        readCSV(csvFile);
    }

    public static class DataframeBuilder {
        private DataframeML dataframeML;
        private String csvFile;

        private DataframeBuilder(String csvFile) {
            this.csvFile = csvFile;
            dataframeML = new DataframeML();
        }

        public DataframeBuilder filter(String d, Predicate<Object> fil) {
            dataframeML.filters.put(d, fil);
            return this;
        }

        public DataframeBuilder categorize(String d) {
            dataframeML.categories.put(d, new HashSet<>());
            return this;
        }

        public DataframeBuilder map(String d, Function<Object, Object> mapping) {
            dataframeML.mapping.put(d, mapping);
            return this;
        }

        public DataframeBuilder setMaxSize(int maxSize) {
            dataframeML.maxSize = maxSize;
            return this;
        }

        public DataframeML build() {
            dataframeML.readCSV(csvFile);
            return dataframeML;
        }
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

	public void trim(String header, int trimmingSize) {
		List<Object> list = dataframe.get(header);
		List<List<Object>> collect = dataframe.entrySet().stream().filter(e -> !e.getKey().equals(header))
                .map(Entry<String, List<Object>>::getValue).collect(Collectors.toList());

		Class<?> class1 = formatMap.get(header);
		if (class1 == String.class) {
			QuickSortML.sort(typedList(list, String.class), (i, j) -> {
				for (List<Object> list2 : collect) {
					Object object = list2.get(i);
					list2.set(i, list2.get(j));
					list2.set(j, object);
				}
			}, String::compareTo);
		}
		List<Entry<String, List<Object>>> entrySet = dataframe.entrySet().stream().collect(Collectors.toList());
		for (int i = 0; i < entrySet.size(); i++) {
			Entry<String, List<Object>> entry = entrySet.get(i);
			List<Object> value = entry.getValue();
			dataframe.put(entry.getKey(), value.subList(trimmingSize, value.size() - trimmingSize - 1));
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private <T> List<T> typedList(List<Object> list, Class<T> c) {
		return (List<T>) list;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Double> crossFeatureObject(String header, ToDoubleFunction<Object[]> mapper, String... dependent) {
		List<Double> collect = IntStream.range(0, size).mapToObj(i -> toArray(i, dependent)).mapToDouble(mapper).boxed()
				.collect(Collectors.toList());
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
        if (StringUtils.isBlank(s)) {
            return "%s\t";
        }
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

    public List<Object> list(String header) {
		return dataframe.get(header);
	}

    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    public <T> List<T> list(String header, Class<T> c) {
        return (List) dataframe.get(header);
	}

    public List<Object> row(int i) {
        return dataframe.values().stream().map(e -> e.get(i)).collect(Collectors.toList());
    }

    public Map<String, Object> rowMap(int i) {
        return dataframe.entrySet().stream()
                .filter(e -> e.getValue().get(i) != null)
                .collect(Collectors.toMap(Entry<String, List<Object>>::getKey, e -> e.getValue().get(i)));
    }

    public void log(String s, Object... e) {
        getLogger().info(s, e);
    }

    public void logln() {
        getLogger().info("");
    }
    public final void readCSV(String csvFile) {
        try (Scanner scanner = new Scanner(ResourceFXUtils.toFile(csvFile));) {
            List<String> header = CSVUtils.parseLine(scanner.nextLine()).stream().map(e -> e.replaceAll("\"", ""))
                    .collect(Collectors.toList());
            for (String column : header) {
                dataframe.put(column, new ArrayList<>());
                formatMap.put(column, String.class);
            }

            readRows(scanner, header);
        } catch (FileNotFoundException e) {
            getLogger().error("FILE NOT FOUND", e);
        }
    }

    private void readRows(Scanner scanner, List<String> header) {
        while (scanner.hasNext()) {
            size++;
            List<String> line2 = CSVUtils.parseLine(scanner.nextLine());
            if (header.size() != line2.size()) {
                getLogger().error("ERROR FIELDS COUNT");
                createNullRow(header, line2);
            }

            for (int i = 0; i < header.size(); i++) {
                String key = header.get(i);
                String field = getFromList(i, line2);
                Object tryNumber = tryNumber(key, field);
                if (filters.containsKey(key) && !filters.get(key).test(tryNumber)) {
                    removeRow(header, i);
                    break;
                }
                categorizeIfCategorizable(key, tryNumber);
                tryNumber = mapIfMappable(key, tryNumber);

                dataframe.get(key).add(tryNumber);
            }
            if (size > maxSize) {
                break;
            }
        }
    }

    private void createNullRow(List<String> header, List<String> line2) {
        if (line2.size() < header.size()) {
            line2.addAll(Stream.generate(() -> "").limit(header.size() - line2.size())
                    .collect(Collectors.toList()));
        }
    }

    private void removeRow(List<String> header, int i) {
        for (int j = 0; j < i; j++) {
            String key2 = header.get(j);
            List<Object> list = dataframe.get(key2);
            Object remove = list.remove(list.size() - 1);
            if (categories.containsKey(key2)) {
                categories.get(key2).remove(remove);
            }
        }
        size--;
    }

    private void categorizeIfCategorizable(String key, Object tryNumber) {
        if (categories.containsKey(key)) {
            Set<String> set = categories.get(key);
            String string = Objects.toString(tryNumber);
            if (!set.contains(string)) {
                set.add(string);
            }
        }
    }

    private Object mapIfMappable(String key, Object tryNumber) {
        if (mapping.containsKey(key)) {
            tryNumber = mapping.get(key).apply(tryNumber);
        }
        return tryNumber;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static <T> T getFromList(int j, List<T> list) {
        return list != null && j < list.size() ? list.get(j) : null;
    }

    public DoubleSummaryStatistics summary(String header) {
        if (!dataframe.containsKey(header)) {
            return new DoubleSummaryStatistics();
        }
        return list(header).stream().filter(Objects::nonNull).map(Number.class::cast)
                .mapToDouble(Number::doubleValue).summaryStatistics();
    }

    private double[] toDoubleArray(int i, String... dependent) {
        double[] d = new double[dependent.length];
        for (int j = 0; j < dependent.length; j++) {
            d[j] = ((Number) dataframe.get(dependent[j]).get(i)).doubleValue();
        }
        return d;
    }

	private Object[] toArray(int i, String... dependent) {
		Object[] d = new Object[dependent.length];
		for (int j = 0; j < dependent.length; j++) {
			d[j] = dataframe.get(dependent[j]).get(i);
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
                    String string = Objects.toString(l.get(j), "");
                    if (string.length() > s.length() + 3) {
                        string = string.substring(0, s.length() + 3);
                    }
                    str.append(String.format(formating(s), string));
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
        if (StringUtils.isBlank(field)) {
            return null;
        }
        String number = field;
        Class<?> currentFormat = formatMap.get(header);
        if (currentFormat == String.class && size > 1
                && list(header).stream().anyMatch(String.class::isInstance)) {
            return field;
        }

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
        if (Number.class.isAssignableFrom(formatMap.get(header))) {
            formatMap.put(header, String.class);
            map(header, e -> Objects.toString(e, ""));
        }
        
        return number;
    }

    public Class<?> getFormat(String header) {
        return formatMap.get(header);
    }

}



