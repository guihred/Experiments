package ml.data;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.SupplierEx;

public class DataframeBuilder extends DataframeML {
    private final DataframeML dataframeML = this;

    protected DataframeBuilder(File csvFile) {
        dataframeML.file = csvFile;
    }

    protected DataframeBuilder(String csvFile) {
        this(ResourceFXUtils.toFile(csvFile));
    }

    public DataframeBuilder addCategory(String d) {
        dataframeML.categories.put(d, new HashSet<>());
        return this;
    }

    public DataframeBuilder addCrossFeature(String d, String[] dependencies, FunctionEx<Object[], ?> mapper) {
        dataframeML.crossFeature.put(d, new AbstractMap.SimpleEntry<>(dependencies,mapper));
        return this;
    }

    public DataframeBuilder addMapping(String d, UnaryOperator<Object> mapper) {
        dataframeML.mapping.put(d, mapper);
        return this;
    }

    public DataframeML build() {
        return build(new SimpleDoubleProperty(0));
    }

    public DataframeML build(DoubleProperty progress) {
        if (ExcelService.isExcel(dataframeML.file)) {
            return ExcelDataReader.readExcel(dataframeML, dataframeML.file);
        }
        DataframeUtils.readCSV(dataframeML.file, progress, dataframeML);
        return dataframeML;
    }

    public Set<Entry<String, DataframeStatisticAccumulator>> columns() {
        return SupplierEx.get(() -> {
            try (Scanner scanner = new Scanner(dataframeML.file, "UTF-8")) {
                DataframeUtils.addHeaders(dataframeML, scanner);
            }
            return dataframeML.getStats().entrySet();
        }, Collections.emptySet());
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

        DataframeUtils.makeStats(dataframeML.file, dataframeML, new SimpleDoubleProperty());
        return dataframeML;
    }

    public DataframeML makeStats(DoubleProperty progress) {
        DataframeUtils.makeStats(dataframeML.file, dataframeML, progress);
        return dataframeML;
    }

    public DataframeBuilder setMaxSize(int maxSize) {
        dataframeML.maxSize = maxSize;
        return this;
    }

    public static DataframeML build(File csvFile) {
        DataframeML dataframeML = new DataframeML();
        dataframeML.file = csvFile;
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