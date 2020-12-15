package extract;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        return SupplierEx.getHandle(() -> {

            File outFile = newJsonFile(ip);
            if (!outFile.exists()) {
                getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
            }
            return JsonExtractor.makeMapFromJsonFile(outFile, "id", "as_owner", "country", 
                    LAST_ANALYSIS_STATS, MALICIOUS_ATTR, "network");
        }, null, e -> LOG.info("ERROR SEARCHING {} {}", ip, e.getMessage()));
    }

    public static File[] getUrlInformation(String url) throws IOException {

        String string = SupplierEx.getFirst(() -> tryToCreateUrl(url),
                () -> url.contains("/") ? "https://" + url : "http://" + url + "/");

        File outFile = newJsonFile(string);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/urls/" + HashVerifier.getSha256Hash(string), outFile);
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