package ml.graph;

import extract.WhoIsScanner;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.chart.XYChart.Data;
import ml.data.*;
import org.apache.commons.lang3.StringUtils;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ex.RunnableEx;

public final class ExplorerHelper {

    protected static final int MAX_ELEMENTS = 3000;

    private ExplorerHelper() {
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn) {
        return ExplorerHelper.fillIPInformation(builder, ipColumn, new SimpleDoubleProperty(0));
    }

    public static DataframeML fillIPInformation(DataframeBuilder builder, String ipColumn, DoubleProperty count) {
        builder.filterOut(ipColumn,
                s -> !s.toString().matches("^10\\..+") && s.toString().matches(WhoIsScanner.IP_REGEX));

        WhoIsScanner whoIsScanner = new WhoIsScanner();
        ObservableMap<String, Map<String, String>> ipInfoCache = FXCollections.observableHashMap();
        builder.addCrossFeature("", e -> {
            Map<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("Network", ExplorerHelper.getFromCache(whoIsScanner, ipInfoCache, e, "network"));
            hashMap.put("Owner", ExplorerHelper.getFromCache(whoIsScanner, ipInfoCache, e, "asname", "as_owner"));
            RunnableEx.runIf(ExplorerHelper.getFromCache(whoIsScanner, ipInfoCache, e, WhoIsScanner.REVERSE_DNS),
                    s -> hashMap.put("Reverse DNS", s));
            hashMap.put("Country", ExplorerHelper.getFromCache(whoIsScanner, ipInfoCache, e, "country", "ascountry"));
            return hashMap;
        }, ipColumn);
        DataframeML filledDataframe = builder.build(count);
        filledDataframe.removeCol("");
        return filledDataframe;
    }

    public static DataframeML fillIPInformation(File csvFile) {
        DataframeBuilder builder = DataframeBuilder.builder(csvFile);
        String ipColumn = ExplorerHelper.getIPColumn(builder);
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
        return Stream.of(keys).map(first::get).filter(Objects::nonNull).findFirst().orElse("");

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

    static void addEntries(PaginatedTableView dataTable2,
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

    static void addToList(ObservableList<Data<String, Number>> dataList, List<Data<String, Number>> array,
            Data<String, Number> others, Function<Data<String, Number>, Double> keyExtractor) {
        CommonsFX.runInPlatformSync(() -> {
            if (dataList.size() >= ExplorerHelper.MAX_ELEMENTS / 4) {
                others.setYValue(keyExtractor.apply(others) + array.stream().mapToDouble(keyExtractor::apply).sum());
                if (!dataList.contains(others)) {
                    dataList.add(others);
                }
            } else {
                array.sort(Comparator.comparing(keyExtractor).reversed());
                dataList.addAll(array);
            }
        });
    }

    static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> bar2List,
            Collection<Entry<String, T>> countMap) {
        RunnableEx.runNewThread(() -> {
            List<Data<String, Number>> barList = Collections.synchronizedList(new ArrayList<>());
            Data<String, Number> others = new Data<>("Others", 0);
            countMap.forEach(entry -> {
                String k = entry.getKey();
                Number v = entry.getValue();
                barList.add(new Data<>(k, v));
                if (barList.size() % (ExplorerHelper.MAX_ELEMENTS / 10) == 0) {
                    addToList(bar2List, new ArrayList<>(barList), others, m -> m.getYValue().doubleValue());
                    barList.clear();
                }
            });
            addToList(bar2List, barList, others, m -> m.getYValue().doubleValue());
        });
    }

    static <T extends Number> void addToPieChart(ObservableList<Data<String, Number>> barList,
            Map<String, T> countMap) {
        addToPieChart(barList, countMap.entrySet());
    }

    static int getTopLength(Entry<String, DataframeStatisticAccumulator> e) {
        String string = Objects.toString(e.getValue().getTop(), "");
        return Stream.of(string.split("\n")).mapToInt(String::length).max().orElse(string.length());
    }

    static List<Entry<String, Number>> toPie(DataframeML dataframe, String title, String key) {
        List<Entry<Object, Double>> createSeries = DataframeUtils.createSeries(dataframe, title, key);
        return createSeries.stream().map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), (Number) e.getValue()))
                .collect(Collectors.toList());
    }

    private static Object getStatAt(List<? extends Entry<String, DataframeStatisticAccumulator>> addedSubList,
            Map<Integer, Map<String, Object>> cache, String key, Integer i) {
        return cache.computeIfAbsent(i, k -> ClassReflectionUtils.getGetterMap(addedSubList.get(k).getValue()))
                .get(key);
    }

}
