package ml.graph;

import extract.web.WhoIsScanner;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import ml.data.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ClassReflectionUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.fx.PaginatedTableView;

public final class ExplorerHelper {

    public static final int MAX_ELEMENTS = 3000;

    private static final Logger LOG = HasLogging.log();
    private static WhoIsScanner whoIsScanner = new WhoIsScanner();
    private ExplorerHelper() {
    }

    public static void addEntries(PaginatedTableView dataTable2,
            List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList) {
        Map<Integer, Map<String, Object>> cache = new HashMap<>();
        List<String> asList = Arrays.asList("Header", "Mean", "Max", "Min", "Distinct", "Median25", "Median50",
                "Median75", "Sum", "Count");
        for (String key : asList) {
            dataTable2.addColumn(key, i -> ExplorerHelper.getStatAt(addedSubList, cache, key.toLowerCase(), i));
        }
        dataTable2.setListSize(addedSubList.size());
        double[] array = asList.stream()
                .mapToDouble(e -> Math.max(
                        Objects.toString(ExplorerHelper.getStatAt(addedSubList, cache, e.toLowerCase(), 0)).length(),
                        e.length()))
                .toArray();
        dataTable2.setColumnsWidth(array);
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        return ExplorerHelper.fillIPInformation(builder, ipColumn, new SimpleDoubleProperty(0));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn, DoubleProperty count) {
        builder.filterOut(ipColumn,
                s -> s.toString().matches(WhoIsScanner.IP_REGEX));

        ObservableMap<String, Map<String, String>> ipInfoCache = FXCollections.observableHashMap();
        builder.addCrossFeature("", e -> {
            Map<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("Network", ExplorerHelper.getFromCache(ipInfoCache, e, "network", "Sub-Rede"));
            hashMap.put("Owner", ExplorerHelper.getFromCache(ipInfoCache, e, "asname", "as_owner", "Nome",
                    WhoIsScanner.REVERSE_DNS));
            hashMap.put("Country", ExplorerHelper.getFromCache(ipInfoCache, e, "country", "ascountry", "Descrição"));
            return hashMap;
        }, ipColumn);
        DataframeML filledDataframe = builder.build(count);
        filledDataframe.removeCol("");
        final int maxSizeDisplayed = 200;
        String dataAsStr = DataframeUtils.toString(filledDataframe, maxSizeDisplayed);
        LOG.info(dataAsStr);
        return filledDataframe;
    }

    public static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = ExplorerHelper.getIPColumn(builder);
        return fillIPInformation(builder, ipColumn);
    }

    public static String getIPColumn(DataframeBuilder builder) {
        return builder.columns().stream().map(Entry<String, DataframeStatisticAccumulator>::getKey)
                .filter(s -> StringUtils.containsIgnoreCase(s, "IP")).findFirst().orElse(null);
    }

    public static <T> T getKey(Map<String, T> first, String... keys) {
        return Stream.of(keys).map(FunctionEx.ignore(first::get)).filter(Objects::nonNull).findFirst().orElse(null);

    }

    public static String getLastNumberField(BaseDataframe dataframe) {
        List<String> numberCols = dataframe.getFormatMap().entrySet().stream().filter(e -> e.getValue() != null)
                .filter(e -> Number.class.isAssignableFrom(e.getValue()))
                .map(Entry<String, Class<? extends Comparable<?>>>::getKey).collect(Collectors.toList());
        return numberCols.get(numberCols.size() - 1);
    }

    public static int getTopLength(Entry<String, DataframeStatisticAccumulator> e) {
        String string = Objects.toString(e.getValue().getTop(), "");
        return Stream.of(string.split("\n")).mapToInt(String::length).max().orElse(string.length());
    }



    public static String reorderAndLog(DataframeML dataframe, String numberField) {
        DataframeUtils.sort(dataframe, numberField);
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, "Network", numberField);
        createSeries.forEach(s2 -> LOG.info("{}", s2));
        List<Entry<Object, Double>> series = DataframeUtils.createSeries(dataframe, "Owner", numberField);
        series.forEach(s1 -> LOG.info("{}", s1));
        dataframe.removeCol("filters");
        return DataframeUtils.toString(dataframe);

    }

    public static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }

    private static String getFromCache(ObservableMap<String, Map<String, String>> ipInfo,
            Object[] e, String... string) {
        return getKey(ipInfo.computeIfAbsent(e[0].toString(), whoIsScanner::getIpInformation),
                string);
    }

    private static Object getStatAt(List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList,
            Map<Integer, Map<String, Object>> cache, String key, Integer i) {
        return cache.computeIfAbsent(i, k -> ClassReflectionUtils.getGetterMap(addedSubList.get(k).getValue()))
                .get(key);
    }

}
