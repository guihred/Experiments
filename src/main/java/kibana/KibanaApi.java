
package kibana;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import extract.JsonExtractor;
import extract.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import ml.graph.IPFill;
import ml.graph.WhoIsScanner;
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

    protected KibanaApi() {
    }

    public static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> String.format(Files.toString(file, StandardCharsets.UTF_8), params),
                "ERROR IN FILE " + file).replaceAll("[\n\t]", "");
    }

    public static Map<String, String> getGeridCredencial(String finalIP) {
        Map<String, String> makeKibanaSearch2 =
                KibanaApi.makeKibanaSearch("geridCredenciaisQuery.json", finalIP, 1, "message");
        String orDefault = makeKibanaSearch2.getOrDefault("message", "");
        String regex = "WHO:\\s+(.+)";
        List<String> collect =
                Stream.of(orDefault.split("\n")).filter(l -> l.matches(regex)).map(s -> s.replaceAll(regex, "$1"))
                        .distinct().filter(StringUtils::isNumeric).collect(Collectors.toList());
        String[] split = orDefault.split("Audit trail record BEGIN");
        return Stream.of(split).filter(l -> collect.contains(getWhoField(regex, l))).distinct()
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
        String key = "key";

        String valueCol = "value";
        String dateKey = "key_as_string";


        WhoIsScanner whoIsScanner = new WhoIsScanner();
        Map<String, SupplierEx<String>> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", () -> query);
        fullScan.put("Provedor",
                () -> {
                    Map<String, String> ipInformation = whoIsScanner.getIpInformation(query);
                    return Objects.toString(IPFill.getKey(ipInformation, "as_owner", "HostName"), "");
                });
        fullScan.put("Geolocation",
                () -> {
                    Map<String, String> ipInformation = whoIsScanner.getIpInformation(query);
                    return Objects
                            .toString(ipInformation.getOrDefault("country", ""));
                });
        fullScan.put("Bloqueio WAF", () -> display(makeKibanaSearch("policiesQuery.json", query, days, key)));
        fullScan.put("Palo Alto Threat", () -> display(makeKibanaSearch("threatQuery.json", query, days, key)));
        fullScan.put("TOP Conexão FW", () -> {
            Map<String, String> destinationSearch =
                    makeKibanaSearch("destinationQuery.json", query, days, key, valueCol);
            convertToBytes(valueCol, destinationSearch);
            return display(destinationSearch);
        });
        fullScan.put("TOP conexões WEB",
                () -> display(makeKibanaSearch("acessosQuery.json", query, days, key, "doc_count")));
        fullScan.put("Ultimo Acesso", () -> {
            Map<String, String> trafficSearch =
                    makeKibanaSearch("trafficQuery.json", query, days, "ReceiveTime", "country_code2");
            trafficSearch.computeIfPresent("ReceiveTime",
                    (k, v) -> Stream.of(v.split("\n")).filter(e -> !e.endsWith("Z")).collect(Collectors.joining("\n")));
            return display(trafficSearch);
        });
        fullScan.put("Total Bytes Received", () -> {
            Map<String, String> totalBytesQuery =
                    makeKibanaSearch("totalBytesQuery.json", query, days + 3, dateKey, valueCol);
            convertToBytes(valueCol, totalBytesQuery);
            removeDateZone(dateKey, totalBytesQuery);
            return display(totalBytesQuery);
        });
        fullScan.put("WAF", () -> display(makeKibanaSearch("wafQuery.json", query, days, key)));
        fullScan.put("Total Bytes Sent", () -> {
            Map<String, String> totalBytesSent =
                    makeKibanaSearch("paloAltoQuery.json", query, days + 3, dateKey, valueCol);
            convertToBytes(valueCol, totalBytesSent);
            removeDateZone(dateKey, totalBytesSent);
            return display(totalBytesSent);
        });
        Set<Entry<String, SupplierEx<String>>> entrySet = fullScan.entrySet();

        Map<String, String> fullScan2 = new LinkedHashMap<>();
        CommonsFX.update(progress, 0);
        for (Entry<String, SupplierEx<String>> entry : entrySet) {
            fullScan2.put(entry.getKey(), SupplierEx.get(entry.getValue()));
            CommonsFX.addProgress(progress, 1. / entrySet.size());
        }
        LOG.info("KIBANA RESULT{}", fullScan2);
        CommonsFX.update(progress, 1);

        return fullScan2;
    }

    public static Map<String, String> makeKibanaSearch(File file, int days, String query, String... params) {
        return SupplierEx.getHandle(() -> {
            File outFile = newJsonFile(removeExtension(file) + query + days);
            if (!outFile.exists() || oneHourModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                RunnableEx.make(() -> getFromURL(ELASTICSEARCH_MSEARCH_URL, getContent(file, query, gte, lte), outFile),
                        e -> LOG.error("ERROR MAKING SEARCH {} {} ", file.getName(), e.getMessage())).run();
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, new HashMap<>(), e -> LOG.error("ERROR MAKING SEARCH {} {} {}", file.getName(), query, e.getMessage()));
    }

    public static Map<String, String> makeKibanaSearch(String file, int days, Map<String, String> search,
            String... params) {
        return makeNewKibanaSearch(ResourceFXUtils.toFile(file), days, search, params);
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
            return String.format("{\"match_phrase\":{\"%s\":{\"query\":\"%s\"}}},", e.getKey(), e.getValue());
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
        String replaceAll = string.replaceAll("[:/{}\" /\n]+", "_");
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

    private static String display(Map<String, String> ob) {
        List<List<String>> collect =
                ob.values().stream().map(s -> Stream.of(s.split("\n")).collect(Collectors.toList()))
                        .collect(Collectors.toList());
        int orElse = collect.stream().mapToInt(List<String>::size).max().orElse(0);
        collect.forEach(l -> {
            if (l.size() < orElse) {
                l.add(0, "\t");
            }
        });
        return IntStream.range(0, orElse).mapToObj(
                j -> collect.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.joining("    ")))
                .distinct().collect(Collectors.joining("\n"));
    }

    private static String getWhoField(String regex, String s) {
        return Stream.of(s.split("\n")).filter(l -> l.matches(regex)).findFirst().orElse("").replaceAll(regex, "$1");
    }

    private static void removeDateZone(String dateKey, Map<String, String> totalBytesQuery) {
        totalBytesQuery.computeIfPresent(dateKey,
        (k1, v1) -> Stream.of(v1.split("\n")).map(s->s.replaceAll("T.+", "")).collect(Collectors.joining("\n")));
    }

    private static String removeExtension(File file) {
        return file.getName().replaceAll("\\.json", "");
    }
}
