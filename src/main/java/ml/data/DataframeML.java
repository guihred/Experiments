package ml.data;
import static ml.data.DataframeUtils.displayCorrelation;
import static ml.data.DataframeUtils.displayStats;
import static ml.data.DataframeUtils.readRows;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class DataframeML implements HasLogging {

	private static final int FRAME_MAX_SIZE = Integer.MAX_VALUE;
	protected int maxSize = FRAME_MAX_SIZE;

	protected Map<String, List<Object>> dataframe = new LinkedHashMap<>();
	protected Map<String, Set<String>> categories = new LinkedHashMap<>();
	protected Map<String, Class<? extends Comparable<?>>> formatMap = new LinkedHashMap<>();
	protected Map<String, Function<Object, Object>> mapping = new LinkedHashMap<>();
	protected int size;
	protected Map<String, DataframeStatisticAccumulator> stats;
	protected Map<String, Predicate<Object>> filters = new HashMap<>();

	public DataframeML() {
	}

    public DataframeML(DataframeML frame) {
        frame.dataframe.forEach((h, l) -> dataframe.put(h, new ArrayList<>(l)));
        formatMap = new LinkedHashMap<>(frame.formatMap);
        mapping = new LinkedHashMap<>(frame.mapping);
        size = frame.size;
        stats = frame.stats;
        filters = new HashMap<>(frame.filters);
    }

	public DataframeML(String csvFile) {
		readCSV(csvFile);
	}

	public void add(Map<String, Object> row) {
        row.forEach(this::add);
    }

    public void add(String header, Object obj) {
		List<Object> list = list(header);
		list.add(obj);
		size=Math.max(size, list.size());
	}

	public void addAll(Object... obj) {
        Collection<List<Object>> values = dataframe.values();
        int i = 0;
        for (Iterator<List<Object>> iterator = values.iterator(); iterator.hasNext();) {
            List<Object> list = iterator.next();
            list.add(obj[i]);
            i++;
        }
        size = dataframe.values().stream().mapToInt(e -> e.size()).max().orElse(0);
    }

	public <T extends Comparable<?>> void addCols(List<String> cols, Class<T> classes) {
        for (String string : cols) {
            dataframe.put(string, new ArrayList<>());
            formatMap.put(string, classes);
        }
    }

	public <T extends Comparable<?>> void addCols(String string, Class<T> classes) {
        dataframe.put(string, new ArrayList<>());
        formatMap.put(string, classes);
    }

    public void apply(String header, DoubleUnaryOperator mapper) {
		dataframe.put(header, dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
				.map(mapper).boxed().collect(Collectors.toList()));
		formatMap.put(header, Double.class);
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

	public Set<String> cols() {
		return dataframe.keySet();
	}

	public void correlation() {
        displayCorrelation(this);
	}

	public List<Entry<Number, Number>> createNumberEntries(String feature, String target) {
		List<Object> list = dataframe.get(feature);
		List<Object> list2 = dataframe.get(target);
		List<Entry<Number, Number>> data = new ArrayList<>();
		IntStream.range(0, size).filter(i -> list.get(i) != null && list2.get(i) != null).forEach(
            i -> data.add(new AbstractMap.SimpleEntry<>((Number) list.get(i), (Number) list2.get(i))));
		return data;
	}

	public List<Double> crossFeature(String header, ToDoubleFunction<double[]> mapper, String... dependent) {
		return DataframeUtils.crossFeature(this, header, mapper, dependent);
	}

	public List<Double> crossFeatureObject(String header, ToDoubleFunction<Object[]> mapper, String... dependent) {
		return DataframeUtils.crossFeatureObject(this, header, mapper, dependent);
	}

    public void describe() {
		if (stats == null) {
			stats = dataframe.entrySet().stream().collect(Collectors.toMap(Entry<String, List<Object>>::getKey,
					e -> e.getValue().stream().collect(() -> new DataframeStatisticAccumulator(this, e.getKey()),
							DataframeStatisticAccumulator::accept, DataframeStatisticAccumulator::combine),
					(m1, m2) -> m1, LinkedHashMap::new));
		}
        displayStats(stats);
	}

    public DataframeML filter(String header, Predicate<Object> v) {
        List<Object> list = dataframe.get(header);
        for (int i = 0; i < list.size(); i++) {
            Object t = list.get(i);
			if (t == null || !v.test(t)) {
                int j = i;
                dataframe.forEach((c, l) -> l.remove(j));
                i--;
            }
        }
        size = dataframe.values().stream().mapToInt(e -> e.size()).max().orElse(0);
        return this;
    }

	public DataframeML filterString(String header, Predicate<String> v) {
		List<Object> list = dataframe.get(header);
		for (int i = 0; i < list.size(); i++) {
			if (!v.test(Objects.toString(list.get(i)))) {
				int j = i;
				dataframe.forEach((c, l) -> l.remove(j));
				i--;
			}
		}
        size = dataframe.values().stream().mapToInt(e -> e.size()).max().orElse(0);
        return this;
	}

	public void forEach(BiConsumer<String, List<Object>> action) {
		dataframe.forEach(action);
	}

	public Set<Object> freeCategory(String header) {
    	return new HashSet<>(dataframe.get(header));
    	
    }

	public Class<? extends Comparable<?>> getFormat(String header) {
		return formatMap.get(header);
	}

	public int getSize() {
		return size;
	}

	public Map<String, Long> histogram(String header) {
		List<Object> list = dataframe.get(header);
		List<String> stringList = list.stream().filter(Objects::nonNull).map(Objects::toString)
				.collect(Collectors.toList());
		return stringList.parallelStream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

	}

	public Map<Double, Long> histogram(String header, int bins) {
		List<Object> list = dataframe.get(header);
		List<Double> columnList = list.stream().map(Number.class::cast).mapToDouble(Number::doubleValue).boxed()
				.collect(Collectors.toList());
		DoubleSummaryStatistics summaryStatistics = columnList.stream().mapToDouble(e -> e).summaryStatistics();
		double min = summaryStatistics.getMin();
		double max = summaryStatistics.getMax();
		double binSize = (max - min) / bins;
		return columnList.parallelStream()
				.collect(Collectors.groupingBy(e -> Math.ceil(e / binSize) * binSize, Collectors.counting()));

	}

	public List<Object> list(String header) {
		return dataframe.get(header);
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public <T> List<T> list(String header, Class<T> c) {
		return (List) dataframe.get(header);
	}

	public void map(String header, UnaryOperator<Object> mapper) {
		dataframe.put(header, dataframe.get(header).stream().map(mapper).collect(Collectors.toList()));
	}

	public void only(String header, Predicate<String> v, IntConsumer cons) {
		List<Object> list = dataframe.get(header);
		for (int i = 0; i < list.size(); i++) {
			if (v.test(Objects.toString(list.get(i)))) {
				cons.accept(i);
			}
		}
	}


    public void readCSV(String csvFile) {
		try (Scanner scanner = new Scanner(ResourceFXUtils.toFile(csvFile), StandardCharsets.UTF_8.displayName())) {
			List<String> header = CSVUtils.parseLine(scanner.nextLine()).stream().map(e -> e.replaceAll("\"", ""))
					.collect(Collectors.toList());
			for (String column : header) {
				dataframe.put(column, new ArrayList<>());
				formatMap.put(column, String.class);
			}

            readRows(this, scanner, header);
		} catch (FileNotFoundException e) {
			getLogger().error("FILE NOT FOUND", e);
		}
	}

	public void removeCol(String... cols) {
        for (String string : cols) {
            dataframe.remove(string);
            formatMap.remove(string);
        }
	}

	public Map<String, Object> rowMap(int i) {
        return dataframe.entrySet().stream().filter(e -> e.getValue().get(i) != null)
            .collect(Collectors.toMap(Entry<String, List<Object>>::getKey, e -> e.getValue().get(i),
                DataframeUtils.throwError(),
                LinkedHashMap<String, Object>::new));
	}

	public DoubleSummaryStatistics summary(String header) {
		if (!dataframe.containsKey(header)) {
			return new DoubleSummaryStatistics();
		}
		return list(header).stream().filter(Objects::nonNull).map(Number.class::cast).mapToDouble(Number::doubleValue)
				.summaryStatistics();
	}

	@Override
	public String toString() {
		return DataframeUtils.toString(this);
	}


	public void trim(String header, int trimmingSize) {
		DataframeUtils.trim(header, trimmingSize, this);
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public <T> List<T> typedList(List<Object> list, Class<T> c) {
		return (List<T>) list;
	}

    public static DataframeBuilder builder(String csvFile) {
		return new DataframeBuilder(csvFile);
	}
}
