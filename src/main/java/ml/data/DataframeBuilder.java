package ml.data;

import extract.ExcelService;
import java.io.File;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import utils.ResourceFXUtils;

public class DataframeBuilder extends DataframeML {
	private DataframeML dataframeML;
    private File csvFile;

    protected DataframeBuilder(File csvFile) {
	    this.csvFile = csvFile;
	    dataframeML = new DataframeML();
	}

    protected DataframeBuilder(String csvFile) {
        this.csvFile = ResourceFXUtils.toFile(csvFile);
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
		DataframeUtils.readCSV(csvFile, dataframeML);
		return dataframeML;
	}

    @Override
    public DataframeBuilder filter(String d, Predicate<Object> fil) {
		dataframeML.filters.put(d, fil);
		return this;
	}

    public DataframeML makeStats() {
        DataframeUtils.makeStats(csvFile, dataframeML, new SimpleDoubleProperty());
        return dataframeML;
	}

    public DataframeML makeStats(DoubleProperty progress) {
        DataframeUtils.makeStats(csvFile, dataframeML, progress);
        return dataframeML;
    }

	public DataframeBuilder setMaxSize(int maxSize) {
		dataframeML.maxSize = maxSize;
		return this;
	}

	public static DataframeML build(File csvFile) {
		DataframeML dataframeML = new DataframeML();
        if (!csvFile.exists()) {
            return dataframeML;
        }
        if (ExcelService.isExcel(csvFile)) {
            return ExcelDataReader.readExcel(dataframeML, csvFile);
        }
        return DataframeUtils.readCSV(csvFile, dataframeML);
	}
	public static DataframeML build(String csvFile) {
		DataframeML dataframeML = new DataframeML();
		DataframeUtils.readCSV(csvFile, dataframeML);
		return dataframeML;
	}

	public static DataframeBuilder builder(File csvFile) {
	    return new DataframeBuilder(csvFile);
	}

    public static DataframeBuilder builder(String csvFile) {
	    return new DataframeBuilder(csvFile);
	}


}