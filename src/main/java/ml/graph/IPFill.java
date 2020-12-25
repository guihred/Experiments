package ml.graph;

import extract.WhoIsScanner;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import ml.data.*;
import org.apache.commons.lang3.StringUtils;
import utils.ex.RunnableEx;

public final class IPFill {

    private IPFill() {
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        return IPFill.fillIPInformation(builder, ipColumn, new SimpleDoubleProperty(0));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn, DoubleProperty count) {
        builder.filterOut(ipColumn,
                s -> !s.toString().matches("^10\\..+") && s.toString().matches(WhoIsScanner.IP_REGEX));
    
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        ObservableMap<String, Map<String, String>> ipInfoCache = FXCollections.observableHashMap();
        builder.addCrossFeature("", e -> {
            Map<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("Network", IPFill.getFromCache(whoIsScanner, ipInfoCache, e, "network"));
            hashMap.put("Owner", IPFill.getFromCache(whoIsScanner, ipInfoCache, e, "asname", "as_owner"));
            RunnableEx.runIf(IPFill.getFromCache(whoIsScanner, ipInfoCache, e, WhoIsScanner.REVERSE_DNS),
                    s -> hashMap.put("Reverse DNS", s));
            hashMap.put("Country", IPFill.getFromCache(whoIsScanner, ipInfoCache, e, "country", "ascountry"));
            return hashMap;
        }, ipColumn);
        DataframeML build = builder.build(count);
        build.removeCol("");
        return build;
    }

    public static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = IPFill.getIPColumn(builder);
        return fillIPInformation(builder, ipColumn);
    }

    public static String getFromCache(WhoIsScanner whoIsScanner, ObservableMap<String, Map<String, String>> ipInfo,
            Object[] e, String... string) {
        return getKey(ipInfo.computeIfAbsent(e[0].toString(), ip -> WhoIsScanner.getIpInformation(whoIsScanner, ip)),
                string);
    }

    public static String getIPColumn(DataframeBuilder builder) {
        return builder.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst().orElse(null);
    }

    public static String getKey(Map<String, String> first, String... keys) {
        return Stream.of(keys).map(first::get).filter(Objects::nonNull).findFirst().orElse(null);
    
    }

    public static String getLastNumberField(BaseDataframe dataframe) {
        List<String> numberCols = dataframe.getFormatMap().entrySet().stream().filter(e -> e.getValue() != null)
                .filter(e -> Number.class.isAssignableFrom(e.getValue()))
                .map(Entry<String, Class<? extends Comparable<?>>>::getKey).collect(Collectors.toList());
        return numberCols.get(numberCols.size() - 1);
    }

    public static String reorderAndLog(DataframeML dataframe, String numberField) {
        DataframeUtils.sort(dataframe, numberField);
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, "Network", numberField);
        createSeries.forEach(s2 -> WhoIsScanner.LOG.info("{}", s2));
        List<Entry<Object, Double>> series = DataframeUtils.createSeries(dataframe, "Owner", numberField);
        series.forEach(s1 -> WhoIsScanner.LOG.info("{}", s1));
        dataframe.removeCol("filters");
        return DataframeUtils.toString(dataframe, 30);
    
    }

}
