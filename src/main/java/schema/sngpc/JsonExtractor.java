package schema.sngpc;

import static utils.RunnableEx.remap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import simplebuilder.SimpleTextBuilder;
import utils.HasLogging;

public final class JsonExtractor {
    private static final Logger LOG = HasLogging.log();

    private JsonExtractor() {
    }

    public static void addValue(JsonNode item, TreeItem<Map<String, String>> e) {
        try {
            Iterator<String> attributes = item.fieldNames();
            for (int i = 0; attributes.hasNext(); i++) {
                String nodeName = attributes.next();
                JsonNode item2 = item.get(nodeName);
                if (e.getValue() == null) {
                    e.setValue(newMap(nodeName, item.asText()));
                }
                e.getValue().put(nodeName, item2.asText());
            }

        } catch (Exception e2) {
            LOG.trace("", e2);
        }
    }


    public static Map.Entry<String, String> newEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map<String, String> newMap(String key, String value) {
        return new SimpleMap(key, value);
    }


    public static void readJsonFile(TreeView<Map<String, String>> build, File file) {
        Map<JsonNode, TreeItem<Map<String, String>>> allItems = new HashMap<>();
        remap(() -> tryToRead(build, allItems, file), "ERROR READING");
    }


    public static Object toObject(JsonNode jsonNode, int depth) {
        if (jsonNode.isValueNode()) {
            return jsonNode.asText();
        }
        if (jsonNode.isArray()) {
            List<Object> arrayObject = new ArrayList<>();
            for (JsonNode arrayItem : jsonNode) {
                arrayObject.add(toObject(arrayItem, depth + 1));
            }
            return arrayObject;
        }
        if (jsonNode.isObject()) {
            Map<String, Object> mapObject = new HashMap<>();
            for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
                Entry<String, JsonNode> next = iterator.next();
                mapObject.put(next.getKey(), toObject(next.getValue(), depth + 1));
            }
            return mapObject;
        }
        return null;

    }


    private static void tryToRead(TreeView<Map<String, String>> build,
            Map<JsonNode, TreeItem<Map<String, String>>> allItems, File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(file.toPath()));

        JsonNode domNode = rootNode;
        List<JsonNode> currentNodes = new ArrayList<>();
        currentNodes.add(domNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(domNode.asText(), domNode.textValue()));
        value.setGraphic(SimpleTextBuilder.newBoldText(domNode.asText()));
        build.setRoot(value);
        allItems.put(domNode, value);
        while (!currentNodes.isEmpty()) {
            domNode = currentNodes.remove(0);
            Iterator<Entry<String, JsonNode>> childNodes = domNode.fields();
            for (int i = 0; childNodes.hasNext(); i++) {
                Entry<String, JsonNode> item = childNodes.next();
                if (item.getValue().isObject()) {
                    currentNodes.add(0, item.getValue());
                    TreeItem<Map<String, String>> e = new TreeItem<>(
                            newMap(item.getKey(), item.getValue().asText()));
                    allItems.get(domNode).getChildren().add(e);
                    allItems.put(item.getValue(), e);
                    e.setGraphic(SimpleTextBuilder.newBoldText(item.getKey()));
                    addValue(item.getValue(), e);
                }
            }
        }
    }
}
