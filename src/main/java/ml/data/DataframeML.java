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

	public DataframeML(final String csvFile) {
		readCSV(csvFile);
	}

	public void add(final String header, final Object obj) {
		List<Object> list = list(header);
		list.add(obj);
		size=Math.max(size, list.size());
	}

    public <T extends Comparable<?>> void addCols(final List<String> cols, final Class<T> classes) {
        for (String string : cols) {
            dataframe.put(string, new ArrayList<>());
            formatMap.put(string, classes);
        }
    }
	public <T extends Comparable<?>> void addCols(final String string, final Class<T> classes) {
        dataframe.put(string, new ArrayList<>());
        formatMap.put(string, classes);
    }

	public void apply(final String header, final DoubleUnaryOperator mapper) {
		dataframe.put(header, dataframe.get(header).stream().map(Number.class::cast).mapToDouble(Number::doubleValue)
				.map(mapper).boxed().collect(Collectors.toList()));
		formatMap.put(header, Double.class);
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<String> categorize(final String header) {
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

	public List<Entry<Number, Number>> createNumberEntries(final String feature, final String target) {
		List<Object> list = dataframe.get(feature);
		List<Object> list2 = dataframe.get(target);
		List<Entry<Number, Number>> data = new ArrayList<>();
		IntStream.range(0, size).filter(i -> list.get(i) != null && list2.get(i) != null).forEach(
				(final int i) -> data.add(new AbstractMap.SimpleEntry<>((Number) list.get(i), (Number) list2.get(i))));
		return data;
	}

	public List<Double> crossFeature(final String header, final ToDoubleFunction<double[]> mapper, final String... dependent) {
		return DataframeUtils.crossFeature(this, header, mapper, dependent);
	}

	public List<Double> crossFeatureObject(final String header, final ToDoubleFunction<Object[]> mapper, final String... dependent) {
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

	public void filterString(final String header, final Predicate<String> v) {
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

	public void forEach(final BiConsumer<String, List<Object>> action) {
		dataframe.forEach(action);
	}

	public Class<? extends Comparable<?>> getFormat(final String header) {
		return formatMap.get(header);
	}

	public int getSize() {
		return size;
	}

	public Map<String, Long> histogram(final String header) {
		List<Object> list = dataframe.get(header);
		List<String> stringList = list.stream().filter(Objects::nonNull).map(String.class::cast)
				.collect(Collectors.toList());
		return stringList.parallelStream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

	}

	public Map<Double, Long> histogram(final String header, final int bins) {
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

	public List<Object> list(final String header) {
		return dataframe.get(header);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> list(final String header, final Class<T> c) {
		return (List) dataframe.get(header);
	}

	public void map(final String header, final UnaryOperator<Object> mapper) {
		dataframe.put(header, dataframe.get(header).stream().map(mapper).collect(Collectors.toList()));
	}

	public void only(final String header, final Predicate<String> v, final IntConsumer cons) {
		List<Object> list = dataframe.get(header);
		for (int i = 0; i < list.size(); i++) {
			if (v.test(Objects.toString(list.get(i)))) {
				cons.accept(i);
			}
		}
	}

	public void readCSV(final String csvFile) {
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


	public Map<String, Object> rowMap(final int i) {
        return dataframe.entrySet().stream().filter(e -> e.getValue().get(i) != null)
            .collect(Collectors.toMap(Entry<String, List<Object>>::getKey, e -> e.getValue().get(i),
                DataframeUtils.throwError(),
                LinkedHashMap<String, Object>::new));
	}

    public DoubleSummaryStatistics summary(final String header) {
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

	public void trim(final String header, final int trimmingSize) {
		DataframeUtils.trim(header, trimmingSize, this);
	}

	@SuppressWarnings({ "unchecked" })
	public <T> List<T> typedList(final List<Object> list, final Class<T> c) {
		return (List<T>) list;
	}


	public static DataframeBuilder builder(final String csvFile) {
		return new DataframeBuilder(csvFile);
	}
}
