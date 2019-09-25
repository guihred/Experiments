package ml.data;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class DataframeML {

	private static final int FRAME_MAX_SIZE = Integer.MAX_VALUE;
	protected int maxSize = FRAME_MAX_SIZE;

	private final Map<String, List<Object>> dataframe = new LinkedHashMap<>();
	protected Map<String, Set<String>> categories = new LinkedHashMap<>();
	private Map<String, Class<? extends Comparable<?>>> formatMap = new LinkedHashMap<>();
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
		stats = new HashMap<>();
		if (frame.stats != null) {
			stats.putAll(frame.stats);
		}
		filters = new HashMap<>(frame.filters);
	}

	public void add(Map<String, Object> row) {
		row.forEach(this::add);
	}

	public void add(String header, Object obj) {
		List<Object> list = list(header);
		list.add(obj);
		size = Math.max(size, list.size());
	}

	public void addAll(Object... obj) {
		Collection<List<Object>> values = dataframe.values();
		int i = 0;
		for (Iterator<List<Object>> iterator = values.iterator(); iterator.hasNext();) {
			List<Object> list = iterator.next();
			list.add(obj[i]);
			i++;
		}
		size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
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
		size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
		return this;
	}

	public DataframeML filterString(String header, Predicate<String> v) {
		List<Object> list = dataframe.get(header);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (!v.test(Objects.toString(list.get(i)))) {
					int j = i;
					dataframe.forEach((c, l) -> l.remove(j));
					i--;
				}
			}
		}
		size = dataframe.values().stream().mapToInt(List<Object>::size).max().orElse(0);
		return this;
	}

	public void forEach(BiConsumer<String, List<Object>> action) {
		dataframe.forEach(action);
	}

	public void forEachRow(Consumer<Map<String, Object>> foreach) {
		for (int i = 0; i < size; i++) {
			foreach.accept(rowMap(i));
		}
	}

	public Set<Object> freeCategory(String header) {
		return new HashSet<>(dataframe.get(header));

	}

	public Map<String, List<Object>> getDataframe() {
		return dataframe;
	}

	public Class<? extends Comparable<?>> getFormat(String header) {
		return formatMap.get(header);
	}

	public Map<String, Class<? extends Comparable<?>>> getFormatMap() {
		return formatMap;
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

	@SuppressWarnings("unchecked")
	public <T> List<T> list(String header) {
		return (List<T>) dataframe.get(header);
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

	public Class<? extends Comparable<?>> putFormat(String header, Class<? extends Comparable<?>> value) {
		return formatMap.put(header, value);
	}

	public void removeCol(String... cols) {
		for (String string : cols) {
			dataframe.remove(string);
			formatMap.remove(string);
		}
	}

	public Map<String, Object> rowMap(int i) {
		return DataframeStatisticAccumulator.rowMap(dataframe, i);
	}

	public DoubleSummaryStatistics summary(String header) {
		if (!dataframe.containsKey(header)) {
			return new DoubleSummaryStatistics();
		}
		return list(header).stream().filter(Objects::nonNull).map(Number.class::cast).mapToDouble(Number::doubleValue)
				.summaryStatistics();
	}


}
