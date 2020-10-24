package fxml.utils;

import static simplebuilder.SimpleTextBuilder.newBoldText;
import static utils.ex.RunnableEx.remap;
import static utils.ex.RunnableEx.run;

import extract.ExcelService;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.SupplierEx;

public final class XMLExtractor {

    private XMLExtractor() {
    }

    public static void addValue(Node item, TreeItem<Map<String, String>> e) {
        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
            Node item2 = attributes.item(i);
            e.getValue().put(item2.getNodeName(), item2.getNodeValue());
        }
        if (!item.hasChildNodes()) {
            return;
        }
        NodeList childNodes = item.getChildNodes();
        List<Node> collect =
                IntStream.range(0, childNodes.getLength()).mapToObj(childNodes::item).collect(Collectors.toList());
        if (collect.stream().allMatch(n -> n.getNodeType() == Node.TEXT_NODE)) {
            if (collect.size() == 1) {
                String nodeValue = collect.get(0).getNodeValue();
                String nodeName = item.getNodeName();
                e.setValue(newMap(nodeName, nodeValue));
                return;
            }
            for (Node item2 : collect) {
                if (item2.getNodeType() == Node.TEXT_NODE) {
                    String nodeValue = item2.getNodeValue();
                    String nodeName = item.getNodeName();
                    e.getValue().put(nodeName, nodeValue);
                }
            }
        }
    }



    public static void exportToExcel(TreeView<Map<String, String>> tree, File file) {
        TreeItem<Map<String, String>> selectedItem = tree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        List<Map<String, String>> finalList = getFinalList(selectedItem);
        Map<String, FunctionEx<Map<String, String>, Object>> mapa = new LinkedHashMap<>();
        int orElse = finalList.stream().mapToInt(Map<String, String>::size).max().orElse(0);
        for (int j = 0; j < orElse; j++) {
            int k = j;
            mapa.put("" + j, t -> getValue(t, k));
        }

        File outFile = ResourceFXUtils.getOutFile("xlsx/" + file.getName().replaceAll("\\.xml", ".xlsx"));
        ExcelService.getExcel(finalList, mapa, outFile);
        ImageFXUtils.openInDesktop(outFile);
    }

    public static Map.Entry<String, String> newEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map<String, String> newMap(String key, String value) {
        return new SimpleMap(key, value);
    }

    public static void onSelectTreeItem(ObservableList<Map<String, String>> list,
            TableView<Map<String, String>> sideTable, TreeItem<Map<String, String>> newValue) {
        list.clear();
        sideTable.getColumns().clear();
        if (newValue == null) {
            return;
        }
        if (newValue.isLeaf()) {
            addColumns(sideTable, newValue.getValue().keySet());
            list.add(newValue.getValue());
            return;
        }
        if (newValue.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf)) {
            addColumns(sideTable, newValue.getValue().keySet());
            Map<String, String> newItem = new HashMap<>();
            newItem.putAll(newValue.getValue());
            list.add(newItem);
            if (newValue.getValue().values().stream().allMatch(Objects::isNull)) {
                newItem.clear();
                sideTable.getColumns().clear();
            }

            List<String> keySet = newValue.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                    .flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toList());
            if (newItem.isEmpty()) {
                keySet.addAll(newItem.keySet());
                sideTable.getColumns().clear();
                addColumns(sideTable, keySet);
                List<Map<String, String>> collect = newValue.getChildren().stream()
                        .map(TreeItem<Map<String, String>>::getValue).collect(Collectors.toList());
                for (Map<String, String> t : collect) {
                    if (newItem.keySet().stream().anyMatch(t::containsKey)) {
                        newItem = new SimpleMap();
                        list.add(newItem);
                    }
                    newItem.putAll(t);
                }
            }
        }
    }

    public static void readXMLFile(TreeView<Map<String, String>> build,
            Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
        remap(() -> tryToRead(build, allItems, file), "ERROR READING");
    }

    private static void addColumns(TableView<Map<String, String>> tableView, Collection<String> keySet) {
        for (String key : keySet) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(
                    param -> new SimpleStringProperty(Objects.toString(param.getValue().get(key), "-")));
            column.prefWidthProperty().bind(tableView.widthProperty().divide(keySet.size()).add(-5));
            tableView.getColumns().add(column);
        }
    }

    private static void addHeader(List<Map<String, String>> finalList, Map<String, String> collect) {
        if (!collect.keySet().stream().allMatch(e -> e.matches("\\w+\\d+"))) {
            Map<String, String> collect3 = collect.keySet().stream().collect(toLinkedHashMap2());
            if (!finalList.contains(collect3)) {
                finalList.add(collect3);
            }
        }
    }

    private static boolean anyChildLeaf(TreeItem<Map<String, String>> e) {
        return e.getChildren().stream().anyMatch(TreeItem<Map<String, String>>::isLeaf);
    }

    private static List<Map<String, String>> getFinalList(TreeItem<Map<String, String>> selectedItem) {
        ObservableList<TreeItem<Map<String, String>>> lista = FXCollections.observableArrayList();
        lista.add(selectedItem);
        List<Map<String, String>> finalList = new ArrayList<>();
        while (!lista.isEmpty()) {
            TreeItem<Map<String, String>> treeItem = lista.remove(0);
            if (anyChildLeaf(treeItem)) {
                Map<String, String> collect = toMap(treeItem);
                addHeader(finalList, collect);
                finalList.add(collect);
            } else if (treeItem.isLeaf()) {
                finalList.add(treeItem.getValue());
            } else {
                lista.addAll(0, treeItem.getChildren());
            }
        }
        finalList.removeIf(Map<String, String>::isEmpty);
        return finalList;
    }




    private static Object getValue(Map<String, String> i, int j) {
        int k = 0;
        for (String string : i.values()) {
            if (j == k) {
                return string;
            }
            k++;
        }

        return "";
    }

    private static boolean isArrayType(List<Set<Map.Entry<String, String>>> entryList) {
        return entryList.stream().flatMap(Set<Entry<String, String>>::stream).map(Entry<String, String>::getKey)
                .distinct().count() < entryList.size();
    }

    private static Map<String, String> newMap(Node item) {
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            return new SimpleMap();
        }

        return newMap(item.getNodeName(), item.getNodeValue());
    }

    private static boolean notTextNorComment(Node item) {
        return item.getNodeType() != Node.COMMENT_NODE && item.getNodeType() != Node.TEXT_NODE;
    }

    private static Collector<Map.Entry<String, String>, ?, Map<String, String>> toLinkedHashMap() {
        return Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue,
                (a, b) -> a + "\n" + b, LinkedHashMap::new);
    }

    private static Collector<String, ?, Map<String, String>> toLinkedHashMap2() {
        return Collectors.toMap(e -> e, e -> e, (a, b) -> a + "\n" + b, LinkedHashMap::new);
    }

    private static Map<String, String> toMap(TreeItem<Map<String, String>> treeItem) {
        List<Set<Map.Entry<String, String>>> entryList = treeItem.getChildren().stream().map(e -> {
            if (e.isLeaf()) {
                return e.getValue().entrySet();
            }
            return Stream
                    .concat(e.getChildren().stream().map(TreeItem<Map<String, String>>::getValue)
                            .flatMap(n -> n.entrySet().stream()), e.getValue().entrySet().stream())
                    .collect(Collectors.toSet());
        }).collect(Collectors.toList());
        if (isArrayType(entryList)) {
            // Is a Array type
            return entryList.stream().flatMap(Set<Entry<String, String>>::stream)
                    .collect(Collectors.groupingBy(Entry<String, String>::getKey,
                            Collectors.mapping(Entry<String, String>::getValue, Collectors.toList())))
                    .entrySet().stream()
                    .flatMap(en -> IntStream.range(0, en.getValue().size())
                            .mapToObj(j -> XMLExtractor.newEntry(en.getKey() + j, en.getValue().get(j))))
                    .collect(toLinkedHashMap());
        }
        return treeItem.getChildren().stream().flatMap(e -> Stream.concat(Stream.of(e), e.getChildren().stream()))
                .flatMap(e -> e.getValue().entrySet().stream()).collect(toLinkedHashMap());
    }

    private static void tryToRead(TreeView<Map<String, String>> build,
            Map<Node, TreeItem<Map<String, String>>> allItems, File file) {
        XmlObject parse = SupplierEx.remap(() -> XmlObject.Factory.parse(file), "ERROR PARSING");

        Node rootNode = parse.getDomNode();
        List<Node> currentNodes = new ArrayList<>();
        currentNodes.add(rootNode);
        TreeItem<Map<String, String>> value = new TreeItem<>(newMap(rootNode));
        value.setGraphic(newBoldText(rootNode.getNodeName()));
        build.setRoot(value);
        build.setShowRoot(false);
        allItems.put(rootNode, value);
        while (!currentNodes.isEmpty()) {
            Node domNode = currentNodes.remove(0);
            NodeList childNodes = domNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (notTextNorComment(item)) {
                    currentNodes.add(0, item);
                    TreeItem<Map<String, String>> e = new TreeItem<>(newMap(item));
                    allItems.get(domNode).getChildren().add(e);
                    allItems.put(item, e);
                    e.setGraphic(newBoldText(item.getNodeName()));
                    run(() -> addValue(item, e));
                }
            }
        }
    }

}
