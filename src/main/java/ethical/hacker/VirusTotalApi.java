package ethical.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        File outFile = ResourceFXUtils.getOutFile(hash + ".json");
        File outFile2 = ResourceFXUtils.getOutFile(hash + "behaviours.json");
        if (path != null) {
            String string = path.getName(path.getNameCount() - 1).toString();
            outFile = rename(outFile, string);
            outFile2 = rename(outFile2, string + "behaviours");
        }

        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES, "last_analysis_stats", "malicious",
                "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type", "probability");
        if (!outFile2.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash + "/behaviours", outFile2);
        }
        JsonExtractor.displayJsonFromFile(outFile2, "data", ATTRIBUTES, "meaningful_name", "tags", "type");
        return new File[] { outFile, outFile2 };
    }


    public static File getIpInformation(String ip) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(ip + ".json");
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
        }
        JsonExtractor.displayJsonFromFile(outFile, "data", ATTRIBUTES, "id", "last_analysis_stats", "malicious");
        return outFile;
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

    private static File rename(File outFile, String string) {
        File outFile3 = ResourceFXUtils.getOutFile(string + ".json");
        if (outFile.exists()) {
            boolean renameTo = outFile.renameTo(outFile3);
            LOG.info("{} renamed to {} {}", outFile, outFile3, renameTo);
        }
        return outFile3;
    }
}
