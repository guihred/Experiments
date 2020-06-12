package schema.sngpc;

import static utils.RunnableEx.remap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import simplebuilder.SimpleTextBuilder;

public final class JsonExtractor {

    private JsonExtractor() {
    }

    public static void addValue(JsonNode item, TreeItem<Map<String, String>> e) {
        if (item.isValueNode()) {
            Set<String> keySet = e.getValue().keySet();
            for (String string : keySet) {
                String asText = item.asText();
                e.getValue().merge(string, asText, (old, val) -> StringUtils.isBlank(old) ? val : old + "\n" + val);
            }
            return;
        }

        Iterator<String> attributes = item.fieldNames();
        while (attributes.hasNext()) {
            String nodeName = attributes.next();
            JsonNode item2 = item.get(nodeName);
            if (item2.isValueNode()) {
                e.getValue().merge(nodeName, item2.asText(), (old, val) -> old + "\n" + val);
            }
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

    private static void clearEmptyEntries(Map<String, String> newMap) {
        List<String> collect = newMap.entrySet().stream().filter(eb -> StringUtils.isBlank(eb.getValue()))
                .map(Entry<String, String>::getKey).collect(Collectors.toList());
        for (String key : collect) {
            newMap.remove(key);
        }
    }

    private static Map<String, String> newMap(Entry<String, JsonNode> item) {
        if(item.getValue().isValueNode()) {
            return newMap(item.getKey(), item.getValue().asText());
        }
        return new LinkedHashMap<>();
    }

    private static void readArray(Map<JsonNode, TreeItem<Map<String, String>>> allItems, List<JsonNode> currentNodes,
            JsonNode domNode, Entry<String, JsonNode> item) {
        String asText = item.getValue().asText();
        Map<String, String> newMap = newMap(item.getKey(), asText);
        TreeItem<Map<String, String>> e = new TreeItem<>(newMap);
        e.setGraphic(SimpleTextBuilder.newBoldText(item.getKey()));
        if (!allItems.containsKey(domNode) && allItems.containsKey(item.getValue())) {
            allItems.putIfAbsent(domNode, allItems.get(item.getValue()).getParent());
        }
        allItems.get(domNode).getChildren().add(e);
        allItems.putIfAbsent(item.getValue(), e);
        for (JsonNode jsonNode : item.getValue()) {
            addValue(jsonNode, e);
            if (jsonNode.isObject()) {
                currentNodes.add(0, jsonNode);
            }
        }
        clearEmptyEntries(newMap);
    }

    private static void readObject(Map<JsonNode, TreeItem<Map<String, String>>> allItems, List<JsonNode> currentNodes,
            JsonNode domNode, Entry<String, JsonNode> item) {
        currentNodes.add(0, item.getValue());
        Map<String, String> newMap = newMap(item);
        TreeItem<Map<String, String>> e = new TreeItem<>(newMap);
        allItems.get(domNode).getChildren().add(e);
        allItems.put(item.getValue(), e);
        e.setGraphic(SimpleTextBuilder.newBoldText(item.getKey()));
        addValue(item.getValue(), e);
    }

    private static void tryToRead(TreeView<Map<String, String>> build,
            Map<JsonNode, TreeItem<Map<String, String>>> allItems, File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(file.toPath()));
        List<JsonNode> currentNodes = new ArrayList<>();
        currentNodes.add(rootNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(rootNode.asText(), rootNode.textValue()));
        value.setGraphic(SimpleTextBuilder.newBoldText(rootNode.asText()));
        build.setRoot(value);
        allItems.put(rootNode, value);
        while (!currentNodes.isEmpty()) {
            JsonNode domNode = currentNodes.remove(0);
            Iterator<Entry<String, JsonNode>> childNodes = domNode.fields();
            while (childNodes.hasNext()) {
                Entry<String, JsonNode> item = childNodes.next();
                if (item.getValue().isArray()) {
                    readArray(allItems, currentNodes, domNode, item);
                }
                if (item.getValue().isObject()) {
                    readObject(allItems, currentNodes, domNode, item);
                }
            }
        }
    }
}
