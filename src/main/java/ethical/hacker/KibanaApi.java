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
import java.util.Map;
import java.util.Objects;
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
import utils.ExtractUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SupplierEx;

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

    public static void main(String[] args) throws IOException {
        Map<String, Object> makeKibanaSearch = makeKibanaSearch("191.101.252.62");
        LOG.info("{}", makeKibanaSearch);
    }

    public static Map<String, Object> makeKibanaSearch(String query) throws IOException {
        File outFile = newJsonFile(query + "kibana");
        if (!outFile.exists()) {
            String gte = Objects.toString(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
            String lte = Objects.toString(Instant.now().toEpochMilli());
            getFromURL("https://n321p000124.fast.prevnet/api/console/proxy?path=_search&method=POST",
                    getContent(ResourceFXUtils.toFile("kibana/policiesQuery.json"), query, gte, lte),
                    outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile);
        LOG.info("{}", displayJsonFromFile);
        return JsonExtractor.makeMapFromJsonFile(outFile, "key", "doc_count");
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

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }
}
