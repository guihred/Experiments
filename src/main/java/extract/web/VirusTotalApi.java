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
import utils.ProjectProperties;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class VirusTotalApi {
    private static final String API_V3_FILES = "api/v3/files/";
    private static final String VIRUSTOTAL_COM = ProjectProperties.getField();
    private static final String ERROR_TAG = "error";
    private static final String MALICIOUS_POSITIVE_REGEX = "malicious=([^0]\\d*)";
    private static final String LAST_ANALYSIS_STATS = "last_analysis_stats";
    private static final String MALICIOUS_ATTR = "malicious";
    private static final String ATTRIBUTES = "attributes";
    private static final Logger LOG = HasLogging.log();
    private static final String VIRUSTOTAL_APIKEY = ProjectProperties.getField();
    private static final String[] FILE_ATTRIBUTES = { "data", ATTRIBUTES, LAST_ANALYSIS_STATS, MALICIOUS_ATTR,
            "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type", "probability" };

    private VirusTotalApi() {
    }

    public static File getFilesInformation(Path path) throws IOException {
        String filename = path.getName(path.getNameCount() - 1).toString();
        File outFile = newJsonFile(filename);
        if (!outFile.exists()) {
            getFromURL(VIRUSTOTAL_COM + API_V3_FILES + HashVerifier.getSha256Hash(path), outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, FILE_ATTRIBUTES);
        Matcher matcher = Pattern.compile(MALICIOUS_POSITIVE_REGEX).matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious FILE {} {}", path, group);
        }
        return outFile;
    }

    public static File getFilesInformation(String hash) throws IOException {
        File outFile = newJsonFile(hash);
        if (!outFile.exists()) {
            getFromURL(VIRUSTOTAL_COM + API_V3_FILES + hash, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, FILE_ATTRIBUTES);
        Matcher matcher = Pattern.compile(MALICIOUS_POSITIVE_REGEX).matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious FILE {} {}", hash, group);
        }
        return outFile;
    }

    public static Map<String, Object> getFilesInformation(String filename, String hash) throws IOException {
        File outFile = newJsonFile(filename);
        if (!outFile.exists()) {
            getFromURL(VIRUSTOTAL_COM + API_V3_FILES + hash, outFile);
        }
        String trid = "trid";
        String data = "data";
        String idkey = "id";
        Map<String, Object> object = JsonExtractor.accessMap(JsonExtractor.toObject(outFile, FILE_ATTRIBUTES));
        String string = Objects.toString(object.get(idkey), "");
        if (StringUtils.isNotBlank(string) && !string.equals(hash)) {
            outFile = newJsonFile(filename + "_" + hash);
            if (!outFile.exists()) {
                getFromURL(VIRUSTOTAL_COM + API_V3_FILES + hash, outFile);
            }
            object = JsonExtractor.accessMap(JsonExtractor.toObject(outFile, FILE_ATTRIBUTES));
        }
        object.remove(idkey);
        object.remove(data);
        object.remove(ATTRIBUTES);
        object.remove(trid);
        LOG.info("{}", object);

        return object;
    }

    public static Entry<File, List<String>> getIpInformation(String ip) throws IOException {
        File outFile = newJsonFile(ip);
        if (!outFile.exists()) {
            getFromURL(VIRUSTOTAL_COM + "api/v3/ip_addresses/" + ip, outFile);
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
                getFromURL(VIRUSTOTAL_COM + "api/v3/ip_addresses/" + ip, outFile);
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

    public static File getUrlInformation(String url) throws IOException {

        String fullUrl = SupplierEx.getFirst(() -> tryToCreateUrl(url),
                () -> url.contains("/") ? "https://" + url : "http://" + url + "/");

        File outFile = newJsonFile(fullUrl);
        if (!outFile.exists()) {
            getFromURL(VIRUSTOTAL_COM + "api/v3/urls/" + HashVerifier.getSha256Hash(fullUrl), outFile);
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
        return outFile;
    }


    private static void getFromURL(String url, File outFile) throws IOException {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("x-apikey", VIRUSTOTAL_APIKEY);
        PhantomJSUtils.makeGet(url, hashMap, outFile);
    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/\\?]+", "_");
        return ResourceFXUtils.getOutFile("json/ip/" + replaceAll + ".json");
    }

    private static String tryToCreateUrl(String url) throws MalformedURLException {
        return new URL(url).toString();
    }
}
