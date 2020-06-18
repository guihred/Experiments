package ethical.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
import utils.HasLogging;
import utils.ResourceFXUtils;

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
                "last_analysis_stats", "malicious",
                "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type", "probability");
        Matcher matcher = Pattern.compile("malicious=([^0]\\d*)").matcher(displayJsonFromFile);
        if (matcher.find()) {
            String group = matcher.group(1);
            LOG.info("Malicious FILE {} {}", path, group);
            return new File[] { outFile };
        }

        return new File[] {};
    }

    public static File[] getIpInformation(String ip) throws IOException {
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
        return new File[] {};
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
        return ResourceFXUtils.getOutFile(string + ".json");
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
