package ethical.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import schema.sngpc.JsonExtractor;
import utils.ConsumerEx;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SupplierEx;

public final class VirusTotalApi {
    private static final String ATTRIBUTES = "attributes";
    public static final Logger LOG = HasLogging.log();
    private static final String VIRUSTOTAL_APIKEY = "397249a87cac6415141dde0a2263710c23166cc759dc89b681e8df70cc536abd";

    private VirusTotalApi() {
    }

    public static File[] getFilesInformation(Path path) throws IOException {
        return getFilesInformation(path, HashVerifier.getSha256Hash(path));
    }

    public static File[] getFilesInformation(Path path, String hash) throws IOException {
        String string = path.getName(path.getNameCount() - 1).toString();
        File outFile = rename(newJsonFile(hash), string);

        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES,
                "last_analysis_stats", "malicious", "type_description", "tags", "type", "trid", "magic",
                "meaningful_name", "file_type", "probability");
        Matcher matcher = Pattern.compile("malicious=([^0]\\d*)").matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious FILE {} {}", path, group);
            return new File[] { outFile };
        }

        return new File[] {};
    }

    public static File[] getIpInformation(String ip) throws IOException {
        return getIpInformation(ip, true);
    }

    public static File[] getIpInformation(String ip, boolean onlyMalicious) throws IOException {
        File outFile = newJsonFile(ip);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES, "id",
                "last_analysis_stats", "malicious");

        Matcher matcher = Pattern.compile("malicious=([^0]\\d*)").matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious IP {} {}", ip, group);
            return new File[] { outFile };
        }
        return onlyMalicious ? new File[] {} : new File[] { outFile };
    }

    public static File[] getUrlInformation(String url) throws IOException {

        String string = SupplierEx.getIgnore(() -> new URL(url).toString(), "http://" + url + "/");

        File outFile = newJsonFile(string);
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/urls/" + HashVerifier.getSha256Hash(string), outFile);
        }
        String displayJsonFromFile = JsonExtractor.displayJsonFromFile(outFile);
        LOG.info(displayJsonFromFile);
        return new File[] { outFile };
    }

    public static void main(String[] args) {
        Arrays.asList("safebrowsing.googleapis.com", "tracking-protection.cdn.mozilla.net",
                "shavar.services.mozilla.com", "lh6.googleusercontent.com", "lh3.googleusercontent.com",
                "lh5.googleusercontent.com", "lh4.googleusercontent.com", "lh6.googleusercontent.com",
                "lh4.googleusercontent.com", "lh3.googleusercontent.com", "lh5.googleusercontent.com",
                "firefox.settings.services.mozilla.com", "cse.google.com", "www.virustotal.com", "www.googleapis.com",
                "storage.googleapis.com", "clients1.google.com", "status.rapidssl.com", "aus5.mozilla.org",
                "versioncheck-bg.addons.mozilla.org", "apis.google.com", "nmap.org", "nmap.org", "img.youtube.com",
                "www.youtube.com", "img.youtube.com", "easylist-downloads.adblockplus.org",
                "notification.adblockplus.org", "mitarchive.info", "mitarchive.info", "gmzdaily.com",
                "catalogue.maps.elastic.co", "tiles.maps.elastic.co", "play.google.com", "0.client-channel.google.com",
                "mail.google.com", "0.client-channel.google.com", "chat-pa.clients6.google.com",
                "people-pa.clients6.google.com", "people-pa.clients6.google.com", "clients6.google.com",
                "mail.google.com", "play.google.com", "http://wwwcztapwlwk.net/plafgxc80333067532").stream().distinct()
                .forEach(ConsumerEx
                        .makeConsumer(s -> LOG.info("{}", getUrlInformation(s)[0])));

    }

    private static void getFromURL(String url, File outFile) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet post = new HttpGet(url);
        post.addHeader("x-apikey", VIRUSTOTAL_APIKEY);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
        List<String> collect = rd.lines().collect(Collectors.toList());
        Files.write(outFile.toPath(), collect);

    }

    private static File newJsonFile(String string) {
        String replaceAll = string.replaceAll("[:/]+", "_");
        return ResourceFXUtils.getOutFile(replaceAll + ".json");
    }

    private static File rename(File outFile, String string) {
        File outFile3 = newJsonFile(string);
        if (outFile.exists()) {
            boolean renameTo = outFile.renameTo(outFile3);
            if (!renameTo) {
                return outFile;
            }
            LOG.info("{} renamed to {} {}", outFile, outFile3, renameTo);
        }
        return outFile3;
    }
}
