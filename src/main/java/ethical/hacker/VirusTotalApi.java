package ethical.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class VirusTotalApi {
    private static final Logger LOG = HasLogging.log();
    private static final String VIRUSTOTAL_APIKEY = "397249a87cac6415141dde0a2263710c23166cc759dc89b681e8df70cc536abd";

    public static void getFilesInformation(String hash) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(hash + ".json");
        if (!outFile.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash, outFile);
        }
        JsonNode rootNode = displayJsonFromFile(outFile, "data", "attributes", "last_modification_date",
                "type_description", "tags", "type", "trid", "magic", "meaningful_name", "file_type", "probability");
        LOG.info("{}", toObject(rootNode, 0));
        File outFile2 = ResourceFXUtils.getOutFile(hash + "behaviours.json");
        if (!outFile2.exists()) {
            getFromURL("https://www.virustotal.com/api/v3/files/" + hash + "/behaviours", outFile2);
        }
        displayJsonFromFile(outFile2);
    }

    private static JsonNode displayJsonFromFile(File outFile, String... a) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(outFile.toPath()));
        StringBuilder yaml2 = new StringBuilder();
        processNode(rootNode, yaml2, 0, a);
        LOG.info("{}", yaml2);
        return rootNode;
    }

    private static void getFromURL(String url, File outFile) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet post = new HttpGet(url);
        post.addHeader("x-apikey", VIRUSTOTAL_APIKEY);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

        Stream<String> lines = rd.lines();
        List<String> collect = lines.collect(Collectors.toList());
        Files.write(outFile.toPath(), collect);
    }

    private static void processNode(JsonNode jsonNode, StringBuilder yaml, int depth, String... filters) {
        if (jsonNode.isValueNode()) {
            String asText = jsonNode.asText();
            if (asText.matches("\\d{10}")) {
                long epochSecond = Long.parseLong(asText);
                yaml.append(
                        Instant.ofEpochSecond(epochSecond).atZone(ZoneId.systemDefault()).toLocalDateTime());
                return;
            }
            yaml.append(asText);
            return;
        }
        String repeat = StringUtils.repeat(" ", depth);
        if (jsonNode.isArray()) {
            yaml.append("[");
            for (Iterator<JsonNode> iterator = jsonNode.iterator(); iterator.hasNext();) {
                JsonNode arrayItem = iterator.next();
                yaml.append("\n" + repeat + " ");
                processNode(arrayItem, yaml, depth + 1, filters);
                if (iterator.hasNext()) {
                    yaml.append(",");
                }
            }
            yaml.append("\n" + repeat + "]");
            return;
        }
        if (jsonNode.isObject()) {
            yaml.append("{");
            for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
                Entry<String, JsonNode> next = iterator.next();
                String key = next.getKey();
                if (filters.length == 0 || Arrays.asList(filters).contains(key)) {
                    yaml.append("\n" + repeat + " " + key + "=");
                    ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(filters));
                    arrayList.remove(key);
                    String[] array = arrayList.toArray(new String[] {});
                    processNode(next.getValue(), yaml, depth + 1, array);
                    if (iterator.hasNext()) {
                        yaml.append(",");
                    }
                }
            }
            yaml.append("\n" + repeat + "}");
        }
    }

    private static Object toObject(JsonNode jsonNode, int depth) {
        if (jsonNode.isValueNode()) {
            return jsonNode.asText();
        }
        if (jsonNode.isArray()) {
            List<Object> arrayList = new ArrayList<>();
            for (JsonNode arrayItem : jsonNode) {
                arrayList.add(toObject(arrayItem, depth + 1));
            }
            return arrayList;
        }
        if (jsonNode.isObject()) {
            Map<String, Object> hashMap = new HashMap<>();
            for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
                Entry<String, JsonNode> next = iterator.next();
                hashMap.put(next.getKey(), toObject(next.getValue(), depth + 1));
            }
            return hashMap;
        }
        return null;

    }
}