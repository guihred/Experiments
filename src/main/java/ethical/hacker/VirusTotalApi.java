package ethical.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.slf4j.Logger;
import schema.sngpc.JsonExtractor;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class VirusTotalApi {
    public static final Logger LOG = HasLogging.log();
    private static final String VIRUSTOTAL_APIKEY = "397249a87cac6415141dde0a2263710c23166cc759dc89b681e8df70cc536abd";

    private VirusTotalApi() {
    }

    public static File[] getFilesInformation(String hash) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(hash + ".json");
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        JsonNode rootNode = JsonExtractor.displayJsonFromFile(outFile, "data", "attributes", "last_modification_date",
                "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type", "probability");
        LOG.info("{}", JsonExtractor.toObject(rootNode, 0));
        File outFile2 = ResourceFXUtils.getOutFile(hash + "behaviours.json");
        if (!outFile2.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash + "/behaviours", outFile2);
        }
        JsonExtractor.displayJsonFromFile(outFile2);
        return new File[] { outFile, outFile2 };
    }

    public static File[] getIpInformation(String ip) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(ip + ".json");
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/ip_addresses/" + ip, outFile);
        }
        JsonNode rootNode = JsonExtractor.displayJsonFromFile(outFile);
        LOG.info("{}", JsonExtractor.toObject(rootNode, 0));

        return new File[] { outFile };
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
}
