package ml.data;

import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class DataframeBuilder extends DataframeML {
	private DataframeML dataframeML;
	private String csvFile;

	DataframeBuilder(String csvFile) {
		this.csvFile = csvFile;
		dataframeML = new DataframeML();
	}

	public DataframeBuilder addCategory(String d) {
		dataframeML.categories.put(d, new HashSet<>());
		return this;
	}

	public DataframeBuilder addMapping(String d, UnaryOperator<Object> mapper) {
		dataframeML.mapping.put(d, mapper);
		return this;
	}

	public DataframeML build() {
		dataframeML.readCSV(csvFile);
		return dataframeML;
	}

    @Override
    public DataframeBuilder filter(String d, Predicate<Object> fil) {
		dataframeML.filters.put(d, fil);
		return this;
	}

	public DataframeBuilder setMaxSize(int maxSize) {
		dataframeML.maxSize = maxSize;
		return this;
	}


}