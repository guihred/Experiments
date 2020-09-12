package ethical.hacker;

import fxml.utils.JsonExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class VirusTotalApi {
    private static final String MALICIOUS_POSITIVE_REGEX = "malicious=([^0]\\d*)";
    private static final String LAST_ANALYSIS_STATS = "last_analysis_stats";
    private static final String MALICIOUS_ATTR = "malicious";
    private static final String ATTRIBUTES = "attributes";
    private static final Logger LOG = HasLogging.log();
    private static final String VIRUSTOTAL_APIKEY = "397249a87cac6415141dde0a2263710c23166cc759dc89b681e8df70cc536abd";

    private VirusTotalApi() {
    }

    public static File[] getFilesInformation(Path path) throws IOException {
        return getFilesInformation(path, HashVerifier.getSha256Hash(path));
    }

    public static File[] getFilesInformation(Path path, String hash) throws IOException {
        String string = path.getName(path.getNameCount() - 1).toString();
        File outFile = newJsonFile(string);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES,
                LAST_ANALYSIS_STATS, MALICIOUS_ATTR, "type_description", "tags", "type", "trid", "magic",
                "meaningful_name", "file_type", "probability");
        Matcher matcher = Pattern.compile(MALICIOUS_POSITIVE_REGEX).matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious FILE {} {}", path, group);
        }
        return new File[] { outFile };
    }

    public static Entry<File, List<String>> getIpInformation(String ip) throws IOException {
        File outFile = newJsonFile(ip);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES, "id",
                LAST_ANALYSIS_STATS, MALICIOUS_ATTR);

        Matcher matcher = Pattern.compile(MALICIOUS_POSITIVE_REGEX).matcher(displayJsonFromFile);
        List<String> malicious = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            malicious.add(group);
        }
        if (!malicious.isEmpty()) {
            LOG.info("Malicious IP {} {}", ip, malicious);
        }
        return new AbstractMap.SimpleEntry<>(outFile, malicious);
    }

    public static Map<String, String> getIpTotalInfo(String ip) {
        return SupplierEx.get(() -> {

            File outFile = newJsonFile(ip);
            if (!outFile.exists()) {
                getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, "as_owner", "country", 
                    LAST_ANALYSIS_STATS, MALICIOUS_ATTR, "network");
        });
    }

    public static File[] getUrlInformation(String url) throws IOException {

        String string = SupplierEx.getIgnore(() -> tryToCreateUrl(url), "http://" + url + "/");

        File outFile = newJsonFile(string);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/urls/" + HashVerifier.getSha256Hash(string), outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile);
        LOG.info(displayJsonFromFile);
        Matcher matcher = Pattern.compile(MALICIOUS_POSITIVE_REGEX).matcher(displayJsonFromFile);
        List<String> malicious = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            malicious.add(group);
        }
        if (!malicious.isEmpty()) {
            LOG.info("Malicious URL {} {}", url, malicious);
        }
        return new File[] { outFile };
    }

    private static void getFromURL(String url, File outFile) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet post = new HttpGet(url);
        post.addHeader("x-apikey", VIRUSTOTAL_APIKEY);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        List<String> collect = rd.lines().collect(Collectors.toList());
        Files.write(outFile.toPath(), collect);
    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }

    private static String tryToCreateUrl(String url) throws MalformedURLException {
        return new URL(url).toString();
    }
}
