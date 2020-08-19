package ml.data;

import extract.ExcelService;
import java.io.File;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class DataframeBuilder extends DataframeML {
    private final DataframeML dataframeML = new DataframeML();
    private final File csvFile;

    protected DataframeBuilder(File csvFile) {
        this.csvFile = csvFile;
    }

    protected DataframeBuilder(String csvFile) {
        this.csvFile = ResourceFXUtils.toFile(csvFile);
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

    public Set<Entry<String, DataframeStatisticAccumulator>> columns() {
        RunnableEx.run(() -> {
            try (Scanner scanner = new Scanner(csvFile, "UTF-8")) {
                DataframeUtils.addHeaders(dataframeML, scanner);
            }
        });
        return dataframeML.getStats().entrySet();
    }

    public DataframeML dataframe() {
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