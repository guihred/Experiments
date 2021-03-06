package extract.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import simplebuilder.SimpleTextBuilder;
import utils.ResourceFXUtils;
import utils.SimpleMap;
import utils.StringSigaUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class JsonExtractor {

    private JsonExtractor() {
    }

    public static <T> T access(Object root, Class<T> cl, Object... param) {
        Object o = root;
        for (Object object : param) {
            o = FunctionEx.apply(ob -> {
                if (object instanceof String) {
                    return ((Map<?, ?>) ob).get(object);
                }
                if (object instanceof Integer) {
                    if (ob instanceof Map) {
                        return ((Map<?, ?>) ob).get(object);
                    }
                    return ((List<?>) ob).get(((Integer) object).intValue());
                }
                return ob;
            }, o, o);
        }

        return cl.cast(o);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> accessList(Object root, Object... param) {
        Object o = root;
        for (Object object : param) {
            o = FunctionEx.apply(ob -> {
                if (object instanceof String) {
                    return ((Map<?, ?>) ob).get(object);
                }
                if (object instanceof Integer) {
                    return ((List<?>) ob).get(((Integer) object).intValue());
                }
                return ob;
            }, o, o);
        }
        if (List.class.isInstance(o)) {
            return List.class.cast(o);
        }
        List<T> arrayList = new ArrayList<>();
        arrayList.add((T) o);
        return arrayList;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> accessMap(Object root, Object... param) {
        return access(root, Map.class, param);
    }

    public static String displayJsonFromFile(File outFile, String... a) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(outFile.toPath()));
        StringBuilder yaml2 = new StringBuilder();
        processNode(rootNode, yaml2, 0, a);
        return yaml2.toString();
    }

    public static boolean isNotRecentFile(File outFile) {
        return !outFile.exists() || oneHourModified(outFile);
    }

    public static boolean isRecentFile(File outFile, int hours) {
        return outFile.exists() && !moreThanXHoursModified(outFile, hours);
    }

    public static Map<String, String> makeMapFromJsonFile(File outFile, String... a) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(outFile.toPath()));
        Map<String, String> yaml2 = new LinkedHashMap<>();
        for (String string : a) {
            yaml2.put(string, "");
        }
        processNode(rootNode, yaml2, 0, a);
        return yaml2;
    }

    public static <K, T> Map.Entry<K, T> newEntry(K key, T value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map.Entry<String, String> newEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map<String, String> newMap(String key, String value) {
        return new SimpleMap(key, value);
    }

    public static void readJsonFile(TreeView<Map<String, String>> build, File file) {
        Map<JsonNode, TreeItem<Map<String, String>>> allItems = new LinkedHashMap<>();
        RunnableEx.remap(() -> tryToRead(build, allItems, file), "ERROR READING");
    }

    public static List<Map<String, String>> remap(Map<String, String> object) {
        List<List<String>> listOfFields =
                object.values().stream().map(s -> Arrays.asList(s.split("\n"))).collect(Collectors.toList());
        int maxNumFields = listOfFields.stream().mapToInt(List<String>::size).max().orElse(0);
        List<String> keys = object.keySet().stream().collect(Collectors.toList());
        List<Map<String, String>> remappedObjs = new ArrayList<>();
        for (int i = 0; i < maxNumFields; i++) {
            Map<String, String> mapOfFields = new LinkedHashMap<>();
            int j = i;
            List<String> remapped =
                    listOfFields.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.toList());
            IntStream.range(0, keys.size()).forEach(k -> mapOfFields.put(keys.get(k), remapped.get(k)));
            remappedObjs.add(mapOfFields);
        }
        return remappedObjs;
    }

    public static List<Map<String, String>> remap(Map<String, String> ob, String regex) {
        if (StringUtils.isBlank(regex)) {
            return remap(ob);
        }
        List<List<String>> listOfFields =
                ob.values().stream().map(s -> Arrays.asList(s.split("\n"))).collect(Collectors.toList());
        int maxNumOfFields = listOfFields.stream().mapToInt(List<String>::size).max().orElse(0);
        List<String> keys = ob.keySet().stream().collect(Collectors.toList());
        List<Map<String, String>> finalList = new ArrayList<>();
        List<List<String>> partialList = new ArrayList<>();
        Map<String, String> reference = null;
        for (int i = 0; i < maxNumOfFields; i++) {
            int j = i;
            List<String> elementsAtJ =
                    listOfFields.stream().map(e -> j < e.size() ? e.get(j) : "").collect(Collectors.toList());
            if (elementsAtJ.stream().anyMatch(s -> s.matches(regex))) {
                reference = new LinkedHashMap<>();
                Map<String, String> m = reference;
                IntStream.range(0, keys.size()).forEach(k -> JsonExtractor.merge(regex, keys, elementsAtJ, m, k));
                reference = JsonExtractor.processPartialList(regex, keys, finalList, partialList, reference);
            } else if (reference == null) {
                partialList.add(elementsAtJ);
            } else {
                Map<String, String> newMap = new LinkedHashMap<>(reference);
                newMap.remove(reference.entrySet().stream().filter(e -> !e.getValue().matches(regex)).findFirst()
                        .map(Entry<String, String>::getKey).orElse(null));
                IntStream.range(0, keys.size()).forEach(k -> JsonExtractor.merge(regex, keys, elementsAtJ, newMap, k));
                finalList.add(newMap);
            }

        }
        return finalList;
    }

    public static boolean splitList(List<Map<String, String>> list, Map<String, String> newItem) {
        long count =
                newItem.values().stream().mapToInt(e -> e.split("\n").length).filter(i -> i > 1).distinct().count();
        if (count != 1) {
            return false;
        }
        Set<Entry<String, String>> entrySet = newItem.entrySet();
        list.clear();
        for (Entry<String, String> entry : entrySet) {
            List<Map<String, String>> valueLines = Stream.of(entry.getValue().split("\n"))
                    .map(e -> newMap(entry.getKey(), e)).collect(Collectors.toList());
            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> map = list.get(i);
                    map.putAll(valueLines.get(i));
                }
                continue;
            }
            list.addAll(valueLines);
        }
        return true;
    }

    public static Object toFullObject(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(file.toPath()));
        return toObject(rootNode, 0, new LinkedHashMap<>());
    }

    public static Object toObject(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(file.toPath()));
        return toObject(rootNode, 0);
    }

    public static Map<String, Object> toObject(File file, String... f) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(Files.newInputStream(file.toPath()));
        Map<String, Object> linkedHashMap = new LinkedHashMap<>();
        toObject(rootNode, 0, linkedHashMap, f);
        List<String> fields = Arrays.asList(f);
        return linkedHashMap.entrySet().stream().sorted(Comparator.comparing(fi -> fields.indexOf(fi.getKey())))
                .collect(Collectors.toMap(Entry<String, Object>::getKey, Entry<String, Object>::getValue,
                        SupplierEx::nonNull, LinkedHashMap::new));
    }

    public static Object toObjectFromJsonContent(String fileContent, String... a) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(fileContent);
        return toObject(rootNode, 0, new LinkedHashMap<>(), a);
    }

    private static void addValue(JsonNode item, TreeItem<Map<String, String>> e) {
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

    private static String appendJsonArray(JsonNode jsonNode, Map<String, String> yaml, int depth, String... filters) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<JsonNode> iterator = jsonNode.iterator(); iterator.hasNext();) {
            JsonNode arrayItem = iterator.next();
            stringBuilder.append(processNode(arrayItem, yaml, depth + 1, filters));
            if (iterator.hasNext()) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
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

    private static String appendJsonObject(JsonNode jsonNode, Map<String, String> ob, int depth, String... filters) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("{");
        for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
            Entry<String, JsonNode> next = iterator.next();
            String key = next.getKey();
            yaml.append("\n" + " \"" + key + "\":");
            String value = processNode(next.getValue(), ob, depth + 1, filters);
            yaml.append(value);
            if (filters.length == 0 || Arrays.asList(filters).contains(key)) {
                ob.merge(key, value, (o, n) -> Stream.of(o, n).collect(Collectors.joining("\n")).trim());
                int orElse = ob.values().stream().mapToInt(s -> s.split("\n").length).max().orElse(0);
                ob.entrySet().stream().filter(e -> e.getValue().split("\n").length < orElse - 1).forEach(e -> {
                    String[] split = e.getValue().split("\n");
                    int a = Math.abs(split.length - orElse + 1);
                    for (int i = 0; i < a; i++) {
                        ob.merge(e.getKey(), split[split.length - 1],
                                (o, n) -> Stream.of(o, n).collect(Collectors.joining("\n")).trim());
                    }
                });

            }
            if (iterator.hasNext()) {
                yaml.append(",");
            }
        }
        yaml.append("\n" + "}");
        return yaml.toString();
    }

    private static void appendJsonObject(JsonNode jsonNode, StringBuilder yaml, int depth, String... filters) {
        String repeat = StringUtils.repeat(" ", depth);
        yaml.append("{");
        for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
            Entry<String, JsonNode> next = iterator.next();
            String key = next.getKey();
            if (filters.length == 0 || Arrays.asList(filters).contains(key)) {
                yaml.append("\n" + repeat + " \"" + key + "\":");
                List<String> filtersCopy = new ArrayList<>(Arrays.asList(filters));
                filtersCopy.remove(key);
                String[] array = filtersCopy.toArray(new String[] {});
                processNode(next.getValue(), yaml, depth + 1, array);
                if (iterator.hasNext()) {
                    yaml.append(",");
                }
            }
        }
        yaml.append("\n" + repeat + "}");
    }

    private static void clearEmptyEntries(Map<String, String> newMap) {
        List<String> blankValues = newMap.entrySet().stream().filter(eb -> StringUtils.isBlank(eb.getValue()))
                .map(Entry<String, String>::getKey).collect(Collectors.toList());
        for (String key : blankValues) {
            newMap.remove(key);
        }
    }

    private static String convertObj(JsonNode jsonNode) {
        return jsonNode.asText();
    }

    private static Object fullObj(JsonNode jsonNode) {
        if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        }

        if (jsonNode.isDouble()) {
            return jsonNode.asDouble();
        }
        if (jsonNode.isInt()) {
            return jsonNode.asInt();
        }
        if (jsonNode.isLong()) {
            return jsonNode.asLong();
        }
        return jsonNode.asText();
    }

    private static boolean getListClass(List<?> a) {
        Class<? extends Object> orElse = a.stream().findFirst().map(Object::getClass).orElse(null);
        return isAggregatable(orElse);
    }

    private static boolean isAggregatable(Class<? extends Object> orElse) {
        return orElse != null
                && (Arrays.asList(Integer.class, Boolean.class, Double.class, Long.class, String.class).contains(orElse)
                        || orElse.isPrimitive());
    }

    private static void merge(String regex, List<String> keys, List<String> collect2, Map<String, String> linkedHashMap,
            int k) {
        int l = 0;
        for (; linkedHashMap.containsKey(keys.get(k) + l); l++) {
            if (Objects.equals(linkedHashMap.get(keys.get(k) + l), collect2.get(k))) {
                return;
            }
            if (!linkedHashMap.get(keys.get(k) + l).matches(regex)) {
                break;
            }
        }

        linkedHashMap.merge(keys.get(k) + l, collect2.get(k), (o, n) -> Objects.equals(o, n) ? n : o + "\n" + n);
    }

    private static void merge(TreeItem<Map<String, String>> e, String nodeName, JsonNode item2) {
        if (item2.isValueNode()) {
            e.getValue().merge(nodeName, convertObj(item2),
                    (old, val) -> StringUtils.isBlank(old) ? val : old + "\n" + val);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object mergeObjs(Object a, Object b) {
        if (a instanceof List && getListClass((List) a) == isAggregatable(b.getClass())) {
            ((List) a).add(b);
            return a;
        }
        if (b instanceof List && getListClass((List) b) == isAggregatable(a.getClass())) {
            ((List) b).add(a);
            return b;
        }
        if (b instanceof List && a instanceof List && getListClass((List) b) == getListClass((List) a)) {
            ((List) b).addAll((List) a);
            return b;
        }
        if (isAggregatable(a.getClass()) == isAggregatable(b.getClass())) {
            return new ArrayList<>(Arrays.asList(a, b));
        }
        HasLogging.log().error("FIX MERGE {} {}", a, b);
        return a;
    }

    private static boolean moreThanXHoursModified(File outFile, int hours) {
        FileTime lastModifiedTime = ResourceFXUtils.computeAttributes(outFile).lastModifiedTime();
        Instant instant = lastModifiedTime.toInstant();
        long between = ChronoUnit.HOURS.between(instant, Instant.now());
        return between > hours;
    }

    private static Map<String, String> newMap(Entry<String, JsonNode> item) {
        if (!item.getValue().isObject()) {
            return newMap(item.getKey(), convertObj(item.getValue()));
        }
        return newMap(item.getKey(), "");
    }

    private static boolean oneHourModified(File outFile) {
        return moreThanXHoursModified(outFile, 1);
    }

    private static String processNode(JsonNode jsonNode, Map<String, String> yaml, int depth, String... filters) {
        if (jsonNode.isValueNode()) {
            return JsonExtractor.convertObj(jsonNode);
        } else if (jsonNode.isArray()) {
            return appendJsonArray(jsonNode, yaml, depth, filters);
        } else if (jsonNode.isObject()) {
            return appendJsonObject(jsonNode, yaml, depth, filters);
        }
        return "";
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

    private static Map<String, String> processPartialList(String regex, List<String> keys,
            Collection<Map<String, String>> finalList, Collection<List<String>> partialList,
            Map<String, String> reference) {
        if (partialList.isEmpty()) {
            return reference;
        }
        for (List<String> list : partialList) {
            Map<String, String> newMap = new LinkedHashMap<>(reference);
            newMap.remove(reference.entrySet().stream().filter(e -> !e.getValue().matches(regex)).findFirst()
                    .map(Entry<String, String>::getKey).orElse(null));
            IntStream.range(0, keys.size()).forEach(k -> merge(regex, keys, list, newMap, k));
            finalList.add(newMap);
        }
        partialList.clear();
        return null;
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

    private static SimpleMap toMap(JsonNode rootNode) {
        Object object = toObject(rootNode, 1);
        if (object instanceof Map) {
            return accessMap(object).entrySet().stream().filter(e -> e.getValue() instanceof String)
                    .collect(Collectors.toMap(e -> StringSigaUtils.toStringSpecial(e.getKey()),
                            e -> StringSigaUtils.toStringSpecial(e.getValue()), (u, v) -> u, SimpleMap::new));
        }
        return new SimpleMap();
    }

    private static Object toObject(JsonNode jsonNode, int depth) {
        if (jsonNode.isValueNode()) {
            return fullObj(jsonNode);
        }
        if (jsonNode.isArray()) {
            List<Object> arrayObject = new ArrayList<>();
            for (JsonNode arrayItem : jsonNode) {
                arrayObject.add(toObject(arrayItem, depth + 1));
            }
            return arrayObject;
        }
        if (jsonNode.isObject()) {
            Map<String, Object> mapObject = new LinkedHashMap<>();
            for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
                Entry<String, JsonNode> next = iterator.next();
                mapObject.put(next.getKey(), toObject(next.getValue(), depth + 1));
            }
            return mapObject;
        }
        return null;

    }

    private static Object toObject(JsonNode jsonNode, int depth, Map<String, Object> finalMap, String... f) {
        if (jsonNode.isValueNode()) {
            return fullObj(jsonNode);
        }
        if (jsonNode.isArray()) {
            List<Object> arrayObject = new ArrayList<>();
            for (JsonNode arrayItem : jsonNode) {
                arrayObject.add(toObject(arrayItem, depth + 1, finalMap, f));
            }
            return arrayObject;
        }
        if (jsonNode.isObject()) {
            Map<String, Object> mapObject = new LinkedHashMap<>();
            for (Iterator<Entry<String, JsonNode>> iterator = jsonNode.fields(); iterator.hasNext();) {
                Entry<String, JsonNode> next = iterator.next();
                Object merge = mapObject.merge(next.getKey(), toObject(next.getValue(), depth + 1, finalMap, f),
                        JsonExtractor::mergeObjs);
                if (f.length == 0 || Arrays.asList(f).contains(next.getKey())) {
                    finalMap.merge(next.getKey(), merge, JsonExtractor::mergeObjs);
                }

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
        List<JsonNode> currentNodes = new ArrayList<>();
        currentNodes.add(rootNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(toMap(rootNode));
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
