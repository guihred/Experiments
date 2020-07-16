package ethical.hacker;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
import schema.sngpc.JsonExtractor;
import utils.*;

public final class KibanaApi {
    private static final Logger LOG = HasLogging.log();

    private static final ImmutableMap<String, String> GET_HEADERS = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0")
            .put("Accept", "text/plain, */*; q=0.01").put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
            .put("Accept-Encoding", "gzip, deflate, br").put("kbn-version", "6.6.2")
            .put("Origin", "https://n321p000124.fast.prevnet").put("DNT", "1").put("Connection", "keep-alive")
            .put("Referer", "https://n321p000124.fast.prevnet/app/kibana").put("Cookie", "io=3PIP6uXMWNC7_9EfAAAE")
            .put("Authorization", "Basic " + ExtractUtils.getEncodedAuthorization()).build();

    private KibanaApi() {
    }

    public static Map<String, String> kibanaFullScan(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, Object> policiesSearch =
                makeKibanaSearch(query, ResourceFXUtils.toFile("kibana/policiesQuery.json"));
        Map<String, Object> accessesSearch =
                makeKibanaSearch(query, ResourceFXUtils.toFile("kibana/acessosQuery.json"), "key", "doc_count");
        Map<String, Object> threatsSearch =
                makeKibanaSearch(query, ResourceFXUtils.toFile("kibana/threatQuery.json"));
        Map<String, Object> destinationSearch =
                makeKibanaSearch(query, ResourceFXUtils.toFile("kibana/destinationQuery.json"), "key", "value");
        destinationSearch.computeIfPresent("value", (k, v) -> Stream.of(v.toString().split("\n")).map(Double::valueOf)
                .map(Double::longValue).map(StringSigaUtils::getFileSize).collect(Collectors.joining("\n")));
        Map<String, Object> trafficSearch =
                makeKibanaSearch(query, ResourceFXUtils.toFile("kibana/trafficQuery.json"), "ReceiveTime",
                        "country_code2");
        Map<String, Object> ipInformation = VirusTotalApi.getIpTotalInfo(query);
        Map<String, String> fullScan = new LinkedHashMap<>();
        fullScan.put("IP", query);
        fullScan.put("Provedor", Objects.toString(ipInformation.get("as_owner")));
        fullScan.put("Geolocation",
                Objects.toString(ipInformation.getOrDefault("country", trafficSearch.remove("country_code2"))));
        fullScan.put("Talos Blacklist", isInBlacklist(query));
        fullScan.put("Bloqueio WAF", display(policiesSearch));
        fullScan.put("Palo Alto Threat", display(threatsSearch));
        LOG.info("KIBANA RESULT{}", fullScan);
        fullScan.put("TOP Conexão FW", display(destinationSearch));
        fullScan.put("TOP conexões WEB", display(accessesSearch));
        fullScan.put("Ultimo Acesso", display(trafficSearch));
        return fullScan;
    }

    public static Map<String, Object> makeKibanaSearch(String query, File file) {
        return makeKibanaSearch(query, file, "key");
    }
    public static Map<String, Object> makeKibanaSearch(String query, File file, String... params) {
        return SupplierEx.get(() -> {
            File outFile = newJsonFile(query + file.getName().replaceAll("\\.json", ""));
            if (!outFile.exists()) {
                String gte = Objects.toString(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
                String lte = Objects.toString(Instant.now().toEpochMilli());
                getFromURL("https://n321p000124.fast.prevnet/api/console/proxy?path=_search&method=POST",
                        getContent(file, query, gte, lte), outFile);
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, params);
        }, Collections.emptyMap());
    }

    private static String display(Map<String, Object> ob) {
        List<List<String>> collect = ob.values().stream().map(Objects::toString).map(s -> Arrays.asList(s.split("\n")))
                .collect(Collectors.toList());
        int orElse = collect.stream().mapToInt(List<String>::size).max().orElse(0);
        return IntStream.range(0, orElse).mapToObj(
                j -> collect.stream().filter(e -> j < e.size()).map(e -> e.get(j)).collect(Collectors.joining("    ")))
                .distinct().collect(Collectors.joining("\n"));

    }

    private static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> {
            String string = Files.toString(file, StandardCharsets.UTF_8);
            return String.format(string, params);
        }, "ERROR IN FILE " + file);
    }

    private static void getFromURL(String url, String content, File outFile) throws IOException {
        ExtractUtils.insertProxyConfig();
        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        get.setConfig(RequestConfig.custom().setSocketTimeout(100000).build());
        get.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
        GET_HEADERS.forEach(get::addHeader);
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        ExtractUtils.copy(rd, outFile);
    }

    private static String isInBlacklist(String query) {
        File file = ResourceFXUtils.toFile("kibana/ip_filter.txt");
        return SupplierEx.get(() -> java.nio.file.Files.lines(file.toPath()).anyMatch(query::equals) ? "sim" : "não",
                "não");
    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }
}
