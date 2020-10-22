
package kibana;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import ethical.hacker.WhoIsScanner;
import fxml.utils.JsonExtractor;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.PhantomJSUtils;
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

    public static Map<String, String> kibanaFullScan(String query) {
        return kibanaFullScan(query, 1);
    }

    public static Map<String, String> kibanaFullScan(String query, int days) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, String> policiesSearch = makeKibanaSearch("policiesQuery.json", query, days, "key");
        Map<String, String> accessesSearch = makeKibanaSearch("acessosQuery.json", query, days, "key", "doc_count");
        Map<String, String> threatsSearch = makeKibanaSearch("threatQuery.json", query, days, "key");
        Map<String, String> destinationSearch = makeKibanaSearch("destinationQuery.json", query, days, "key", "value");
        destinationSearch.computeIfPresent("value",
                (k, v) -> Stream.of(v.split("\n")).map(StringSigaUtils::getFileSize).collect(Collectors.joining("\n")));
        Map<String, String> trafficSearch =
                makeKibanaSearch("trafficQuery.json", query, days, "ReceiveTime", "country_code2");
        trafficSearch.computeIfPresent("ReceiveTime",
                (k, v) -> Stream.of(v.split("\n")).filter(e -> !e.endsWith("Z")).collect(Collectors.joining("\n")));
        Map<String, String> ipInformation = new WhoIsScanner().getIpInformation(query);
        Map<String, String> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", query);
        fullScan.put("Provedor", Objects.toString(WhoIsScanner.getKey(ipInformation, "as_owner", "HostName"), ""));
        fullScan.put("Geolocation",
                Objects.toString(ipInformation.getOrDefault("country", trafficSearch.remove("country_code2"))));
        fullScan.put("Talos Blacklist", isInBlacklist(query));
        fullScan.put("Bloqueio WAF", display(policiesSearch));
        fullScan.put("Palo Alto Threat", display(threatsSearch));
        fullScan.put("TOP Conexão FW", display(destinationSearch));
        fullScan.put("TOP conexões WEB", display(accessesSearch));
        fullScan.put("Ultimo Acesso", display(trafficSearch));
        LOG.info("KIBANA RESULT{}", fullScan);
        return fullScan;
    }

    public static Map<String, String> makeKibanaSearch(File file, int days, String query, String... params) {
        return SupplierEx.getHandle(() -> {
            File outFile = newJsonFile(removeExtension(file) + query + days);
            if (!outFile.exists() || oneDayModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                getFromURL(ELASTICSEARCH_MSEARCH_URL, getContent(file, query, gte, lte), outFile);
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
            if (!outFile.exists() || oneDayModified(outFile)) {
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

    public static List<Map<String, String>> remap(Map<String, String> ob, String regex) {
        if (StringUtils.isBlank(regex)) {
            return remap(ob);
        }
        List<List<String>> collect =
                ob.values().stream().map(s -> Arrays.asList(s.split("\n"))).collect(Collectors.toList());
        int orElse = collect.stream().mapToInt(List<String>::size).max().orElse(0);
        List<String> keys = ob.keySet().stream().collect(Collectors.toList());
        List<Map<String, String>> finalList = new ArrayList<>();
        List<List<String>> partialList = new ArrayList<>();
        Map<String, String> reference = null;
        for (int i = 0; i < orElse; i++) {
            int j = i;
            List<String> collect2 =
                    collect.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.toList());
            if (collect2.stream().anyMatch(s -> s.matches(regex))) {
                reference = new LinkedHashMap<>();
                Map<String, String> m = reference;
                IntStream.range(0, keys.size()).forEach(k -> merge(regex, keys, collect2, m, k));
                reference = processPartialList(regex, keys, finalList, partialList, reference);
            } else if (reference == null) {
                partialList.add(collect2);
            } else {
                Map<String, String> newMap = new LinkedHashMap<>(reference);
                newMap.remove(reference.entrySet().stream().filter(e -> !e.getValue().matches(regex)).findFirst()
                        .map(Entry<String, String>::getKey).orElse(null));
                IntStream.range(0, keys.size()).forEach(k -> merge(regex, keys, collect2, newMap, k));
                finalList.add(newMap);
            }

        }
        return finalList;
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

    protected static boolean oneDayModified(File outFile) {
        FileTime lastModifiedTime = ResourceFXUtils.computeAttributes(outFile).lastModifiedTime();
        Instant instant = lastModifiedTime.toInstant();
        long between = ChronoUnit.HOURS.between(instant, Instant.now());
        return between > 1;
    }

    private static String display(Map<String, String> ob) {
        List<List<String>> collect =
                ob.values().stream().map(s -> Arrays.asList(s.split("\n"))).collect(Collectors.toList());
        int orElse = collect.stream().mapToInt(List<String>::size).max().orElse(0);
        return IntStream.range(0, orElse).mapToObj(
                j -> collect.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.joining("    ")))
                .distinct().collect(Collectors.joining("\n"));
    }

    private static String isInBlacklist(String query) {
        File file = ResourceFXUtils.toFile("kibana/ip_filter.txt");
        return SupplierEx.get(() -> java.nio.file.Files.lines(file.toPath()).anyMatch(query::equals) ? "sim" : "não",
                "não");
    }

    private static void merge(String regex, List<String> keys, List<String> collect2, Map<String, String> linkedHashMap,
            int k) {
        int l = 0;
        for (; linkedHashMap.containsKey(keys.get(k) + l); l++) {
            if (Objects.equals(linkedHashMap.get(keys.get(k) + l), collect2.get(k))) {
                return;
            }
            if (!linkedHashMap.get(keys.get(k) + l).matches(regex)) {
                break;
            }
        }

        linkedHashMap.merge(keys.get(k) + l, collect2.get(k), (o, n) -> Objects.equals(o, n) ? n : o + "\n" + n);
    }

    private static Map<String, String> processPartialList(String regex, List<String> keys,
            List<Map<String, String>> finalList, List<List<String>> partialList, Map<String, String> reference) {
        if (partialList.isEmpty()) {
            return reference;
        }
        for (List<String> list : partialList) {
            Map<String, String> newMap = new LinkedHashMap<>(reference);
            newMap.remove(reference.entrySet().stream().filter(e -> !e.getValue().matches(regex)).findFirst()
                    .map(Entry<String, String>::getKey).orElse(null));
            IntStream.range(0, keys.size()).forEach(k -> merge(regex, keys, list, newMap, k));
            finalList.add(newMap);
        }
        partialList.clear();
        return null;
    }

    private static List<Map<String, String>> remap(Map<String, String> ob) {
        List<List<String>> collect =
                ob.values().stream().map(s -> Arrays.asList(s.split("\n"))).collect(Collectors.toList());
        int orElse = collect.stream().mapToInt(List<String>::size).max().orElse(0);
        List<String> keys = ob.keySet().stream().collect(Collectors.toList());
        List<Map<String, String>> arrayList = new ArrayList<>();
        for (int i = 0; i < orElse; i++) {
            Map<String, String> linkedHashMap = new LinkedHashMap<>();
            int j = i;
            List<String> collect2 =
                    collect.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.toList());
            IntStream.range(0, keys.size()).forEach(k -> linkedHashMap.put(keys.get(k), collect2.get(k)));
            arrayList.add(linkedHashMap);
        }
        return arrayList;
    }

    private static String removeExtension(File file) {
        return file.getName().replaceAll("\\.json", "");
    }
}
