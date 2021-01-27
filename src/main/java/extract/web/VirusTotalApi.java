package extract.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class VirusTotalApi {
    private static final String ERROR_TAG = "error";
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
        String filename = path.getName(path.getNameCount() - 1).toString();
        File outFile = newJsonFile(filename);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES, LAST_ANALYSIS_STATS,
                MALICIOUS_ATTR, "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type",
                "probability");
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
        return SupplierEx.getHandle(() -> {

            File outFile = newJsonFile(ip);
            if (!outFile.exists()) {
                getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
            }
            Map<String, String> jsonFile =
                    JsonExtractor.makeMapFromJsonFile(outFile, "id", "as_owner", "country", "network", ERROR_TAG);
            if (StringUtils.isNotBlank(jsonFile.get(ERROR_TAG))) {
                Files.deleteIfExists(outFile.toPath());
                CIDRUtils.makeNetworkCSV();
            }
            return jsonFile;
        }, null, e -> LOG.info("ERROR SEARCHING {} {}", ip, e.getMessage()));
    }

    public static Map<String, Object> getUrlInfo(String url) {

        return SupplierEx.getHandle(() -> {
            File outFile = getUrlInformation(url)[0];
            Map<String, Object> jsonFile = JsonExtractor.accessMap(JsonExtractor.toFullObject(outFile));
            if (Objects.nonNull(jsonFile.get(ERROR_TAG))) {
                Files.deleteIfExists(outFile.toPath());
            }
            return jsonFile;
        }, null, e -> LOG.info("ERROR SEARCHING {} {}", url, e.getMessage()));

    }
    public static File[] getUrlInformation(String url) throws IOException {

        String fullUrl = SupplierEx.getFirst(() -> tryToCreateUrl(url),
                () -> url.contains("/") ? "https://" + url : "http://" + url + "/");

        File outFile = newJsonFile(fullUrl);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/urls/" + HashVerifier.getSha256Hash(fullUrl), outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile);
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
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("x-apikey", VIRUSTOTAL_APIKEY);
        PhantomJSUtils.makeGet(url, hashMap, outFile);
    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/\\?]+", "_");
        return ResourceFXUtils.getOutFile("json/" + replaceAll + ".json");
    }

    private static String tryToCreateUrl(String url) throws MalformedURLException {
        return new URL(url).toString();
    }
}
