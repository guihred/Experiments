package ethical.hacker;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
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
import utils.ExtractUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SupplierEx;

public final class TimelionApi {
    private static final Logger LOG = HasLogging.log();

    private static final ImmutableMap<String, String> GET_HEADERS = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json;charset=utf-8")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0")
            .put("Accept", "application/json, text/plain, */*; q=0.01")
            .put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3").put("Accept-Encoding", "gzip, deflate, br")
            .put("kbn-version", "6.6.2").put("Origin", "https://n321p000124.fast.prevnet").put("DNT", "1")
            .put("Connection", "keep-alive").put("Referer", "https://n321p000124.fast.prevnet/app/timelion")
            .put("Host", "n321p000124.fast.prevnet").put("Cookie", "io=3PIP6uXMWNC7_9EfAAAE")
            .put("Authorization", "Basic " + ExtractUtils.getEncodedAuthorization()).build();

    private TimelionApi() {
    }

    public static String getContent(File file, Object... params) {
        return SupplierEx.remap(() -> {
            String string = Files.toString(file, StandardCharsets.UTF_8);
            return String.format(string, params);
        }, "ERROR IN FILE " + file);
    }

    public static void main(String[] args) {
        TimelionApi.timelionFullScan("first");
    }

    public static Object maketimelionSearch(File file, int days, String query) {
        return SupplierEx.get(() -> {
            File outFile = newJsonFile(query + file.getName().replaceAll("\\.json", "") + days);
            if (!outFile.exists() || oneDayModified(outFile)) {
                String content = getContent(file, query);
                getFromURL("https://n321p000124.fast.prevnet/api/timelion/run", content, outFile);
            }
            return JsonExtractor.toObject(outFile);
        });
    }

    public static ObservableList<XYChart.Series<Number, Number>> timelionFullScan(String query) {
        if (StringUtils.isBlank(query)) {
            return FXCollections.emptyObservableList();
        }
        Object policiesSearch = maketimelionSearch(ResourceFXUtils.toFile("kibana/acessosTarefasQuery.json"), 1, query);
        Object access = access(policiesSearch, "sheet");
        LOG.info("{}", access);

        return extracted(access);
    }

    @SuppressWarnings("rawtypes")
    private static Object access(Object root, Object... param) {
        Object o = root;
        for (Object object : param) {
            if (object instanceof String) {
                o = ((Map) o).get(object);
            }
            if (object instanceof Integer) {
                o = ((List) o).get(((Integer) object).intValue());
            }
            if (o == null) {
                return null;
            }
        }
        return o;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ObservableList<XYChart.Series<Number, Number>> extracted(Object access) {
        return ((List<Object>) access).stream().flatMap(e -> ((List<Object>) access(e, "list")).stream())
                .map((Object o) -> {
                    XYChart.Series<Number, Number> java = new XYChart.Series<>();
                    java.setName(Objects.toString(access(o, "label")));
                    ((List) access(o, "data")).stream().filter(f -> !"0".equals(access(f, 1))).forEach(f -> {
                        Long access2 = Long.valueOf(Objects.toString(access(f, 0)));
                        Long access3 = Long.valueOf(Objects.toString(access(f, 1)));
                        java.getData().add(new XYChart.Data<>(access2, access3));
                    });
                    return java;
                }).collect(Collectors.toCollection(FXCollections::observableArrayList));
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
        String replaceAll = string.replaceAll("[:/{}\" ]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }

    private static boolean oneDayModified(File outFile) {
        FileTime lastModifiedTime = ResourceFXUtils.computeAttributes(outFile).lastModifiedTime();
        Instant instant = lastModifiedTime.toInstant();
        long between = ChronoUnit.HOURS.between(instant, Instant.now());
        return between > 12;
    }
}
