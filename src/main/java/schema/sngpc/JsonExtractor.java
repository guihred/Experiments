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
import utils.DateFormatUtils;

public final class JsonExtractor {


    private JsonExtractor() {
    }

    public static void addValue(JsonNode item, TreeItem<Map<String, String>> e) {
        if (item.isValueNode()) {
            Set<String> keySet = e.getValue().keySet();
            for (String string : keySet) {
                merge(e, string, item);
            }
            return;
        }
        Iterator<String> attributes = item.fieldNames();
        while (attributes.hasNext()) {
            String nodeName = attributes.next();
            JsonNode item2 = item.get(nodeName);
            merge(e, nodeName, item2);
            if (item2.isArray()) {
                for (JsonNode string : item2) {
                    merge(e, nodeName, string);
                }
            }
        }
    }

    public static String convertObj(JsonNode jsonNode) {
        String asText = jsonNode.asText();
        if (!asText.matches("\\d{10}")) {
            return asText;
        }
        return DateFormatUtils.epochSecondToLocalDate(asText).toString();
    }

    public static String displayJsonFromFile(File outFile, String... a) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(outFile.toPath()));
        StringBuilder yaml2 = new StringBuilder();
        processNode(rootNode, yaml2, 0, a);

        return yaml2.toString();
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
            return convertObj(jsonNode);
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

    private static void appendJsonArray(JsonNode jsonNode, StringBuilder yaml, int depth, String... filters) {
        String repeat = StringUtils.repeat(" ", depth);

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
    }

    private static void appendJsonObject(JsonNode jsonNode, StringBuilder yaml, int depth, String... filters) {
        String repeat = StringUtils.repeat(" ", depth);
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

    private static void clearEmptyEntries(Map<String, String> newMap) {
        List<String> collect = newMap.entrySet().stream().filter(eb -> StringUtils.isBlank(eb.getValue()))
                .map(Entry<String, String>::getKey).collect(Collectors.toList());
        for (String key : collect) {
            newMap.remove(key);
        }
    }

    private static void merge(TreeItem<Map<String, String>> e, String nodeName, JsonNode item2) {
        if (item2.isValueNode()) {
            e.getValue().merge(nodeName, convertObj(item2),
                    (old, val) -> StringUtils.isBlank(old) ? val : old + "\n" + val);
        }
    }

    private static Map<String, String> newMap(Entry<String, JsonNode> item) {
        if (!item.getValue().isObject()) {
            return newMap(item.getKey(), convertObj(item.getValue()));
        }
        return newMap(item.getKey(), "");
    }

    private static void processNode(JsonNode jsonNode, StringBuilder yaml, int depth, String... filters) {
        if (jsonNode.isValueNode()) {
            yaml.append(JsonExtractor.convertObj(jsonNode));
        } else if (jsonNode.isArray()) {
            appendJsonArray(jsonNode, yaml, depth, filters);
        } else if (jsonNode.isObject()) {
            appendJsonObject(jsonNode, yaml, depth, filters);
        }
    }

    private static void readArray(Map<JsonNode, TreeItem<Map<String, String>>> allItems, List<JsonNode> currentNodes,
            JsonNode domNode, Entry<String, JsonNode> item) {
        Map<String, String> newMap = newMap(item);
        TreeItem<Map<String, String>> e = new TreeItem<>(newMap);
        e.setGraphic(SimpleTextBuilder.newBoldText(item.getKey()));
        if (!allItems.containsKey(domNode) && allItems.containsKey(item.getValue())) {
            allItems.putIfAbsent(domNode, allItems.get(item.getValue()).getParent());
        }
        allItems.get(domNode).getChildren().add(e);
        allItems.putIfAbsent(item.getValue(), e);
        int i = 0;
        for (JsonNode jsonNode : item.getValue()) {
            if (!jsonNode.isObject()) {
                addValue(jsonNode, e);
                continue;
            }
            currentNodes.add(0, jsonNode);
            String key = "" + i;
            Map<String, String> newMap2 = newMap(key, jsonNode.asText(key));
            TreeItem<Map<String, String>> e2 = new TreeItem<>(newMap2);
            e2.setGraphic(SimpleTextBuilder.newBoldText(key));
            allItems.get(item.getValue()).getChildren().add(e2);
            allItems.putIfAbsent(jsonNode, e2);
            addValue(jsonNode, e2);
            clearEmptyEntries(newMap2);
            i++;
        }
        clearEmptyEntries(newMap);
    }

    private static void readObject(Map<JsonNode, TreeItem<Map<String, String>>> allItems, List<JsonNode> currentNodes,
            JsonNode domNode, Entry<String, JsonNode> item) {
        currentNodes.add(0, item.getValue());
        Map<String, String> newMap = newMap(item);
        TreeItem<Map<String, String>> e = new TreeItem<>(newMap);
        if (!allItems.containsKey(domNode) && allItems.containsKey(item.getValue())) {
            allItems.putIfAbsent(domNode, allItems.get(item.getValue()).getParent());
        }
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
        allItems.values().stream().map(TreeItem<Map<String, String>>::getValue).filter(Objects::nonNull)
                .forEach(JsonExtractor::clearEmptyEntries);

    }
}
