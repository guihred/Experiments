package ml.data;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import utils.ExcelService;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.PredicateEx;
import utils.ex.SupplierEx;

public final class DataframeBuilder extends DataframeML {

    private DataframeBuilder(File csvFile) {
        file = csvFile;
    }

    private DataframeBuilder(String csvFile) {
        this(ResourceFXUtils.toFile(csvFile));
    }

    public DataframeBuilder addCategory(String d) {
        categories.put(d, new HashSet<>());
        return this;
    }

    public DataframeBuilder addCrossFeature(String d, FunctionEx<Object[], ?> mapper, String... dependencies) {
        crossFeature.put(d, new AbstractMap.SimpleEntry<>(dependencies, mapper));
        return this;
    }

    public DataframeBuilder addCrossFeature(String d, String[] dependencies, FunctionEx<Object[], ?> mapper) {
        crossFeature.put(d, new AbstractMap.SimpleEntry<>(dependencies, mapper));
        return this;
    }

    public DataframeBuilder addMapping(String d, UnaryOperator<Object> mapper) {
        mapping.put(d, mapper);
        return this;
    }

    public DataframeML build() {
        return SupplierEx.measureTime("building " + file.getName(), () -> build(new SimpleDoubleProperty(0)));
    }

    public DataframeML build(DoubleProperty progress) {
        if (ExcelService.isExcel(file)) {
            return ExcelDataReader.readExcel(this, file);
        }
        DataframeUtils.readCSV(file, progress, this);
        return this;
    }

    public Set<Entry<String, DataframeStatisticAccumulator>> columns() {
        return SupplierEx.get(() -> {

            if (getStats() != null && !getStats().isEmpty()) {
                return getStats().entrySet();
            }
            List<String> addHeaders;
            try (Scanner scanner = new Scanner(file, "UTF-8")) {
                addHeaders = DataframeUtils.addHeaders(this, scanner);
            }
            if (addHeaders.equals(Arrays.asList("<html>"))) {
                ExcelDataReader.extractHTML(this);
            }
            return getStats().entrySet();
        }, Collections.emptySet());
    }

    public DataframeBuilder filterOut(String d, PredicateEx<Object> fil) {
        filters.put(d, fil);
        return this;
    }

    public DataframeML makeStats() {

        DataframeUtils.makeStats(file, this, new SimpleDoubleProperty());
        return this;
    }

    public DataframeML makeStats(DoubleProperty progress) {
        DataframeUtils.makeStats(file, this, progress);
        return this;
    }

    public DataframeBuilder rename(String destination, String source) {
        renaming.put(destination, source);
        return this;
    }

    public DataframeBuilder setMaxSize(int maxSize) {
        this.maxSize = maxSize;
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