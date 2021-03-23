package kibana;

import static utils.ex.RunnableEx.measureTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import ethical.hacker.PortServices;
import extract.web.CIDRUtils;
import extract.web.JsonExtractor;
import extract.web.PhantomJSUtils;
import extract.web.WhoIsScanner;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class KibanaApi {

    private static final String KIBANA_FOLDER = "kibana/";

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

    public static String destinationPorts(String ip, int days) {
        String key = "key";
        String valueCol = "value";
        String buckets = "buckets";
        List<Map<Object, Object>> accessList = JsonExtractor.accessList(
                makeKibanaSearchObj("destinationPortQuery.json", days, new String[] { "DestinationIP", "SourceIP", ip },
                        "aggregations"),
                "aggregations", "3", buckets);
        List<Map<Object, Object>> accessList2 = JsonExtractor.accessList(
                makeKibanaSearchObj("destinationPortQuery.json", days, new String[] { "SourceIP", "DestinationIP", ip },
                        "aggregations"),
                "aggregations", "3", buckets);
        accessList.addAll(accessList2);
        return accessList.stream().map((Map<Object, Object> map) -> {
            String ipd = JsonExtractor.access(map, String.class, key);
            String reverseDNS = getHostname(ipd);
            String ongValue = JsonExtractor.<Map<Object, Object>>accessList(map, "4", buckets).stream().map(s -> {
                Integer port = JsonExtractor.access(s, Integer.class, key);
                PortServices serviceByPort = PortServices.getServiceByPort(port);
                Number size = JsonExtractor.access(s, Number.class, "1", valueCol);
                return reverseDNS + " " + serviceByPort + " " + StringSigaUtils.getFileSize(size.longValue());
            }).collect(Collectors.joining("\n"));
            return ongValue;
        }).collect(Collectors.joining("\n"));
    }

    public static <T> String display(Map<String, T> ob) {
        if (ob == null) {
            return "";
        }
        List<List<String>> listOfFields = getFieldList(ob);
        int maxNumFields = listOfFields.stream().mapToInt(List<String>::size).max().orElse(0);
        adjustToMax(listOfFields, maxNumFields);
        return IntStream.range(0, maxNumFields).mapToObj(
                j -> listOfFields.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }

    public static String geoLocation(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "";
        }

        return Stream.of(ip.split("\n")).map(s -> WHOIS_SCANNER.getGeoIpInformation(s))
                .map(s -> StringSigaUtils.getKey(s, "Descrição", "country", "ascountry"))
                .collect(Collectors.joining("\n"));

    }

    public static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> String.format(Files.toString(file, StandardCharsets.UTF_8), params),
                "ERROR IN FILE " + file).replaceAll("[\n\t]", "");
    }

    public static Map<String, String> getGeridCredencial(String finalIP, String index, int days) {
        List<String> message = getMessageList(finalIP, index, days);
        String suppliedCredential = "WHAT: supplied credentials: .+?(\\d{11}).+";
        String regex = "WHO:\\s+(\\d+)|" + suppliedCredential;
        List<String> linesThatMatch =
                message.stream().flatMap(m -> Stream.of(m.split("\n"))).filter(l -> l.matches(regex))
                        .map(s -> s.replaceAll(regex, "$1$2")).distinct().collect(Collectors.toList());
        return message.stream().filter(l -> linesThatMatch.contains(getWhoField(regex, l))).distinct()
                .collect(Collectors.toMap(s -> getWhoField(regex, s), s -> s,
                        (t, u) -> getFirstMatch(suppliedCredential, t, u)));
    }

    public static String getHostname(Object e) {
        String ip = Objects.toString(e, "");
        Map<String, String> ipInformation = WHOIS_SCANNER.getIpInformation(ip);
        String key = StringSigaUtils.getKey(ipInformation, "as_owner", "Nome", "HostName", "Descrição", "asname");
        if (!key.equals(ip)) {
            return key;
        }
        String key2 = StringSigaUtils.getKey(ipInformation, "as_owner", "Nome", "Descrição", "asname", "id");
        if (StringUtils.isNotBlank(key2)) {
            return key2;
        }
        return ip;
    }

    public static Map<String, String> getIPsByCredencial(String credencial, String index, int days) {
        List<String> message = getMessageList(credencial, index, days);
        String ipAddress = "CLIENT IP ADDRESS: (.+)";
        List<String> linesThatMatch =
                message.stream().flatMap(m -> Stream.of(m.split("\n"))).filter(l -> l.matches(ipAddress))
                        .map(s -> s.replaceAll(ipAddress, "$1")).distinct().collect(Collectors.toList());
        return message.stream().filter(l -> linesThatMatch.contains(getWhoField(ipAddress, l, "$1")))
                .sorted(Comparator.comparing(s -> !s.matches(ipAddress))).distinct()
                .collect(Collectors.toMap(s -> getWhoField(ipAddress, s, "$1"), s -> s, SupplierEx::nonNull));
    }

    public static String getLoginTimeCredencial(String query, String index, int days) {
        List<String> message = getMessageList(query, index, days);
        String whenRegex = "WHEN: (.+)";
        return message.stream().flatMap(m -> Stream.of(m.split("\n"))).filter(s -> s.matches(whenRegex))
                .map(s -> s.replaceAll(whenRegex, "$1"))
                .map(s -> DateFormatUtils.parse(s, "E MMM dd HH:mm:ss z yyyy", ZonedDateTime::from))
                .collect(new SimpleSummary<>()).format(t -> DateFormatUtils.format("dd/MM/yyyy HH:mm:ss", t));
    }

    public static List<String> getMessageList(String finalIP, String index, int days) {
        Map<String, Object> makeKibanaSearch2 = KibanaApi.makeKibanaSearchObj("geridCredenciaisQuery.json", days,
                new String[] { index, finalIP }, "message");
        return JsonExtractor.accessList(makeKibanaSearch2, "message").stream().map(Objects::toString)
                .collect(Collectors.toList());
    }

    public static String getURL(String string) {
        return string.replaceAll("(?<=[/])[\\-\\d]+|(?<=(=|%3B))[^&]+"
                + "|.+(?=\\.(css|png|woff|ttf|gif|jpg|svg|ico|eot))|.+(?=\\.(js)$)", "*");
    }

    public static String isInBlacklist(String query) {
        File file = ResourceFXUtils.toFile("kibana/ip_filter.txt");
        return SupplierEx.get(() -> java.nio.file.Files.lines(file.toPath()).anyMatch(query::equals) ? "sim" : "não",
                "não");
    }

    public static Map<String, String> kibanaFullScan(String query, int days) {
        return kibanaFullScan(query, days, null);

    }

    public static Map<String, String> kibanaFullScan(String query, int days, Property<Number> progress) {
        return kibanaFullScan(query, days, progress, Arrays.asList());
    }

    public static Map<String, String> kibanaFullScan(String query, int days, Property<Number> progress,
            List<String> cols) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, SupplierEx<String>> fullScan = scanByIp(query, days);
        Map<String, String> fullScan2 = new LinkedHashMap<>();
        CommonsFX.update(progress, 0);
        measureTime("Kibana Full Scan " + query, () -> {
            for (Entry<String, SupplierEx<String>> entry : fullScan.entrySet()) {
                if (cols.isEmpty() || cols.contains(entry.getKey())) {
                    fullScan2.put(entry.getKey(), Objects.toString(SupplierEx.get(entry.getValue()), ""));
                }
                CommonsFX.addProgress(progress, 1. / fullScan.size());
            }
        });
        CommonsFX.update(progress, 1);

        return fullScan2;
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, Map<String, String> search,
            String... params) {
        return makeNewKibanaSearch(kibanaFile(file), days, search, params);
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, String query, String... params) {
        return makeKibanaSearch(kibanaFile(file), days, query, params);
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, String[] query, String... params) {

        return makeKibanaSearch(kibanaFile(file), days, query, params);
    }

    public static Map<String, Object> makeKibanaSearchObj(File file, int days, String[] query, String... params) {
        return SupplierEx.getHandle(() -> {
            File outFile = searchIfDoesNotExist(file, days, query);
            Object object = JsonExtractor.toObject(outFile, params);
            return JsonExtractor.accessMap(object);
        }, new HashMap<>(), e -> LOG.error("ERROR MAKING SEARCH {} {} {}", file.getName(), query, e.getMessage()));
    }

    public static Map<String, Object> makeKibanaSearchObj(String file, int days, String query, String... params) {
        return makeKibanaSearchObj(kibanaFile(file), days, new String[] { query }, params);
    }

    public static Map<String, Object> makeKibanaSearchObj(String file, int days, String[] query, String... params) {
        return makeKibanaSearchObj(kibanaFile(file), days, query, params);
    }

    public static Map<String, String> makeNewKibanaSearch(File file, int days, Map<String, String> search,
            String... params) {
        return SupplierEx.get(() -> {
            File outFile = newSearch(file, days, search);
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, new HashMap<>());
    }

    public static Map<String, SupplierEx<String>> scanByIp(String ip, int days) {
        String key = "key";
        String valueCol = "value";
        Map<String, SupplierEx<String>> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", () -> ip);
        fullScan.put("Provedor", () -> StringSigaUtils.getKey(WHOIS_SCANNER.getIpInformation(ip), "as_owner", "Nome",
                "HostName", "asname"));
        fullScan.put("Geolocation", () -> StringSigaUtils.getKey(WHOIS_SCANNER.getGeoIpInformation(ip), "country",
                "ascountry", "Descrição"));
        String pattern = CIDRUtils.addressToPattern(ip);
        fullScan.put("WAF_Policy", () -> {
            String policy = "policy-name";
            Map<String, Object> makeKibanaSearch = makeKibanaSearchObj("wafQuery.json", days, pattern, policy);
            makeKibanaSearch.computeIfPresent(policy, (k, v) -> JsonExtractor.<String>accessList(v).stream()
                    .map(WHOIS_SCANNER::lookupPolicy).map(KibanaApi::displayDistinct).collect(Collectors.toList()));
            return displayDistinct(makeKibanaSearch);
        });
        fullScan.put("PaloAlto_Threat", () -> {
            if (CIDRUtils.isVPNNetwork(ip)) {
                Map<String, Object> userQuery =
                        makeKibanaSearchObj("userQuery.json", days, new String[] { ip }, key, valueCol);
                userQuery.computeIfPresent(valueCol, (k, v) -> {
                    List<String> collect = JsonExtractor.<Number>accessList(v).stream()
                            .map(s -> DateFormatUtils.format("dd/MM/yy HH:mm", s.longValue()))
                            .collect(Collectors.toList());
                    return group(collect, 2);
                });
                return displayDistinct(userQuery);
            }
            return displayDistinct(makeKibanaSearch("threatQuery.json", days, ip, key));
        });
        fullScan.put("TOP_FW", () -> {
            Map<String, Object> destinationSearch =
                    makeKibanaSearchObj("destinationQuery.json", days, ip, key, valueCol);
            destinationSearch.computeIfPresent(key, (k, v) -> JsonExtractor.<String>accessList(v).stream()
                    .map(WHOIS_SCANNER::reverseDNS).collect(Collectors.joining("\n")));
            convertToBytes(destinationSearch, valueCol);
            return displayDistinct(destinationSearch);
        });
        fullScan.put("TOP_WEB",
                () -> displayDistinct(makeKibanaSearch("acessosQuery.json", days, pattern, key, "doc_count")));
        fullScan.put("Ports", () -> {
            return destinationPorts(ip, days);
        });
        fullScan.put("Acesso", () -> {
            Map<String, String> trafficSearch = makeKibanaSearch("trafficQuery.json", days, ip, valueCol);
            trafficSearch.computeIfPresent(valueCol,
                    (k, v) -> Stream.of(v.split("\n"))
                            .map(i -> DateFormatUtils.format("dd/MM/yyyy", StringSigaUtils.toLong(i))).distinct()
                            .collect(Collectors.joining(" – ")));
            return displayDistinct(trafficSearch);
        });
        fullScan.put("Bytes_Received", () -> {
            Map<String, Object> totalBytesQuery = makeKibanaSearchObj("totalBytesQuery.json", days, ip, valueCol);
            convertToStats(valueCol, totalBytesQuery);
            return displayDistinct(totalBytesQuery);
        });
        fullScan.put("Bytes_Sent", () -> {
            Map<String, Object> totalBytesSent = makeKibanaSearchObj("paloAltoQuery.json", days, ip, valueCol);
            convertToStats(valueCol, totalBytesSent);
            return display(totalBytesSent);
        });
        fullScan.put("URLs", () -> urls(days, pattern));
        return fullScan;
    }

    protected static String convertSearchKeywords(Map<String, String> search) {
        return search.entrySet().stream().map(e -> {
            if (e.getValue().contains("\n")) {
                return Stream.of(e.getValue().split("\n"))
                        .map(v -> String.format("{\"query_string\": {\"query\": \"%s:%s\",\"analyze_wildcard\": true,"
                                + "\"default_field\": \"*\"}}", e.getKey(), v))
                        .collect(Collectors.joining(",", "{\"bool\":{\"should\":[", "],\"minimum_should_match\":1}},"));
            }
            return String.format("{\"query_string\": {\"query\": \"%s:%s\",\"analyze_wildcard\": true,"
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
        String replaceAll = string.replaceAll("[:/{}\" */\n?\\\\=]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }

    private static void adjustToMax(List<List<String>> listOfFields, int maxNumFields) {
        listOfFields.forEach(l -> {
            if (l.size() < maxNumFields) {
                l.add(0, "\t");
            }
        });
    }

    private static void convertToBytes(Map<String, Object> destinationSearch, String valueCol) {
        destinationSearch.computeIfPresent(valueCol, (k, v) -> JsonExtractor.accessList(v).stream()
                .map(StringSigaUtils::toLong).map(StringSigaUtils::getFileSize).collect(Collectors.joining("\n")));
    }

    private static void convertToStats(String valueCol, Map<String, Object> destinationSearch) {
        destinationSearch.computeIfPresent(valueCol, (k, v) -> {
            DoubleSummaryStatistics stats = JsonExtractor.<Number>accessList(v).stream().skip(1)
                    .mapToDouble(StringSigaUtils::toDouble).summaryStatistics();
            if (stats.getCount() == 0) {
                return "";
            }
            String last = StringSigaUtils.getFileSize(stats.getSum());
            if (stats.getCount() == 1) {
                return String.format("%s", last);
            }
            String min = StringSigaUtils.getFileSize(stats.getAverage());
            String max = StringSigaUtils.getFileSize(stats.getMax());
            return String.format("%s (%s a %s)", last, min, max);
        });

    }

    private static <T> String displayDistinct(Map<String, T> ob) {
        return Stream.of(display(ob).split("\n")).distinct().collect(Collectors.joining("\n"));
    }

    private static <T> List<List<String>> getFieldList(Map<String, T> ob) {
        return ob.values().stream().map(t -> {
            if (t instanceof List) {
                return ((List<?>) t).stream().map(StringSigaUtils::toStringSpecial).collect(Collectors.joining("\n"));
            }
            return StringSigaUtils.toStringSpecial(t);
        }).map(s -> Stream.of(s.split("\n")).map(m -> StringUtils.abbreviate(m, 100)).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private static String getFirstMatch(String suppliedCredential, String t, String u) {
        return Stream.of(t, u).filter(Objects::nonNull)
                .min(Comparator.comparing(s -> StringSigaUtils.matches(s, suppliedCredential).isEmpty())).orElse(null);
    }

    private static String getWhoField(String regex, String s) {
        return getWhoField(regex, s, "$1$2");
    }

    private static String getWhoField(String regex, String s, String replacement) {
        return Stream.of(s.split("\n")).filter(l -> l.matches(regex)).findFirst().orElse("").replaceAll(regex,
                replacement);
    }

    private static String group(List<String> collect, final int d) {
        return groupStream(collect, d).collect(Collectors.joining("\n"));
    }

    private static Stream<String> groupStream(List<String> collect, final int d) {
        return IntStream.range(0, collect.size() / d)
                .mapToObj(i -> IntStream.range(i, i + d).filter(j -> j < collect.size()).mapToObj(j -> collect.get(j))
                        .flatMap(s -> Stream.of(s.split(" "))).distinct().collect(Collectors.joining(" ")));
    }

    private static File kibanaFile(String file) {
        return ResourceFXUtils.toFile(KIBANA_FOLDER + file);
    }

    private static Map<String, String> makeKibanaSearch(File file, int days, String query, String... params) {
        return makeKibanaSearch(file, days, new String[] { query }, params);
    }

    private static Map<String, String> makeKibanaSearch(File file, int days, String[] query, String... params) {
        return SupplierEx.getHandle(() -> {
            File outFile = searchIfDoesNotExist(file, days, query);
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, new HashMap<>(), e -> LOG.error("ERROR MAKING SEARCH {} {} {}", file.getName(), query, e.getMessage()));
    }

    private static File newSearch(File file, int days, Map<String, String> search) {
        String values = search.values().stream().collect(Collectors.joining());
        File outFile = newJsonFile(removeExtension(file) + values + "_" + days);
        if (JsonExtractor.isNotRecentFile(outFile)) {
            String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
            String lte = Objects.toString(Instant.now().toEpochMilli());
            String keywords = convertSearchKeywords(search);
            String content = getContent(file, keywords, gte, lte);
            RunnableEx.make(() -> getFromURL(ELASTICSEARCH_MSEARCH_URL, content, outFile),
                    e -> LOG.error("ERROR MAKING SEARCH {} {} ", file.getName(), e.getMessage())).run();
        }
        return outFile;
    }

    private static String removeExtension(File file) {
        return file.getName().replaceAll("\\.json", "");
    }

    private static File searchIfDoesNotExist(File file, int days, String[] query) {
        File outFile =
                newJsonFile(
                        removeExtension(file) + Stream.of(query).distinct().collect(Collectors.joining()) + "_" + days);
        if (JsonExtractor.isNotRecentFile(outFile)) {
            String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
            String lte = Objects.toString(Instant.now().toEpochMilli());
            RunnableEx.make(
                    () -> getFromURL(ELASTICSEARCH_MSEARCH_URL,
                            getContent(file, Stream.concat(Stream.of(query), Stream.of(gte, lte)).toArray()), outFile),
                    e -> LOG.error("ERROR MAKING  SEARCH {} {} ", file.getName(), e.getMessage())).run();
        }
        return outFile;
    }

    private static String urls(int days, String pattern) {
        Map<String, String> filter1 = new LinkedHashMap<>();
        filter1.put("clientip", pattern);
        String docCount = "doc_count";
        String key = "key";
        Map<String, String> nsInformation =
                KibanaApi.makeKibanaSearch("requestedPath.json", days, filter1, key, docCount);
        List<Map<String, String>> remap = JsonExtractor.remap(nsInformation, "^/.*");
        Map<String, List<Map<String, String>>> collect2 =
                remap.stream().collect(Collectors.groupingBy(e -> e.get("key1"),
                        LinkedHashMap<String, List<Map<String, String>>>::new, Collectors.toList()));
        return collect2.entrySet().stream().map(entry -> {
            Map<String,
                    Long> collect = entry.getValue().stream()
                            .map(e -> new AbstractMap.SimpleEntry<>(getURL(e.get("key0")),
                                    StringSigaUtils.toLong(e.get(docCount + "0"))))
                            .collect(Collectors.groupingBy(SimpleEntry<String, Long>::getKey,
                                    Collectors.summingLong(SimpleEntry<String, Long>::getValue)));
            String collect3 = collect.entrySet().stream()
                    .sorted(Comparator.comparingLong(Entry<String, Long>::getValue).reversed())
                    .map(Entry<String, Long>::getKey).filter(s -> !s.startsWith("*")).collect(Collectors.joining("\n"));
            return entry.getKey() + "\n" + collect3;
        }).collect(Collectors.joining("\r\n"));
    }
}
