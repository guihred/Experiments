
package kibana;

import static utils.ex.RunnableEx.measureTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import ethical.hacker.PortServices;
import extract.CIDRUtils;
import extract.JsonExtractor;
import extract.PhantomJSUtils;
import extract.WhoIsScanner;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import ml.graph.ExplorerHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class KibanaApi {

    private static final String ELASTICSEARCH_MSEARCH_URL =
            "https://n321p000124.fast.prevnet/elasticsearch/_msearch?rest_total_hits_as_int=true&ignore_throttled=true";

    private static final Logger LOG = HasLogging.log();

    private static final Map<String, String> GET_HEADERS = ImmutableMap.<String, String>builder()
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
            .put("Accept", "application/json, text/plain, */*")
            .put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3").put("Accept-Encoding", "gzip, deflate, br")
            .put("kbn-version", "6.6.2").put("Origin", "https://n321p000124.fast.prevnet").put("DNT", "1")
            .put("Authorization", "Basic " + ExtractUtils.getEncodedAuthorization()).put("Connection", "keep-alive")
            .put("Referer", "https://n321p000124.fast.prevnet/app/kibana").put("Cookie", "io=3PIP6uXMWNC7_9EfAAAE")
            .build();

    private static final WhoIsScanner WHOIS_SCANNER = new WhoIsScanner();

    protected KibanaApi() {
    }

    public static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> String.format(Files.toString(file, StandardCharsets.UTF_8), params),
                "ERROR IN FILE " + file).replaceAll("[\n\t]", "");
    }

    public static Map<String, String> getGeridCredencial(String finalIP, String index) {
        Map<String, String> makeKibanaSearch2 =
                KibanaApi.makeKibanaSearch("geridCredenciaisQuery.json", 3, new String[] { index, finalIP }, "message");
        String message = makeKibanaSearch2.getOrDefault("message", "");
        String regex = "WHO:\\s+(.+)";
        List<String> linesThatMatch =
                Stream.of(message.split("\n")).filter(l -> l.matches(regex)).map(s -> s.replaceAll(regex, "$1"))
                        .distinct().filter(StringUtils::isNumeric).collect(Collectors.toList());
        String[] messages = message.split("Audit trail record BEGIN");
        return Stream.of(messages).filter(l -> linesThatMatch.contains(getWhoField(regex, l))).distinct()
                .collect(Collectors.toMap(s -> getWhoField(regex, s), s -> s, SupplierEx::nonNull));
    }

    public static String isInBlacklist(String query) {
        File file = ResourceFXUtils.toFile("kibana/ip_filter.txt");
        return SupplierEx.get(() -> java.nio.file.Files.lines(file.toPath()).anyMatch(query::equals) ? "sim" : "não",
                "não");
    }

    public static Map<String, String> kibanaFullScan(String query) {
        return kibanaFullScan(query, 1, null);
    }

    public static Map<String, String> kibanaFullScan(String query, int days) {
        return kibanaFullScan(query, days, null);

    }

    public static Map<String, String> kibanaFullScan(String query, int days, Property<Number> progress) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, SupplierEx<String>> fullScan = scanByIp(query, days);
        Map<String, String> fullScan2 = new LinkedHashMap<>();
        CommonsFX.update(progress, 0);
        measureTime("Kibana Full Scan " + query, () -> {
            for (Entry<String, SupplierEx<String>> entry : fullScan.entrySet()) {
                fullScan2.put(entry.getKey(), SupplierEx.get(entry.getValue()));
                CommonsFX.addProgress(progress, 1. / fullScan.size());
            }
        });
        CommonsFX.update(progress, 1);

        return fullScan2;
    }

    public static Map<String, String> makeKibanaSearch(File file, int days, String query, String... params) {
        return makeKibanaSearch(file, days, new String[] { query }, params);
    }

    public static Map<String, String> makeKibanaSearch(File file, int days, String[] query, String... params) {
        return SupplierEx.getHandle(() -> {
            File outFile = newJsonFile(removeExtension(file) + Stream.of(query).collect(Collectors.joining()) + days);
            if (!outFile.exists() || oneHourModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                RunnableEx
                        .make(() -> getFromURL(ELASTICSEARCH_MSEARCH_URL,
                                getContent(file, Stream.concat(Stream.of(query), Stream.of(gte, lte)).toArray()),
                                outFile), e -> LOG.error("ERROR MAKING SEARCH {} {} ", file.getName(), e.getMessage()))
                        .run();
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, new HashMap<>(), e -> LOG.error("ERROR MAKING SEARCH {} {} {}", file.getName(), query, e.getMessage()));
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, Map<String, String> search,
            String... params) {
        return makeNewKibanaSearch(ResourceFXUtils.toFile(file), days, search, params);
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, String[] query, String... params) {

        return makeKibanaSearch(ResourceFXUtils.toFile("kibana/" + file), days, query, params);
    }

    public static Map<String, String> makeKibanaSearch(String file, String query, int days, String... params) {
        return makeKibanaSearch(ResourceFXUtils.toFile("kibana/" + file), days, query, params);
    }

    public static Map<String, String> makeNewKibanaSearch(File file, int days, Map<String, String> search,
            String... params) {
        return SupplierEx.get(() -> {
            String values = search.values().stream().collect(Collectors.joining());
            File outFile = newJsonFile(removeExtension(file) + values + days);
            if (!outFile.exists() || oneHourModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                String keywords = convertSearchKeywords(search);
                RunnableEx
                        .make(() -> getFromURL(ELASTICSEARCH_MSEARCH_URL, getContent(file, keywords, gte, lte),
                                outFile), e -> LOG.error("ERROR MAKING SEARCH {} {} ", file.getName(), e.getMessage()))
                        .run();
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, new HashMap<>());
    }

    protected static String convertSearchKeywords(Map<String, String> search) {
        return search.entrySet().stream().map(e -> {
            if (e.getValue().contains("\n")) {
                return Stream.of(e.getValue().split("\n"))
                        .map(v -> String.format("{\"match_phrase\":{\"%s\":\"%s\"}}", e.getKey(), v))
                        .collect(Collectors.joining(",", "{\"bool\":{\"should\":[", "],\"minimum_should_match\":1}},"));
            }
            return String.format("{\"query_string\": {\"query\": \"%s:\\\"%s\\\"\",\"analyze_wildcard\": true,"
                    + "\"default_field\": \"*\"}},", e.getKey(), e.getValue());
        }).collect(Collectors.joining("\n"));
    }

    protected static void getFromURL(String url, String cont, File outFile) throws IOException {
        String content = cont.replaceAll("[\n\t]+", "").replaceFirst("\\}\\{", "}\n{") + "\n";
        Map<String, String> hashMap = new HashMap<>(GET_HEADERS);
        hashMap.put("Content-Type", "application/x-ndjson");
        PhantomJSUtils.postNdJson(url, content, hashMap, outFile);
    }

    protected static void getFromURLJson(String url, String content, File outFile) throws IOException {
        Map<String, String> hashMap = new HashMap<>(GET_HEADERS);
        hashMap.put("Content-Type", "application/json; charset=utf-8");
        PhantomJSUtils.postJson(url, content, hashMap, outFile);
    }

    protected static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/{}\" */\n?]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }

    protected static boolean oneHourModified(File outFile) {
        FileTime lastModifiedTime = ResourceFXUtils.computeAttributes(outFile).lastModifiedTime();
        Instant instant = lastModifiedTime.toInstant();
        long between = ChronoUnit.HOURS.between(instant, Instant.now());
        return between > 1;
    }

    private static void convertToBytes(String valueCol, Map<String, String> destinationSearch) {
        destinationSearch.computeIfPresent(valueCol,
                (k, v) -> Stream.of(v.split("\n")).map(StringSigaUtils::getFileSize).collect(Collectors.joining("\n")));
    }

    private static void convertToStats(String valueCol, Map<String, String> destinationSearch) {
        destinationSearch.computeIfPresent(valueCol, (k, v) -> {
            String[] lines = v.split("\n");
            DoubleSummaryStatistics summaryStatistics =
                    Stream.of(lines).skip(1).mapToDouble(StringSigaUtils::toDouble).summaryStatistics();
            if (summaryStatistics.getCount() == 0) {
                return "";
            }
            String min = StringSigaUtils.getFileSize(summaryStatistics.getMin());
            String max = StringSigaUtils.getFileSize(summaryStatistics.getMax());
            String last = StringSigaUtils.getFileSize(summaryStatistics.getSum());
            return String.format("%s (%s a %s)", last, min, max);
        });

    }

    private static <T> String display(Map<String, T> ob) {
        List<List<String>> listOfFields = ob.values().stream().map(StringSigaUtils::toStringSpecial)
                .map(s -> Stream.of(s.split("\n")).collect(Collectors.toList())).collect(Collectors.toList());
        int maxNumFields = listOfFields.stream().mapToInt(List<String>::size).max().orElse(0);
        listOfFields.forEach(l -> {
            if (l.size() < maxNumFields) {
                l.add(0, "\t");
            }
        });
        return IntStream.range(0, maxNumFields).mapToObj(
                j -> listOfFields.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.joining("    ")))
                .distinct().collect(Collectors.joining("\n"));
    }

    private static String getURL(Map<String, String> e) {
        return e.get("key0")
                .replaceAll("(?<=[/])[\\-\\d]+|(?<=(=|%3B))[^&]+|.+(?=\\.(css|js|png|woff|ttf|gif|jpg|svg|ico))", "*");
    }

    private static String getWhoField(String regex, String s) {
        return Stream.of(s.split("\n")).filter(l -> l.matches(regex)).findFirst().orElse("").replaceAll(regex, "$1");
    }

    private static String removeExtension(File file) {
        return file.getName().replaceAll("\\.json", "");
    }

    private static Map<String, SupplierEx<String>> scanByIp(String ip, int days) {
        String key = "key";
        String valueCol = "value";
        Map<String, SupplierEx<String>> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", () -> ip);
        fullScan.put("Provedor",
                () -> ExplorerHelper.getKey(WHOIS_SCANNER.getIpInformation(ip), "as_owner", "HostName", "asname"));
        fullScan.put("Geolocation",
                () -> ExplorerHelper.getKey(WHOIS_SCANNER.getIpInformation(ip), "country", "ascountry", "Descrição"));
        String pattern = CIDRUtils.addressToPattern(ip);
        fullScan.put("WAF_Policy", () -> display(
                makeKibanaSearch("wafQuery.json", pattern, days, "action", "policy-name", "alert.description")));
        fullScan.put("PaloAlto_Threat", () -> display(makeKibanaSearch("threatQuery.json", ip, days, key)));
        fullScan.put("TOP_FW", () -> {
            Map<String, String> destinationSearch = makeKibanaSearch("destinationQuery.json", ip, days, key, valueCol);
            convertToBytes(valueCol, destinationSearch);
            destinationSearch.computeIfPresent(key, (k, v) -> Stream.of(v.split("\n")).map(WHOIS_SCANNER::reverseDNS)
                    .collect(Collectors.joining("\n")));
            return display(destinationSearch);
        });
        fullScan.put("TOP_WEB",
                () -> display(makeKibanaSearch("acessosQuery.json", pattern, days, key, "doc_count")));
        fullScan.put("Ports", () -> {
            Map<String, String> destinationPort =
                    makeKibanaSearch("destinationPortQuery.json", ip, days, key, valueCol);
            destinationPort.computeIfPresent(key,
                    (k, v) -> Stream.of(v.split("\n")).map(StringSigaUtils::toInteger)
                            .map(PortServices::getServiceByPort)
                            .map(le -> Arrays.toString(le.getPorts()) + " " + le.getDescription().replaceAll(",.+", ""))
                            .collect(Collectors.joining("\n")));
            convertToBytes(valueCol, destinationPort);
            return display(destinationPort);
        });
        fullScan.put("Acesso", () -> {
            Map<String, String> trafficSearch = makeKibanaSearch("trafficQuery.json", ip, days, "ReceiveTime");
            trafficSearch.computeIfPresent("ReceiveTime",
                    (k, v) -> Stream.of(v.split("\n")).filter(e -> !e.endsWith("Z")).collect(Collectors.joining("\n")));
            return display(trafficSearch);
        });
        fullScan.put("Bytes_Received", () -> {
            Map<String, String> totalBytesQuery = makeKibanaSearch("totalBytesQuery.json", ip, days, valueCol);
            convertToStats(valueCol, totalBytesQuery);
            return display(totalBytesQuery);
        });
        fullScan.put("Bytes_Sent", () -> {
            Map<String, String> totalBytesSent = makeKibanaSearch("paloAltoQuery.json", ip, days, valueCol);
            convertToStats(valueCol, totalBytesSent);
            return display(totalBytesSent);
        });
        fullScan.put("URLs", () -> {
            Map<String, String> filter1 = new LinkedHashMap<>();
            filter1.put("clientip.keyword", pattern);
            String docCount = "doc_count";
            Map<String, String> nsInformation =
                    KibanaApi.makeKibanaSearch("kibana/requestedPath.json", days, filter1, key, docCount);
            List<Map<String, String>> remap = JsonExtractor.remap(nsInformation, "^/.*");
            Map<String, Long> collect = remap.stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(getURL(e), StringSigaUtils.toLong(e.get(docCount + "0"))))
                    .collect(Collectors.groupingBy(SimpleEntry<String, Long>::getKey,
                            Collectors.summingLong(SimpleEntry<String, Long>::getValue)));
            return collect.entrySet().stream()
                    .sorted(Comparator.comparingLong(Entry<String, Long>::getValue).reversed())
                    .map(e -> e.getKey() + "\t" + e.getValue()).filter(s -> !s.startsWith("*"))
                    .collect(Collectors.joining("\n"));
        });
        fullScan.put("WAF", () -> display(makeKibanaSearch("wafQuery.json", pattern, days, "Name", "Value")));
        return fullScan;
    }
}
