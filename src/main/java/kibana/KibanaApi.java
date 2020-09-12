package kibana;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import ethical.hacker.InstallCert;
import ethical.hacker.VirusTotalApi;
import fxml.utils.JsonExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import utils.*;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public class KibanaApi {
    private static final Logger LOG = HasLogging.log();

    private static final ImmutableMap<String, String> GET_HEADERS = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0")
            .put("Accept", "text/plain, */*; q=0.01").put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
            .put("Accept-Encoding", "gzip, deflate, br").put("kbn-version", "6.6.2")
            .put("Origin", "https://n321p000124.fast.prevnet").put("DNT", "1").put("Connection", "keep-alive")
            .put("Referer", "https://n321p000124.fast.prevnet/app/kibana")
            .put("Authorization", "Basic " + ExtractUtils.getEncodedAuthorization()).build();

    protected KibanaApi() {
    }

    public static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> String.format(Files.toString(file, StandardCharsets.UTF_8), params),
                "ERROR IN FILE " + file);
    }

    public static Map<String, String> kibanaFullScan(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, String> policiesSearch = makeKibanaSearch("kibana/policiesQuery.json", query, "key");
        Map<String, String> accessesSearch = makeKibanaSearch("kibana/acessosQuery.json", query, "key", "doc_count");
        Map<String, String> threatsSearch = makeKibanaSearch("kibana/threatQuery.json", query, "key");
        Map<String, String> destinationSearch = makeKibanaSearch("kibana/destinationQuery.json", query, "key", "value");
        destinationSearch.computeIfPresent("value",
                (k, v) -> Stream.of(v.split("\n")).map(StringSigaUtils::getFileSize).collect(Collectors.joining("\n")));
        Map<String, String> trafficSearch =
                makeKibanaSearch("kibana/trafficQuery.json", query, "ReceiveTime", "country_code2");
        trafficSearch.computeIfPresent("ReceiveTime",
                (k, v) -> Stream.of(v.split("\n")).filter(e -> !e.endsWith("Z")).collect(Collectors.joining("\n")));
        Map<String, String> ipInformation = VirusTotalApi.getIpTotalInfo(query);
        Map<String, String> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", query);
        fullScan.put("Provedor", Objects.toString(ipInformation.get("as_owner")));
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

    public static Map<String, String> makeKibanaSearch(File file, int days, Map<String,String> search, String... params) {
        return makeKibanaSearch(file, "", days, search, params);
    }

    public static Map<String, String> makeKibanaSearch(File file, int days, String query, String... params) {
        return SupplierEx.get(() -> {
            File outFile = newJsonFile(query + file.getName().replaceAll("\\.json", "") + days);
            if (!outFile.exists() || oneDayModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                getFromURL("https://n321p000124.fast.prevnet/api/console/proxy?path=_search&method=POST",
                        getContent(file, query, gte, lte), outFile);
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, Collections.emptyMap());
    }

    public static Map<String, String> makeKibanaSearch(File file,String index, int days, Map<String,String> search, String... params) {
        return SupplierEx.get(() -> {
            String values = search.values().stream().collect(Collectors.joining());
            File outFile = newJsonFile(
                    file.getName().replaceAll("\\.json", "") +values +days);
            if (!outFile.exists() || oneDayModified(outFile)) {
                String gte = Objects.toString(Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                String keywords = search
                        .entrySet().stream().map(e -> String
                                .format("{\"match_phrase\": {\"%s\": {\"query\": \"%s\"}}},", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n"));
                getFromURL("https://n321p000124.fast.prevnet/api/console/proxy?path="+index
                        + "_search&method=POST",
                        getContent(file, keywords , gte, lte), outFile);
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, Collections.emptyMap());
    }


    public static Map<String, String> makeKibanaSearch(String file, String query, String... params) {
        return makeKibanaSearch(ResourceFXUtils.toFile(file), 1, query, params);
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
        Map<String, String> reference = null;
        for (int i = 0; i < orElse; i++) {
            int j = i;
            List<String> collect2 =
                    collect.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.toList());
            Map<String, String> newMap;
            if (collect2.get(0).matches(regex) || reference == null) {
                reference = new LinkedHashMap<>();
                newMap = reference;
            } else {
                newMap = new LinkedHashMap<>(reference);
                newMap.remove(reference.entrySet().stream().filter(e -> !e.getValue().matches(regex)).findFirst()
                        .map(Entry<String, String>::getKey).orElse(null));
            }

            Map<String, String> m = newMap;
            IntStream.range(0, keys.size()).forEach(k -> merge(regex, keys, collect2, m, k));
            finalList.add(newMap);
        }
        return finalList;
    }

    protected static void getFromURL(String url, String content, File outFile) throws IOException {
        ExtractUtils.insertProxyConfig();
        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        get.setConfig(RequestConfig.custom().setSocketTimeout(100000).build());
        get.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
        GET_HEADERS.forEach(get::addHeader);
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(get), () -> {
            InstallCert.installCertificate(url);
            return client.execute(get);
        });
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        ExtractUtils.copy(rd, outFile);
    }

    protected static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/{}\" ]+", "_");
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
            if (!linkedHashMap.get(keys.get(k) + l).matches(regex)) {
                break;
            }
        }

        linkedHashMap.merge(keys.get(k) + l, collect2.get(k), (o, n) -> Objects.equals(o, n) ? n : o + "\n" + n);
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
}
